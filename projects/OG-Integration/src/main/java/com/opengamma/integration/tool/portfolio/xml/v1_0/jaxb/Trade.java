/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.portfolio.xml.v1_0.jaxb;

import java.math.BigDecimal;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.joda.beans.BeanDefinition;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectBean;
import org.threeten.bp.LocalDate;

import com.google.common.collect.ImmutableMap;
import com.opengamma.integration.tool.portfolio.xml.v1_0.conversion.TradeSecurityExtractor;
import com.opengamma.util.money.Currency;

import java.util.List;
import java.util.Map;
import org.joda.beans.BeanBuilder;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.impl.direct.DirectMetaBean;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

@XmlRootElement
// Ensure we look at subclasses when unmarshalling
@XmlSeeAlso({ AbstractFxOptionTrade.class, SwapTrade.class, EquityVarianceSwapTrade.class, FxForwardTrade.class,
                SwaptionTrade.class, OtcEquityIndexOptionTrade.class, ListedSecurityTrade.class, FraTrade.class })
@XmlAccessorType(XmlAccessType.FIELD)
@BeanDefinition
public abstract class Trade extends DirectBean {

  @XmlAttribute
  @XmlID
  @PropertyDefinition
  private String _id;

  @XmlElement(name = "externalSystemId", required = true)
  @PropertyDefinition
  private IdWrapper _externalSystemId;

  @XmlElement(name = "tradeDate")
  @PropertyDefinition
  private LocalDate _tradeDate;

  @XmlElement(name = "maturityDate")
  @PropertyDefinition
  private LocalDate _maturityDate;

  @XmlElement(name = "counterparty")
  @PropertyDefinition
  private IdWrapper _counterparty;

  @XmlElementWrapper(name = "additionalCashflows")
  @XmlElement(name = "additionalCashflow")
  @PropertyDefinition
  private List<AdditionalCashflow> _additionalCashflows;

  @XmlJavaTypeAdapter(AttributeMapAdapter.class)
  @XmlElement(name = "additionalAttributes")
  @PropertyDefinition(get = "manual")
  private Map<String, String> _additionalAttributes;

  public BigDecimal getQuantity() {
    return BigDecimal.ONE;
  }

  /**
   * Gets the additionalAttributes.
   * @return the value of the property
   */
  public Map<String, String> getAdditionalAttributes() {
    return _additionalAttributes == null ? ImmutableMap.<String, String>of() : _additionalAttributes;
  }

  public abstract boolean canBePositionAggregated();

  public abstract TradeSecurityExtractor getSecurityExtractor();

