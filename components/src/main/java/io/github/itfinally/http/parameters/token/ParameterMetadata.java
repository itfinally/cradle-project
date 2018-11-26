package io.github.itfinally.http.parameters.token;

public class ParameterMetadata {
  private final String name;
  private final Class<?> type;

  public ParameterMetadata( String name, Class<?> type ) {
    this.name = name;
    this.type = type;
  }

  public String getName() {
    return name;
  }

  public Class<?> getType() {
    return type;
  }

  @Override
  public String toString() {
    return "ParameterMetadata{" +
        "name='" + name + '\'' +
        ", type=" + type +
        '}';
  }
}
