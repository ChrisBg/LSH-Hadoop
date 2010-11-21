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

package org.apache.mahout.math;

import java.util.Random;

import org.apache.mahout.math.function.BinaryFunction;
import org.apache.mahout.math.function.UnaryFunction;

import com.google.common.collect.Maps;

/** 
 * Matrix of random but consistent doubles. 
 * Double.MIN_Value -> Double.MAX_VALUE 
 * Linear, limited gaussian, and raw Gaussian distributions
 * 
 * Seed for [row][col] is this.seed + (row * #columns) + column.
 * This allows a RandomVector to take seed + (row * #columns) as its seed
 * and be reproducible from this matrix.
 * 
 * Is read-only. Can be given a writable cache.
 * One quirk: Matrix.like() means "give a writable matrix
 * with the same dense/sparsity profile. The cache also supplies that.
 * */
public class RandomMatrix extends AbstractMatrix {
  // TODO: use enums for this? don't know how to use them
  public static final int LINEAR = 0;
  public static final int GAUSSIAN = 1;
  public static final int GAUSSIAN01 = 2;

  final private Random rnd = new Random();
  final private long seed;
  final private int distribution;
  
  /*
   * cache has two jobs:
   *    cache the values
   *    supply the 'like()' matrix with the same dense/sparsity profile
   */
  Matrix cache = null;

  /**
   * Constructs a zero-size matrix.
   * Some serialization thing?
   */

  public RandomMatrix() {
    cardinality[ROW] = 0;
    cardinality[COL] = 0;
    seed = 0;
    distribution = LINEAR;
  }

  /**
   * Constructs an empty matrix of the given size.
   * Linear distribution.
   * @param rows  The number of rows in the result.
   * @param columns The number of columns in the result.
   */
  public RandomMatrix(int rows, int columns) {
    cardinality[ROW] = rows;
    cardinality[COL] = columns;
    seed = 0;
    distribution = LINEAR;
  }

  /*
   * Constructs an empty matrix of the given size.
   * Linear distribution.
   * @param rows  The number of rows in the result.
   * @param columns The number of columns in the result.
   * @param seed Random seed.
   * @param distribution Random distribution: LINEAR, GAUSSIAN, GAUSSIAN01.
   * @param cache Vector to use as cache. Doubles as 'like' source.
  */
  public RandomMatrix(int rows, int columns, long seed, int distribution, Matrix cache) {
    cardinality[ROW] = rows;
    cardinality[COL] = columns;
    this.seed = seed;
    this.distribution = distribution;
    this.cache = cache;
  }
  
  @Override
  public Matrix clone() {
    // it would thread-safe to pass the cache object itself.
    RandomMatrix clone = new RandomMatrix(rowSize(), columnSize(), seed, distribution, cache.clone());
    if (rowLabelBindings != null) {
      clone.rowLabelBindings = Maps.newHashMap(rowLabelBindings);
    }
    if (columnLabelBindings != null) {
      clone.columnLabelBindings = Maps.newHashMap(columnLabelBindings);
    }
    return clone;
  }

  @Override
  public double getQuick(int row, int column) {
    if (row < 0 || row >= rowSize())
      throw new CardinalityException(row, rowSize());
    if (column < 0 || column >= columnSize())
      throw new CardinalityException(column, columnSize());
    if (cache != null) {
      // already did cardinality checks
      double d = cache.get(row, column);
      if (d != 0)
        return d;
    }
    rnd.setSeed(getSeed(row, column));
    double value = getRandom();
    if (!(value > Double.MIN_VALUE && value < Double.MAX_VALUE))
      throw new Error("RandomVector: getQuick created NaN");
    if (null != cache)
      cache.setQuick(row, column, value);
    return value;
  }

  private long getSeed(int row, int column) {
    return seed + (row * columnSize()) + column;
  }

  double getRandom() {
    switch (distribution) {
    case LINEAR: return rnd.nextDouble();
    case GAUSSIAN: return rnd.nextGaussian();
    case GAUSSIAN01: return gaussian01();
    default: throw new Error("RandomMatrix: not a random distribution: " + distribution);
    }
  }

