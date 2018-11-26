package io.github.itfinally.http.parameters.interceptor;

import org.apache.catalina.util.ParameterMap;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.*;

class DelegatedHttpServletRequest implements HttpServletRequest {
  private final HttpServletRequest request;
  private final Map<String, String[]> parameters;

  DelegatedHttpServletRequest( HttpServletRequest request, Map<String, String[]> parameters ) {
    this.request = request;
    this.parameters = parameters;
  }

  @Override
  public String getParameter( String name ) {
    if ( parameters.containsKey( name ) ) {
      String[] value = parameters.get( name );

      if ( value.length == 0 ) {
        return "";
      }

      return value[ 0 ];
    }

    return request.getParameter( name );
  }

  @Override
  public Enumeration<String> getParameterNames() {
    Set<String> keys = new HashSet<>();

    Enumeration<String> iterator = request.getParameterNames();
    while ( iterator.hasMoreElements() ) {
      keys.add( iterator.nextElement() );
    }

    keys.addAll( parameters.keySet() );

    return Collections.enumeration( keys );
  }

  @Override
  public String[] getParameterValues( String name ) {
    if ( parameters.containsKey( name ) ) {
      return parameters.get( name );
    }

    return request.getParameterValues( name );
  }

  @Override
  public Map<String, String[]> getParameterMap() {
    ParameterMap<String, String[]> parameterMap = ( ParameterMap<String, String[]> ) request.getParameterMap();
    parameterMap.setLocked( false );

    for ( Map.Entry<String, String[]> entry : parameters.entrySet() ) {
      parameterMap.put( entry.getKey(), entry.getValue() );
    }

    parameterMap.setLocked( true );

    return parameterMap;
  }

  // Delegated method

  @Override
  public String getAuthType() {
    return request.getAuthType();
  }

  @Override
  public Cookie[] getCookies() {
    return request.getCookies();
  }

  @Override
  public long getDateHeader( String name ) {
    return request.getDateHeader( name );
  }

  @Override
  public String getHeader( String name ) {
    return request.getHeader( name );
  }

  @Override
  public Enumeration<String> getHeaders( String name ) {
    return request.getHeaders( name );
  }

  @Override
  public Enumeration<String> getHeaderNames() {
    return request.getHeaderNames();
  }

  @Override
  public int getIntHeader( String name ) {
    return request.getIntHeader( name );
  }

  @Override
  public String getMethod() {
    return request.getMethod();
  }

  @Override
  public String getPathInfo() {
    return request.getPathInfo();
  }

  @Override
  public String getPathTranslated() {
    return request.getPathTranslated();
  }

  @Override
  public String getContextPath() {
    return request.getContextPath();
  }

  @Override
  public String getQueryString() {
    return request.getQueryString();
  }

  @Override
  public String getRemoteUser() {
    return request.getRemoteUser();
  }

  @Override
  public boolean isUserInRole( String role ) {
    return request.isUserInRole( role );
  }

  @Override
  public Principal getUserPrincipal() {
    return request.getUserPrincipal();
  }

  @Override
  public String getRequestedSessionId() {
    return request.getRequestedSessionId();
  }

  @Override
  public String getRequestURI() {
    return request.getRequestURI();
  }

  @Override
  public StringBuffer getRequestURL() {
    return request.getRequestURL();
  }

  @Override
  public String getServletPath() {
    return request.getServletPath();
  }

  @Override
  public HttpSession getSession( boolean create ) {
    return request.getSession( create );
  }

  @Override
  public HttpSession getSession() {
    return request.getSession();
  }

  @Override
  public String changeSessionId() {
    return request.changeSessionId();
  }

  @Override
  public boolean isRequestedSessionIdValid() {
    return request.isRequestedSessionIdValid();
  }

  @Override
  public boolean isRequestedSessionIdFromCookie() {
    return request.isRequestedSessionIdFromCookie();
  }

  @Override
  public boolean isRequestedSessionIdFromURL() {
    return request.isRequestedSessionIdFromURL();
  }

