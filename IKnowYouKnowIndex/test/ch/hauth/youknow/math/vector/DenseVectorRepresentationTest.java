package ch.hauth.youknow.math.vector;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class DenseVectorRepresentationTest {
	private DenseVectorRepresentation denseVectorData1;
	private DenseVectorRepresentation denseVectorData2;

	@Before
	public void setUp() {
		denseVectorData1 = new DenseVectorRepresentation(new float[] {1.0f, 0.0f, 3.0f});
		denseVectorData2 = new DenseVectorRepresentation(new float[] {3.0f, 2.0f, 0.0f});
	}

	@Test
	public void testAdd() {
		DenseVectorRepresentation expectedResult = new DenseVectorRepresentation(new float[] {7.0f, 4.0f, 3.0f});

		assertEquals(expectedResult, denseVectorData1.add(denseVectorData2, 2.0f));
	}
	
	@Test
	public void testGet() {
		assertEquals(1.0f, denseVectorData1.getValueAtPosition(0), 0.00001);
		assertEquals(0.0f, denseVectorData1.getValueAtPosition(1), 0.00001);
		assertEquals(3.0f, denseVectorData1.getValueAtPosition(2), 0.00001);
	}
}
