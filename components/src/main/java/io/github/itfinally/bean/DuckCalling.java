package io.github.itfinally.bean;

import com.google.common.base.Joiner;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import io.github.itfinally.exception.ExecutionRuntimeException;
import io.github.itfinally.exception.MethodInvokeRuntimeException;
import io.github.itfinally.exception.MultiMatchMethodRuntimeException;
import io.github.itfinally.exception.NoSuchMethodRuntimeException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.*;

import static java.lang.Runtime.getRuntime;

// Duck model in Java
public final class DuckCalling {
  private static final HashFunction md5Function = Hashing.hmacMd5( DuckCalling.class.getName().getBytes() );

  private static final Cache<Long, Method> methodMappings = CacheBuilder.newBuilder()
      .concurrencyLevel( getRuntime().availableProcessors() )
      .expireAfterAccess( 30, TimeUnit.MINUTES )
      .initialCapacity( 32 )
      .build();

  private static final Cache<Class<?>, List<Method>> classMethods = CacheBuilder.newBuilder()
      .concurrencyLevel( getRuntime().availableProcessors() )
      .expireAfterAccess( 30, TimeUnit.MINUTES )
      .initialCapacity( 32 )
      .build();

  private DuckCalling() {
  }

  public static <T> T invoke( Object applier, final String name, Object... args )
      throws NoSuchMethodRuntimeException, MethodInvokeRuntimeException {

    final Object[] actualArgs = null == args ? new Object[] { null } : args;

    final Class<?> clazz = applier instanceof Class ? ( Class<?> ) applier : applier.getClass();
    final long methodHashCode = buildMethodHashCode( clazz, name, actualArgs );

    try {
      Method method = methodMappings.get( methodHashCode, new Callable<Method>() {
        @Override
        public Method call() throws Exception {

          List<Method> methodsTable = classMethods.get( clazz, new Callable<List<Method>>() {
            @Override
            public List<Method> call() {
              return getMethods( clazz, new ArrayList<Method>(), new HashSet<String>() );
            }
          } );

          return findMatchMethod( clazz, name, actualArgs, methodsTable );
        }
      } );

      return invokeMethod( method, applier, actualArgs );

    } catch ( ExecutionException e ) {
      throw new ExecutionRuntimeException( e );
    }
  }

  private static long buildMethodHashCode( Class<?> clazz, String name, Object[] args ) {
    List<String> argTypeNames = new ArrayList<>( args.length );

    for ( Object item : args ) {
      argTypeNames.add( null == item ? "null" : item.getClass().getName() );
    }

    String originName = String.format( "%s.%s.%s.%d", clazz.getName(), name,
        Joiner.on( "/" ).join( argTypeNames ), argTypeNames.size() );

    return md5Function.newHasher().putString( originName, Charset.defaultCharset() ).hash().asLong();
  }

  private static List<Method> getMethods( Class<?> clazz, List<Method> methods, Set<String> filter ) {
    if ( Object.class == clazz ) {
      return methods;
    }

    for ( Method method : clazz.getDeclaredMethods() ) {
      if ( Modifier.isPublic( method.getModifiers() ) && !filter.contains( method.getName() ) ) {
        filter.add( method.getName() );
        methods.add( method );
      }
    }

    return getMethods( clazz.getSuperclass(), methods, filter );
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

      List<String> parametersName = new ArrayList<>();

      for ( Class<?> item : getParameterTypes( args ) ) {
        parametersName.add( null == item ? "?" : item.getName() );
      }

      throw new NoSuchMethodRuntimeException( String.format( "No method '%s(%s)' on class '%s'",
          name, Joiner.on( ", " ).join( parametersName ), clazz.getName() ) );
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
      return ( T ) method.invoke( Modifier.isStatic( method.getModifiers() )
          ? applier.getClass() : applier, args );

    } catch ( IllegalAccessException | InvocationTargetException e ) {
      throw new MethodInvokeRuntimeException( e );
    }
  }
}