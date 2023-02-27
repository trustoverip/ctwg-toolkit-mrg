package org.trustoverip.ctwg.toolkit.mrg.processors;

import static org.trustoverip.ctwg.toolkit.mrg.processors.MRGGenerationException.CANNOT_PARSE_TERM;
import static org.trustoverip.ctwg.toolkit.mrg.processors.MRGGenerationException.CANNOT_WRITE_MRG;
import static org.trustoverip.ctwg.toolkit.mrg.processors.MRGGenerationException.UNABLE_TO_PARSE_MRG;
import static org.trustoverip.ctwg.toolkit.mrg.processors.MRGGenerationException.UNABLE_TO_PARSE_SAF;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.springframework.stereotype.Service;
import org.trustoverip.ctwg.toolkit.mrg.model.MRGModel;
import org.trustoverip.ctwg.toolkit.mrg.model.SAFModel;
import org.trustoverip.ctwg.toolkit.mrg.model.Term;
import lombok.extern.slf4j.Slf4j;

/**
 * @author sih
 */
@Service
@Slf4j

public final class YamlWrangler {

  private final ObjectMapper yamlMapper;

  YamlWrangler() {
    yamlMapper = new ObjectMapper(new YAMLFactory().enable(Feature.INDENT_ARRAYS));
    yamlMapper.findAndRegisterModules();
  }

  SAFModel parseSaf(String safAsString) throws MRGGenerationException {
    try {
      log.debug(String.format("SAF As String: %s", safAsString));
      return yamlMapper.readValue(safAsString, SAFModel.class);
    } catch (Exception e) {
      throw new MRGGenerationException(UNABLE_TO_PARSE_SAF, e);
    }
  }

  Term parseTerm(String termString) throws MRGGenerationException {
    try {
      return yamlMapper.readValue(termString, Term.class);
    } catch (Exception e) {
      throw new MRGGenerationException(CANNOT_PARSE_TERM, e);
    }
  }

  MRGModel parseMrg(String mrgAsString) throws MRGGenerationException {
    try {
      log.debug(String.format("MRG As String: %s", mrgAsString));
      return yamlMapper.readValue(mrgAsString, MRGModel.class);
    } catch (Exception e) {
      throw new MRGGenerationException(UNABLE_TO_PARSE_MRG, e);
    }
  }

  void writeMrg(Path location, MRGModel mrg) throws MRGGenerationException {
    try (OutputStream fos = Files.newOutputStream(location)) {
      yamlMapper.writeValue(fos, mrg);
    } catch (IOException ioException) {
      throw new MRGGenerationException(String.format(CANNOT_WRITE_MRG, location.toAbsolutePath()), ioException);
    }
  }

  public String asYamlString(MRGModel model) throws MRGGenerationException {
    try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
      yamlMapper.writerWithDefaultPrettyPrinter().writeValue(baos, model);
      return baos.toString(StandardCharsets.UTF_8);
    } catch (IOException ioe) {
      throw new MRGGenerationException("Cannot convert MRG to YAML", ioe);
    }
  }
}
