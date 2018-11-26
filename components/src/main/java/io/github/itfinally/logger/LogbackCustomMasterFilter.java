package io.github.itfinally.logger;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import java.util.Iterator;
import java.util.List;

public class LogbackCustomMasterFilter extends Filter<ILoggingEvent> {
  private String[] packages;

  @Override
  public FilterReply decide( ILoggingEvent event ) {
    if ( isBetweenInfoAndError( event.getLevel() )
        || ( event.getLevel() == Level.DEBUG && isExpectedPackage( event.getLoggerName() ) ) ) {

      return FilterReply.ACCEPT;
    }

    return FilterReply.DENY;
  }

  private boolean isBetweenInfoAndError( final Level targetLevel ) {
    return Level.INFO.levelInt <= targetLevel.levelInt && targetLevel.levelInt <= Level.ERROR.levelInt;
  }

  private boolean isExpectedPackage( String packageName ) {
    for ( String item : packages ) {
      if ( packageName.startsWith( item ) ) {
        return true;
      }
    }

    return false;
  }

  public void setPackages( String packages ) {
    List<String> packagesList = Lists.newArrayList( packages.split( "," ) );

    Iterator<String> iter = packagesList.iterator();
    while ( iter.hasNext() ) {
      if ( Strings.isNullOrEmpty( iter.next() ) ) {
        iter.remove();
      }
    }

    this.packages = packagesList.toArray( new String[ 0 ] );
    for ( int index = this.packages.length - 1; index >= 0; index -= 1 ) {
      this.packages[ index ] = this.packages[ index ].trim();
    }
  }
}
