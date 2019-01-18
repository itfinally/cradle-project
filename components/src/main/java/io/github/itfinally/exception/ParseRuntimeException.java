package io.github.itfinally.exception;

public class ParseRuntimeException extends RuntimeException {
  public ParseRuntimeException() {
  }

  public ParseRuntimeException( String message ) {
    super( message );
  }

  public ParseRuntimeException( String message, Throwable cause ) {
    super( message, cause );
  }

  public ParseRuntimeException( Throwable cause ) {
    super( cause );
  }

  public ParseRuntimeException( String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace ) {
    super( message, cause, enableSuppression, writableStackTrace );
  }
}
