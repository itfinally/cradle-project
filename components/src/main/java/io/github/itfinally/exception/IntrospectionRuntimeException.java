package io.github.itfinally.exception;

public class IntrospectionRuntimeException extends RuntimeException {
  public IntrospectionRuntimeException() {
  }

  public IntrospectionRuntimeException( String message ) {
    super( message );
  }

  public IntrospectionRuntimeException( String message, Throwable cause ) {
    super( message, cause );
  }

  public IntrospectionRuntimeException( Throwable cause ) {
    super( cause );
  }

  public IntrospectionRuntimeException( String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace ) {
    super( message, cause, enableSuppression, writableStackTrace );
  }
}
