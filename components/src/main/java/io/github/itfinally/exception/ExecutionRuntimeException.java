package io.github.itfinally.exception;

public class ExecutionRuntimeException extends RuntimeException {
  public ExecutionRuntimeException() {
  }

  public ExecutionRuntimeException( String message ) {
    super( message );
  }

  public ExecutionRuntimeException( String message, Throwable cause ) {
    super( message, cause );
  }

  public ExecutionRuntimeException( Throwable cause ) {
    super( cause );
  }

  public ExecutionRuntimeException( String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace ) {
    super( message, cause, enableSuppression, writableStackTrace );
  }
}
