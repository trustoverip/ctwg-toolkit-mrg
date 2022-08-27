package org.trustoverip.ctwg.toolkit.mrg.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.trustoverip.ctwg.toolkit.mrg.model.MRGModel;
import org.trustoverip.ctwg.toolkit.mrg.processors.MRGGenerationException;
import org.trustoverip.ctwg.toolkit.mrg.processors.MRGlossaryGenerator;
import org.trustoverip.ctwg.toolkit.mrg.processors.YamlWrangler;

/**
 * @author sih
 */
@RestController
@RequiredArgsConstructor
public class MRGApi {

  private final MRGlossaryGenerator generator;
  private final YamlWrangler yamlWrangler;

  @PostMapping(
      value = "/ctwg/mrg",
      produces = MediaType.APPLICATION_JSON_VALUE,
      consumes = MediaType.APPLICATION_JSON_VALUE)
  ResponseEntity<String> createMrg(@RequestBody MRGParams params) {
    MRGModel model =
        generator.generate(params.scopedir(), params.safFilename(), params.versionTag());
    String mrg = yamlWrangler.asYamlString(model);
    return ResponseEntity.ok(mrg);
  }

  @ExceptionHandler
  public ResponseEntity<String> handleException(Exception e) {
    String errorMessage;
    if (e instanceof MRGGenerationException) {
      errorMessage = String.format("Unable to generate MRG. Error was %s", e.getMessage());
    } else if (e instanceof HttpMessageNotReadableException) {
      return ResponseEntity.badRequest().build();
    } else {
      errorMessage = "Unexpected error generating MRG.";
    }
    return ResponseEntity.internalServerError().body(errorMessage);
  }
}
