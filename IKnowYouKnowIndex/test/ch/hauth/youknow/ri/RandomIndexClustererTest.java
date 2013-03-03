package ch.hauth.youknow.ri;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

import ch.hauth.youknow.math.vector.Vector;

public class RandomIndexClustererTest {
	@Test public void testGetClusterRoots() {
		RandomIndexClusterer clusterer = new RandomIndexClusterer(3);
		Map<Integer, Vector> roots = clusterer.getClusterRoots();
		assertEquals(3, roots.size());
		assertFalse(roots.get(1).equals(roots.get(2)));
		assertFalse(roots.get(1).equals(roots.get(3)));
		assertFalse(roots.get(2).equals(roots.get(3)));
	}
	
	@Test public void testGetClosestClusterId() {
		RandomIndexClusterer clusterer = new RandomIndexClusterer(3);
		Vector clusterVector = clusterer.getClusterRoots().get(1);
		assertEquals(1, clusterer.getClosestClusterId(clusterVector));
	}
}