  @Override
  @Deprecated
  public boolean isRequestedSessionIdFromUrl() {
    return request.isRequestedSessionIdFromUrl();
  }

  @Override
  public boolean authenticate( HttpServletResponse response ) throws IOException, ServletException {
    return request.authenticate( response );
  }

  @Override
  public void login( String username, String password ) throws ServletException {
    request.login( username, password );
  }

  @Override
  public void logout() throws ServletException {
    request.logout();
  }

  @Override
  public Collection<Part> getParts() throws IOException, ServletException {
    return request.getParts();
  }

  @Override
  public Part getPart( String name ) throws IOException, ServletException {
    return request.getPart( name );
  }

  @Override
  public <T extends HttpUpgradeHandler> T upgrade( Class<T> httpUpgradeHandlerClass ) throws IOException, ServletException {
    return request.upgrade( httpUpgradeHandlerClass );
  }

  @Override
  public Object getAttribute( String name ) {
    return request.getAttribute( name );
  }

  @Override
  public Enumeration<String> getAttributeNames() {
    return request.getAttributeNames();
  }

  @Override
  public String getCharacterEncoding() {
    return request.getCharacterEncoding();
  }

  @Override
  public void setCharacterEncoding( String env ) throws UnsupportedEncodingException {
    request.setCharacterEncoding( env );
  }

  @Override
  public int getContentLength() {
    return request.getContentLength();
  }

  @Override
  public long getContentLengthLong() {
    return request.getContentLengthLong();
  }

  @Override
  public String getContentType() {
    return request.getContentType();
  }

  @Override
  public ServletInputStream getInputStream() throws IOException {
    return request.getInputStream();
  }

  @Override
  public String getProtocol() {
    return request.getProtocol();
  }

  @Override
  public String getScheme() {
    return request.getScheme();
  }

  @Override
  public String getServerName() {
    return request.getServerName();
  }

  @Override
  public int getServerPort() {
    return request.getServerPort();
  }

  @Override
  public BufferedReader getReader() throws IOException {
    return request.getReader();
  }

  @Override
  public String getRemoteAddr() {
    return request.getRemoteAddr();
  }

  @Override
  public String getRemoteHost() {
    return request.getRemoteHost();
  }

  @Override
  public void setAttribute( String name, Object o ) {
    request.setAttribute( name, o );
  }

  @Override
  public void removeAttribute( String name ) {
    request.removeAttribute( name );
  }

  @Override
  public Locale getLocale() {
    return request.getLocale();
  }

  @Override
  public Enumeration<Locale> getLocales() {
    return request.getLocales();
  }

  @Override
  public boolean isSecure() {
    return request.isSecure();
  }

  @Override
  public RequestDispatcher getRequestDispatcher( String path ) {
    return request.getRequestDispatcher( path );
  }

  @Override
  @Deprecated
  public String getRealPath( String path ) {
    return request.getRealPath( path );
  }

  @Override
  public int getRemotePort() {
    return request.getRemotePort();
  }

  @Override
  public String getLocalName() {
    return request.getLocalName();
  }

  @Override
  public String getLocalAddr() {
    return request.getLocalAddr();
  }

  @Override
  public int getLocalPort() {
    return request.getLocalPort();
  }

  @Override
  public ServletContext getServletContext() {
    return request.getServletContext();
  }

  @Override
  public AsyncContext startAsync() throws IllegalStateException {
    return request.startAsync();
  }

  @Override
  public AsyncContext startAsync( ServletRequest servletRequest, ServletResponse servletResponse ) throws IllegalStateException {
    return request.startAsync( servletRequest, servletResponse );
  }

  @Override
  public boolean isAsyncStarted() {
    return request.isAsyncStarted();
  }

  @Override
  public boolean isAsyncSupported() {
    return request.isAsyncSupported();
  }

  @Override
  public AsyncContext getAsyncContext() {
    return request.getAsyncContext();
  }

  @Override
  public DispatcherType getDispatcherType() {
    return request.getDispatcherType();
  }
}
