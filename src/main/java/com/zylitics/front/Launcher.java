package com.zylitics.front;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zylitics.front.api.VMService;
import com.zylitics.front.config.APICoreProperties;
import com.zylitics.front.model.EspErrorResponse;
import com.zylitics.front.services.LocalVMService;
import com.zylitics.front.services.ProductionVMService;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.DelegatingFilterProxyRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

// TODO: I am not too sure what DataAccessExceptions should be re-tried, let's first watch logs and
//  decide if retry can help recovering from them. Hikari automatically retries until connection
//  timeout so probably we could retry on lock failure, deadlock etc. Any code that invokes methods
//  on NamedParameterJdbcTemplate or JdbcTemplate can throw subclasses of this exception.
//  Perhaps the best way to do it would be to extend NamedParameterJdbcTemplate and the methods
//  we're using. Detect errors there, reattempt if necessary and throw if failed.
//  https://docs.spring.io/spring/docs/current/spring-framework-reference/data-access.html#dao-exceptions
@SpringBootApplication
public class Launcher {
  
  private static final String USER_INFO_REQ_HEADER = "X-Endpoint-API-UserInfo";
  
  private static final String FIREBASE_SERVICE_ACCOUNT_KEY = "FIREBASE_SA";
  
  public static void main(String[] args) {
    SpringApplication.run(Launcher.class, args);
  }
  
  @Bean
  @Profile({"production", "e2e"})
  Storage storage() {
    return StorageOptions.getDefaultInstance().getService();
  }
  
