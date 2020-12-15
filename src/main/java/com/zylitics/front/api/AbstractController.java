package com.zylitics.front.api;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zylitics.front.SecretsManager;
import com.zylitics.front.http.ErrorResponse;
import com.zylitics.front.model.UserFromProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.io.IOException;
import java.time.Clock;
import java.util.Base64;

public abstract class AbstractController {
  
  private static final Logger LOG = LoggerFactory.getLogger(AbstractController.class);
  
  private static final String MASKED_ERROR = "An exception occurred at server while processing" +
      " the request and we're working on to fix it. Please contact us if the issue persists.";
  
  static final String USER_INFO_REQ_HEADER = "X-Endpoint-API-UserInfo";
  
  final SecretsManager secretsManager;
  
  final Clock clock;
  
  @SuppressWarnings("unused")
  AbstractController(SecretsManager secretsManager) {
    this(secretsManager, Clock.systemUTC());
  }
  
  AbstractController(SecretsManager secretsManager, Clock clock) {
    this.secretsManager = secretsManager;
    this.clock = clock;
  }
  
  int getUserId(String userInfoHeaderValue) {
    byte[] decoded = Base64.getUrlDecoder().decode(userInfoHeaderValue);
    ObjectMapper mapper = new ObjectMapper();
    mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    try {
      UserFromProxy user = mapper.readValue(decoded, UserFromProxy.class);
      return Integer.parseInt(user.getId());
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }
  
  /**
   * Invoked when @RequestBody binding is failed
   */
  @SuppressWarnings("unused")
  @ExceptionHandler
  public ResponseEntity<ErrorResponse> handleExceptions(MethodArgumentNotValidException ex) {
    LOG.debug("An ArgumentNotValid handler was called");
  
    // Don't send actual error message on bad request as well cause it would be us if that binding is failed,
    // which means client code has bug.
    return processErrResponse(ex, HttpStatus.BAD_REQUEST, MASKED_ERROR);
  }
  
  @SuppressWarnings("unused")
  @ExceptionHandler
  public ResponseEntity<ErrorResponse> handleExceptions(IllegalArgumentException ex) {
    LOG.debug("An IllegalArgumentException handler was called");
    
    // use 422 on validation failure https://stackoverflow.com/a/25489597/1624454
    return processErrResponse(ex, HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
  }
  
  /**
   * Catch all exception handler for spring raised errors. Later divide it into specific errors.
   * Reference:
   * docs.spring.io/spring/docs/current/spring-framework-reference/web.html#mvc-ann-exceptionhandler
   * @param ex the catched {@link Exception} type.
   * @return {@link ResponseEntity}
   */
  @SuppressWarnings("unused")
  @ExceptionHandler
  public ResponseEntity<ErrorResponse> handleExceptions(Exception ex) {
    LOG.debug("An Exception handler was called");
  
    // Note: original error is not sent to client as this api is internal and we don't want end user
    // to see internal exceptions.
    return processErrResponse(ex, HttpStatus.INTERNAL_SERVER_ERROR, MASKED_ERROR);
  }
  
  private ResponseEntity<ErrorResponse> processErrResponse(Throwable ex, HttpStatus status,
                                                           String userErrorMsg) {
    // Log exception.
    // TODO: we'll have to see what type of errors we may get here and may require more information
    //  from handlers to better debug error causes, for example the state of program when
    //  this exception occurred, the received parameters from client, etc.
    LOG.error("", ex);
    
    return ResponseEntity
        .status(status)
        .body(new ErrorResponse().setMessage(userErrorMsg));
  }
  
  // published when all beans are loaded
  @EventListener(ContextRefreshedEvent.class)
  void onContextRefreshedEvent() throws IOException {
    LOG.debug("ContextRefreshEvent was triggered");
    
    // Close SecretsManager once all beans that required it are loaded, as we don't need to until
    // this VM is deleted from here, where a new manager is created.
    if (secretsManager != null) {
      LOG.debug("secretsManager will now close");
      secretsManager.close();
    }
  }
}
