package io.github.itfinally.vo;

import java.io.Serializable;
import java.util.Objects;

@SuppressWarnings( "unchecked" )
public class BasicResponseVo<Vo extends BasicResponseVo<Vo>> implements Serializable {

  // Response status
  private int status;

  // Response message
  private String message;

  public BasicResponseVo() {
  }

  public BasicResponseVo( ResponseStatus responseStatus ) {
    status = responseStatus.getCode();
    message = responseStatus.getMessage();
  }

  public int getStatus() {
    return status;
  }

  public Vo setStatus( int status ) {
    this.status = status;
    return ( Vo ) this;
  }

  public String getMessage() {
    return message;
  }

  public Vo setMessage( String message ) {
    this.message = message;
    return ( Vo ) this;
  }

  @Override
  public boolean equals( Object o ) {
    if ( this == o ) return true;
    if ( o == null || getClass() != o.getClass() ) return false;
    BasicResponseVo<?> that = ( BasicResponseVo<?> ) o;
    return status == that.status &&
        Objects.equals( message, that.message );
  }

  @Override
  public int hashCode() {
    return Objects.hash( status, message );
  }

  @Override
  public String toString() {
    return "BasicResponseVo{" +
        "status=" + status +
        ", message='" + message + '\'' +
        '}';
  }

  public static class Default extends BasicResponseVo<Default> {
    public Default() {
    }

    public Default( ResponseStatus responseStatus ) {
      super( responseStatus );
    }
  }
}