  // High level client is not closed explicitly and left opened until the life of application
  // because many requests may come one after another and creating/closing is not efficient. It is
  // hoped that it will delete idle connections from pool after a certain time.
  // TODO: see if there is something to set that idle timeout for connections in pool.
  @Bean
  @Profile({"production", "e2e"})
  RestHighLevelClient restHighLevelClient(APICoreProperties apiCoreProperties,
                                          SecretsManager secretsManager) {
    APICoreProperties.Esdb esdb = apiCoreProperties.getEsdb();
    
    // TODO (optional): Should've in secret store but it's in env since I wrote infra scripts that
    //   use it too, let it be there for now and make a note for future.
    String esDBHostFromEnv = System.getenv(esdb.getEnvVarHost());
    Preconditions.checkArgument(!Strings.isNullOrEmpty(esDBHostFromEnv),
        esdb.getEnvVarHost() + " env. variable is not set.");
    
    String secret = secretsManager.getSecretAsPlainText(esdb.getAuthUserSecretCloudFile());
    CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
    credentialsProvider.setCredentials(AuthScope.ANY,
        new UsernamePasswordCredentials(esdb.getAuthUser(), secret));
    
    // TODO: see if we need to disable preemptive auth so that credentials are not sent with every
    //  request https://www.elastic.co/guide/en/elasticsearch/client/java-rest/current/_basic_authentication.html
    
    return new RestHighLevelClient(RestClient.builder(HttpHost.create(esDBHostFromEnv))
        .setHttpClientConfigCallback(httpClientBuilder ->
            httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider)));
  }
  
  // https://github.com/brettwooldridge/HikariCP
  // https://github.com/pgjdbc/pgjdbc#connection-properties
  // Boot won't autoconfigure DataSource if a bean is already declared.
  @Bean
  @Profile("production")
  DataSource hikariDataSource(APICoreProperties apiCoreProperties, SecretsManager secretsManager) {
    APICoreProperties.DataSource ds = apiCoreProperties.getDataSource();
    String privateHost = secretsManager.getSecretAsPlainText(ds.getPrivateHostCloudFile());
    String userPwd = secretsManager.getSecretAsPlainText(ds.getUserSecretCloudFile());
    
    HikariConfig config = new HikariConfig();
    config.setJdbcUrl(String.format("jdbc:postgresql://%s/%s", privateHost, ds.getDbName()));
    config.setUsername(ds.getUserName());
    config.setPassword(userPwd);
    config.setMinimumIdle(ds.getMinIdleConnPool());
    // TODO (optional): This note is to remember that we can customize pgjdbc driver by sending
    //  various options via query string or addDataSourceProperty. see here:
    //  https://github.com/pgjdbc/pgjdbc#connection-properties
    return new HikariDataSource(config);
  }
  
  @Bean
  @Profile("e2e")
  // a different bean method name is required even if profiles are different else context won't
  // load this bean.
  DataSource hikariLocalDataSource(APICoreProperties apiCoreProperties) {
    APICoreProperties.DataSource ds = apiCoreProperties.getDataSource();
    HikariConfig config = new HikariConfig();
    config.setJdbcUrl(String.format("jdbc:postgresql://localhost/%s", ds.getDbName()));
    config.setUsername(ds.getUserName());
    config.setMinimumIdle(ds.getMinIdleConnPool());
    return new HikariDataSource(config);
  }
  
  // https://docs.spring.io/spring/docs/current/spring-framework-reference/data-access.html#tx-prog-template-settings
  @Bean
  @Profile({"production", "e2e"})
  TransactionTemplate transactionTemplate(PlatformTransactionManager platformTransactionManager) {
    TransactionTemplate transactionTemplate = new TransactionTemplate(platformTransactionManager);
    // TODO (optional): specify any transaction settings.
    transactionTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_DEFAULT);
    return transactionTemplate;
  }
  
  @Bean
  @Profile("production")
  VMService productionVMService(APICoreProperties apiCoreProperties,
                                WebClient.Builder webClientBuilder,
                                SecretsManager secretsManager) {
    return new ProductionVMService(webClientBuilder, apiCoreProperties, secretsManager);
  }
  
  @Bean
  @Profile("e2e")
  VMService localVMService(APICoreProperties apiCoreProperties) {
    return new LocalVMService(apiCoreProperties);
  }
  
  @Bean
  @Profile({"production", "e2e"})
  FirebaseApp firebaseApp() throws Exception {
    String firebaseSAKey = System.getenv(FIREBASE_SERVICE_ACCOUNT_KEY);
    Preconditions.checkArgument(!Strings.isNullOrEmpty(firebaseSAKey),
        FIREBASE_SERVICE_ACCOUNT_KEY + " env. variable is not set.");
    FirebaseOptions options = FirebaseOptions.builder()
        .setCredentials(GoogleCredentials.fromStream(new FileInputStream(firebaseSAKey))).build();
  
    return FirebaseApp.initializeApp(options);
  }
  
  @Bean
  @Profile({"production", "e2e"})
  FirebaseAuth firebaseAuth(FirebaseApp firebaseApp) {
    return FirebaseAuth.getInstance(firebaseApp);
  }
  
  // https://docs.spring.io/spring-boot/docs/2.4.2/reference/htmlsingle/#boot-features-embedded-container
  // this will let out filter interact with other beans and start lazily
  @Bean
  @Profile("e2e")
  DelegatingFilterProxyRegistrationBean authCheckBean() {
    return new DelegatingFilterProxyRegistrationBean("authFilter");
  }
  
  // Used for only locally authorizing request, in production ESP does the auth much efficiently.
  // References:
  // https://stackoverflow.com/a/2811865/1624454
  // https://www.oracle.com/java/technologies/filters.html#72674
  // https://stackoverflow.com/a/19830906/1624454
  // ESP error response structures and codes references:
  // https://cloud.google.com/endpoints/docs/openapi/troubleshoot-jwt
  // https://cloud.google.com/endpoints/docs/openapi/troubleshoot-response-errors
  // https://cloud.google.com/apis/design/errors
  @Bean
  @Profile("e2e")
  Filter authFilter(FirebaseAuth firebaseAuth) {
    return (request, response, chain) -> {
      HttpServletRequest httpServletRequest = (HttpServletRequest) request;
      HttpServletResponse httpServletResponse = (HttpServletResponse) response;
      ObjectMapper mapper = new ObjectMapper();
      try {
        // Preflight requests must bypass auth check as they don't contain Auth header.
        // https://stackoverflow.com/a/15734032/1624454
        if (httpServletRequest.getMethod().equals(HttpMethod.OPTIONS.name())) {
          chain.doFilter(request, response);
          return;
        }
        // let actuator endpoints go through without auth
        if (httpServletRequest.getRequestURI().contains("/actuator")) {
          chain.doFilter(request, response);
          return;
        }
        String authHeader = httpServletRequest.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
          httpServletResponse.sendError(HttpStatus.UNPROCESSABLE_ENTITY.value());
          return;
        }
        String token = authHeader.split(" ")[1];
        FirebaseToken decodedToken = firebaseAuth.verifyIdToken(token);
        // https://cloud.google.com/endpoints/docs/openapi/authenticating-users-firebase
        // userInfo value is created just like ESP does
        String userInfo = "{" +
            String.format("\"id\": \"%s\"", decodedToken.getUid()) +
            "," +
            String.format("\"email\": \"%s\"", decodedToken.getEmail()) +
            "}";
        String encodedUserInfoHeaderValue = Base64.getUrlEncoder()
            .encodeToString(userInfo.getBytes(StandardCharsets.UTF_8));
        HttpServletRequestWrapper requestWrapper = new HttpServletRequestWrapper(httpServletRequest)
        {
          @Override
          public String getHeader(String name) {
            if (name.equals(USER_INFO_REQ_HEADER)) {
              return encodedUserInfoHeaderValue;
            }
            return super.getHeader(name);
          }
  
          @Override
          public Enumeration<String> getHeaders(String name) {
            if (name.equals(USER_INFO_REQ_HEADER)) {
              return Collections
                  .enumeration(Collections.singletonList(encodedUserInfoHeaderValue));
            }
            return super.getHeaders(name);
          }
  
          @Override
          public Enumeration<String> getHeaderNames() {
            List<String> names = Collections.list(super.getHeaderNames());
            names.add(USER_INFO_REQ_HEADER);
            return Collections.enumeration(names);
          }
        };
        chain.doFilter(requestWrapper, response);
      } catch (FirebaseAuthException authException) {
        System.out.printf("authException occurred: %s, errorCode: %s, authErrorCode: %s%n",
            authException.getMessage(), authException.getErrorCode(),
            authException.getAuthErrorCode());
        // let's assume for now that firebase exception occurs on token expire only
        EspErrorResponse error = new EspErrorResponse()
            .setError(new EspErrorResponse.Error().setMessage("TIME_CONSTRAINT_FAILURE"));
        httpServletResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
        httpServletResponse.setContentType(MediaType.APPLICATION_JSON_VALUE);
        httpServletResponse.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, "*");
        httpServletResponse.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, "*");
        httpServletResponse.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
        mapper.writeValue(httpServletResponse.getWriter(), error);
      } catch (Exception ex) {
        throw new RuntimeException(ex);
      }
    };
  }
}
