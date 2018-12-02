package io.github.itfinally.utils;

import com.google.common.collect.Sets;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ThreadLocalDelegatedFactory {
  private final static Set<SafetyThreadLocal<?>> threadLocals = Sets.newConcurrentHashSet();

  private ThreadLocalDelegatedFactory() {
  }

  public static <T> ThreadLocal<T> newThreadLocal() {
    ThreadLocal<T> threadLocal = new SafetyThreadLocal<>();
    threadLocals.add( ( SafetyThreadLocal<?> ) threadLocal );

    return threadLocal;
  }

  public static void removeThreadLocal( ThreadLocal<?> threadLocal ) {
    if ( threadLocal instanceof SafetyThreadLocal ) {
      threadLocals.remove( threadLocal );
    }
  }

  private static void cleanup() {
    for ( SafetyThreadLocal<?> threadLocal : threadLocals ) {
      threadLocal.remove();
    }
  }

  private static class SafetyThreadLocal<T> extends ThreadLocal<T> {
    private static final Class<Void> placeholder = Void.class;
    private final ConcurrentMap<String, Class<Void>> setupThread = new ConcurrentHashMap<>();

    @Override
    public void set( T value ) {
      Thread currentThread = Thread.currentThread();
      String currentThreadName = currentThread.getName();

      if ( !setupThread.containsKey( currentThreadName ) && null == setupThread.putIfAbsent( currentThreadName, placeholder ) ) {
        currentThread.setUncaughtExceptionHandler( new ThreadExceptionHandler(
            currentThread.getUncaughtExceptionHandler() ) );
      }

      super.set( value );
    }
  }

  private static final class ThreadExceptionHandler implements Thread.UncaughtExceptionHandler {
    private final Thread.UncaughtExceptionHandler defaultExceptionHandler;

    private ThreadExceptionHandler( Thread.UncaughtExceptionHandler defaultExceptionHandler ) {
      this.defaultExceptionHandler = defaultExceptionHandler;
    }

    @Override
    public void uncaughtException( Thread t, Throwable e ) {
      ThreadLocalDelegatedFactory.cleanup();

      if ( defaultExceptionHandler != null ) {
        defaultExceptionHandler.uncaughtException( t, e );

      } else {
        throw new RuntimeException( e );
      }
    }
  }
}
