/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.cds;

import java.util.List;
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

import com.google.common.collect.Lists;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityVisitor;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;

/**
 * A security for Credit Indices.
 */
@BeanDefinition
public class CreditIndexSecurity extends FinancialSecurity {
  
  /** Serialization version. */
  private static final long serialVersionUID = 1L;
  /**
   * The security type.
   */
  public static final String SECURITY_TYPE = "CREDIT_INDEX";
  /**
   * The version number.
   */
  @PropertyDefinition(validate = "notNull")
  private String _version;
  /**
   * The series number.
   */
  @PropertyDefinition(validate = "notNull")
  private String _series;
  /**
   * The family
   */
  @PropertyDefinition(validate = "notNull")
  private String _family;
  /**
   * The currency.
   */
  @PropertyDefinition(validate = "notNull")
  private Currency _currency;
  /**
   * The terms.
   */
  @PropertyDefinition(validate = "notNull")
  private final List<Tenor> _terms = Lists.newArrayList();
  /**
   * The index components.
   */
  @PropertyDefinition(validate = "notNull")
  private final List<CreditIndexComponent> _components = Lists.newArrayList();
  
  
  /**
   * Creates an instance
   */
  CreditIndexSecurity() { //For builder
    super(SECURITY_TYPE);
  }
  
