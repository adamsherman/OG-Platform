/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument;

import com.opengamma.financial.instrument.future.BondFutureDefinition;

/**
 * 
 * @param <T> Type of the data 
 * @param <U> Type of the result
 */
public interface FixedIncomeFutureInstrumentDefinitionVisitor<T, U> {

  U visitBondFutureDefinition(BondFutureDefinition bondFuture, T data);

  U visitBondFutureDefinition(BondFutureDefinition bondFuture);

}
