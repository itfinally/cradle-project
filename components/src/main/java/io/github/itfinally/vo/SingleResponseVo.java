package io.github.itfinally.vo;

import java.util.Objects;

public class SingleResponseVo<T> extends BasicResponseVo<SingleResponseVo<T>> {
  private T result;

  public SingleResponseVo() {
  }

  public SingleResponseVo( ResponseStatus responseStatus ) {
    super( responseStatus );
  }

  public SingleResponseVo( ResponseStatus responseStatus, T result ) {
    this( responseStatus );
    this.result = result;
  }

  public T getResult() {
    return result;
  }

  public SingleResponseVo<T> setResult( T result ) {
    this.result = result;
    return this;
  }

  @Override
  public boolean equals( Object o ) {
    if ( this == o ) return true;
    if ( o == null || getClass() != o.getClass() ) return false;
    if ( !super.equals( o ) ) return false;
    SingleResponseVo<?> that = ( SingleResponseVo<?> ) o;
    return Objects.equals( result, that.result );
  }

  @Override
  public int hashCode() {
    return Objects.hash( super.hashCode(), result );
  }

  @Override
  public String toString() {
    return "SingleResponseVo{" +
        "result=" + result +
        "} " + super.toString();
  }
}
