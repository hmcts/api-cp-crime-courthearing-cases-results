package uk.gov.hmcts.cp.config;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.cp.openapi.api.SubscriptionApi;
import uk.gov.hmcts.cp.openapi.model.ClientSubscription;
import uk.gov.hmcts.cp.openapi.model.ClientSubscriptionRequest;
import uk.gov.hmcts.cp.openapi.model.EventType;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
class OpenAPISpecTest {
    @Test
    void generated_error_response_should_have_expected_fields() {
        // What no ErrorResponse ? All the others have one. TBD
        // assertThat(ErrorResponse.class).hasDeclaredFields("error", "message", "details", "traceId");
    }

    @Test
    void enventType_should_have_correct_entries() {
        assertThat(EventType.PCR.getValue()).isEqualTo("PCR");
        assertThat(EventType.CUSTODIAL_RESULT.getValue()).isEqualTo("CUSTODIAL_RESULT");
    }

    @Test
    void subscription_generated_request_should_have_expected_fields() {
        assertThat(ClientSubscriptionRequest.class).hasDeclaredFields("notificationEndpoint");
        assertThat(ClientSubscriptionRequest.class).hasDeclaredFields("eventTypes");
    }

    @Test
    void subscription_generated_response_should_have_expected_fields() {
        assertThat(ClientSubscription.class).hasDeclaredFields("clientSubscriptionId");
        assertThat(ClientSubscription.class).hasDeclaredFields("notificationEndpoint");
        assertThat(ClientSubscription.class).hasDeclaredFields("eventTypes");
        assertThat(ClientSubscription.class).hasDeclaredFields("createdAt");
        assertThat(ClientSubscription.class).hasDeclaredFields("updatedAt");
    }

    @Test
    void generated_api_should_have_expected_methods() {
        assertThat(SubscriptionApi.class).hasDeclaredMethods("createClientSubscription");
    }
}