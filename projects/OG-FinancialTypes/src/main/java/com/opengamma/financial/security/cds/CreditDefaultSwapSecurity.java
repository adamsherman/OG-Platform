/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.cds;

import java.util.Map;

import org.joda.beans.BeanBuilder;
import org.joda.beans.BeanDefinition;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.credit.DebtSeniority;
import com.opengamma.analytics.financial.credit.RestructuringClause;
import com.opengamma.financial.convention.StubType;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.security.swap.InterestRateNotional;
import com.opengamma.id.ExternalId;

/**
 * An abstract base class for credit securities.
 */
@BeanDefinition
public abstract class CreditDefaultSwapSecurity extends AbstractCreditDefaultSwapSecurity {

  /** Serialization version. */
  private static final long serialVersionUID = 2L;

  /**
   * The debt seniority.
   */
  @PropertyDefinition(validate = "notNull")
  private DebtSeniority _debtSeniority;

  /**
   * The restructuring clause.
   */
  @PropertyDefinition(validate = "notNull")
  private RestructuringClause _restructuringClause;

  /**
   * The region id.
   */
  @PropertyDefinition(validate = "notNull")
  private ExternalId _regionId;

  CreditDefaultSwapSecurity(String securityType) { // For Fudge builder
    super(securityType);
  }



  public CreditDefaultSwapSecurity(final boolean isBuy, final ExternalId protectionSeller, final ExternalId protectionBuyer, final ExternalId referenceEntity, //CSIGNORE
                                   final DebtSeniority debtSeniority, final RestructuringClause restructuringClause, final ExternalId regionId, final ZonedDateTime startDate,
                                   final ZonedDateTime effectiveDate, final ZonedDateTime maturityDate, final StubType stubType, final Frequency couponFrequency, final DayCount dayCount,
                                   final BusinessDayConvention businessDayConvention, final boolean immAdjustMaturityDate, final boolean adjustEffectiveDate,
                                   final boolean adjustMaturityDate, final InterestRateNotional notional, final boolean includeAccruedPremium,
                                   final boolean protectionStart, final String securityType) {
    super(securityType,
          isBuy,
          protectionBuyer,
          protectionSeller,
          referenceEntity,
          startDate,
          effectiveDate,
          maturityDate,
          stubType,
          couponFrequency,
          dayCount,
          businessDayConvention,
          immAdjustMaturityDate,
          adjustEffectiveDate,
          adjustMaturityDate,
          notional,
          includeAccruedPremium,
          protectionStart);
    setDebtSeniority(debtSeniority);
    setRestructuringClause(restructuringClause);
    setRegionId(regionId);
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code CreditDefaultSwapSecurity}.
   * @return the meta-bean, not null
   */
  public static CreditDefaultSwapSecurity.Meta meta() {
    return CreditDefaultSwapSecurity.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(CreditDefaultSwapSecurity.Meta.INSTANCE);
  }

  @Override
  public CreditDefaultSwapSecurity.Meta metaBean() {
    return CreditDefaultSwapSecurity.Meta.INSTANCE;
  }

  @Override
  protected Object propertyGet(String propertyName, boolean quiet) {
    switch (propertyName.hashCode()) {
      case 1737168171:  // debtSeniority
        return getDebtSeniority();
      case -1774904020:  // restructuringClause
        return getRestructuringClause();
      case -690339025:  // regionId
        return getRegionId();
    }
    return super.propertyGet(propertyName, quiet);
  }

  @Override
  protected void propertySet(String propertyName, Object newValue, boolean quiet) {
    switch (propertyName.hashCode()) {
      case 1737168171:  // debtSeniority
        setDebtSeniority((DebtSeniority) newValue);
        return;
      case -1774904020:  // restructuringClause
        setRestructuringClause((RestructuringClause) newValue);
        return;
      case -690339025:  // regionId
        setRegionId((ExternalId) newValue);
        return;
    }
    super.propertySet(propertyName, newValue, quiet);
  }