  /*


      <!--optional-->
      <isCleared>false</isCleared>
      <!--optional-->

      <!--optional-->
      <collateralAgreement>Only if not cleared</collateralAgreement>

   */

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code Trade}.
   * @return the meta-bean, not null
   */
  public static Trade.Meta meta() {
    return Trade.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(Trade.Meta.INSTANCE);
  }

  @Override
  public Trade.Meta metaBean() {
    return Trade.Meta.INSTANCE;
  }

  @Override
  protected Object propertyGet(String propertyName, boolean quiet) {
    switch (propertyName.hashCode()) {
      case 3355:  // id
        return getId();
      case -1924302699:  // externalSystemId
        return getExternalSystemId();
      case 752419634:  // tradeDate
        return getTradeDate();
      case -414641441:  // maturityDate
        return getMaturityDate();
      case -1651301782:  // counterparty
        return getCounterparty();
      case -254405301:  // additionalCashflows
        return getAdditionalCashflows();
      case -1075726114:  // additionalAttributes
        return getAdditionalAttributes();
    }
    return super.propertyGet(propertyName, quiet);
  }

  @SuppressWarnings("unchecked")
  @Override
  protected void propertySet(String propertyName, Object newValue, boolean quiet) {
    switch (propertyName.hashCode()) {
      case 3355:  // id
        setId((String) newValue);
        return;
      case -1924302699:  // externalSystemId
        setExternalSystemId((IdWrapper) newValue);
        return;
      case 752419634:  // tradeDate
        setTradeDate((LocalDate) newValue);
        return;
      case -414641441:  // maturityDate
        setMaturityDate((LocalDate) newValue);
        return;
      case -1651301782:  // counterparty
        setCounterparty((IdWrapper) newValue);
        return;
      case -254405301:  // additionalCashflows
        setAdditionalCashflows((List<AdditionalCashflow>) newValue);
        return;
      case -1075726114:  // additionalAttributes
        setAdditionalAttributes((Map<String, String>) newValue);
        return;
    }
    super.propertySet(propertyName, newValue, quiet);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      Trade other = (Trade) obj;
      return JodaBeanUtils.equal(getId(), other.getId()) &&
          JodaBeanUtils.equal(getExternalSystemId(), other.getExternalSystemId()) &&
          JodaBeanUtils.equal(getTradeDate(), other.getTradeDate()) &&
          JodaBeanUtils.equal(getMaturityDate(), other.getMaturityDate()) &&
          JodaBeanUtils.equal(getCounterparty(), other.getCounterparty()) &&
          JodaBeanUtils.equal(getAdditionalCashflows(), other.getAdditionalCashflows()) &&
          JodaBeanUtils.equal(getAdditionalAttributes(), other.getAdditionalAttributes());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash += hash * 31 + JodaBeanUtils.hashCode(getId());
    hash += hash * 31 + JodaBeanUtils.hashCode(getExternalSystemId());
    hash += hash * 31 + JodaBeanUtils.hashCode(getTradeDate());
    hash += hash * 31 + JodaBeanUtils.hashCode(getMaturityDate());
    hash += hash * 31 + JodaBeanUtils.hashCode(getCounterparty());
    hash += hash * 31 + JodaBeanUtils.hashCode(getAdditionalCashflows());
    hash += hash * 31 + JodaBeanUtils.hashCode(getAdditionalAttributes());
    return hash;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the id.
   * @return the value of the property
   */
  public String getId() {
    return _id;
  }

  /**
   * Sets the id.
   * @param id  the new value of the property
   */
  public void setId(String id) {
    this._id = id;
  }

  /**
   * Gets the the {@code id} property.
   * @return the property, not null
   */
  public final Property<String> id() {
    return metaBean().id().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the externalSystemId.
   * @return the value of the property
   */
  public IdWrapper getExternalSystemId() {
    return _externalSystemId;
  }

  /**
   * Sets the externalSystemId.
   * @param externalSystemId  the new value of the property
   */
  public void setExternalSystemId(IdWrapper externalSystemId) {
    this._externalSystemId = externalSystemId;
  }

  /**
   * Gets the the {@code externalSystemId} property.
   * @return the property, not null
   */
  public final Property<IdWrapper> externalSystemId() {
    return metaBean().externalSystemId().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the tradeDate.
   * @return the value of the property
   */
  public LocalDate getTradeDate() {
    return _tradeDate;
  }

  /**
   * Sets the tradeDate.
   * @param tradeDate  the new value of the property
   */
  public void setTradeDate(LocalDate tradeDate) {
    this._tradeDate = tradeDate;
  }

  /**
   * Gets the the {@code tradeDate} property.
   * @return the property, not null
   */
  public final Property<LocalDate> tradeDate() {
    return metaBean().tradeDate().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the maturityDate.
   * @return the value of the property
   */
  public LocalDate getMaturityDate() {
    return _maturityDate;
  }

  /**
   * Sets the maturityDate.
   * @param maturityDate  the new value of the property
   */
  public void setMaturityDate(LocalDate maturityDate) {
    this._maturityDate = maturityDate;
  }

  /**
   * Gets the the {@code maturityDate} property.
   * @return the property, not null
   */
  public final Property<LocalDate> maturityDate() {
    return metaBean().maturityDate().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the counterparty.
   * @return the value of the property
   */
  public IdWrapper getCounterparty() {
    return _counterparty;
  }

  /**
   * Sets the counterparty.
   * @param counterparty  the new value of the property
   */
  public void setCounterparty(IdWrapper counterparty) {
    this._counterparty = counterparty;
  }

  /**
   * Gets the the {@code counterparty} property.
   * @return the property, not null
   */
  public final Property<IdWrapper> counterparty() {
    return metaBean().counterparty().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the additionalCashflows.
   * @return the value of the property
   */
  public List<AdditionalCashflow> getAdditionalCashflows() {
    return _additionalCashflows;
  }

  /**
   * Sets the additionalCashflows.
   * @param additionalCashflows  the new value of the property
   */
  public void setAdditionalCashflows(List<AdditionalCashflow> additionalCashflows) {
    this._additionalCashflows = additionalCashflows;
  }

  /**
   * Gets the the {@code additionalCashflows} property.
   * @return the property, not null
   */
  public final Property<List<AdditionalCashflow>> additionalCashflows() {
    return metaBean().additionalCashflows().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Sets the additionalAttributes.
   * @param additionalAttributes  the new value of the property
   */
  public void setAdditionalAttributes(Map<String, String> additionalAttributes) {
    this._additionalAttributes = additionalAttributes;
  }

  /**
   * Gets the the {@code additionalAttributes} property.
   * @return the property, not null
   */
  public final Property<Map<String, String>> additionalAttributes() {
    return metaBean().additionalAttributes().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code Trade}.
   */
  public static class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code id} property.
     */
    private final MetaProperty<String> _id = DirectMetaProperty.ofReadWrite(
        this, "id", Trade.class, String.class);
    /**
     * The meta-property for the {@code externalSystemId} property.
     */
    private final MetaProperty<IdWrapper> _externalSystemId = DirectMetaProperty.ofReadWrite(
        this, "externalSystemId", Trade.class, IdWrapper.class);
    /**
     * The meta-property for the {@code tradeDate} property.
     */
    private final MetaProperty<LocalDate> _tradeDate = DirectMetaProperty.ofReadWrite(
        this, "tradeDate", Trade.class, LocalDate.class);
    /**
     * The meta-property for the {@code maturityDate} property.
     */
    private final MetaProperty<LocalDate> _maturityDate = DirectMetaProperty.ofReadWrite(
        this, "maturityDate", Trade.class, LocalDate.class);
    /**
     * The meta-property for the {@code counterparty} property.
     */
    private final MetaProperty<IdWrapper> _counterparty = DirectMetaProperty.ofReadWrite(
        this, "counterparty", Trade.class, IdWrapper.class);
    /**
     * The meta-property for the {@code additionalCashflows} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<List<AdditionalCashflow>> _additionalCashflows = DirectMetaProperty.ofReadWrite(
        this, "additionalCashflows", Trade.class, (Class) List.class);
    /**
     * The meta-property for the {@code additionalAttributes} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<Map<String, String>> _additionalAttributes = DirectMetaProperty.ofReadWrite(
        this, "additionalAttributes", Trade.class, (Class) Map.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "id",
        "externalSystemId",
        "tradeDate",
        "maturityDate",
        "counterparty",
        "additionalCashflows",
        "additionalAttributes");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case 3355:  // id
          return _id;
        case -1924302699:  // externalSystemId
          return _externalSystemId;
        case 752419634:  // tradeDate
          return _tradeDate;
        case -414641441:  // maturityDate
          return _maturityDate;
        case -1651301782:  // counterparty
          return _counterparty;
        case -254405301:  // additionalCashflows
          return _additionalCashflows;
        case -1075726114:  // additionalAttributes
          return _additionalAttributes;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends Trade> builder() {
      throw new UnsupportedOperationException("Trade is an abstract class");
    }

    @Override
    public Class<? extends Trade> beanType() {
      return Trade.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code id} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> id() {
      return _id;
    }

    /**
     * The meta-property for the {@code externalSystemId} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<IdWrapper> externalSystemId() {
      return _externalSystemId;
    }

    /**
     * The meta-property for the {@code tradeDate} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<LocalDate> tradeDate() {
      return _tradeDate;
    }

    /**
     * The meta-property for the {@code maturityDate} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<LocalDate> maturityDate() {
      return _maturityDate;
    }

    /**
     * The meta-property for the {@code counterparty} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<IdWrapper> counterparty() {
      return _counterparty;
    }

    /**
     * The meta-property for the {@code additionalCashflows} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<List<AdditionalCashflow>> additionalCashflows() {
      return _additionalCashflows;
    }

    /**
     * The meta-property for the {@code additionalAttributes} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Map<String, String>> additionalAttributes() {
      return _additionalAttributes;
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
