package io.github.itfinally.http.parameters;

import com.google.common.base.Strings;
import io.github.itfinally.bean.BeanInfoExplorer;
import io.github.itfinally.bean.DuckCalling;
import io.github.itfinally.exception.NoSuchMethodRuntimeException;
import io.github.itfinally.http.parameters.token.MethodMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;

import javax.annotation.Resource;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
@ConditionalOnBean( RequestParametersVerifierConfiguration.class )
public class HttpVerifierCollector implements ApplicationListener<ContextRefreshedEvent> {
  private static final LocalVariableTableParameterNameDiscoverer parameterNameDiscoverer = new LocalVariableTableParameterNameDiscoverer();
  private static final Logger logger = LoggerFactory.getLogger( HttpVerifierCollector.class );
  private static Map<String, Map<String, MethodMetadata>> methodMetadataMappings;

  private final ConcurrentMap<Class<?>, Object> verifierApplier = new ConcurrentHashMap<>();

  @Resource
  private ApplicationContext springContext;

  public static Map<String, Map<String, MethodMetadata>> getMethodMetadataMappings() {
    return methodMetadataMappings;
  }

  @Override
  public void onApplicationEvent( ContextRefreshedEvent contextRefreshedEvent ) {
    Map<String, Object> controllers = new HashMap<>( 32 );
    controllers.putAll( springContext.getBeansWithAnnotation( Controller.class ) );
    controllers.putAll( springContext.getBeansWithAnnotation( RestController.class ) );

    List<Method> methods;
    MethodMetadata methodMetadata;
    Map<String, Map<String, MethodMetadata>> methodMetadataMappings = new HashMap<>( 64 );

    for ( Object controller : controllers.values() ) {
      methods = AopUtils.isAopProxy( controller )
          ? Arrays.asList( AopUtils.getTargetClass( controller ).getDeclaredMethods() )
          : Arrays.asList( controller.getClass().getDeclaredMethods() );

      for ( Method method : methods ) {
        methodMetadata = findControllerVerifier( method );
        if ( null == methodMetadata ) {
          continue;
        }

        for ( Map.Entry<String, Set<String>> pathInfo : methodMetadata.getRequestPaths().entrySet() ) {
          if ( !methodMetadataMappings.containsKey( pathInfo.getKey() ) ) {
            methodMetadataMappings.put( pathInfo.getKey(), new HashMap<String, MethodMetadata>( pathInfo.getValue().size() ) );
          }

          for ( String path : pathInfo.getValue() ) {
            methodMetadataMappings.get( pathInfo.getKey() ).put( path.toLowerCase(), methodMetadata );
          }
        }
      }
    }

    HttpVerifierCollector.methodMetadataMappings = Collections.unmodifiableMap( methodMetadataMappings );
  }

  @SuppressWarnings( "unchecked" )
  private MethodMetadata findControllerVerifier( Method method ) {
    if ( method.getParameterTypes().length <= 0 || !isControllerMethod( method ) ) {
      return null;
    }

    String[] call = new String[]{ null };
    Class<? extends HttpArgumentsVerifier> verifierClass = findVerifierClass( method, call );
    if ( null == verifierClass || Strings.isNullOrEmpty( call[ 0 ] ) ) {
      logger.warn( "Method '{}' of class '{}' has no verifier.", method.getName(), method.getDeclaringClass().getName() );
      return null;
    }

    int[] parameterLength = new int[ 1 ];
    Method verifier = findVerifyMethod( verifierClass, call[ 0 ], parameterLength );

    Map<String, Set<String>> requestPaths = findRequestPaths( method.getDeclaringClass().getAnnotations(), method.getDeclaredAnnotations() );
    Map<String, Class<?>> parameterMetadata = buildParameterMetadata( method );
    Object applier = getApplier( verifier.getDeclaringClass() );

    return new MethodMetadata( parameterMetadata, requestPaths, verifier, parameterLength[ 0 ], applier );
  }

  private Object getApplier( Class<?> verifierOfClass ) {
    Object applier;

    if ( verifierApplier.containsKey( verifierOfClass ) ) {
      applier = verifierApplier.get( verifierOfClass );

    } else {
      try {
        applier = verifierOfClass.newInstance();
        verifierApplier.put( verifierOfClass, applier );

      } catch ( InstantiationException | IllegalAccessException e ) {
        throw new RuntimeException( e );
      }
    }

    return applier;
  }