  @Override
  protected void validate() {
    JodaBeanUtils.notNull(_debtSeniority, "debtSeniority");
    JodaBeanUtils.notNull(_restructuringClause, "restructuringClause");
    JodaBeanUtils.notNull(_regionId, "regionId");
    super.validate();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      CreditDefaultSwapSecurity other = (CreditDefaultSwapSecurity) obj;
      return JodaBeanUtils.equal(getDebtSeniority(), other.getDebtSeniority()) &&
          JodaBeanUtils.equal(getRestructuringClause(), other.getRestructuringClause()) &&
          JodaBeanUtils.equal(getRegionId(), other.getRegionId()) &&
          super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash += hash * 31 + JodaBeanUtils.hashCode(getDebtSeniority());
    hash += hash * 31 + JodaBeanUtils.hashCode(getRestructuringClause());
    hash += hash * 31 + JodaBeanUtils.hashCode(getRegionId());
    return hash ^ super.hashCode();
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the debt seniority.
   * @return the value of the property, not null
   */
  public DebtSeniority getDebtSeniority() {
    return _debtSeniority;
  }

  /**
   * Sets the debt seniority.
   * @param debtSeniority  the new value of the property, not null
   */
  public void setDebtSeniority(DebtSeniority debtSeniority) {
    JodaBeanUtils.notNull(debtSeniority, "debtSeniority");
    this._debtSeniority = debtSeniority;
  }

  /**
   * Gets the the {@code debtSeniority} property.
   * @return the property, not null
   */
  public final Property<DebtSeniority> debtSeniority() {
    return metaBean().debtSeniority().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the restructuring clause.
   * @return the value of the property, not null
   */
  public RestructuringClause getRestructuringClause() {
    return _restructuringClause;
  }

  /**
   * Sets the restructuring clause.
   * @param restructuringClause  the new value of the property, not null
   */
  public void setRestructuringClause(RestructuringClause restructuringClause) {
    JodaBeanUtils.notNull(restructuringClause, "restructuringClause");
    this._restructuringClause = restructuringClause;
  }

  /**
   * Gets the the {@code restructuringClause} property.
   * @return the property, not null
   */
  public final Property<RestructuringClause> restructuringClause() {
    return metaBean().restructuringClause().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the region id.
   * @return the value of the property, not null
   */
  public ExternalId getRegionId() {
    return _regionId;
  }

  /**
   * Sets the region id.
   * @param regionId  the new value of the property, not null
   */
  public void setRegionId(ExternalId regionId) {
    JodaBeanUtils.notNull(regionId, "regionId");
    this._regionId = regionId;
  }

  /**
   * Gets the the {@code regionId} property.
   * @return the property, not null
   */
  public final Property<ExternalId> regionId() {
    return metaBean().regionId().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code CreditDefaultSwapSecurity}.
   */
  public static class Meta extends AbstractCreditDefaultSwapSecurity.Meta {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code debtSeniority} property.
     */
    private final MetaProperty<DebtSeniority> _debtSeniority = DirectMetaProperty.ofReadWrite(
        this, "debtSeniority", CreditDefaultSwapSecurity.class, DebtSeniority.class);
    /**
     * The meta-property for the {@code restructuringClause} property.
     */
    private final MetaProperty<RestructuringClause> _restructuringClause = DirectMetaProperty.ofReadWrite(
        this, "restructuringClause", CreditDefaultSwapSecurity.class, RestructuringClause.class);
    /**
     * The meta-property for the {@code regionId} property.
     */
    private final MetaProperty<ExternalId> _regionId = DirectMetaProperty.ofReadWrite(
        this, "regionId", CreditDefaultSwapSecurity.class, ExternalId.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "debtSeniority",
        "restructuringClause",
        "regionId");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case 1737168171:  // debtSeniority
          return _debtSeniority;
        case -1774904020:  // restructuringClause
          return _restructuringClause;
        case -690339025:  // regionId
          return _regionId;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends CreditDefaultSwapSecurity> builder() {
      throw new UnsupportedOperationException("CreditDefaultSwapSecurity is an abstract class");
    }

    @Override
    public Class<? extends CreditDefaultSwapSecurity> beanType() {
      return CreditDefaultSwapSecurity.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code debtSeniority} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<DebtSeniority> debtSeniority() {
      return _debtSeniority;
    }

    /**
     * The meta-property for the {@code restructuringClause} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<RestructuringClause> restructuringClause() {
      return _restructuringClause;
    }

    /**
     * The meta-property for the {@code regionId} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ExternalId> regionId() {
      return _regionId;
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}



