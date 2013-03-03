package ch.hauth.youknow.math.vector;

import java.util.Arrays;
import java.util.List;


public class DenseVectorRepresentation implements IRepresentVectorData {
    private static final long serialVersionUID = 8103260612925869334L;

    private final float[] values;
    private final int positionsCount;

    public DenseVectorRepresentation(final int size, final List<VectorValue> values) {
    	this.values = new float[size];
    	for (VectorValue value : values) {
    		this.values[value.getPosition()] = value.getValue();
    	}
    	this.positionsCount = values.size();
    }

    public DenseVectorRepresentation(final int size, final IRepresentVectorData vectorData) {
    	this.values = new float[size];
    	int index = 0;
    	for (; index < vectorData.positionsCount(); ++index) {
    		values[vectorData.getPosition(index)] = vectorData.getValue(index);
    	}
    	this.positionsCount = index;
    }

    public DenseVectorRepresentation(final float[] values) {
    	this.values = values;
    	this.positionsCount = this.values.length;
    }

    @Override
	public IRepresentVectorData add(final IRepresentVectorData other, final float scalingFactor) {
		float[] resultValues = Arrays.copyOf(this.values, this.values.length);

		for (int i = 0; i < other.positionsCount(); ++i) {
			int position = other.getPosition(i);
			resultValues[position] += other.getValue(i) * scalingFactor;
		}
		return new DenseVectorRepresentation(resultValues);
    }

    @Override
    public int getPosition(int i) {
    	return i;
    }

    @Override
    public float getValue(int i) {
    	return this.values[i];
    }

    @Override
    public int positionsCount() {
    	return this.positionsCount;
    }
    
    @Override
    public float getValueAtPosition(int i) {
    	return getValue(i);
    }

    @Override
    public boolean equals(Object obj) {
    	if (obj instanceof DenseVectorRepresentation) {
    		DenseVectorRepresentation other = (DenseVectorRepresentation) obj;
    		return Arrays.equals(this.values, other.values);
    	}
    	return false;
    }

    @Override
    public int hashCode() {
    	return Arrays.hashCode(this.values);
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
