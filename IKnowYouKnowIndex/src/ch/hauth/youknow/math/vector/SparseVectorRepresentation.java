package ch.hauth.youknow.math.vector;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;


public class SparseVectorRepresentation implements IRepresentVectorData {
    private static final long serialVersionUID = -2589444319218389900L;

    @SuppressWarnings("unchecked")
    public static final SparseVectorRepresentation EMPTY_VECTOR = new SparseVectorRepresentation(Collections.EMPTY_LIST);

    private final int[] positions;
    private final float[] values;

    public SparseVectorRepresentation(final List<VectorValue> sortedVectorValues) {
    	this.positions = new int[sortedVectorValues.size()];
    	this.values = new float[sortedVectorValues.size()];
    	int i = 0;
    	for (VectorValue vectorValue : sortedVectorValues) {
    		this.positions[i] = vectorValue.getPosition();
    		this.values[i] = vectorValue.getValue();
    		++i;
    	}
    }

    public SparseVectorRepresentation(final int[] positions, final float[] values) {
    	if (positions.length != values.length) {
    		throw new IllegalArgumentException("Need same amount of positions and values.");
    	}

    	this.positions = positions;
    	this.values = values;
    }

    @Override
	public IRepresentVectorData add(final IRepresentVectorData other, final float scalingFactor) {
		int thisPositionsCount = positionsCount();
		int otherPositionsCount = other.positionsCount();

		int usedPositions = 0;
		int[] resultPositions = new int[thisPositionsCount + otherPositionsCount];
		float[] resultValues = new float[thisPositionsCount + otherPositionsCount];

		int thisIndex = 0;
		int otherIndex = 0;
		while (thisIndex < thisPositionsCount || otherIndex < otherPositionsCount) {
			int thisPos = (thisIndex < thisPositionsCount) ? getPosition(thisIndex) : Integer.MAX_VALUE;
			int otherPos = (otherIndex < otherPositionsCount) ? other.getPosition(otherIndex) : Integer.MAX_VALUE;

			if (thisPos == otherPos) {
				resultPositions[usedPositions] = thisPos;
				resultValues[usedPositions] = getValue(thisIndex) + other.getValue(otherIndex) * scalingFactor;
				++thisIndex;
				++otherIndex;
			} else if (thisPos < otherPos) {
				resultPositions[usedPositions] = getPosition(thisIndex);
				resultValues[usedPositions] = getValue(thisIndex);
				thisIndex++;
			} else {
				resultPositions[usedPositions] = other.getPosition(otherIndex);
				resultValues[usedPositions] = other.getValue(otherIndex) * scalingFactor;
				otherIndex++;
			}
			++usedPositions;
		}
		return new SparseVectorRepresentation(Arrays.copyOf(resultPositions, usedPositions), Arrays.copyOf(resultValues, usedPositions));
    }

    @Override
    public int getPosition(int i) {
    	return this.positions[i];
    }

    @Override
    public float getValue(int i) {
    	return this.values[i];
    }

    @Override
    public int positionsCount() {
    	return this.positions.length;
    }
    
    @Override
    public float getValueAtPosition(int i) {
    	int index = Arrays.binarySearch(this.positions, i);
    	if (index < 0) {
    		return 0.0f;
    	}
    	return getValue(index);
    }

    @Override
    public boolean equals(Object obj) {
    	if (obj instanceof SparseVectorRepresentation) {
    		SparseVectorRepresentation other = (SparseVectorRepresentation) obj;
    		return Arrays.equals(this.positions, other.positions) &&
    			   Arrays.equals(this.values, other.values);
    	}
    	return false;
    }

    @Override
    public int hashCode() {
    	return Arrays.hashCode(this.positions) + 31 * Arrays.hashCode(this.values);
    }

    @Override
    public String toString() {
    	StringBuilder sb = new StringBuilder();
    	sb.append("{");
    	for (int i = 0; i < positionsCount(); ++i) {
    		sb.append("( " + getPosition(i) + ", " + getValue(i) + ")");
    	}
    	sb.append("}");
    	return sb.toString();
    }
}
