/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.component.factory.source;

import java.util.LinkedHashMap;
import java.util.Map;

import org.joda.beans.BeanBuilder;
import org.joda.beans.BeanDefinition;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.impl.direct.DirectBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

import com.opengamma.component.ComponentInfo;
import com.opengamma.component.ComponentRepository;
import com.opengamma.component.factory.ComponentInfoAttributes;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.config.impl.DataConfigSourceResource;
import com.opengamma.core.config.impl.NonVersionedRedisConfigSource;
import com.opengamma.core.config.impl.RemoteConfigSource;

/**
 * 
 */
@BeanDefinition
public class NonVersionedRedisConfigSourceComponentFactory extends AbstractNonVersionedRedisSourceComponentFactory {

  @Override
  public void init(ComponentRepository repo, LinkedHashMap<String, String> configuration) throws Exception {
    NonVersionedRedisConfigSource source = new NonVersionedRedisConfigSource(getRedisConnector().getJedisPool(), getRedisPrefix());
    
    ComponentInfo info = new ComponentInfo(ConfigSource.class, getClassifier());
    info.addAttribute(ComponentInfoAttributes.LEVEL, 1);
    info.addAttribute(ComponentInfoAttributes.REMOTE_CLIENT_JAVA, RemoteConfigSource.class);
    repo.registerComponent(info, source);
    if (isPublishRest()) {
      repo.getRestComponents().publish(info, new DataConfigSourceResource(source));
    }
  }
  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code NonVersionedRedisConfigSourceComponentFactory}.
   * @return the meta-bean, not null
   */
  public static NonVersionedRedisConfigSourceComponentFactory.Meta meta() {
    return NonVersionedRedisConfigSourceComponentFactory.Meta.INSTANCE;
  }
  static {
    JodaBeanUtils.registerMetaBean(NonVersionedRedisConfigSourceComponentFactory.Meta.INSTANCE);
  }

  @Override
  public NonVersionedRedisConfigSourceComponentFactory.Meta metaBean() {
    return NonVersionedRedisConfigSourceComponentFactory.Meta.INSTANCE;
  }

  @Override
  protected Object propertyGet(String propertyName, boolean quiet) {
    return super.propertyGet(propertyName, quiet);
  }

  @Override
  protected void propertySet(String propertyName, Object newValue, boolean quiet) {
    super.propertySet(propertyName, newValue, quiet);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      return super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    return hash ^ super.hashCode();
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code NonVersionedRedisConfigSourceComponentFactory}.
   */
  public static class Meta extends AbstractNonVersionedRedisSourceComponentFactory.Meta {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, (DirectMetaPropertyMap) super.metaPropertyMap());

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    public BeanBuilder<? extends NonVersionedRedisConfigSourceComponentFactory> builder() {
      return new DirectBeanBuilder<NonVersionedRedisConfigSourceComponentFactory>(new NonVersionedRedisConfigSourceComponentFactory());
    }

    @Override
    public Class<? extends NonVersionedRedisConfigSourceComponentFactory> beanType() {
      return NonVersionedRedisConfigSourceComponentFactory.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
