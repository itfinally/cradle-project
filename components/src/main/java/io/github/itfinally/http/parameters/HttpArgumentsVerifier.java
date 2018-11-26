package io.github.itfinally.http.parameters;

import java.util.Map;

public abstract class HttpArgumentsVerifier {

  // Verify priority -> self -> class -> super class
  public VerifyResultPair verification( Map<String, String> args, Map<String, Class<?>> parameterMetadata ) {
    return new VerifyResultPair().setPassed( false ).setRespondingContent( "UnImplement this verifier" );
  }
}