  private Map<String, Class<?>> buildParameterMetadata( Method method ) {
    Annotation[][] parameterAnnotations = method.getParameterAnnotations();
    String[] names = parameterNameDiscoverer.getParameterNames( method );
    Class<?>[] parameterTypes = method.getParameterTypes();

    Map<String, Class<?>> parameterMetadata = new HashMap<>();
    Class<?> type;
    for ( int index = 0, length = names.length; index < length; index += 1 ) {
      type = parameterTypes[ index ];

      if ( isServletApi( type ) || isSpringMvcApi( type ) || isRequestBody( parameterAnnotations, index ) ) {
        continue;
      }

      if ( isBasicType( type ) || isTimeType( type ) ) {
        parameterMetadata.put( findRequestParameterName( names, parameterAnnotations, index ), type );
        continue;
      }

      for ( Map.Entry<String, BeanInfoExplorer.BeanInfo> entry : BeanInfoExplorer.getPropertiesInfo( type ).entrySet() ) {
        parameterMetadata.put( entry.getKey(), entry.getValue().getProperty().getType() );
      }
    }

    return parameterMetadata;
  }

  // static method


  private static boolean isControllerMethod( Method method ) {
    return method.getAnnotation( RequestMapping.class ) != null
        || method.getAnnotation( GetMapping.class ) != null
        || method.getAnnotation( PostMapping.class ) != null
        || method.getAnnotation( PutMapping.class ) != null
        || method.getAnnotation( DeleteMapping.class ) != null;
  }

  private static Class<? extends HttpArgumentsVerifier> findVerifierClass( Method method, String[] call ) {
    RequestVerify requestVerifyMarkOnClass = method.getDeclaringClass().getAnnotation( RequestVerify.class );
    RequestVerify requestVerifyMarkOnMethod = method.getAnnotation( RequestVerify.class );
    Class<? extends HttpArgumentsVerifier> requestVerifierClass = null;

    if ( requestVerifyMarkOnMethod != null ) {
      if ( requestVerifyMarkOnMethod.value() != HttpArgumentsVerifier.class ) {
        requestVerifierClass = requestVerifyMarkOnMethod.value();
      }

      if ( !Strings.isNullOrEmpty( requestVerifyMarkOnMethod.method() ) ) {
        call[ 0 ] = requestVerifyMarkOnMethod.method();
      }
    }

    if ( requestVerifyMarkOnClass != null ) {
      if ( null == requestVerifierClass && requestVerifyMarkOnClass.value() != HttpArgumentsVerifier.class ) {
        requestVerifierClass = requestVerifyMarkOnClass.value();
      }

      if ( !Strings.isNullOrEmpty( call[ 0 ] ) && !Strings.isNullOrEmpty( requestVerifyMarkOnClass.method() ) ) {
        call[ 0 ] = requestVerifyMarkOnClass.method();
      }
    }

    return requestVerifierClass;
  }

  private static Method findVerifyMethod( Class<?> verifierClass, String call, int[] parameterLength ) {
    Method verifier = findVerifyMethodInRecursive( verifierClass, call, parameterLength );

    if ( null == verifier ) {
      throw new NoSuchMethodRuntimeException( String.format( "Method '%s' of class '%s' is not found. " +
              "try 'VerifyResultPair xxxx( Map<String, String> args )' " +
              "or 'VerifyResultPair xxxx( Map<String, String> args, Map<String, MethodMetadata.ArgMetadata> argMetadata )'",

          call, verifierClass.getName() ) );
    }

    if ( VerifyResultPair.class != verifier.getReturnType() ) {
      throw new UnsupportedOperationException( String.format( "Should be use 'VerifyResultPair' as '%s.%s' return type.",
          call, verifierClass.getName() ) );
    }

    return verifier;
  }

  private static Method findVerifyMethodInRecursive( Class<?> verifierClass, String call, int[] parameterLength ) {
    if ( !HttpArgumentsVerifier.class.isAssignableFrom( verifierClass ) ) {
      return null;
    }

    try {
      parameterLength[ 0 ] = 1;
      return verifierClass.getDeclaredMethod( call, Map.class );

    } catch ( NoSuchMethodException ignore ) {
    }

    try {
      parameterLength[ 0 ] = 2;
      return verifierClass.getDeclaredMethod( call, Map.class, Map.class );

    } catch ( NoSuchMethodException ignore ) {
    }

    return findVerifyMethodInRecursive( verifierClass.getSuperclass(), call, parameterLength );
  }

  private static boolean isBasicType( Class<?> type ) {
    return ( boolean.class == type || Boolean.class == type )
        || ( char.class == type || Character.class == type )
        || ( byte.class == type || Byte.class == type )
        || ( short.class == type || Short.class == type )
        || ( int.class == type || Integer.class == type )
        || ( long.class == type || Long.class == type )
        || ( float.class == type || Float.class == type )
        || ( double.class == type || Double.class == type )
        || String.class == type;
  }

  private static boolean isTimeType( Class<?> type ) {
    return Date.class.isAssignableFrom( type ) || Calendar.class.isAssignableFrom( type );
  }

  private static boolean isServletApi( Class<?> type ) {
    return ServletRequest.class.isAssignableFrom( type ) || ServletResponse.class.isAssignableFrom( type )
        || HttpSession.class.isAssignableFrom( type ) || Cookie.class.isAssignableFrom( type );
  }

