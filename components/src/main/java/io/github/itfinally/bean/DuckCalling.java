package io.github.itfinally.bean;

import io.github.itfinally.exception.MultiMatchMethodRuntimeException;
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
  private static final ConcurrentMap<String, List<Method>> methodMappings = new ConcurrentHashMap<>( 32 );
  private static final ConcurrentMap<Class<?>, List<Method>> classMethods = new ConcurrentHashMap<>( 32 );

  private DuckCalling() {
  }

  @SuppressWarnings( "unchecked" )
  public static <T> T invoke( Object applier, String name, Object... args ) {
    Class<?> clazz = applier.getClass();
    String alias = buildAlias( clazz, name, args.length );

    if ( methodMappings.containsKey( alias ) ) {
      List<Method> methods = methodMappings.get( alias );
      return invokeMethod( findMatchMethod( clazz, name, args, methods ), applier, args );
    }

    if ( !classMethods.containsKey( clazz ) ) {
      classMethods.putIfAbsent( clazz, getMethods( clazz, new ArrayList<Method>() ) );
    }

    Method method = findMatchMethod( clazz, name, args, classMethods.get( clazz ) );
    if ( !methodMappings.containsKey( alias ) ) {
      methodMappings.putIfAbsent( alias, new ArrayList<Method>() );
    }

    methodMappings.get( alias ).add( method );
    return invokeMethod( method, applier, args );
  }

  private static String buildAlias( Class<?> clazz, String name, int argLength ) {
    return String.format( "%s.%s.%d", clazz.getName(), name, argLength );
  }

  private static List<Method> getMethods( Class<?> clazz, List<Method> methods ) {
    if ( Object.class == clazz ) {
      return methods;
    }

    methods.addAll( Arrays.asList( clazz.getDeclaredMethods() ) );
    return getMethods( clazz.getSuperclass(), methods );
  }

  private static Class<?>[] getParameterTypes( Object[] args ) {
    List<Class<?>> types = new ArrayList<>( args.length );

    for ( Object arg : args ) {
      types.add( null == arg ? null : arg.getClass() );
    }

    return types.toArray( new Class<?>[ 0 ] );
  }

  private static boolean matchMethodParameterTypes( Class<?>[] realType, Class<?>[] shapeType ) {
    if ( realType.length != shapeType.length ) {
      return false;
    }

    for ( int index = 0, length = realType.length; index < length; index += 1 ) {
      if ( null == realType[ index ] ) {
        continue;
      }

      if ( realType[ index ] != shapeType[ index ] ) {
        return false;
      }
    }

    return true;
  }

  private static Method findMatchMethod( Class<?> clazz, String name, Object[] args, List<Method> methods ) {
    List<Method> candidateMethod = new ArrayList<>();

    if ( 1 == methods.size() ) {
      candidateMethod.add( methods.get( 0 ) );

    } else {
      for ( Method method : methods ) {
        if ( method.getName().equals( name ) && matchMethodParameterTypes(
            getParameterTypes( args ), method.getParameterTypes() ) ) {

          candidateMethod.add( method );
        }
      }
    }

    if ( candidateMethod.isEmpty() ) {
      throw new NoSuchMethodRuntimeException( String.format(
          "No method '%s' from class '%s'", name, clazz.getName() ) );
    }

    if ( candidateMethod.size() != 1 ) {
      throw new MultiMatchMethodRuntimeException( String.format( "Ambiguous method call, There are more than one method " +
          "name as '%s' with parameter length of %d in class '%s'", name, args.length, clazz.getName() ) );
    }

    return candidateMethod.get( 0 );
  }

  @SuppressWarnings( "unchecked" )
  private static <T> T invokeMethod( Method method, Object applier, Object[] args ) {
    try {
      return null == method ? null : ( T ) method.invoke( applier, args );

    } catch ( IllegalAccessException | InvocationTargetException e ) {
      throw new RuntimeException( e );
    }
  }
}
