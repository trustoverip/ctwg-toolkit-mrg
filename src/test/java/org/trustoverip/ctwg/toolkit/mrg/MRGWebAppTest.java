package org.trustoverip.ctwg.toolkit.mrg;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author sih
 */
@SpringBootTest
class MRGWebAppTest {

  @Test
  @DisplayName("Ensure that the Spring beans load correctly")
  void testContextLoads() {
    assertThat(true).isTrue();
  }
}
