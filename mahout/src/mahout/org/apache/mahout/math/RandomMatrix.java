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

import java.util.Map;
import java.util.Random;

import org.apache.mahout.math.function.PlusMult;

/** Matrix of random but consistent doubles. 
 * Double.MIN_Value -> Double.MAX_VALUE 
 * linear and Gaussian distributions
 * 
 * Seed for [row][col] is this.seed + (row * #columns) + column.
 * This allows a RandomVector to take seed + (row * #columns) as its seed
 * and be reproducible from this matrix.
 * 
 * Use Float min/max to avoid Outer Limits problems.
 * */
public class RandomMatrix extends AbstractMatrix {
	private static final double MIN_BOUND = 0.0000000001;
	private static final double MAX_BOUND = 0.99999999999;

	//	final int rows, columns;
	final Random rnd = new Random();
	final long seed;
	final boolean gaussian;
	final double lowerBound;
	final double upperBound;

	// Some serialization thing?
	public RandomMatrix() {
		cardinality[ROW] = 0;
		cardinality[COL] = 0;
		seed = 0;
		lowerBound = MIN_BOUND;
		upperBound = MAX_BOUND;
		gaussian = false;
	}

	/**
	 * Constructs an empty matrix of the given size.
	 * @param rows  The number of rows in the result.
	 * @param columns The number of columns in the result.
	 */
	public RandomMatrix(int rows, int columns) {
		cardinality[ROW] = rows;
		cardinality[COL] = columns;
		seed = 0;
		lowerBound = MIN_BOUND;
		upperBound = MAX_BOUND;
		gaussian = false;
	}

	public RandomMatrix(int rows, int columns, long seed, double lowerBound,
			double upperBound, boolean gaussian) {
		cardinality[ROW] = rows;
		cardinality[COL] = columns;
		this.seed = seed;
		this.lowerBound = lowerBound;
		this.upperBound = upperBound;
		this.gaussian = gaussian;
	}

	@Override
	public Matrix clone() {
		RandomMatrix clone = new RandomMatrix(rowSize(), columnSize(), seed, lowerBound, upperBound, gaussian);
		return clone;
	}

	public double getQuick(int row, int column) {
		if (row < 0 || row >= rowSize())
			throw new CardinalityException(row, rowSize());
		if (column < 0 || column >= columnSize())
			throw new CardinalityException(column, columnSize());
		rnd.setSeed(getSeed(row, column));
		double value = getRandom();
		if (!(value > lowerBound && value < upperBound))
			throw new Error("RandomVector: getQuick created NaN");
		return value;
	}

	private long getSeed(int row, int column) {
		return seed + (row * columnSize()) + column;
	}

	// give a wide range but avoid NaN land
	private double getRandom() {
		double raw = gaussian ? rnd.nextGaussian() : rnd.nextDouble();
		if (lowerBound >= 0.0 && upperBound <= 1.0)
			return raw;
		double range = (upperBound - lowerBound);
		double expand = raw * range;
		expand += lowerBound;
		return expand;
	}

	public Matrix like() {
		throw new UnsupportedOperationException();
	}

	public Matrix like(int rows, int columns) {
		throw new UnsupportedOperationException();
	}

	public void setQuick(int row, int column, double value) {
		throw new UnsupportedOperationException();
	}

	public int[] getNumNondefaultElements() {
		return size();
	}

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

	public Matrix assignColumn(int column, Vector other) {
		throw new UnsupportedOperationException();
	}

	public Matrix assignRow(int row, Vector other) {
		throw new UnsupportedOperationException();
	}

	public Vector getColumn(int column) {
		if (column < 0 || column >= columnSize()) {
			throw new IndexException(column, columnSize());
		}
		//		return new TransposeViewVector(this, column);
		return new RandomVector(cardinality[ROW], seed + cardinality[COL] * column, cardinality[COL], lowerBound, upperBound, gaussian);

	}

	public Vector getRow(int row) {
		if (row < 0 || row >= rowSize()) {
			throw new IndexException(row, rowSize());
		}
		return new RandomVector(columnSize(), seed + row * columnSize(), 1, lowerBound, upperBound, gaussian);
	}

