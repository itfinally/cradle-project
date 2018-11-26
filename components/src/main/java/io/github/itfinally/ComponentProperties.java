package io.github.itfinally;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties( prefix = "components" )
public class ComponentProperties {
  private boolean allowRequestParameterVerify = false;

  public boolean isAllowRequestParameterVerify() {
    return allowRequestParameterVerify;
  }

  public ComponentProperties setAllowRequestParameterVerify( boolean allowRequestParameterVerify ) {
    this.allowRequestParameterVerify = allowRequestParameterVerify;
    return this;
  }
}