  // Gaussian.java has better one
  private double gaussian01() {
    double sum = 0;
    for(int i = 0; i < 12; i++) {
      sum += rnd.nextDouble();
    }
    return sum / 12.0;
  }

  @Override
  public Matrix like() {
    if (null == cache)
      return new DenseMatrix(rowSize(), columnSize());
    return cache.like();
  }

  @Override
 public Matrix like(int rows, int columns) {
    if (null == cache)
      return new DenseMatrix(rows, columns);
    return cache.like(rows, columns);
  }

  @Override
  public void setQuick(int row, int column, double value) {
    throw new UnsupportedOperationException();
  }

  @Override
  public int[] getNumNondefaultElements() {
    if (null != cache)
      return cache.getNumNondefaultElements();
    else
      return size();
  }

  @Override
  public Matrix viewPart(int[] offset, int[] size) {
    int rowOffset = offset[ROW];
    int rowsRequested = size[ROW];
    int columnOffset = offset[COL];
    int columnsRequested = size[COL];

    return viewPart(rowOffset, rowsRequested, columnOffset, columnsRequested);
  }

  @Override
  public Matrix viewPart(int rowOffset, int rowsRequested, int columnOffset, int columnsRequested) {
    if (rowOffset < 0) {
      throw new IndexException(rowOffset, rowSize());
    }
    if (rowOffset + rowsRequested > rowSize()) {
      throw new IndexException(rowOffset + rowsRequested, rowSize());
    }
    if (columnOffset < 0) {
      throw new IndexException(columnOffset, columnSize());
    }
    if (columnOffset + columnsRequested > columnSize()) {
      throw new IndexException(columnOffset + columnsRequested, columnSize());
    }
    return new MatrixView(this, new int[]{rowOffset, columnOffset}, new int[]{rowsRequested, columnsRequested});
  }

  @Override
  public Matrix assign(double value) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Matrix assignColumn(int column, Vector other) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Matrix assignRow(int row, Vector other) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Matrix assign(double[][] values) {
    throw new UnsupportedOperationException();
  }
  
  @Override
  public Matrix assign(UnaryFunction function) {
    throw new UnsupportedOperationException();
  }
  
  @Override
  public Matrix assign(Matrix other, BinaryFunction function) {
    throw new UnsupportedOperationException();
  }
  
  // TODO: make matching cache vector for RandomVector
  @Override
  public Vector getColumn(int column) {
    if (column < 0 || column >= columnSize()) {
      throw new IndexException(column, columnSize());
    }
    //		return new TransposeViewVector(this, column);
    return new RandomVector(cardinality[ROW], seed + cardinality[COL] * column, cardinality[COL], distribution);

  }

  // TODO: make matching cache vector for RandomVector
  @Override
  public Vector getRow(int row) {
    if (row < 0 || row >= rowSize()) {
      throw new IndexException(row, rowSize());
    }
    return new RandomVector(columnSize(), seed + row * columnSize(), 1, distribution);
  }

