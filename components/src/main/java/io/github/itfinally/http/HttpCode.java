package io.github.itfinally.http;

public enum HttpCode implements HttpCodeAdapter {
  OK( 200, "请求成功" ),
  UNAUTHORIZED( 401, "未授权" ),
  TIMEOUT( 408, "请求超时" ),
  SERVER_ERROR( 500, "服务异常" );

  private int code;
  private String message;

  HttpCode( int code, String message ) {
    this.code = code;
    this.message = message;
  }

  public int getCode() {
    return code;
  }

  public String getMessage() {
    return message;
  }
}
