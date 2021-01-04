package com.zylitics.front.runs;

import com.zylitics.front.model.DryRunResult;
import com.zylitics.front.model.RunError;
import com.zylitics.front.util.CommonUtil;
import com.zylitics.front.util.StringUtil;
import com.zylitics.zwl.antlr4.StoringErrorListener;
import com.zylitics.zwl.api.ZwlApi;
import com.zylitics.zwl.api.ZwlDryRunProperties;
import com.zylitics.zwl.exception.ZwlLangException;
import org.antlr.v4.runtime.ANTLRErrorListener;

import java.io.PrintStream;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;

// Must be instantiated on every dry run request. This class is built to be able to run multiple
// dry runs with the same instance.
public class DryRun {
  
  private final List<ANTLRErrorListener> listeners =
      Collections.singletonList(new StoringErrorListener());
  
  private final StringBuilder output = new StringBuilder();
  
  private final Clock clock = Clock.systemUTC();
  
  private final ZwlDryRunProperties props;
  
  public DryRun(ZwlDryRunProperties.Capabilities capabilities,
                ZwlDryRunProperties.Variables variables) {
    props = new ZwlDryRunProperties() {
      @Override
      public PrintStream getPrintStream() {
        return new CallbackOnlyPrintStream(msg -> {
          output.append(msg);
          output.append("\n");
        });
      }
    
      @Override
      public Capabilities getCapabilities() {
        return capabilities;
      }
    
      @Override
      public Variables getVariables() {
        return variables;
      }
    };
  }
  
  public DryRunResult run(String code) {
    DryRunResult result = new DryRunResult();
    if (StringUtil.isBlank(code)) {
      result
          .setTimeTaken(0)
          .setError(null)
          .setOutput("No code to run!");
      return result;
    }
    if (output.length() > 0) {
      output.setLength(0);
    }
    RunError error = null;
    ZwlApi zwlApi = new ZwlApi(code, listeners);
    Instant start = clock.instant();
    try {
      zwlApi.interpret(props, null);
    } catch (ZwlLangException z) {
      if (z.getMessage() == null) {
        // dry run can either get zwl-lang specific exceptions (no webdriver ones) or recognition
        // exceptions, which are captured as ZwlLangException without a cause field having exception
        // message with line info.
        throw new RuntimeException("Dry run caught an error but no error message found");
      }
      error = new RunError()
          .setMsg(z.getMessage())
          .setFrom(CommonUtil.getLineInfo(z.getFromPos()))
          .setTo(CommonUtil.getLineInfo(z.getToPos()));
    }
    result
        .setTimeTaken(ChronoUnit.MILLIS.between(start, clock.instant()))
        .setError(error)
        .setOutput(output.toString());
    return result;
  }
}
