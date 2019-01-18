package io.github.itfinally.bean;

import io.github.itfinally.exception.IntrospectionRuntimeException;
import io.github.itfinally.exception.NoSuchFieldRuntimeException;
import org.springframework.beans.BeanInfoFactory;
import org.springframework.beans.ExtendedBeanInfoFactory;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class BeanInfoExplorer {
  private static final BeanInfoFactory beanInfoFactory = new ExtendedBeanInfoFactory();
  private static final ConcurrentMap<Class<?>, Map<String, BeanInfo>> beanInfoCache = new ConcurrentHashMap<>();

  private BeanInfoExplorer() {
  }

  public static Map<String, BeanInfo> getPropertiesInfo( Class<?> beanClass )
      throws NoSuchFieldRuntimeException, IntrospectionRuntimeException {

    if ( beanInfoCache.containsKey( Objects.<Class<?>>requireNonNull( beanClass, "Bean class require not null" ) ) ) {
      return beanInfoCache.get( beanClass );
    }

    try {
      java.beans.BeanInfo beanInfo = beanInfoFactory.getBeanInfo( beanClass );
      PropertyDescriptor[] descriptors = beanInfo.getPropertyDescriptors();

      Map<String, BeanInfo> propertiesInfo = new HashMap<>( descriptors.length );

      for ( PropertyDescriptor descriptor : descriptors ) {
        if ( "class".equals( descriptor.getName() ) ) {
          continue;
        }

        Field property = findPropertyRealNameInRecursive( descriptor.getReadMethod().getDeclaringClass(),
            descriptor.getReadMethod(), descriptor.getName() );

        if ( null == property ) {
          throw new NoSuchFieldRuntimeException( String.format( "Property '%s' of class '%s' is not found.",
              descriptor.getName(), descriptor.getReadMethod().getDeclaringClass().getName() ) );
        }

        propertiesInfo.put( property.getName(), new BeanInfo( property, descriptor.getReadMethod(), descriptor.getWriteMethod() ) );
      }

      beanInfoCache.put( beanClass, propertiesInfo );
      return propertiesInfo;

    } catch ( IntrospectionException e ) {
      throw new IntrospectionRuntimeException( e );
    }
  }

  public static final class BeanInfo {
    private final Field property;
    private final Method readMethod;
    private final Method writeMethod;

    private BeanInfo( Field property, Method readMethod, Method writeMethod ) {
      this.property = property;
      this.readMethod = readMethod;
      this.writeMethod = writeMethod;
    }

    public Field getProperty() {
      return property;
    }

    public Method getReadMethod() {
      return readMethod;
    }

    public Method getWriteMethod() {
      return writeMethod;
    }
  }

  // According java bean specification
  // If primitive boolean type -> start with 'is'.
  // If type name first character start with upper mode -> change first character to lower mode
  private static Field findPropertyRealNameInRecursive( Class<?> clazz, Method readMethod, String name ) {
    if ( Object.class == clazz ) {
      return null;
    }

    try {
      return clazz.getDeclaredField( name );

    } catch ( NoSuchFieldException ignore ) {
    }

    String pettyName = name.replaceFirst( "^\\w", name.substring( 0, 1 ).toUpperCase() );

    try {
      if ( boolean.class == readMethod.getReturnType() ) {
        return clazz.getDeclaredField( String.format( "is%s", pettyName ) );
      }

    } catch ( NoSuchFieldException ignore ) {
    }

    try {
      return clazz.getDeclaredField( pettyName );

    } catch ( NoSuchFieldException ignore ) {
    }

    return findPropertyRealNameInRecursive( clazz.getSuperclass(), readMethod, name );
  }
}