  private static boolean isSpringMvcApi( Class<?> type ) {
    return Model.class.isAssignableFrom( type ) || ModelAndView.class.isAssignableFrom( type )
        || ModelMap.class.isAssignableFrom( type ) || View.class.isAssignableFrom( type )
        || MultipartFile.class.isAssignableFrom( type );
  }

  private static boolean isRequestBody( Annotation[][] annotations, int index ) {
    for ( Annotation annotation : annotations[ index ] ) {
      if ( annotation instanceof RequestBody ) {
        return true;
      }
    }

    return false;
  }

  private static String findRequestParameterName( String[] names, Annotation[][] annotations, int index ) {
    String name = null;

    for ( Annotation annotation : annotations[ index ] ) {
      if ( !( annotation instanceof RequestParam || annotation instanceof PathVariable ) ) {
        continue;
      }

      if ( Strings.isNullOrEmpty( name ) ) {
        name = DuckCalling.invoke( annotation, "value" );
      }

      if ( Strings.isNullOrEmpty( name ) ) {
        name = DuckCalling.invoke( annotation, "name" );
      }

      if ( !Strings.isNullOrEmpty( name ) ) {
        break;
      }
    }

    return Strings.isNullOrEmpty( name ) ? names[ index ] : name;
  }

  private static Map<String, Set<String>> findRequestPaths( Annotation[] classAnnotations, Annotation[] methodAnnotations ) {
    Map<String, Set<String>> classRequestPaths = extractRequestPath( classAnnotations );
    Map<String, Set<String>> methodRequestPaths = extractRequestPath( methodAnnotations );

    Map<String, Set<String>> pathMappings = new HashMap<>( classRequestPaths.size() + methodRequestPaths.size() );
    String requestMethod;

    for ( Map.Entry<String, Set<String>> parentPathInfo : classRequestPaths.entrySet() ) {
      for ( String parentPath : parentPathInfo.getValue() ) {
        for ( Map.Entry<String, Set<String>> childPathInfo : methodRequestPaths.entrySet() ) {
          for ( String childPath : childPathInfo.getValue() ) {

            requestMethod = !"".equals( childPathInfo.getKey() )
                ? childPathInfo.getKey()

                : !"".equals( parentPathInfo.getKey() )
                ? parentPathInfo.getKey()

                : "";

            if ( !pathMappings.containsKey( requestMethod ) ) {
              pathMappings.put( requestMethod, new HashSet<String>() );
            }

            pathMappings.get( requestMethod ).add( String.format( "%s/%s", parentPath, childPath )
                .replaceAll( "//", "/" ) );
          }
        }
      }
    }

    return pathMappings;
  }

  private static Map<String, Set<String>> extractRequestPath( Annotation[] annotations ) {
    Map<String, Set<String>> pathMappings = new HashMap<>();
    String[] paths;

    for ( Annotation annotation : annotations ) {
      if ( !( annotation instanceof RequestMapping
          || annotation instanceof GetMapping
          || annotation instanceof PostMapping
          || annotation instanceof PutMapping
          || annotation instanceof DeleteMapping ) ) {

        continue;
      }

      for ( String method : extractRequestMethod( annotation ) ) {
        if ( !pathMappings.containsKey( method ) ) {
          pathMappings.put( method, new HashSet<String>() );
        }

        paths = DuckCalling.invoke( annotation, "value" );
        if ( paths != null && paths.length > 0 ) {
          pathMappings.get( method ).addAll( Arrays.asList( paths ) );
        }

        paths = DuckCalling.invoke( annotation, "path" );
        if ( paths != null && paths.length > 0 ) {
          pathMappings.get( method ).addAll( Arrays.asList( paths ) );
        }
      }
    }

    return pathMappings;
  }

  private static Set<String> extractRequestMethod( Annotation annotation ) {
    Set<String> requestMethods = new HashSet<>();

    if ( annotation instanceof RequestMapping ) {
      RequestMethod[] methods = ( ( RequestMapping ) annotation ).method();

      if ( methods.length <= 0 ) {
        requestMethods.add( "" );

      } else {
        for ( RequestMethod method : methods ) {

          if ( RequestMethod.GET == method ) {
            requestMethods.add( "get" );

          } else if ( RequestMethod.POST == method ) {
            requestMethods.add( "post" );

          } else if ( RequestMethod.PUT == method ) {
            requestMethods.add( "put" );

          } else if ( RequestMethod.DELETE == method ) {
            requestMethods.add( "delete" );
          }
        }
      }

    } else if ( annotation instanceof GetMapping ) {
      requestMethods.add( "get" );

    } else if ( annotation instanceof PostMapping ) {
      requestMethods.add( "post" );

    } else if ( annotation instanceof PutMapping ) {
      requestMethods.add( "put" );

    } else if ( annotation instanceof DeleteMapping ) {
      requestMethods.add( "delete" );
    }

    return requestMethods;
  }
}