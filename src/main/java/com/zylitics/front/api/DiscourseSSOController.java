package com.zylitics.front.api;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.zylitics.front.SecretsManager;
import com.zylitics.front.config.APICoreProperties;
import com.zylitics.front.exception.UnauthorizedException;
import com.zylitics.front.model.DiscourseSSOResponse;
import com.zylitics.front.model.User;
import com.zylitics.front.provider.UserProvider;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@RestController
@RequestMapping("${app-short-version}/discourseSSO")
public class DiscourseSSOController extends AbstractController {
  
  private final String secret;
  
  private final UserProvider userProvider;
  
  public DiscourseSSOController(APICoreProperties apiCoreProperties,
                                SecretsManager secretsManager,
                                UserProvider userProvider) {
    APICoreProperties.Services services = apiCoreProperties.getServices();
    secret = secretsManager.getSecretAsPlainText(services.getDiscourseSsoSecretCloudFile());
    this.userProvider = userProvider;
  }
  
  // https://meta.discourse.org/t/discourseconnect-official-single-sign-on-for-discourse-sso/13045
  @SuppressWarnings("UnstableApiUsage")
  @GetMapping("/getUserDetailForSSO")
  public ResponseEntity<DiscourseSSOResponse> getUserDetailForSSO(
      @RequestParam String sso,
      @RequestParam String sig,
      @RequestHeader(USER_INFO_REQ_HEADER) String userInfo
  ) throws Exception {
    HashFunction hashFunction = Hashing.hmacSha256(secret.getBytes(StandardCharsets.UTF_8));
    // decoded sso is Base64 of payload
    String ssoUrlDecoded = URLDecoder.decode(sso, StandardCharsets.UTF_8.toString());
    // calculate signature using Base64 of payload and match with provided signature. This proves that
    // the signature given is derived using our own secret.
    Preconditions.checkArgument(
        sig.equals(hashFunction.hashString(ssoUrlDecoded, StandardCharsets.UTF_8).toString()),
        "Signature and payload didn't match");
    
    int userId = getUserId(userInfo);
    User user = userProvider.getUser(userId)
        .orElseThrow(() -> new UnauthorizedException("User not found"));
    // Decode Base64 of payload to get the raw payload.
    String incomingPayload =
        new String(Base64.getDecoder().decode(ssoUrlDecoded), StandardCharsets.UTF_8);
    int nonceTextLength = "nonce=".length();
    String nonce = incomingPayload.substring(incomingPayload.indexOf("nonce=") + nonceTextLength,
        incomingPayload.indexOf("&"));
    // create entire payload we'd send to discourse. url encode all the values
    Preconditions.checkArgument(!Strings.isNullOrEmpty(nonce), "nonce can't be empty");
    String outgoingPayload = String.format("nonce=%s&name=%s&email=%s&external_id=%s",
        getUrlEncoded(nonce),
        getUrlEncoded(Common.getUserDisplayName(user.getFirstName(), user.getLastName())),
        getUrlEncoded(user.getEmail()),
        userId);
    String outgoingPayloadBase64 =
        Base64.getEncoder().encodeToString(outgoingPayload.getBytes(StandardCharsets.UTF_8));
    String newSig =
        hashFunction.hashString(outgoingPayloadBase64, StandardCharsets.UTF_8).toString();
    DiscourseSSOResponse discourseSSOResponse = new DiscourseSSOResponse()
        .setSso(getUrlEncoded(outgoingPayloadBase64))
        .setSig(newSig);
    return ResponseEntity.ok(discourseSSOResponse);
  }
  
  private String getUrlEncoded(String value) throws Exception {
    return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
  }
}
