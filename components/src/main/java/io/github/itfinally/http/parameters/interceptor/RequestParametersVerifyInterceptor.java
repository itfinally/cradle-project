package io.github.itfinally.http.parameters.interceptor;

import com.google.common.collect.Sets;
import io.github.itfinally.http.parameters.FailureResponse;
import io.github.itfinally.http.parameters.HttpVerifierCollector;
import io.github.itfinally.http.parameters.RequestParametersVerifierConfiguration;
import io.github.itfinally.http.parameters.token.MethodMetadata;
import io.github.itfinally.http.parameters.VerifyResultPair;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.Resource;
import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.*;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

@Component
@ConditionalOnBean( RequestParametersVerifierConfiguration.class )
@WebFilter( filterName = "requestParametersVerifyInterceptor", urlPatterns = "/*" )
public class RequestParametersVerifyInterceptor implements Filter {
  private static final Set<String> ignoreMethod = Sets.newHashSet(
      RequestMethod.TRACE.toString().toLowerCase(),
      RequestMethod.HEAD.toString().toLowerCase(),
      RequestMethod.PATCH.toString().toLowerCase(),
      RequestMethod.OPTIONS.toString().toLowerCase()
  );

  @Resource
  private FailureResponse failureResponse;

  @Override
  public void init( FilterConfig filterConfig ) {
  }

  @Override
  public void doFilter( ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain ) throws IOException, ServletException {
    HttpServletRequest request = ( HttpServletRequest ) servletRequest;
    HttpServletResponse response = ( HttpServletResponse ) servletResponse;

    Map<String, Map<String, MethodMetadata>> methodMetadataMappings = HttpVerifierCollector.getMethodMetadataMappings();
    String requestUri = request.getRequestURI().toLowerCase();
    String requestMethod = request.getMethod().toLowerCase();

    if ( ignoreMethod.contains( requestMethod )
        || !( methodMetadataMappings.containsKey( "" )
        || methodMetadataMappings.containsKey( requestMethod ) ) ) {

      chain.doFilter( request, response );
      return;
    }

    MethodMetadata metadata = null;
    if ( methodMetadataMappings.get( "" ).containsKey( requestUri ) ) {
      metadata = methodMetadataMappings.get( "" ).get( requestUri );

    } else if ( methodMetadataMappings.get( requestMethod ).containsKey( requestUri ) ) {
      metadata = methodMetadataMappings.get( requestMethod ).get( requestUri );
    }

    if ( null == metadata ) {
      chain.doFilter( request, response );
      return;
    }

    Map<String, String> requestParameters = new HashMap<>();
    for ( String name : metadata.getParameterMetadata().keySet() ) {
      requestParameters.put( name, request.getParameter( name ) );
    }

    MarkHashMap<String> markedParameters = new MarkHashMap<>( requestParameters );
    Object result;

    try {
      if ( 1 == metadata.getVerifierParameterLength() ) {
        result = metadata.getVerifier().invoke( metadata.getApplier(), markedParameters );

      } else if ( 2 == metadata.getVerifierParameterLength() ) {
        result = metadata.getVerifier().invoke( metadata.getApplier(), markedParameters, metadata.getParameterMetadata() );

      } else {
        throw new IllegalStateException( "No match correct method parameter length." );
      }

    } catch ( IllegalAccessException | InvocationTargetException e ) {
      throw new RuntimeException( e );
    }

    if ( !( result instanceof VerifyResultPair ) ) {
      setResponseHeaders( response );
      failureResponse.responding( String.format( "The return type on verifier of path '%s' is error, " +
          "should be return an instance of 'VerifyResultPair'.", request.getRequestURI() ), response );

      return;
    }

    VerifyResultPair resultPair = ( VerifyResultPair ) result;
    if ( !resultPair.isPassed() ) {
      setResponseHeaders( response );
      failureResponse.responding( resultPair.getRespondingContent(), response );
      return;
    }

    HttpServletRequest newRequest = markedParameters.isModified()
        ? new DelegatedHttpServletRequest( request, modifyRequestParameters( request.getParameterMap(), markedParameters ) )
        : null;

    chain.doFilter( newRequest != null ? newRequest : request, response );
  }

  private void setResponseHeaders( HttpServletResponse response ) {
    response.setHeader( "Access-Control-Allow-Credentials", "true" );
    response.setHeader( "Access-Control-Allow-Origin", "*" );
    response.setContentType( "application/json;charset=UTF-8" );
  }

  private Map<String, String[]> modifyRequestParameters( Map<String, String[]> parameters, MarkHashMap<String> args ) {
    Map<String, String[]> newParameters = new HashMap<>();
    String[] values, newValue;

    for ( String field : args.getModifiedFields() ) {
      values = parameters.get( field );

      if ( null == values || values.length <= 1 ) {
        newParameters.put( field, new String[]{ args.get( field ) } );
        continue;
      }

      newValue = Arrays.copyOf( values, values.length );
      newValue[ 0 ] = args.get( field );

      newParameters.put( field, newValue );
    }

    return newParameters;
  }

  @Override
  public void destroy() {
  }
}
