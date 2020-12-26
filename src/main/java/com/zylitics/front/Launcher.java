package com.zylitics.front;

import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zylitics.front.config.APICoreProperties;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;

// TODO: I am not too sure what DataAccessExceptions should be re-tried, let's first watch logs and
//  decide if retry can help recovering from them. Hikari automatically retries until connection
//  timeout so probably we could retry on lock failure, deadlock etc. Any code that invokes methods
//  on NamedParameterJdbcTemplate or JdbcTemplate can throw subclasses of this exception.
//  Perhaps the best way to do it would be to extend NamedParameterJdbcTemplate and the methods
//  we're using. Detect errors there, reattempt if necessary and throw if failed.
//  https://docs.spring.io/spring/docs/current/spring-framework-reference/data-access.html#dao-exceptions
@SpringBootApplication
public class Launcher {
  
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
  
}
