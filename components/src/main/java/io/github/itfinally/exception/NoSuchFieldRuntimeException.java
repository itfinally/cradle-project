package io.github.itfinally.exception;

public class NoSuchFieldRuntimeException extends RuntimeException {
  public NoSuchFieldRuntimeException() {
  }

  public NoSuchFieldRuntimeException( String message ) {
    super( message );
  }

  public NoSuchFieldRuntimeException( String message, Throwable cause ) {
    super( message, cause );
  }

  public NoSuchFieldRuntimeException( Throwable cause ) {
    super( cause );
  }

  public NoSuchFieldRuntimeException( String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace ) {
    super( message, cause, enableSuppression, writableStackTrace );
  }
}
