package com.zylitics.front.api;

import com.zylitics.front.model.BrowserNameToVersions;
import com.zylitics.front.provider.BrowserProvider;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotBlank;
import java.util.List;

@RestController
@RequestMapping("${app-short-version}/browsers")
public class BrowserController extends AbstractController {
  
  private final BrowserProvider browserProvider;
  
  public BrowserController(BrowserProvider browserProvider) {
    this.browserProvider = browserProvider;
  }
  
  @GetMapping()
  public ResponseEntity<List<BrowserNameToVersions>> getBrowsers() {
    return ResponseEntity.ok(browserProvider.getBrowsers(null));
  }
}
