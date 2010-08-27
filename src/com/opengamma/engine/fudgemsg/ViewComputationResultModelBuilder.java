/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.fudgemsg;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.time.Instant;

import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;
import org.fudgemsg.mapping.GenericFudgeBuilderFor;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.view.ViewCalculationResultModel;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.ViewResultModel;

/**
 */
@GenericFudgeBuilderFor(ViewComputationResultModel.class)
public class ViewComputationResultModelBuilder extends ViewResultModelBuilder implements FudgeBuilder<ViewComputationResultModel> {
  
  @Override
  public MutableFudgeFieldContainer buildMessage(FudgeSerializationContext context, ViewComputationResultModel resultModel) {
    final MutableFudgeFieldContainer message = ViewResultModelBuilder.createResultModelMessage(context, resultModel);
    return message;
  }
  
  @Override
  public ViewComputationResultModel buildObject(FudgeDeserializationContext context, FudgeFieldContainer message) {
    return (ViewComputationResultModel) bootstrapCommonDataFromMessage(context, message);
  }

  @Override
  protected ViewResultModel constructImpl(
      final FudgeDeserializationContext context,
      final FudgeFieldContainer message,
      final Instant inputDataTimestamp,
      final Instant resultTimestamp,
      final Map<String, ViewCalculationResultModel> map) {
    return new ViewComputationResultModel() {
      @Override
      public Collection<ComputationTargetSpecification> getAllTargets() {
        Set<ComputationTargetSpecification> allTargetSpecs = new HashSet<ComputationTargetSpecification>();
        for (ViewCalculationResultModel calcResultModel : map.values()) {
          allTargetSpecs.addAll(calcResultModel.getAllTargets());
        }
        return allTargetSpecs;
      }
      @Override
      public Collection<String> getCalculationConfigurationNames() {
        return map.keySet();
      }
      @Override
      public ViewCalculationResultModel getCalculationResult(String calcConfigurationName) {
        return map.get(calcConfigurationName);
      }
      @Override
      public Instant getValuationTime() {
        return inputDataTimestamp;
      }
      @Override
      public Instant getResultTimestamp() {
        return resultTimestamp;
      }
      @Override
      public String toString() {
        return "ViewComputationResultModel, valuation time=" + getValuationTime() + ", result timestamp=" + getResultTimestamp();
      }
    };
  }

}
