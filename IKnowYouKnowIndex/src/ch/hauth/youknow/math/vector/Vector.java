package ch.hauth.youknow.math.vector;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import ch.hauth.youknow.Config;

public class Vector implements Serializable {
	private static final long serialVersionUID = -4031028584846115761L;

	private static final short DEFAULT_VECTOR_SIZE = (short) Config.getInt("defaultVectorSize");
	@SuppressWarnings("unchecked")
	private static final Vector DEFAULT_SIZE_EMPTY_VECTOR = valueOf(Collections.EMPTY_LIST);

	private final int size;
	private final IRepresentVectorData vectorData;

	private Vector(final int size, final List<VectorValue> values) {
		this.size = size;
		if ((values.size() / 2) < size) {
			this.vectorData = new SparseVectorRepresentation(values);
		} else {
			this.vectorData = new DenseVectorRepresentation(size, values);
		}
	}

	private Vector(final int size, final IRepresentVectorData vectorData) {
		this.size = size;

		if ((this.size / 2) < vectorData.positionsCount()) {
			this.vectorData = new DenseVectorRepresentation(size, vectorData);
		} else {
			this.vectorData = vectorData;
		}
	}

	public static Vector emptyVector() {
		return DEFAULT_SIZE_EMPTY_VECTOR;
	}

	public static Vector valueOf(final int size, final VectorValue ... values) {
		return valueOf(size, Arrays.asList(values));
	}

	public static Vector valueOf(final List<VectorValue> values) {
		return valueOf(DEFAULT_VECTOR_SIZE, values);
	}

	public static Vector valueOf(final int size, final List<VectorValue> values) {
		Collections.sort(values, new Comparator<VectorValue>() {
			@Override
			public int compare(VectorValue v1, VectorValue v2) {
				return Integer.valueOf(v1.getPosition()).compareTo(Integer.valueOf(v2.getPosition()));
			}
		});
		return new Vector(size, values);
	}

	public Vector add(final Vector other) {
		return add(other, 1.0f);
	}

	public Vector add(final Vector other, float scalingFactor) {
		if (getSize() != other.getSize()) {
			throw new UnsupportedOperationException();
		}
		return new Vector(getSize(), this.vectorData.add(other.vectorData, scalingFactor));
	}

	public float crossProduct(Vector other) {
		if (getSize() != other.getSize()) {
			throw new UnsupportedOperationException();
		}
		float result = 0;

		int otherIndex = 0;
		int thisIndex = 0;
		while (thisIndex < this.vectorData.positionsCount() &&
				otherIndex < other.vectorData.positionsCount()) {
			int thisPosition = this.vectorData.getPosition(thisIndex);
			int otherPosition = other.vectorData.getPosition(otherIndex);
			if (thisPosition == otherPosition) {
				result += this.vectorData.getValue(thisIndex) * other.vectorData.getValue(otherIndex);
			}
			if (thisPosition <= otherPosition) {
				++thisIndex;
			}
			if (otherPosition <= thisPosition) {
				++otherIndex;
			}
		}
		return result;
	}

	public float abs() {
		return (float) Math.sqrt(crossProduct(this));
	}

	public int getSize() {
		return this.size;
	}

	public float get(int position) {
		return this.vectorData.getValueAtPosition(position);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Vector) {
			Vector other = (Vector) obj;
			return this.vectorData.equals(other.vectorData) &&
				   getSize() == other.getSize();
		}
		return false;
	}

	@Override
	public int hashCode() {
		return this.vectorData.hashCode();
	}

	@Override
	public String toString() {
		return "Size: " + getSize() + ", Values: " + this.vectorData;
	}
}
