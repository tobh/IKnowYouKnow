package ch.hauth.youknow.ri;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import ch.hauth.youknow.Config;
import ch.hauth.youknow.math.vector.Vector;
import ch.hauth.youknow.math.vector.VectorValue;

public class RandomDocumentVectorCreator {
	private static final Random RANDOM = new Random();
	private static final int DEFAULT_VECTOR_SIZE = Config.getInt("defaultVectorSize");
	private static final int NON_ZERO_COUNT = Config.getInt("randomVectorNonZeroCount");

	public static Vector create() {
		Set<Integer> positionSet = new TreeSet<Integer>();
		while (positionSet.size() < NON_ZERO_COUNT) {
			positionSet.add(RANDOM.nextInt(DEFAULT_VECTOR_SIZE));
		}

		List<Integer> positions = new ArrayList<Integer>(positionSet);
		Set<Integer> negativPositions = new HashSet<Integer>(NON_ZERO_COUNT / 2);
		while (negativPositions.size() < NON_ZERO_COUNT / 2) {
			negativPositions.add(positions.get(RANDOM.nextInt(positions.size())));
		}

		List<VectorValue> values = new ArrayList<VectorValue>(NON_ZERO_COUNT);
		for (Integer position : positionSet) {
			if (!negativPositions.contains(position)) {
				values.add(new VectorValue(position, 1.0f));
			} else {
				values.add(new VectorValue(position, -1.0f));
			}
		}
		return Vector.valueOf(values);
	}
}
