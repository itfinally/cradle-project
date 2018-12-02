package io.github.itfinally.exception;

public class MultiMatchMethodRuntimeException extends RuntimeException {
  public MultiMatchMethodRuntimeException() {
  }

  public MultiMatchMethodRuntimeException( String message ) {
    super( message );
  }

  public MultiMatchMethodRuntimeException( String message, Throwable cause ) {
    super( message, cause );
  }

  public MultiMatchMethodRuntimeException( Throwable cause ) {
    super( cause );
  }

  public MultiMatchMethodRuntimeException( String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace ) {
    super( message, cause, enableSuppression, writableStackTrace );
  }
}
