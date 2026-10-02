package me.eeshe.tempus;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

import me.eeshe.tempus.controller.AngularController;
import me.eeshe.tempus.support.ControllerTestBase;

@WebMvcTest(AngularController.class)
public class AngularControllerTest extends ControllerTestBase {

    @Test
    void forwardsTopLevelRouteToIndex() {
        final MvcTestResult result = mockMvc.get().uri("/projects").exchange();

        assertThat(result).hasStatusOk();
        assertThat(result.getResponse().getForwardedUrl()).isEqualTo("/");
    }

    @Test
    void forwardsNestedRouteToIndex() {
        final MvcTestResult result = mockMvc.get().uri("/account/migration").exchange();

        assertThat(result).hasStatusOk();
        assertThat(result.getResponse().getForwardedUrl()).isEqualTo("/");
    }

    @Test
    void doesNotForwardAssetPaths() {
        final MvcTestResult result = mockMvc.get().uri("/assets/app.js").exchange();

        assertThat(result).hasStatus(404);
        assertThat(result.getResponse().getForwardedUrl()).isNull();
    }
}
