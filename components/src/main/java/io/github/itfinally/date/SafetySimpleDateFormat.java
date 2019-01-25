package io.github.itfinally.date;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.github.itfinally.exception.ExecutionRuntimeException;
import io.github.itfinally.exception.ParseRuntimeException;

import java.text.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.*;

import static java.lang.Math.abs;
import static java.lang.Runtime.getRuntime;
import static java.lang.Thread.currentThread;

public class SafetySimpleDateFormat {
  private static final Cache<String, List<SimpleDateFormat>> datetimeFormats = CacheBuilder.newBuilder()
      .concurrencyLevel( getRuntime().availableProcessors() )
      .initialCapacity( 16 )
      .maximumSize( 10240 )
      .build();

  private final String format;

  public SafetySimpleDateFormat( String format ) {
    this.format = format;
  }

  private SimpleDateFormat getSimpleDateFormat() throws ExecutionRuntimeException {
    try {
      List<SimpleDateFormat> formats = datetimeFormats.get( format, new Callable<List<SimpleDateFormat>>() {
        @Override
        public List<SimpleDateFormat> call() {
          return createFormatter( format, new ArrayList<SimpleDateFormat>() );
        }
      } );

      return formats.get( abs( currentThread().getName().hashCode() % formats.size() ) );

    } catch ( ExecutionException e ) {
      throw new ExecutionRuntimeException( e );
    }
  }

  @SuppressWarnings( "all" )
  public Date parse( String source ) throws ParseRuntimeException {
    SimpleDateFormat formatter = getSimpleDateFormat();

    synchronized ( formatter ) {
      try {
        return formatter.parse( source );

      } catch ( ParseException e ) {
        throw new ParseRuntimeException( e );
      }
    }
  }

  @SuppressWarnings( "all" )
  public String format( Date date ) {
    SimpleDateFormat formatter = getSimpleDateFormat();

    synchronized ( formatter ) {
      return formatter.format( date );
    }
  }

  @SuppressWarnings( "all" )
  public String format( Object value ) {
    SimpleDateFormat formatter = getSimpleDateFormat();

    synchronized ( formatter ) {
      return formatter.format( value );
    }
  }

  private static List<SimpleDateFormat> createFormatter( String format, List<SimpleDateFormat> formats ) {
    for ( int index = 0, end = getRuntime().availableProcessors(); index < end; index += 1 ) {
      formats.add( new SimpleDateFormat( format ) );
    }

    // build virtual hash node
    for ( int size = formats.size(), index = size, end = index * 3; index < end; index += 1 ) {
      formats.add( formats.get( index - size ) );
    }

    // Avoid even number leads to insufficient hash, the result of ${even number} - 1 must be a odd number
    // hash format: ${target number} % ( ${even number} - 1 )
    int size = formats.size();
    if ( size % 2 != 0 ) {
      formats.add( formats.get( ThreadLocalRandom.current().nextInt( 0, size - 1 ) ) );
    }

    return formats;
  }
}
