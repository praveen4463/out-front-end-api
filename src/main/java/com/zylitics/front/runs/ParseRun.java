package com.zylitics.front.runs;

import com.zylitics.front.model.RunError;
import com.zylitics.front.util.CommonUtil;
import com.zylitics.front.util.StringUtil;
import com.zylitics.zwl.antlr4.StoringErrorListener;
import com.zylitics.zwl.api.ZwlApi;
import com.zylitics.zwl.exception.ZwlLangException;
import org.antlr.v4.runtime.ANTLRErrorListener;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

// A single instance app wide. The listener will be reset on every parse error so that's ok to use
// a same instance.
@Component
public class ParseRun {
  
  private final List<ANTLRErrorListener> listeners =
      Collections.singletonList(new StoringErrorListener());
  
  public Optional<RunError> parse(String code) {
    if (StringUtil.isBlank(code)) {
      return Optional.empty();
    }
    RunError error = null;
    ZwlApi zwlApi = new ZwlApi(code, listeners);
    try {
      zwlApi.parse();
    } catch (ZwlLangException z) {
      if (z.getMessage() == null) {
        throw new RuntimeException("Parse error caught but no error message found");
      }
      error = new RunError()
          .setMsg(z.getMessage())
          .setFrom(CommonUtil.getLineInfo(z.getFromPos()))
          .setTo(CommonUtil.getLineInfo(z.getToPos()));
    }
    return Optional.ofNullable(error);
  }
}
