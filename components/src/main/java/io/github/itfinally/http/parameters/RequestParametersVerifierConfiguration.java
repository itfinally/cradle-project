package io.github.itfinally.http.parameters;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty( prefix = "components", value = "allow-request-parameter-verify", havingValue = "true" )
public class RequestParametersVerifierConfiguration {
}
