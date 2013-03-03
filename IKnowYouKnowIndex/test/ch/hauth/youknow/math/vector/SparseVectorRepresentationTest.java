package ch.hauth.youknow.math.vector;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class SparseVectorRepresentationTest {
	private SparseVectorRepresentation sparseVectorData1;
	private SparseVectorRepresentation sparseVectorData2;

	@Before
	public void setUp() {
		sparseVectorData1 = new SparseVectorRepresentation(new int[] {1, 3}, new float[] {1.0f, 3.0f});
		sparseVectorData2 = new SparseVectorRepresentation(new int[] {1, 2}, new float[] {3.0f, 2.0f});
	}

	@Test
	public void testAdd() {
		SparseVectorRepresentation expectedResult = new SparseVectorRepresentation(new int[] {1, 2, 3}, new float[] {7.0f, 4.0f, 3.0f});

		assertEquals(expectedResult, sparseVectorData1.add(sparseVectorData2, 2.0f));
	}
	
	@Test
	public void testGet() {
		assertEquals(1.0f, sparseVectorData1.getValueAtPosition(1), 0.00001);
		assertEquals(0.0f, sparseVectorData1.getValueAtPosition(2), 0.00001);
		assertEquals(3.0f, sparseVectorData1.getValueAtPosition(3), 0.00001);
	}
}