  // redo arithmetic from abstract matrix
//  public Matrix minus(Matrix other) {
//    int[] c = size();
//    int[] o = other.size();
//    if (c[ROW] != o[ROW]) {
//      throw new CardinalityException(c[ROW], o[ROW]);
//    }
//    if (c[COL] != o[COL]) {
//      throw new CardinalityException(c[COL], o[COL]);
//    }
//    Matrix result = clone();
//    for (int row = 0; row < c[ROW]; row++) {
//      for (int col = 0; col < c[COL]; col++) {
//        result.setQuick(row, col, result.getQuick(row, col)
//            - other.getQuick(row, col));
//      }
//    }
//    return result;
//  }

//  public Matrix plus(double x) {
//    Matrix result = new DenseMatrix(rowSize(), columnSize());
//    int[] c = size();
//    for (int row = 0; row < c[ROW]; row++) {
//      for (int col = 0; col < c[COL]; col++) {
//        result.setQuick(row, col, getQuick(row, col) + x);
//      }
//    }
//    return result;
//  }

//  public Matrix plus(Matrix other) {
//    int[] c = size();
//    int[] o = other.size();
//    if (c[ROW] != o[ROW]) {
//      throw new CardinalityException(c[ROW], o[ROW]);
//    }
//    if (c[COL] != o[COL]) {
//      throw new CardinalityException(c[COL], o[COL]);
//    }
//    Matrix result = clone();
//    for (int row = 0; row < c[ROW]; row++) {
//      for (int col = 0; col < c[COL]; col++) {
//        result.setQuick(row, col, result.getQuick(row, col)
//            + other.getQuick(row, col));
//      }
//    }
//    return result;
//  }


//  @Override
//  public Matrix divide(double x) {
//    int[] c = size();
//    Matrix result = new DenseMatrix(c[ROW], c[COL]);
//    for (int row = 0; row < c[ROW]; row++) {
//      for (int col = 0; col < c[COL]; col++) {
//        result.setQuick(row, col, this.getQuick(row, col) / x);
//      }
//    }
//    return result;
//  }

//  public Matrix times(double x) {
//    Matrix result =  new DenseMatrix(rowSize(), columnSize());
//    int[] c = size();
//    for (int row = 0; row < c[ROW]; row++) {
//      for (int col = 0; col < c[COL]; col++) {
//        result.setQuick(row, col, getQuick(row, col) * x);
//      }
//    }
//    return result;
//  }

//  public Matrix times(Matrix other) {
//    int[] c = size();
//    int[] o = other.size();
//    if (c[COL] != o[ROW]) {
//      throw new CardinalityException(c[COL], o[ROW]);
//    }
//    Matrix result = new DenseMatrix(rowSize(), columnSize());
//    for (int row = 0; row < c[ROW]; row++) {
//      for (int col = 0; col < o[COL]; col++) {
//        double sum = 0;
//        for (int k = 0; k < c[COL]; k++) {
//          sum += getQuick(row, k) * other.getQuick(k, col);
//        }
//        result.setQuick(row, col, sum);
//      }
//    }
//    return result;
//  }

//  public Vector times(Vector v) {
//    int[] c = size();
//    if (c[COL] != v.size()) {
//      throw new CardinalityException(c[COL], v.size());
//    }
//    Vector w = new DenseVector(c[ROW]);
//    for (int i = 0; i < c[ROW]; i++) {
//      w.setQuick(i, v.dot(getRow(i)));
//    }
//    return w;
//  }

//  public Vector timesSquared(Vector v) {
//    int[] c = size();
//    if (c[COL] != v.size()) {
//      throw new CardinalityException(c[COL], v.size());
//    }
//    Vector w = new DenseVector(c[COL]);
//    for (int i = 0; i < c[ROW]; i++) {
//      Vector xi = getRow(i);
//      double d = xi.dot(v);
//      if (d != 0.0) {
//        w.assign(xi, new PlusMult(d));
//      }
//
//    }
//    return w;
//  }

//  public Matrix transpose() {
//    int[] card = size();
//    Matrix result = new DenseMatrix(columnSize(), rowSize());
//    for (int row = 0; row < card[ROW]; row++) {
//      for (int col = 0; col < card[COL]; col++) {
//        result.setQuick(col, row, getQuick(row, col));
//      }
//    }
//    return result;
//  }

  /*
   * Can set bindings for all rows and columns.
   * Can fetch values for bindings.
   * Cannot set values with bindings.
   */
  
  @Override
  public
  void set(String rowLabel, String columnLabel, double value) {
    throw new UnsupportedOperationException();
  }

  @Override
  public
  void set(String rowLabel, String columnLabel, int row, int column, double value) {
    throw new UnsupportedOperationException();

  }

  @Override
  public
  void set(String rowLabel, double[] rowData) {
    throw new UnsupportedOperationException();

  }

  @Override
  public
  void set(String rowLabel, int row, double[] rowData) {		
    throw new UnsupportedOperationException();
  }

}
