package ch.hauth.youknow.math.vector;

import java.io.Serializable;

public interface IRepresentVectorData extends Serializable {
	public IRepresentVectorData add(final IRepresentVectorData other, final float scalingFactor);
	public int getPosition(final int i);
	public float getValue(final int i);
	public int positionsCount();
	public float getValueAtPosition(int i);
}
