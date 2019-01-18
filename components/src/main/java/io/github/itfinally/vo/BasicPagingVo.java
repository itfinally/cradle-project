package io.github.itfinally.vo;

@SuppressWarnings( "unchecked" )
public class BasicPagingVo<Vo extends BasicPagingVo<Vo>> extends BasicResponseVo<Vo> {

  // Page size
  private int page = 0;

  // Single page size
  private int limit = 0;

  public BasicPagingVo() {
  }

  public BasicPagingVo( ResponseStatus responseStatus ) {
    super( responseStatus );
  }

  public BasicPagingVo( ResponseStatus responseStatus, int page, int limit ) {
    super( responseStatus );
    this.page = page;
    this.limit = limit;
  }

  public int getPage() {
    return page;
  }

  public Vo setPage( int page ) {
    this.page = page;
    return ( Vo ) this;
  }

  public int getLimit() {
    return limit;
  }

  public Vo setLimit( int limit ) {
    this.limit = limit;
    return ( Vo ) this;
  }

  @Override
  public String toString() {
    return "BasicPagingVo{" +
        "page=" + page +
        ", limit=" + limit +
        "} " + super.toString();
  }
}
