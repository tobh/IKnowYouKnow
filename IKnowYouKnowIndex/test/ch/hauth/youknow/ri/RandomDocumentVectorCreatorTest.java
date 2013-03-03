package ch.hauth.youknow.ri;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import ch.hauth.youknow.math.vector.Vector;

public class RandomDocumentVectorCreatorTest {
	@Test public void testCreate() {
		Vector vector = RandomDocumentVectorCreator.create();
		int minusOnes = 0;
		int plusOnes = 0;
		for (int i = 0; i < vector.getSize(); ++i) {
			float value = vector.get(i);
			if (value == -1.0f) {
				++minusOnes;
			} else if (value == 1.0f) {
				++plusOnes;
			} else if (value != 0.0f) {
				System.out.println(value);
			}
		}
		assertEquals(6, plusOnes);
		assertEquals(6, minusOnes);
	}
}
