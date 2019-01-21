package io.github.itfinally.logger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CheckedLogger {
  private final Logger logger;

  public CheckedLogger( Class<?> clazz ) {
    this.logger = LoggerFactory.getLogger( clazz );
  }

  public void trace( String format, Object... args ) {
    if ( logger.isTraceEnabled() ) {
      logger.trace( format, args );
    }
  }

  public void debug( String format, Object... args ) {
    if ( logger.isDebugEnabled() ) {
      logger.debug( format, args );
    }
  }

  public void info( String format, Object... args ) {
    if ( logger.isInfoEnabled() ) {
      logger.info( format, args );
    }
  }

  public void warn( String format, Object... args ) {
    if ( logger.isWarnEnabled() ) {
      logger.warn( format, args );
    }
  }

  public void error( String format, Object... args ) {
    if ( logger.isErrorEnabled() ) {
      logger.error( format, args );
    }
  }

  public Logger getLogger() {
    return logger;
  }
}
