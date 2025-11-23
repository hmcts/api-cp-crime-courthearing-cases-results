package uk.gov.hmcts.cp.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.OpenAPIV3Parser;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.lang3.StringUtils;

public class OpenAPIConfigurationLoader {

    private static final Logger LOG = LoggerFactory.getLogger(OpenAPIConfigurationLoader.class);
    private static final String JUDGES_OPENAPI = "openapi/TODO_ADD_FILENAME.openapi.yml";

    public static OpenAPI loadOpenApiFromClasspath(final String path) {
        if (StringUtils.isBlank(path)) {
            throw new IllegalArgumentException("Provided path is null or blank");
        }

        try (InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(path)) {
            if (inputStream == null) {
                LOG.error("OpenAPI specification file path is required but was null or blank");
                throw new IllegalArgumentException("Missing resource: " + path);
            }
            final String yaml = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            return new OpenAPIV3Parser().readContents(yaml, null, null).getOpenAPI();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public final OpenAPI openAPI() {
        return loadOpenApiFromClasspath(JUDGES_OPENAPI);
    }
}