	// redo arithmetic from abstract matrix
	public Matrix minus(Matrix other) {
		int[] c = size();
		int[] o = other.size();
		if (c[ROW] != o[ROW]) {
			throw new CardinalityException(c[ROW], o[ROW]);
		}
		if (c[COL] != o[COL]) {
			throw new CardinalityException(c[COL], o[COL]);
		}
		Matrix result = clone();
		for (int row = 0; row < c[ROW]; row++) {
			for (int col = 0; col < c[COL]; col++) {
				result.setQuick(row, col, result.getQuick(row, col)
						- other.getQuick(row, col));
			}
		}
		return result;
	}

	public Matrix plus(double x) {
		Matrix result = new DenseMatrix(rowSize(), columnSize());
		int[] c = size();
		for (int row = 0; row < c[ROW]; row++) {
			for (int col = 0; col < c[COL]; col++) {
				result.setQuick(row, col, getQuick(row, col) + x);
			}
		}
		return result;
	}

	public Matrix plus(Matrix other) {
		int[] c = size();
		int[] o = other.size();
		if (c[ROW] != o[ROW]) {
			throw new CardinalityException(c[ROW], o[ROW]);
		}
		if (c[COL] != o[COL]) {
			throw new CardinalityException(c[COL], o[COL]);
		}
		Matrix result = clone();
		for (int row = 0; row < c[ROW]; row++) {
			for (int col = 0; col < c[COL]; col++) {
				result.setQuick(row, col, result.getQuick(row, col)
						+ other.getQuick(row, col));
			}
		}
		return result;
	}


	@Override
	public Matrix divide(double x) {
		int[] c = size();
		Matrix result = new DenseMatrix(c[ROW], c[COL]);
		for (int row = 0; row < c[ROW]; row++) {
			for (int col = 0; col < c[COL]; col++) {
				result.setQuick(row, col, this.getQuick(row, col) / x);
			}
		}
		return result;
	}
	public Matrix times(double x) {
		Matrix result =  new DenseMatrix(rowSize(), columnSize());
		int[] c = size();
		for (int row = 0; row < c[ROW]; row++) {
			for (int col = 0; col < c[COL]; col++) {
				result.setQuick(row, col, getQuick(row, col) * x);
			}
		}
		return result;
	}

	public Matrix times(Matrix other) {
		int[] c = size();
		int[] o = other.size();
		if (c[COL] != o[ROW]) {
			throw new CardinalityException(c[COL], o[ROW]);
		}
		Matrix result = new DenseMatrix(rowSize(), columnSize());
		for (int row = 0; row < c[ROW]; row++) {
			for (int col = 0; col < o[COL]; col++) {
				double sum = 0;
				for (int k = 0; k < c[COL]; k++) {
					sum += getQuick(row, k) * other.getQuick(k, col);
				}
				result.setQuick(row, col, sum);
			}
		}
		return result;
	}

	public Vector times(Vector v) {
		int[] c = size();
		if (c[COL] != v.size()) {
			throw new CardinalityException(c[COL], v.size());
		}
		Vector w = new DenseVector(c[ROW]);
		for (int i = 0; i < c[ROW]; i++) {
			w.setQuick(i, v.dot(getRow(i)));
		}
		return w;
	}

	public Vector timesSquared(Vector v) {
		int[] c = size();
		if (c[COL] != v.size()) {
			throw new CardinalityException(c[COL], v.size());
		}
		Vector w = new DenseVector(c[COL]);
		for (int i = 0; i < c[ROW]; i++) {
			Vector xi = getRow(i);
			double d = xi.dot(v);
			if (d != 0.0) {
				w.assign(xi, new PlusMult(d));
			}

		}
		return w;
	}

	public Matrix transpose() {
		int[] card = size();
		Matrix result = new DenseMatrix(columnSize(), rowSize());
		for (int row = 0; row < card[ROW]; row++) {
			for (int col = 0; col < card[COL]; col++) {
				result.setQuick(col, row, getQuick(row, col));
			}
		}
		return result;
	}

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

	  };




}
