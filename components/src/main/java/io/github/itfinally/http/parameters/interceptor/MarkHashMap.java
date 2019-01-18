package io.github.itfinally.http.parameters.interceptor;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;

public class MarkHashMap<V> extends HashMap<String, V> implements Map<String, V> {
  private Set<String> modifiedFields = new HashSet<>();

  public MarkHashMap( int initialCapacity, float loadFactor ) {
    super( initialCapacity, loadFactor );
  }

  public MarkHashMap( int initialCapacity ) {
    super( initialCapacity );
  }

  public MarkHashMap() {
  }

  public MarkHashMap( Map<? extends String, ? extends V> m ) {
    super( m );
  }

  @Override
  public V put( String key, V value ) {
    modifiedFields.add( key );
    return super.put( key, value );
  }

  @Override
  @ParametersAreNonnullByDefault
  public void putAll( Map<? extends String, ? extends V> m ) {
    modifiedFields.addAll( m.keySet() );
    super.putAll( m );
  }

  public boolean isModified() {
    return !modifiedFields.isEmpty();
  }

  public Set<String> getModifiedFields() {
    return Collections.unmodifiableSet( modifiedFields );
  }
}
