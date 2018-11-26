package io.github.itfinally.logger;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.config.Node;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.filter.AbstractFilter;
import org.apache.logging.log4j.message.Message;

import java.util.Iterator;
import java.util.List;

@Plugin( name = "Log4j2CustomMasterFilter", category = Node.CATEGORY, elementType = Filter.ELEMENT_TYPE, printObject = true )
public class Log4j2CustomMasterFilter extends AbstractFilter {
  private String[] packages;

  private Log4j2CustomMasterFilter( String packages ) {
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

  @PluginFactory
  public static Log4j2CustomMasterFilter createFilter( @PluginAttribute( "packages" ) String packages ) {
    return new Log4j2CustomMasterFilter( packages );
  }

  @Override
  public Result filter( final Logger logger, final Level level, final Marker marker, final String msg,
                        final Object... params ) {
    return filter( level, level.getDeclaringClass().getName() );
  }

  @Override
  public Result filter( final Logger logger, final Level level, final Marker marker, final Object msg,
                        final Throwable t ) {
    return filter( level, level.getDeclaringClass().getName() );
  }

  @Override
  public Result filter( final Logger logger, final Level level, final Marker marker, final Message msg,
                        final Throwable t ) {
    return filter( level, level.getDeclaringClass().getName() );
  }

  @Override
  public Result filter( final LogEvent event ) {
    return filter( event.getLevel(), event.getSource().getClassName() );
  }

  private Result filter( final Level level, final String declareClassName ) {
    if ( level.isInRange( Level.ERROR, Level.INFO ) || ( level == Level.DEBUG && isExpectedPackage( declareClassName ) ) ) {

      return Result.ACCEPT;
    }

    return Result.DENY;
  }

  private boolean isExpectedPackage( String packageName ) {
    for ( String item : packages ) {
      if ( packageName.startsWith( item ) ) {
        return true;
      }
    }

    return false;
  }
}
