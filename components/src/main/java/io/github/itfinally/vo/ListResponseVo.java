package io.github.itfinally.vo;

import io.github.itfinally.http.HttpCodeAdapter;

import java.util.Collection;
import java.util.Objects;

public class ListResponseVo<T> extends BasicPagingVo<ListResponseVo<T>> {
  private Collection<T> result;

  public ListResponseVo() {
  }

  public ListResponseVo( HttpCodeAdapter httpCodeAdapter ) {
    super( httpCodeAdapter );
  }

  public ListResponseVo( HttpCodeAdapter httpCodeAdapter, int page, int limit ) {
    super( httpCodeAdapter, page, limit );
  }

  public ListResponseVo( HttpCodeAdapter httpCodeAdapter, int page, int limit, Collection<T> result ) {
    super( httpCodeAdapter, page, limit );
    this.result = result;
  }

  public Collection<T> getResult() {
    return result;
  }

  public ListResponseVo<T> setResult( Collection<T> result ) {
    this.result = result;
    return this;
  }

  @Override
  public boolean equals( Object o ) {
    if ( this == o ) return true;
    if ( o == null || getClass() != o.getClass() ) return false;
    if ( !super.equals( o ) ) return false;
    ListResponseVo<?> that = ( ListResponseVo<?> ) o;
    return Objects.equals( result, that.result );
  }

  @Override
  public int hashCode() {
    return Objects.hash( super.hashCode(), result );
  }

  @Override
  public String toString() {
    return "ListResponseVo{" +
        "result=" + result +
        "} " + super.toString();
  }
}
