package io.github.itfinally.bean;

import io.github.itfinally.exception.NoSuchMethodRuntimeException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

// Duck model in Java
public class DuckCalling {
  private static final ConcurrentMap<String, Method> methodMappings = new ConcurrentHashMap<>( 32 );
  private static final ConcurrentMap<Class<?>, List<Method>> classMethods = new ConcurrentHashMap<>( 32 );

  private DuckCalling() {
  }

  @SuppressWarnings( "unchecked" )
  public static <T> T invoke( Object applier, String name, Object... args ) {
    Class<?> clazz = applier.getClass();
    String key = buildKey( clazz, name, args );

    if ( !methodMappings.containsKey( key ) ) {
      if ( !classMethods.containsKey( clazz ) ) {
        classMethods.put( clazz, getMethods( clazz, new ArrayList<Method>() ) );
      }

      for ( Method method : classMethods.get( clazz ) ) {
        if ( method.getName().equals( name ) && method.getParameterTypes().length == args.length ) {
          methodMappings.put( key, method );
          break;
        }
      }

      // If method is not found.
      if ( !methodMappings.containsKey( key ) ) {
        throw new NoSuchMethodRuntimeException( String.format( "Method '%s' is not found from class '%s'", name, clazz ) );
      }
    }

    Method method = methodMappings.get( key );
    try {
      return null == method ? null : ( T ) method.invoke( applier, args );

    } catch ( IllegalAccessException | InvocationTargetException e ) {
      throw new RuntimeException( e );
    }
  }

  private static String buildKey( Class<?> clazz, String name, Object[] args ) {
    return String.format( "%s-%s-%d", clazz.getName(), name, args.length );
  }

  private static List<Method> getMethods( Class<?> clazz, List<Method> methods ) {
    if ( Object.class == clazz ) {
      return methods;

    } else {
      methods.addAll( Arrays.asList( clazz.getDeclaredMethods() ) );
      return getMethods( clazz.getSuperclass(), methods );
    }
  }
}