  @Override
  public <T> T accept(FinancialSecurityVisitor<T> visitor) {
    return visitor.visitCreditIndexSecurity(this);
  }
  
  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code CreditIndexSecurity}.
   * @return the meta-bean, not null
   */
  public static CreditIndexSecurity.Meta meta() {
    return CreditIndexSecurity.Meta.INSTANCE;
  }
  static {
    JodaBeanUtils.registerMetaBean(CreditIndexSecurity.Meta.INSTANCE);
  }

  @Override
  public CreditIndexSecurity.Meta metaBean() {
    return CreditIndexSecurity.Meta.INSTANCE;
  }

  @Override
  protected Object propertyGet(String propertyName, boolean quiet) {
    switch (propertyName.hashCode()) {
      case 351608024:  // version
        return getVersion();
      case -905838985:  // series
        return getSeries();
      case -1281860764:  // family
        return getFamily();
      case 575402001:  // currency
        return getCurrency();
      case 110250375:  // terms
        return getTerms();
      case -447446250:  // components
        return getComponents();
    }
    return super.propertyGet(propertyName, quiet);
  }

  @SuppressWarnings("unchecked")
  @Override
  protected void propertySet(String propertyName, Object newValue, boolean quiet) {
    switch (propertyName.hashCode()) {
      case 351608024:  // version
        setVersion((String) newValue);
        return;
      case -905838985:  // series
        setSeries((String) newValue);
        return;
      case -1281860764:  // family
        setFamily((String) newValue);
        return;
      case 575402001:  // currency
        setCurrency((Currency) newValue);
        return;
      case 110250375:  // terms
        setTerms((List<Tenor>) newValue);
        return;
      case -447446250:  // components
        setComponents((List<CreditIndexComponent>) newValue);
        return;
    }
    super.propertySet(propertyName, newValue, quiet);
  }

  @Override
  protected void validate() {
    JodaBeanUtils.notNull(_version, "version");
    JodaBeanUtils.notNull(_series, "series");
    JodaBeanUtils.notNull(_family, "family");
    JodaBeanUtils.notNull(_currency, "currency");
    JodaBeanUtils.notNull(_terms, "terms");
    JodaBeanUtils.notNull(_components, "components");
    super.validate();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      CreditIndexSecurity other = (CreditIndexSecurity) obj;
      return JodaBeanUtils.equal(getVersion(), other.getVersion()) &&
          JodaBeanUtils.equal(getSeries(), other.getSeries()) &&
          JodaBeanUtils.equal(getFamily(), other.getFamily()) &&
          JodaBeanUtils.equal(getCurrency(), other.getCurrency()) &&
          JodaBeanUtils.equal(getTerms(), other.getTerms()) &&
          JodaBeanUtils.equal(getComponents(), other.getComponents()) &&
          super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash += hash * 31 + JodaBeanUtils.hashCode(getVersion());
    hash += hash * 31 + JodaBeanUtils.hashCode(getSeries());
    hash += hash * 31 + JodaBeanUtils.hashCode(getFamily());
    hash += hash * 31 + JodaBeanUtils.hashCode(getCurrency());
    hash += hash * 31 + JodaBeanUtils.hashCode(getTerms());
    hash += hash * 31 + JodaBeanUtils.hashCode(getComponents());
    return hash ^ super.hashCode();
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the version number.
   * @return the value of the property, not null
   */
  public String getVersion() {
    return _version;
  }

  /**
   * Sets the version number.
   * @param version  the new value of the property, not null
   */
  public void setVersion(String version) {
    JodaBeanUtils.notNull(version, "version");
    this._version = version;
  }

  /**
   * Gets the the {@code version} property.
   * @return the property, not null
   */
  public final Property<String> version() {
    return metaBean().version().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the series number.
   * @return the value of the property, not null
   */
  public String getSeries() {
    return _series;
  }

  /**
   * Sets the series number.
   * @param series  the new value of the property, not null
   */
  public void setSeries(String series) {
    JodaBeanUtils.notNull(series, "series");
    this._series = series;
  }

  /**
   * Gets the the {@code series} property.
   * @return the property, not null
   */
  public final Property<String> series() {
    return metaBean().series().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the family
   * @return the value of the property, not null
   */
  public String getFamily() {
    return _family;
  }

  /**
   * Sets the family
   * @param family  the new value of the property, not null
   */
  public void setFamily(String family) {
    JodaBeanUtils.notNull(family, "family");
    this._family = family;
  }

  /**
   * Gets the the {@code family} property.
   * @return the property, not null
   */
  public final Property<String> family() {
    return metaBean().family().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the currency.
   * @return the value of the property, not null
   */
  public Currency getCurrency() {
    return _currency;
  }

  /**
   * Sets the currency.
   * @param currency  the new value of the property, not null
   */
  public void setCurrency(Currency currency) {
    JodaBeanUtils.notNull(currency, "currency");
    this._currency = currency;
  }

  /**
   * Gets the the {@code currency} property.
   * @return the property, not null
   */
  public final Property<Currency> currency() {
    return metaBean().currency().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the terms.
   * @return the value of the property, not null
   */
  public List<Tenor> getTerms() {
    return _terms;
  }

  /**
   * Sets the terms.
   * @param terms  the new value of the property
   */
  public void setTerms(List<Tenor> terms) {
    this._terms.clear();
    this._terms.addAll(terms);
  }

  /**
   * Gets the the {@code terms} property.
   * @return the property, not null
   */
  public final Property<List<Tenor>> terms() {
    return metaBean().terms().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the index components.
   * @return the value of the property, not null
   */
  public List<CreditIndexComponent> getComponents() {
    return _components;
  }

  /**
   * Sets the index components.
   * @param components  the new value of the property
   */
  public void setComponents(List<CreditIndexComponent> components) {
    this._components.clear();
    this._components.addAll(components);
  }

  /**
   * Gets the the {@code components} property.
   * @return the property, not null
   */
  public final Property<List<CreditIndexComponent>> components() {
    return metaBean().components().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code CreditIndexSecurity}.
   */
  public static class Meta extends FinancialSecurity.Meta {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code version} property.
     */
    private final MetaProperty<String> _version = DirectMetaProperty.ofReadWrite(
        this, "version", CreditIndexSecurity.class, String.class);
    /**
     * The meta-property for the {@code series} property.
     */
    private final MetaProperty<String> _series = DirectMetaProperty.ofReadWrite(
        this, "series", CreditIndexSecurity.class, String.class);
    /**
     * The meta-property for the {@code family} property.
     */
    private final MetaProperty<String> _family = DirectMetaProperty.ofReadWrite(
        this, "family", CreditIndexSecurity.class, String.class);
    /**
     * The meta-property for the {@code currency} property.
     */
    private final MetaProperty<Currency> _currency = DirectMetaProperty.ofReadWrite(
        this, "currency", CreditIndexSecurity.class, Currency.class);
    /**
     * The meta-property for the {@code terms} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<List<Tenor>> _terms = DirectMetaProperty.ofReadWrite(
        this, "terms", CreditIndexSecurity.class, (Class) List.class);
    /**
     * The meta-property for the {@code components} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<List<CreditIndexComponent>> _components = DirectMetaProperty.ofReadWrite(
        this, "components", CreditIndexSecurity.class, (Class) List.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "version",
        "series",
        "family",
        "currency",
        "terms",
        "components");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case 351608024:  // version
          return _version;
        case -905838985:  // series
          return _series;
        case -1281860764:  // family
          return _family;
        case 575402001:  // currency
          return _currency;
        case 110250375:  // terms
          return _terms;
        case -447446250:  // components
          return _components;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends CreditIndexSecurity> builder() {
      return new DirectBeanBuilder<CreditIndexSecurity>(new CreditIndexSecurity());
    }

    @Override
    public Class<? extends CreditIndexSecurity> beanType() {
      return CreditIndexSecurity.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code version} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> version() {
      return _version;
    }

    /**
     * The meta-property for the {@code series} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> series() {
      return _series;
    }

    /**
     * The meta-property for the {@code family} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> family() {
      return _family;
    }

    /**
     * The meta-property for the {@code currency} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Currency> currency() {
      return _currency;
    }

    /**
     * The meta-property for the {@code terms} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<List<Tenor>> terms() {
      return _terms;
    }

    /**
     * The meta-property for the {@code components} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<List<CreditIndexComponent>> components() {
      return _components;
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}



