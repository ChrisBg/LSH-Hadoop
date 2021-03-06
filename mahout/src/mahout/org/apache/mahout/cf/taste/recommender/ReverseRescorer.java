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

package org.apache.mahout.cf.taste.recommender;

import org.apache.mahout.cf.taste.recommender.IDRescorer;
import org.apache.mahout.cf.taste.recommender.Rescorer;
import org.apache.mahout.common.LongPair;

/**
 * <p>
 * A simple {@link Rescorer} which always returns the original score.
 * </p>
 */
public final class ReverseRescorer<T> implements Rescorer<T>, IDRescorer {

  private static final IDRescorer USER_OR_ITEM_INSTANCE = new ReverseRescorer<Long>();
  private static final Rescorer<LongPair> ITEM_ITEM_PAIR_INSTANCE = new ReverseRescorer<LongPair>();
  private static final Rescorer<LongPair> USER_USER_PAIR_INSTANCE = new ReverseRescorer<LongPair>();

  private ReverseRescorer() {
  }

  public static IDRescorer getItemInstance() {
    return USER_OR_ITEM_INSTANCE;
  }

  public static IDRescorer getUserInstance() {
    return USER_OR_ITEM_INSTANCE;
  }

  public static Rescorer<LongPair> getItemItemPairInstance() {
    return ITEM_ITEM_PAIR_INSTANCE;
  }

  public static Rescorer<LongPair> getUserUserPairInstance() {
    return USER_USER_PAIR_INSTANCE;
  }

  /**
   * @param thing
   *          to rescore
   * @param originalScore
   *          current score for item
   * @return same originalScore as new score, always
   */
  @Override
  public double rescore(T thing, double originalScore) {
    return -originalScore;
  }

  /**
   * Returns <code>true</code> to exclude the given thing.
   * 
   * @param thing
   *          the thing to filter
   * @return <code>true</code> to exclude, <code>false</code> otherwise
   */

  @Override
  public boolean isFiltered(T thing) {
    return false;
  }

  /**
   * @param id
   *          ID of thing (user, item, etc.) to rescore
   * @param originalScore
   *          original score
   * @return modified score, or {@link Double#NaN} to indicate that this should be excluded entirely
   */
  @Override
  public double rescore(long id, double originalScore) {
    return -originalScore;
  }

  /**
   * Returns <code>true</code> to exclude the given thing.
   * 
   * @param id
   *          ID of thing (user, item, etc.) to rescore
   * @return <code>true</code> to exclude, <code>false</code> otherwise
   */

  @Override
  public boolean isFiltered(long id) {
    return false;
  }

  @Override
  public String toString() {
    return "ReverseRescorer";
  }

}
