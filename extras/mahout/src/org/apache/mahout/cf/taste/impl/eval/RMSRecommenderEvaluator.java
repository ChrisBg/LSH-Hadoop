/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.mahout.cf.taste.impl.eval;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.FullRunningAverage;
import org.apache.mahout.cf.taste.impl.common.RunningAverage;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.Preference;
import org.apache.mahout.cf.taste.recommender.Recommender;

/**
 * <p>
 * A {@link org.apache.mahout.cf.taste.eval.RecommenderEvaluator} which computes the "root mean squared"
 * difference between predicted and actual ratings for users. This is the square root of the average of this
 * difference, squared.
 * </p>
 */
@Deprecated
public final class RMSRecommenderEvaluator extends AbstractDifferenceRecommenderEvaluator {
  
  private RunningAverage average;
  
  @Override
  void reset() {
    average = new FullRunningAverage();
  }
  
  @Override
  void processOneEstimate(float estimatedPreference, Preference realPref) {
    double diff = realPref.getValue() - estimatedPreference;
    average.addDatum(diff * diff);
  }
  
  @Override
  double computeFinalEvaluation() {
    return Math.sqrt(average.getAverage());
  }
  
  @Override
  public String toString() {
    return "RMSRecommenderEvaluator";
  }

  @Override
  public void evaluate(Recommender recommender1, Recommender recommender2,
      int samples, RunningAverage tracker, Formula formula)
      throws TasteException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void evaluate(Recommender recommender, DataModel model, int samples,
      RunningAverage tracker, Formula formula) throws TasteException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void evaluate(DataModel model1, DataModel model2, int samples,
      RunningAverage tracker, Formula formula) throws TasteException {
    // TODO Auto-generated method stub
    
  }
  
}