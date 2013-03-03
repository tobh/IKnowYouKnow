package ch.hauth.youknow.math.vector;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class VectorTest {
	private int vectorSize;
	private Vector vector1;
	private Vector vector2;

	@Before
	public void setUp() {
		vectorSize = 10;
		vector1 = Vector.valueOf(vectorSize, new VectorValue(1, 2.0f), new VectorValue(2, 4.0f));
		vector2 = Vector.valueOf(vectorSize, new VectorValue(2, 1.0f), new VectorValue(3, 3.0f), new VectorValue(4, 1.0f));
	}

	@Test
	public void testAdd() {
		Vector sumVector1Vector2 = Vector.valueOf(vectorSize, new VectorValue(1, 2.0f), new VectorValue(2, 5.0f), new VectorValue(3, 3.0f), new VectorValue(4, 1.0f));

		assertEquals(sumVector1Vector2, vector1.add(vector2));
		assertEquals(sumVector1Vector2, vector2.add(vector1));
	}

	@Test
	public void testAddWithScalingFactor() {
		Vector sumVector1Vector2Scaled = Vector.valueOf(vectorSize, new VectorValue(1, 2.0f), new VectorValue(2, 9.0f), new VectorValue(3, 15.0f), new VectorValue(4, 5.0f));
		Vector sumVector2Vector1Scaled = Vector.valueOf(vectorSize, new VectorValue(1, 10.0f), new VectorValue(2, 21.0f), new VectorValue(3, 3.0f), new VectorValue(4, 1.0f));

		assertEquals(sumVector1Vector2Scaled, vector1.add(vector2, 5));
		assertEquals(sumVector2Vector1Scaled, vector2.add(vector1, 5));
	}

	@Test
	public void testCrossProduct() {
		assertEquals(4, vector1.crossProduct(vector2), 0.0001);
		assertEquals(4, vector2.crossProduct(vector1), 0.0001);
	}
}
