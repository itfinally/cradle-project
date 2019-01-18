package io.github.itfinally.exception;

public class MethodInvokeRuntimeException extends RuntimeException {
  public MethodInvokeRuntimeException() {
  }

  public MethodInvokeRuntimeException( String message ) {
    super( message );
  }

  public MethodInvokeRuntimeException( String message, Throwable cause ) {
    super( message, cause );
  }

  public MethodInvokeRuntimeException( Throwable cause ) {
    super( cause );
  }

  public MethodInvokeRuntimeException( String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace ) {
    super( message, cause, enableSuppression, writableStackTrace );
  }
}
