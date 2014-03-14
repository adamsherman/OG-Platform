/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.livedata;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import net.sf.ehcache.CacheManager;

import org.fudgemsg.FudgeMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.opengamma.bbg.referencedata.ReferenceDataProvider;
import com.opengamma.bbg.util.BloombergDataUtils;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.id.ExternalScheme;
import com.opengamma.livedata.normalization.StandardRuleResolver;
import com.opengamma.livedata.resolver.DefaultDistributionSpecificationResolver;
import com.opengamma.livedata.resolver.DistributionSpecificationResolver;
import com.opengamma.livedata.resolver.EHCachingDistributionSpecificationResolver;
import com.opengamma.livedata.resolver.IdResolver;
import com.opengamma.livedata.resolver.NormalizationRuleResolver;
import com.opengamma.livedata.server.StandardLiveDataServer;
import com.opengamma.livedata.server.Subscription;
import com.opengamma.util.ArgumentChecker;


/**
 * Allows common functionality to be shared between the live and recorded Bloomberg data servers
 */
public abstract class AbstractBloombergLiveDataServer extends StandardLiveDataServer {
  private static final Logger s_logger = LoggerFactory.getLogger(AbstractBloombergLiveDataServer.class);
  private static final String DEFAULT_BBG_SUB_PREFIX = "/buid/";
  
  private NormalizationRuleResolver _normalizationRules;  
  private IdResolver _idResolver;
  private DistributionSpecificationResolver _defaultDistributionSpecificationResolver;

  /**
   * Creates an instance.
   * 
   * @param cacheManager  the cache manager, not null
   */
  public AbstractBloombergLiveDataServer(CacheManager cacheManager) {
    super(cacheManager);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the reference data provider.
   * 
   * @return the reference data provider
   */
  protected abstract ReferenceDataProvider getReferenceDataProvider();

  @Override
  protected ExternalScheme getUniqueIdDomain() {
    return ExternalSchemes.BLOOMBERG_BUID;
  }
  
  @Override
  protected boolean snapshotOnSubscriptionStartRequired(Subscription subscription) {
    // As per Kirk, it is possible that you don't get all fields initially.
    // Should we optimize this by asset type?
    return true;
  }
  
  /**
   * 
   * @return the prefix to use when making the subscription, including slashes.
   * e.g. "/buid/"
   */
  protected String getBloombergSubscriptionPathPrefix() {
    return DEFAULT_BBG_SUB_PREFIX;
  }
  
  
  @Override
  public Map<String, FudgeMsg> doSnapshot(Collection<String> uniqueIds) {
    ArgumentChecker.notNull(uniqueIds, "Unique IDs");
    if (uniqueIds.isEmpty()) {
      return Collections.emptyMap();
    }
    
    Set<String> buids = Sets.newHashSetWithExpectedSize(uniqueIds.size());
    for (String uniqueId : uniqueIds) {
      String buid = getBloombergSubscriptionPathPrefix() + uniqueId;
      buids.add(buid);
    }
    
    // caching ref data provider must not be used here
    Map<String, FudgeMsg> snapshotValues = getReferenceDataProvider().getReferenceDataIgnoreCache(buids, BloombergDataUtils.STANDARD_FIELDS_SET);
    Map<String, FudgeMsg> returnValue = Maps.newHashMap();
    for (String buid : buids) {
      FudgeMsg fieldData = snapshotValues.get(buid);
      if (fieldData == null) {
        Exception e = new Exception("Stack Trace");
        e.fillInStackTrace();
        s_logger.error("Could not find result for {} in data snapshot, skipping", buid, e);
        //throw new OpenGammaRuntimeException("Result for " + buid + " was not found");
      } else {
        String securityUniqueId = buid.substring(getBloombergSubscriptionPathPrefix().length());
        returnValue.put(securityUniqueId, fieldData);
      }
    }
    return returnValue;
  }

  public synchronized NormalizationRuleResolver getNormalizationRules() {
    if (_normalizationRules == null) {
      _normalizationRules = new StandardRuleResolver(BloombergDataUtils.getDefaultNormalizationRules(getReferenceDataProvider(), getCacheManager(), getUniqueIdDomain()));
    }
    return _normalizationRules;
  }

  public synchronized IdResolver getIdResolver() {
    if (_idResolver == null) {
      _idResolver = new BloombergIdResolver(getReferenceDataProvider());
    }
    return _idResolver;
  }

  public synchronized DistributionSpecificationResolver getDefaultDistributionSpecificationResolver() {
    if (_defaultDistributionSpecificationResolver == null) {
      BloombergJmsTopicNameResolver topicResolver = new BloombergJmsTopicNameResolver(getReferenceDataProvider());
      DefaultDistributionSpecificationResolver distributionSpecResolver = new DefaultDistributionSpecificationResolver(getIdResolver(), getNormalizationRules(), topicResolver);
      return new EHCachingDistributionSpecificationResolver(distributionSpecResolver, getCacheManager(), "BBG");
    }
    return _defaultDistributionSpecificationResolver;
  }

}
