package io.github.itfinally.http.parameters.token;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;

public class MethodMetadata {
  private final Map<String, Class<?>> parameterMetadata;
  private final Map<String, Set<String>> requestPaths;
  private final Method verifier;
  private final int verifierParameterLength;
  private final Object applier;

  public MethodMetadata( Map<String, Class<?>> parameterMetadata, Map<String, Set<String>> requestPaths,
                         Method verifier, int verifierParameterLength, Object applier ) {

    this.parameterMetadata = parameterMetadata;
    this.requestPaths = requestPaths;
    this.verifier = verifier;
    this.verifierParameterLength = verifierParameterLength;
    this.applier = applier;
  }

  public Map<String, Class<?>> getParameterMetadata() {
    return parameterMetadata;
  }

  public Map<String, Set<String>> getRequestPaths() {
    return requestPaths;
  }

  public Method getVerifier() {
    return verifier;
  }

  public int getVerifierParameterLength() {
    return verifierParameterLength;
  }

  public Object getApplier() {
    return applier;
  }
}
