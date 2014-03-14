/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention;

import java.util.Map;

import org.joda.beans.BeanBuilder;
import org.joda.beans.BeanDefinition;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.time.Tenor;

/**
 * Convention for a vanilla ibor swap leg.
 */
@BeanDefinition
public class VanillaIborLegConvention extends Convention {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /**
   * The ibor index.
   */
  @PropertyDefinition(validate = "notNull")
  private ExternalId _iborIndexConvention;

  /**
   * Is the fixing in advance (true) or in arrears (false).
   */
  @PropertyDefinition
  private boolean _isAdvanceFixing;

  /**
   * The interpolation method to use for broken periods.
   */
  @PropertyDefinition(validate = "notNull")
  private String _interpolationMethod;

  /**
   * The reset tenor of the underlying index
   */
  @PropertyDefinition(validate = "notNull")
  private Tenor _resetTenor;

  /**
   * The number of settlement days.
   */
  @PropertyDefinition
  private int _settlementDays;

  /**
   * Is the schedule end-of-month.
   */
  @PropertyDefinition
  private boolean _isEOM;

  /**
   * The stub type.
   */
  @PropertyDefinition(validate = "notNull")
  private StubType _stubType;

  /**
   * Is the notional exchanged.
   */
  @PropertyDefinition
  private boolean _isExchangeNotional;

  /**
   * The payment lag in days.
   */
  @PropertyDefinition
  private int _paymentLag;

  /**
   * For the builder
   */
  /* package */ VanillaIborLegConvention() {
    super();
  }

  /**
   * @param name The convention name, not null
   * @param externalIdBundle The external identifiers for this convention, not null
   * @param iborIndexConvention The underlying ibor index convention, not null
   * @param isAdvanceFixing True if fixing is in advance
   * @param interpolationMethod The interpolation method for broken periods, not null
   * @param resetTenor The reset tenor, not null
   * @param settlementDays The number of settlement days
   * @param isEOM True if dates follow the end-of-month rule
   * @param stubType The stub type, not null
   * @param isExchangeNotional True if notional is to be exchanged
   * @param paymentLag The payment lag in days
   */
  public VanillaIborLegConvention(final String name, final ExternalIdBundle externalIdBundle, final ExternalId iborIndexConvention, final boolean isAdvanceFixing,
      final String interpolationMethod, final Tenor resetTenor, final int settlementDays, final boolean isEOM, final StubType stubType, final boolean isExchangeNotional,
      final int paymentLag) {
    super(name, externalIdBundle);
    setIborIndexConvention(iborIndexConvention);
    setIsAdvanceFixing(isAdvanceFixing);
    setInterpolationMethod(interpolationMethod);
    setResetTenor(resetTenor);
    setSettlementDays(settlementDays);
    setIsEOM(isEOM);
    setStubType(stubType);
    setIsExchangeNotional(isExchangeNotional);
    setPaymentLag(paymentLag);
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code VanillaIborLegConvention}.
   * @return the meta-bean, not null
   */
  public static VanillaIborLegConvention.Meta meta() {
    return VanillaIborLegConvention.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(VanillaIborLegConvention.Meta.INSTANCE);
  }

  @Override
  public VanillaIborLegConvention.Meta metaBean() {
    return VanillaIborLegConvention.Meta.INSTANCE;
  }

  @Override
  protected Object propertyGet(String propertyName, boolean quiet) {
    switch (propertyName.hashCode()) {
      case -1542426233:  // iborIndexConvention
        return getIborIndexConvention();
      case 1363941829:  // isAdvanceFixing
        return isIsAdvanceFixing();
      case 374385573:  // interpolationMethod
        return getInterpolationMethod();
      case -1687017807:  // resetTenor
        return getResetTenor();
      case -295948000:  // settlementDays
        return getSettlementDays();
      case 100464505:  // isEOM
        return isIsEOM();
      case 1873675528:  // stubType
        return getStubType();
      case 348962765:  // isExchangeNotional
        return isIsExchangeNotional();
      case 1612870060:  // paymentLag
        return getPaymentLag();
    }
    return super.propertyGet(propertyName, quiet);
  }

