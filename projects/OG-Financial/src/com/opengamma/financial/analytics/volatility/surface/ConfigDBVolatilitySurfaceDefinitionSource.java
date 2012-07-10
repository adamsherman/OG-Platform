/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.surface;

import javax.time.Instant;

import com.opengamma.core.config.ConfigSource;
import com.opengamma.financial.analytics.model.InstrumentTypeProperties;
import com.opengamma.util.ArgumentChecker;

/**
 * A source of volatility surface definitions based on configuration.
 * <p>
 * This supplies surface definitions from a {@link ConfigSource}.
 */
public class ConfigDBVolatilitySurfaceDefinitionSource implements VolatilitySurfaceDefinitionSource {

  /**
   * The config source for the data.
   */
  private final ConfigSource _configSource;

  /**
   * Creates an instance backed by a config source.
   * @param configSource  the source, not null
   */
  public ConfigDBVolatilitySurfaceDefinitionSource(final ConfigSource configSource) {
    ArgumentChecker.notNull(configSource, "configSource");
    _configSource = configSource;
  }

  /**
   * Gets the config source.
   * @return the config source, not null
   */
  protected ConfigSource getConfigSource() {
    return _configSource;
  }

  //-------------------------------------------------------------------------
  @Override
  public VolatilitySurfaceDefinition<?, ?> getDefinition(final String name, final String instrumentType) {
    final VolatilitySurfaceDefinition<?, ?> definition = _configSource.getLatestByName(VolatilitySurfaceDefinition.class, name + "_" + instrumentType);
    if (definition == null && InstrumentTypeProperties.FOREX.equals(instrumentType)) {
      final String[] substrings = name.split("_");
      if (substrings.length == 2 && substrings[1].length() == 6) {
        final String firstCcy = substrings[1].substring(0, 3);
        final String secondCcy = substrings[1].substring(3, 6);
        final String reversedName = secondCcy + firstCcy;
        return _configSource.getLatestByName(VolatilitySurfaceDefinition.class, reversedName + "_" + instrumentType);
      }
    }
    return definition;
  }

  @Override
  public VolatilitySurfaceDefinition<?, ?> getDefinition(final String name, final String instrumentType, final Instant version) {
    final VolatilitySurfaceDefinition<?, ?> definition = _configSource.getByName(VolatilitySurfaceDefinition.class, name + "_" + instrumentType, version);
    if (definition == null && InstrumentTypeProperties.FOREX.equals(instrumentType)) {
      final String[] substrings = name.split("_");
      if (substrings.length == 2 && substrings[1].length() == 6) {
        final String firstCcy = substrings[1].substring(0, 3);
        final String secondCcy = substrings[1].substring(3, 6);
        final String reversedName = secondCcy + firstCcy;
        return _configSource.getByName(VolatilitySurfaceDefinition.class, reversedName + "_" + instrumentType, version);
      }
    }
    return definition;
  }

}
