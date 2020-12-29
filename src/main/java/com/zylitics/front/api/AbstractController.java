package com.zylitics.front.api;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zylitics.front.exception.UnauthorizedException;
import com.zylitics.front.model.Error;
import com.zylitics.front.model.UserFromProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Base64;

public abstract class AbstractController {
  
  private static final Logger LOG = LoggerFactory.getLogger(AbstractController.class);
  
  private static final String MASKED_ERROR = "An exception occurred at server while processing" +
      " the request and we're working on to fix it. Please contact us if the issue persists.";
  
  static final String USER_INFO_REQ_HEADER = "X-Endpoint-API-UserInfo";
  
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
  public ResponseEntity<Error> handleExceptions(MethodArgumentNotValidException ex) {
    LOG.debug("An ArgumentNotValid handler was called");
    
    // Don't send actual error message on bad request as well cause it would be us if that binding is failed,
    // which means client code has bug.
    return processErrResponse(ex, HttpStatus.BAD_REQUEST, MASKED_ERROR);
  }
  
  // TODO: IllegalArgumentException errors are relayed as it to front end but sometimes error generated
  //  by libraries also throw this exception which causes cryptic errors to be shown to user. Let's
  //  think about it some later on, probably we can filter out exception triggered by our app from
  //  trace and relay only those messages. We throw this mostly when some validation fails.
  @SuppressWarnings("unused")
  @ExceptionHandler
  public ResponseEntity<Error> handleExceptions(IllegalArgumentException ex) {
    LOG.debug("An IllegalArgumentException handler was called");
    
    // use 422 on validation failure https://stackoverflow.com/a/25489597/1624454
    return processErrResponse(ex, HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
  }
  
  @SuppressWarnings("unused")
  @ExceptionHandler
  public ResponseEntity<Error> handleExceptions(UnauthorizedException ex) {
    LOG.debug("An UnauthorizedException handler was called");
    
    return processErrResponse(ex, HttpStatus.UNAUTHORIZED, ex.getMessage());
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
  public ResponseEntity<Error> handleExceptions(Exception ex) {
    LOG.debug("An Exception handler was called");
  
    // Note: original error is not sent to client as this api is internal and we don't want end user
    // to see internal exceptions.
    return processErrResponse(ex, HttpStatus.INTERNAL_SERVER_ERROR, MASKED_ERROR);
  }
  
  private ResponseEntity<Error> processErrResponse(Throwable ex, HttpStatus status,
                                                           String userErrorMsg) {
    // Log exception.
    // TODO: we'll have to see what type of errors we may get here and may require more information
    //  from handlers to better debug error causes, for example the state of program when
    //  this exception occurred, the received parameters from client, etc.
    LOG.error("", ex);
    
    return ResponseEntity
        .status(status)
        .body(new Error().setMessage(userErrorMsg));
  }
}