  @Override
  protected void propertySet(String propertyName, Object newValue, boolean quiet) {
    switch (propertyName.hashCode()) {
      case -1542426233:  // iborIndexConvention
        setIborIndexConvention((ExternalId) newValue);
        return;
      case 1363941829:  // isAdvanceFixing
        setIsAdvanceFixing((Boolean) newValue);
        return;
      case 374385573:  // interpolationMethod
        setInterpolationMethod((String) newValue);
        return;
      case -1687017807:  // resetTenor
        setResetTenor((Tenor) newValue);
        return;
      case -295948000:  // settlementDays
        setSettlementDays((Integer) newValue);
        return;
      case 100464505:  // isEOM
        setIsEOM((Boolean) newValue);
        return;
      case 1873675528:  // stubType
        setStubType((StubType) newValue);
        return;
      case 348962765:  // isExchangeNotional
        setIsExchangeNotional((Boolean) newValue);
        return;
      case 1612870060:  // paymentLag
        setPaymentLag((Integer) newValue);
        return;
    }
    super.propertySet(propertyName, newValue, quiet);
  }

  @Override
  protected void validate() {
    JodaBeanUtils.notNull(_iborIndexConvention, "iborIndexConvention");
    JodaBeanUtils.notNull(_interpolationMethod, "interpolationMethod");
    JodaBeanUtils.notNull(_resetTenor, "resetTenor");
    JodaBeanUtils.notNull(_stubType, "stubType");
    super.validate();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      VanillaIborLegConvention other = (VanillaIborLegConvention) obj;
      return JodaBeanUtils.equal(getIborIndexConvention(), other.getIborIndexConvention()) &&
          JodaBeanUtils.equal(isIsAdvanceFixing(), other.isIsAdvanceFixing()) &&
          JodaBeanUtils.equal(getInterpolationMethod(), other.getInterpolationMethod()) &&
          JodaBeanUtils.equal(getResetTenor(), other.getResetTenor()) &&
          JodaBeanUtils.equal(getSettlementDays(), other.getSettlementDays()) &&
          JodaBeanUtils.equal(isIsEOM(), other.isIsEOM()) &&
          JodaBeanUtils.equal(getStubType(), other.getStubType()) &&
          JodaBeanUtils.equal(isIsExchangeNotional(), other.isIsExchangeNotional()) &&
          JodaBeanUtils.equal(getPaymentLag(), other.getPaymentLag()) &&
          super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash += hash * 31 + JodaBeanUtils.hashCode(getIborIndexConvention());
    hash += hash * 31 + JodaBeanUtils.hashCode(isIsAdvanceFixing());
    hash += hash * 31 + JodaBeanUtils.hashCode(getInterpolationMethod());
    hash += hash * 31 + JodaBeanUtils.hashCode(getResetTenor());
    hash += hash * 31 + JodaBeanUtils.hashCode(getSettlementDays());
    hash += hash * 31 + JodaBeanUtils.hashCode(isIsEOM());
    hash += hash * 31 + JodaBeanUtils.hashCode(getStubType());
    hash += hash * 31 + JodaBeanUtils.hashCode(isIsExchangeNotional());
    hash += hash * 31 + JodaBeanUtils.hashCode(getPaymentLag());
    return hash ^ super.hashCode();
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the ibor index.
   * @return the value of the property, not null
   */
  public ExternalId getIborIndexConvention() {
    return _iborIndexConvention;
  }

  /**
   * Sets the ibor index.
   * @param iborIndexConvention  the new value of the property, not null
   */
  public void setIborIndexConvention(ExternalId iborIndexConvention) {
    JodaBeanUtils.notNull(iborIndexConvention, "iborIndexConvention");
    this._iborIndexConvention = iborIndexConvention;
  }

  /**
   * Gets the the {@code iborIndexConvention} property.
   * @return the property, not null
   */
  public final Property<ExternalId> iborIndexConvention() {
    return metaBean().iborIndexConvention().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets is the fixing in advance (true) or in arrears (false).
   * @return the value of the property
   */
  public boolean isIsAdvanceFixing() {
    return _isAdvanceFixing;
  }

  /**
   * Sets is the fixing in advance (true) or in arrears (false).
   * @param isAdvanceFixing  the new value of the property
   */
  public void setIsAdvanceFixing(boolean isAdvanceFixing) {
    this._isAdvanceFixing = isAdvanceFixing;
  }

  /**
   * Gets the the {@code isAdvanceFixing} property.
   * @return the property, not null
   */
  public final Property<Boolean> isAdvanceFixing() {
    return metaBean().isAdvanceFixing().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the interpolation method to use for broken periods.
   * @return the value of the property, not null
   */
  public String getInterpolationMethod() {
    return _interpolationMethod;
  }

  /**
   * Sets the interpolation method to use for broken periods.
   * @param interpolationMethod  the new value of the property, not null
   */
  public void setInterpolationMethod(String interpolationMethod) {
    JodaBeanUtils.notNull(interpolationMethod, "interpolationMethod");
    this._interpolationMethod = interpolationMethod;
  }

  /**
   * Gets the the {@code interpolationMethod} property.
   * @return the property, not null
   */
  public final Property<String> interpolationMethod() {
    return metaBean().interpolationMethod().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the reset tenor of the underlying index
   * @return the value of the property, not null
   */
  public Tenor getResetTenor() {
    return _resetTenor;
  }

  /**
   * Sets the reset tenor of the underlying index
   * @param resetTenor  the new value of the property, not null
   */
  public void setResetTenor(Tenor resetTenor) {
    JodaBeanUtils.notNull(resetTenor, "resetTenor");
    this._resetTenor = resetTenor;
  }

  /**
   * Gets the the {@code resetTenor} property.
   * @return the property, not null
   */
  public final Property<Tenor> resetTenor() {
    return metaBean().resetTenor().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the number of settlement days.
   * @return the value of the property
   */
  public int getSettlementDays() {
    return _settlementDays;
  }

  /**
   * Sets the number of settlement days.
   * @param settlementDays  the new value of the property
   */
  public void setSettlementDays(int settlementDays) {
    this._settlementDays = settlementDays;
  }

  /**
   * Gets the the {@code settlementDays} property.
   * @return the property, not null
   */
  public final Property<Integer> settlementDays() {
    return metaBean().settlementDays().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets is the schedule end-of-month.
   * @return the value of the property
   */
  public boolean isIsEOM() {
    return _isEOM;
  }

  /**
   * Sets is the schedule end-of-month.
   * @param isEOM  the new value of the property
   */
  public void setIsEOM(boolean isEOM) {
    this._isEOM = isEOM;
  }

  /**
   * Gets the the {@code isEOM} property.
   * @return the property, not null
   */
  public final Property<Boolean> isEOM() {
    return metaBean().isEOM().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the stub type.
   * @return the value of the property, not null
   */
  public StubType getStubType() {
    return _stubType;
  }

  /**
   * Sets the stub type.
   * @param stubType  the new value of the property, not null
   */
  public void setStubType(StubType stubType) {
    JodaBeanUtils.notNull(stubType, "stubType");
    this._stubType = stubType;
  }

  /**
   * Gets the the {@code stubType} property.
   * @return the property, not null
   */
  public final Property<StubType> stubType() {
    return metaBean().stubType().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets is the notional exchanged.
   * @return the value of the property
   */
  public boolean isIsExchangeNotional() {
    return _isExchangeNotional;
  }

  /**
   * Sets is the notional exchanged.
   * @param isExchangeNotional  the new value of the property
   */
  public void setIsExchangeNotional(boolean isExchangeNotional) {
    this._isExchangeNotional = isExchangeNotional;
  }

  /**
   * Gets the the {@code isExchangeNotional} property.
   * @return the property, not null
   */
  public final Property<Boolean> isExchangeNotional() {
    return metaBean().isExchangeNotional().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the payment lag in days.
   * @return the value of the property
   */
  public int getPaymentLag() {
    return _paymentLag;
  }

  /**
   * Sets the payment lag in days.
   * @param paymentLag  the new value of the property
   */
  public void setPaymentLag(int paymentLag) {
    this._paymentLag = paymentLag;
  }

  /**
   * Gets the the {@code paymentLag} property.
   * @return the property, not null
   */
  public final Property<Integer> paymentLag() {
    return metaBean().paymentLag().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code VanillaIborLegConvention}.
   */
  public static class Meta extends Convention.Meta {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code iborIndexConvention} property.
     */
    private final MetaProperty<ExternalId> _iborIndexConvention = DirectMetaProperty.ofReadWrite(
        this, "iborIndexConvention", VanillaIborLegConvention.class, ExternalId.class);
    /**
     * The meta-property for the {@code isAdvanceFixing} property.
     */
    private final MetaProperty<Boolean> _isAdvanceFixing = DirectMetaProperty.ofReadWrite(
        this, "isAdvanceFixing", VanillaIborLegConvention.class, Boolean.TYPE);
    /**
     * The meta-property for the {@code interpolationMethod} property.
     */
    private final MetaProperty<String> _interpolationMethod = DirectMetaProperty.ofReadWrite(
        this, "interpolationMethod", VanillaIborLegConvention.class, String.class);
    /**
     * The meta-property for the {@code resetTenor} property.
     */
    private final MetaProperty<Tenor> _resetTenor = DirectMetaProperty.ofReadWrite(
        this, "resetTenor", VanillaIborLegConvention.class, Tenor.class);
    /**
     * The meta-property for the {@code settlementDays} property.
     */
    private final MetaProperty<Integer> _settlementDays = DirectMetaProperty.ofReadWrite(
        this, "settlementDays", VanillaIborLegConvention.class, Integer.TYPE);
    /**
     * The meta-property for the {@code isEOM} property.
     */
    private final MetaProperty<Boolean> _isEOM = DirectMetaProperty.ofReadWrite(
        this, "isEOM", VanillaIborLegConvention.class, Boolean.TYPE);
    /**
     * The meta-property for the {@code stubType} property.
     */
    private final MetaProperty<StubType> _stubType = DirectMetaProperty.ofReadWrite(
        this, "stubType", VanillaIborLegConvention.class, StubType.class);
    /**
     * The meta-property for the {@code isExchangeNotional} property.
     */
    private final MetaProperty<Boolean> _isExchangeNotional = DirectMetaProperty.ofReadWrite(
        this, "isExchangeNotional", VanillaIborLegConvention.class, Boolean.TYPE);
    /**
     * The meta-property for the {@code paymentLag} property.
     */
    private final MetaProperty<Integer> _paymentLag = DirectMetaProperty.ofReadWrite(
        this, "paymentLag", VanillaIborLegConvention.class, Integer.TYPE);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "iborIndexConvention",
        "isAdvanceFixing",
        "interpolationMethod",
        "resetTenor",
        "settlementDays",
        "isEOM",
        "stubType",
        "isExchangeNotional",
        "paymentLag");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case -1542426233:  // iborIndexConvention
          return _iborIndexConvention;
        case 1363941829:  // isAdvanceFixing
          return _isAdvanceFixing;
        case 374385573:  // interpolationMethod
          return _interpolationMethod;
        case -1687017807:  // resetTenor
          return _resetTenor;
        case -295948000:  // settlementDays
          return _settlementDays;
        case 100464505:  // isEOM
          return _isEOM;
        case 1873675528:  // stubType
          return _stubType;
        case 348962765:  // isExchangeNotional
          return _isExchangeNotional;
        case 1612870060:  // paymentLag
          return _paymentLag;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends VanillaIborLegConvention> builder() {
      return new DirectBeanBuilder<VanillaIborLegConvention>(new VanillaIborLegConvention());
    }

    @Override
    public Class<? extends VanillaIborLegConvention> beanType() {
      return VanillaIborLegConvention.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code iborIndexConvention} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ExternalId> iborIndexConvention() {
      return _iborIndexConvention;
    }

    /**
     * The meta-property for the {@code isAdvanceFixing} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Boolean> isAdvanceFixing() {
      return _isAdvanceFixing;
    }

    /**
     * The meta-property for the {@code interpolationMethod} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> interpolationMethod() {
      return _interpolationMethod;
    }

    /**
     * The meta-property for the {@code resetTenor} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Tenor> resetTenor() {
      return _resetTenor;
    }

    /**
     * The meta-property for the {@code settlementDays} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Integer> settlementDays() {
      return _settlementDays;
    }

    /**
     * The meta-property for the {@code isEOM} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Boolean> isEOM() {
      return _isEOM;
    }

    /**
     * The meta-property for the {@code stubType} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<StubType> stubType() {
      return _stubType;
    }

    /**
     * The meta-property for the {@code isExchangeNotional} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Boolean> isExchangeNotional() {
      return _isExchangeNotional;
    }

    /**
     * The meta-property for the {@code paymentLag} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Integer> paymentLag() {
      return _paymentLag;
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
