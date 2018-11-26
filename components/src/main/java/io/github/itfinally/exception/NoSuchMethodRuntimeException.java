package io.github.itfinally.exception;

public class NoSuchMethodRuntimeException extends RuntimeException {
  public NoSuchMethodRuntimeException() {
  }

  public NoSuchMethodRuntimeException( String message ) {
    super( message );
  }

  public NoSuchMethodRuntimeException( String message, Throwable cause ) {
    super( message, cause );
  }

  public NoSuchMethodRuntimeException( Throwable cause ) {
    super( cause );
  }

  public NoSuchMethodRuntimeException( String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace ) {
    super( message, cause, enableSuppression, writableStackTrace );
  }
}
