package ch.hauth.youknow.math.vector;

import java.io.Serializable;

public class VectorValue implements Serializable {
	private static final long serialVersionUID = -1857353400499698349L;

	private final int position;
	private final float value;

	public VectorValue(int position, float value) {
		this.position = position;
		this.value = value;
	}

	public int getPosition() {
		return this.position;
	}

	public float getValue() {
		return this.value;
	}

	public VectorValue multiply(float factor) {
		return new VectorValue(this.position, this.value * factor);
	}

	public VectorValue add(float summand) {
		return new VectorValue(this.position, this.value + summand);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof VectorValue) {
			VectorValue other = (VectorValue) obj;
			return this.position == other.position &&
				   this.value == other.value;
		}
		return false;
	}

	@Override
	public String toString() {
		return "(" + getPosition() + ", " + getValue() + ")";
	}
}
