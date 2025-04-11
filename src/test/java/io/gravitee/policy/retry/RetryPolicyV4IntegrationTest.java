/*
 * Copyright © 2015 The Gravitee team (http://gravitee.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.gravitee.policy.retry;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.notFound;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.stubbing.Scenario;
import io.gravitee.apim.gateway.tests.sdk.annotations.DeployApi;
import io.gravitee.apim.gateway.tests.sdk.annotations.GatewayTest;
import io.gravitee.definition.model.ExecutionMode;
import io.vertx.core.http.HttpMethod;
import io.vertx.rxjava3.core.http.HttpClient;
import io.vertx.rxjava3.core.http.HttpClientRequest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@Slf4j
@GatewayTest(v2ExecutionMode = ExecutionMode.V4_EMULATION_ENGINE)
class RetryPolicyV4IntegrationTest extends V4EngineTest {

    @ParameterizedTest
    @DisplayName("Should succeed before max retries")
    @DeployApi({ "/apis/v4/retry.json", "/apis/v2/retry.json" })
    @ValueSource(strings = { "/v4-retry", "/test" })
    void shouldSucceedBeforeMaxRetries(String requestPath, HttpClient client) {
        wiremock.stubFor(
            get("/endpoint")
                .inScenario("retry")
                .whenScenarioStateIs(Scenario.STARTED)
                .willReturn(aResponse().withStatus(504))
                .willSetStateTo("firstCall")
        );
        wiremock.stubFor(
            get("/endpoint")
                .inScenario("retry")
                .whenScenarioStateIs("firstCall")
                .willReturn(aResponse().withStatus(505))
                .willSetStateTo("secondCall")
        );
        wiremock.stubFor(
            get("/endpoint").inScenario("retry").whenScenarioStateIs("secondCall").willReturn(aResponse().withStatus(506)).willReturn(ok())
        );

        client
            .rxRequest(HttpMethod.GET, requestPath)
            .flatMap(HttpClientRequest::rxSend)
            .test()
            .awaitDone(10, SECONDS)
            .assertComplete()
            .assertValue(response -> {
                assertThat(response.statusCode()).isEqualTo(200);
                return true;
            })
            .assertNoErrors();

        wiremock.verify(3, getRequestedFor(urlPathEqualTo("/endpoint")));
    }

    @ParameterizedTest
    @DeployApi({ "/apis/v4/retry.json", "/apis/v2/retry.json" })
    @ValueSource(strings = { "/v4-retry", "/test" })
    @DisplayName("Should fail after max retries")
    void shouldFailAfterMaxRetries(String requestPath, HttpClient client) {
        wiremock.stubFor(
            get("/endpoint")
                .inScenario("retry")
                .whenScenarioStateIs(Scenario.STARTED)
                .willReturn(aResponse().withStatus(505))
                .willSetStateTo("firstCall")
        );
        wiremock.stubFor(
            get("/endpoint")
                .inScenario("retry")
                .whenScenarioStateIs("firstCall")
                .willReturn(aResponse().withStatus(506))
                .willSetStateTo("secondCall")
        );
        wiremock.stubFor(
            get("/endpoint")
                .inScenario("retry")
                .whenScenarioStateIs("secondCall")
                .willReturn(aResponse().withStatus(507))
                .willSetStateTo("thirdCall")
        );
        wiremock.stubFor(get("/endpoint").inScenario("retry").whenScenarioStateIs("thirdCall").willReturn(aResponse().withStatus(508)));

        client
            .rxRequest(HttpMethod.GET, requestPath)
            .flatMap(HttpClientRequest::rxSend)
            .test()
            .awaitDone(10, SECONDS)
            .assertComplete()
            .assertValue(response -> {
                assertThat(response.statusCode()).isEqualTo(502);
                return true;
            })
            .assertNoErrors();

        wiremock.verify(4, getRequestedFor(urlPathEqualTo("/endpoint")));
    }

    @ParameterizedTest
    @DeployApi({ "/apis/v4/retry.json", "/apis/v2/retry.json" })
    @ValueSource(strings = { "/v4-retry", "/test" })
    @DisplayName("Should fail after all retries in timeout")
    void shouldFailAfterTimeout(String requestPath, HttpClient client) {
        wiremock.stubFor(
            get("/endpoint")
                .inScenario("retry")
                .whenScenarioStateIs(Scenario.STARTED)
                .willReturn(ok().withFixedDelay(600))
                .willSetStateTo("firstCall")
        );
        wiremock.stubFor(
            get("/endpoint")
                .inScenario("retry")
                .whenScenarioStateIs("firstCall")
                .willReturn(ok().withFixedDelay(600))
                .willSetStateTo("secondCall")
        );
        wiremock.stubFor(
            get("/endpoint")
                .inScenario("retry")
                .whenScenarioStateIs("secondCall")
                .willReturn(ok().withFixedDelay(600))
                .willSetStateTo("thirdCall")
        );
        wiremock.stubFor(get("/endpoint").inScenario("retry").whenScenarioStateIs("thirdCall").willReturn(ok().withFixedDelay(600)));

        client
            .rxRequest(HttpMethod.GET, requestPath)
            .flatMap(HttpClientRequest::rxSend)
            .test()
            .awaitDone(10, SECONDS)
            .assertComplete()
            .assertValue(response -> {
                assertThat(response.statusCode()).isEqualTo(502);
                return true;
            })
            .assertNoErrors();

        wiremock.verify(4, getRequestedFor(urlPathEqualTo("/endpoint")));
    }

    @ParameterizedTest
    @ValueSource(strings = { "/v4-retry-last-response", "/test-last-response" })
    @DeployApi({ "/apis/v4/retry-last-response.json", "/apis/v2/retry-last-response.json" })
    @DisplayName("Should fail after too many conditions failure and return last response")
    void shouldFailAfterTooManyConditionFailureAndReturnLastResponse(String requestUrl, HttpClient client) {
        wiremock.stubFor(
            get("/endpoint").inScenario("retry").whenScenarioStateIs(Scenario.STARTED).willReturn(notFound()).willSetStateTo("firstCall")
        );
        wiremock.stubFor(
            get("/endpoint").inScenario("retry").whenScenarioStateIs("firstCall").willReturn(notFound()).willSetStateTo("secondCall")
        );
        wiremock.stubFor(
            get("/endpoint").inScenario("retry").whenScenarioStateIs("secondCall").willReturn(notFound()).willSetStateTo("thirdCall")
        );
        wiremock.stubFor(get("/endpoint").inScenario("retry").whenScenarioStateIs("thirdCall").willReturn(notFound()));

        client
            .rxRequest(HttpMethod.GET, requestUrl)
            .flatMap(HttpClientRequest::rxSend)
            .test()
            .awaitDone(10, SECONDS)
            .assertComplete()
            .assertValue(response -> {
                assertThat(response.statusCode()).isEqualTo(404);
                return true;
            })
            .assertNoErrors();

        wiremock.verify(4, getRequestedFor(urlPathEqualTo("/endpoint")));
    }

    @ParameterizedTest
    @ValueSource(strings = { "/v4-retry-last-response", "/test-last-response" })
    @DeployApi({ "/apis/v4/retry-last-response.json", "/apis/v2/retry-last-response.json" })
    @DisplayName("Should fail after too many timeout failures and does not return last response")
    void shouldFailAfterTimeoutAndReturnLastResponse(String requestUrl, HttpClient client) {
        wiremock.stubFor(
            get("/endpoint")
                .inScenario("retry")
                .whenScenarioStateIs(Scenario.STARTED)
                .willReturn(ok().withFixedDelay(600))
                .willSetStateTo("firstCall")
        );
        wiremock.stubFor(
            get("/endpoint")
                .inScenario("retry")
                .whenScenarioStateIs("firstCall")
                .willReturn(ok().withFixedDelay(600))
                .willSetStateTo("secondCall")
        );
        wiremock.stubFor(
            get("/endpoint")
                .inScenario("retry")
                .whenScenarioStateIs("secondCall")
                .willReturn(ok().withFixedDelay(600))
                .willSetStateTo("thirdCall")
        );
        wiremock.stubFor(get("/endpoint").inScenario("retry").whenScenarioStateIs("thirdCall").willReturn(ok().withFixedDelay(600)));

        client
            .rxRequest(HttpMethod.GET, requestUrl)
            .flatMap(HttpClientRequest::rxSend)
            .test()
            .awaitDone(10, SECONDS)
            .assertComplete()
            .assertValue(response -> {
                assertThat(response.statusCode()).isEqualTo(502);
                return true;
            })
            .assertNoErrors();

        wiremock.verify(4, getRequestedFor(urlPathEqualTo("/endpoint")));
    }

    @ParameterizedTest
    @DisplayName("Should delay retry requests")
    @DeployApi({ "/apis/v4/retry-delay.json", "/apis/v2/retry-delay.json" })
    @ValueSource(strings = { "/v4-retry-delay", "/test-delay" })
    void shouldDelayRetryRequests(String requestPath, HttpClient client) {
        wiremock.stubFor(
            get("/endpoint")
                .inScenario("retry")
                .whenScenarioStateIs(Scenario.STARTED)
                .willReturn(aResponse().withStatus(504))
                .willSetStateTo("firstCall")
        );
        wiremock.stubFor(
            get("/endpoint")
                .inScenario("retry")
                .whenScenarioStateIs("firstCall")
                .willReturn(aResponse().withStatus(505))
                .willSetStateTo("secondCall")
        );
        wiremock.stubFor(get("/endpoint").inScenario("retry").whenScenarioStateIs("secondCall").willReturn(ok()));

        var startTime = System.currentTimeMillis();
        client
            .rxRequest(HttpMethod.GET, requestPath)
            .flatMap(HttpClientRequest::rxSend)
            .test()
            .awaitDone(10, SECONDS)
            .assertComplete()
            .assertValue(response -> {
                assertThat(response.statusCode()).isEqualTo(200);
                return true;
            })
            .assertNoErrors();

        var elapsedTime = System.currentTimeMillis() - startTime;
        assertThat(elapsedTime)
            .describedAs("Elapsed time should be at least 3 seconds (1.5 seconds between each retry)")
            .isGreaterThanOrEqualTo(3000);
        wiremock.verify(3, getRequestedFor(urlPathEqualTo("/endpoint")));
    }
}
