package com.zylitics.front.api;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import com.zylitics.front.model.*;
import com.zylitics.front.provider.EmailVerificationProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotBlank;
import java.util.Optional;

@RestController
@RequestMapping("${app-short-version}/emailVerifications")
public class EmailVerificationController extends AbstractController {
  
  private static final Logger LOG = LoggerFactory.getLogger(EmailVerificationController.class);
  
  private final EmailVerificationProvider emailVerificationProvider;
  
  private final FirebaseAuth firebaseAuth;
  
  public EmailVerificationController(EmailVerificationProvider emailVerificationProvider,
                                     FirebaseAuth firebaseAuth) {
    this.emailVerificationProvider = emailVerificationProvider;
    this.firebaseAuth = firebaseAuth;
  }
  
  @PatchMapping("/{code}/validate")
  public ResponseEntity<?> validate(
      @PathVariable @NotBlank String code,
      @RequestHeader(USER_INFO_REQ_HEADER) String userInfo) {
    int userId = getUserId(userInfo);
    Optional<EmailVerification> emailVerificationOptional =
        emailVerificationProvider.getEmailVerification(code);
    if (!emailVerificationOptional.isPresent()) {
      throw new IllegalArgumentException("Can't identify verification link, the code is invalid");
    }
    EmailVerification emailVerification = emailVerificationOptional.get();
    if (emailVerification.isUsed()) {
      return ResponseEntity.ok().build();
    }
    emailVerificationProvider.updateToUsed(emailVerification.getId());
    // update user in firebase too
    try {
      firebaseAuth.updateUser(new UserRecord.UpdateRequest(Integer.toString(userId))
          .setEmailVerified(true));
    } catch (FirebaseAuthException f) {
      LOG.error("Couldn't mark email verified in firebase, userId: " +
          userId, f);
    }
    return ResponseEntity.ok().build();
  }
}
