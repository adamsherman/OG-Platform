/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.livedata.cogda.msg;

import java.util.Map;

import org.joda.beans.BeanBuilder;
import org.joda.beans.BeanDefinition;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectBean;
import org.joda.beans.impl.direct.DirectMetaBean;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

/**
 * The common base class for any response from a server based on
 * a {@link CogdaCommandMessage} request. 
 */
@BeanDefinition
public abstract class CogdaCommandResponseMessage extends DirectBean {
  /**
   * A client-generated identifier to correlate request/response pairs.
   */
  @PropertyDefinition(validate = "notNull")
  private long _correlationId;
  /**
   * The generic form of the response.
   */
  @PropertyDefinition(validate = "notNull")
  private CogdaCommandResponseResult _genericResult;
  /**
   * Where the {@link _genericResult} requires additional detail, a
   * user-capable string to be presented to the user.
   */
  @PropertyDefinition
  private String _userMessage;

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code CogdaCommandResponseMessage}.
   * @return the meta-bean, not null
   */
  public static CogdaCommandResponseMessage.Meta meta() {
    return CogdaCommandResponseMessage.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(CogdaCommandResponseMessage.Meta.INSTANCE);
  }

  @Override
  public CogdaCommandResponseMessage.Meta metaBean() {
    return CogdaCommandResponseMessage.Meta.INSTANCE;
  }

  @Override
  protected Object propertyGet(String propertyName, boolean quiet) {
    switch (propertyName.hashCode()) {
      case -764983747:  // correlationId
        return getCorrelationId();
      case 60604628:  // genericResult
        return getGenericResult();
      case 653058492:  // userMessage
        return getUserMessage();
    }
    return super.propertyGet(propertyName, quiet);
  }

  @Override
  protected void propertySet(String propertyName, Object newValue, boolean quiet) {
    switch (propertyName.hashCode()) {
      case -764983747:  // correlationId
        setCorrelationId((Long) newValue);
        return;
      case 60604628:  // genericResult
        setGenericResult((CogdaCommandResponseResult) newValue);
        return;
      case 653058492:  // userMessage
        setUserMessage((String) newValue);
        return;
    }
    super.propertySet(propertyName, newValue, quiet);
  }

  @Override
  protected void validate() {
    JodaBeanUtils.notNull(_correlationId, "correlationId");
    JodaBeanUtils.notNull(_genericResult, "genericResult");
    super.validate();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      CogdaCommandResponseMessage other = (CogdaCommandResponseMessage) obj;
      return JodaBeanUtils.equal(getCorrelationId(), other.getCorrelationId()) &&
          JodaBeanUtils.equal(getGenericResult(), other.getGenericResult()) &&
          JodaBeanUtils.equal(getUserMessage(), other.getUserMessage());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash += hash * 31 + JodaBeanUtils.hashCode(getCorrelationId());
    hash += hash * 31 + JodaBeanUtils.hashCode(getGenericResult());
    hash += hash * 31 + JodaBeanUtils.hashCode(getUserMessage());
    return hash;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets a client-generated identifier to correlate request/response pairs.
   * @return the value of the property, not null
   */
  public long getCorrelationId() {
    return _correlationId;
  }

  /**
   * Sets a client-generated identifier to correlate request/response pairs.
   * @param correlationId  the new value of the property, not null
   */
  public void setCorrelationId(long correlationId) {
    JodaBeanUtils.notNull(correlationId, "correlationId");
    this._correlationId = correlationId;
  }

  /**
   * Gets the the {@code correlationId} property.
   * @return the property, not null
   */
  public final Property<Long> correlationId() {
    return metaBean().correlationId().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the generic form of the response.
   * @return the value of the property, not null
   */
  public CogdaCommandResponseResult getGenericResult() {
    return _genericResult;
  }

  /**
   * Sets the generic form of the response.
   * @param genericResult  the new value of the property, not null
   */
  public void setGenericResult(CogdaCommandResponseResult genericResult) {
    JodaBeanUtils.notNull(genericResult, "genericResult");
    this._genericResult = genericResult;
  }

  /**
   * Gets the the {@code genericResult} property.
   * @return the property, not null
   */
  public final Property<CogdaCommandResponseResult> genericResult() {
    return metaBean().genericResult().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets where the {@link _genericResult} requires additional detail, a
   * user-capable string to be presented to the user.
   * @return the value of the property
   */
  public String getUserMessage() {
    return _userMessage;
  }

  /**
   * Sets where the {@link _genericResult} requires additional detail, a
   * user-capable string to be presented to the user.
   * @param userMessage  the new value of the property
   */
  public void setUserMessage(String userMessage) {
    this._userMessage = userMessage;
  }

  /**
   * Gets the the {@code userMessage} property.
   * user-capable string to be presented to the user.
   * @return the property, not null
   */
  public final Property<String> userMessage() {
    return metaBean().userMessage().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code CogdaCommandResponseMessage}.
   */
  public static class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code correlationId} property.
     */
    private final MetaProperty<Long> _correlationId = DirectMetaProperty.ofReadWrite(
        this, "correlationId", CogdaCommandResponseMessage.class, Long.TYPE);
    /**
     * The meta-property for the {@code genericResult} property.
     */
    private final MetaProperty<CogdaCommandResponseResult> _genericResult = DirectMetaProperty.ofReadWrite(
        this, "genericResult", CogdaCommandResponseMessage.class, CogdaCommandResponseResult.class);
    /**
     * The meta-property for the {@code userMessage} property.
     */
    private final MetaProperty<String> _userMessage = DirectMetaProperty.ofReadWrite(
        this, "userMessage", CogdaCommandResponseMessage.class, String.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "correlationId",
        "genericResult",
        "userMessage");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case -764983747:  // correlationId
          return _correlationId;
        case 60604628:  // genericResult
          return _genericResult;
        case 653058492:  // userMessage
          return _userMessage;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends CogdaCommandResponseMessage> builder() {
      throw new UnsupportedOperationException("CogdaCommandResponseMessage is an abstract class");
    }

    @Override
    public Class<? extends CogdaCommandResponseMessage> beanType() {
      return CogdaCommandResponseMessage.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code correlationId} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Long> correlationId() {
      return _correlationId;
    }

    /**
     * The meta-property for the {@code genericResult} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<CogdaCommandResponseResult> genericResult() {
      return _genericResult;
    }

    /**
     * The meta-property for the {@code userMessage} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> userMessage() {
      return _userMessage;
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
