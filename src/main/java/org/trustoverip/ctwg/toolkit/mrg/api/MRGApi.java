package org.trustoverip.ctwg.toolkit.mrg.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.request.WebRequest;
import org.trustoverip.ctwg.toolkit.mrg.model.MRGModel;
import org.trustoverip.ctwg.toolkit.mrg.processors.MRGGenerationException;
import org.trustoverip.ctwg.toolkit.mrg.processors.MRGlossaryGenerator;
import org.trustoverip.ctwg.toolkit.mrg.processors.YamlWrangler;

/**
 * @author sih
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class MRGApi {

  private final MRGlossaryGenerator generator;
  private final YamlWrangler yamlWrangler;

  @RequestMapping(value = "/ctwg/mrg", method = RequestMethod.GET)
  String mrgForm() {
    return "mrg-form";
  }

  @RequestMapping(value = "/ctwg/mrg", method = RequestMethod.POST)
  String createMrg(WebRequest webRequest, Model webMvcModel) {
    MRGParams params =
        new MRGParams(
            webRequest.getParameter("scopedir"),
            webRequest.getParameter("safFilename"),
            webRequest.getParameter("versionTag"));
    MRGModel mrg = generator.generate(params.scopedir(), params.safFilename(), params.versionTag());
    String mrgString = yamlWrangler.asYamlString(mrg);
    webMvcModel.addAttribute("mrg", mrgString);
    return "mrg-result";
  }

  @ExceptionHandler
  public ResponseEntity<String> handleException(Exception e) {
    String errorMessage;
    if (e instanceof MRGGenerationException) {
      errorMessage = String.format("Unable to generate MRG. Error was %s", e.getMessage());
    } else if (e instanceof HttpMessageNotReadableException) {
      return ResponseEntity.badRequest().build();
    } else {
      errorMessage = String.format("Unexpected error generating MRG. Error was %s", e.getMessage());
    }
    return ResponseEntity.internalServerError().body(errorMessage);
  }
}
