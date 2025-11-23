package uk.gov.hmcts.cp.config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.ExternalDocumentation;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class OpenAPIConfigurationLoaderTest {

    private static final Logger log = LoggerFactory.getLogger(OpenAPIConfigurationLoaderTest.class);

    @Test
    void openAPI_bean_should_have_expected_properties() {
        OpenAPIConfigurationLoader config = new OpenAPIConfigurationLoader();
        OpenAPI openAPI = config.openAPI();
        assertNotNull(openAPI);

        Info info = openAPI.getInfo();
        assertNotNull(info);
        assertEquals("Common Platform API", info.getTitle());
        assertEquals("API description", info.getDescription());

        String apiGitHubRepository = "api-hmcts-marketplace-template";
        String expectedVersion = System.getProperty("API_SPEC_VERSION", "0.0.0");
        log.info("API version set to: {}", expectedVersion);

        assertEquals(expectedVersion, info.getVersion());

        License license = info.getLicense();
        assertNotNull(license);
        assertEquals("MIT", license.getName());
        assertEquals("https://opensource.org/licenses/MIT", license.getUrl());

        assertNotNull(info.getContact());
        assertEquals("no-reply@hmcts.com", info.getContact().getEmail());

        assertNotNull(openAPI.getServers());
        assertFalse(openAPI.getServers().isEmpty());
        assertEquals("https://virtserver.swaggerhub.com/HMCTS-DTS/" + apiGitHubRepository + "/" + expectedVersion,
                openAPI.getServers().get(0).getUrl());
    }

    @Test
    void loadOpenApiFromClasspath_should_throw_for_missing_resource() {
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                OpenAPIConfigurationLoader.loadOpenApiFromClasspath("nonexistent-file.yaml")
        );
        assertTrue(exception.getMessage().contains("Missing resource"));
    }

    @Test
    void loadOpenApiFromClasspath_should_throw_for_blank_path() {
        try{
            OpenAPIConfigurationLoader.loadOpenApiFromClasspath(" ");
            assert false;
        } catch (IllegalArgumentException e) {
            assert true;
        }
    }

    @Test
    void loadOpenApiFromClasspath_should_throw_for_null_path() {
        try{
            OpenAPIConfigurationLoader.loadOpenApiFromClasspath(null);
            assert false;
        } catch (IllegalArgumentException e) {
            assert true;
        }
    }
}