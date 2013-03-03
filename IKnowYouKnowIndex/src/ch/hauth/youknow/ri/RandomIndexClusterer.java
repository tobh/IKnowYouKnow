package ch.hauth.youknow.ri;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import ch.hauth.youknow.Config;
import ch.hauth.youknow.math.vector.Vector;
import ch.hauth.youknow.math.vector.VectorValue;

public class RandomIndexClusterer {
	private static final short DEFAULT_VECTOR_SIZE = (short) Config.getInt("defaultVectorSize");

	private Map<Integer, Cluster> clusters;

	public RandomIndexClusterer(int clusterCount) {
		this.clusters = new HashMap<Integer, Cluster>();
		for (int i = 0; i < clusterCount; i++) {
			this.clusters.put(i, new Cluster(i, clusterCount));
		}
	}

	public void add(Vector vector) {
		Cluster closestCluster = null;
		double minDistance = Double.MAX_VALUE;
		for (Cluster cluster : clusters.values()) {
			double distance = cluster.weightedDistanceTo(vector);
			if (distance < minDistance) {
				minDistance = distance;
				closestCluster = cluster;
			}
		}
		closestCluster.update(vector);
	}

	public int getClosestClusterId(Vector vector) {
		int closestCluster = -1;
		double minDistance = Double.MAX_VALUE;
		for (Entry<Integer, Cluster> entry : clusters.entrySet()) {
			double distance = entry.getValue().distanceTo(vector);
			if (distance < minDistance) {
				minDistance = distance;
				closestCluster = entry.getKey();
			}
		}
		return closestCluster;
	}

	public Map<Integer, Vector> getClusterRoots() {
		Map<Integer, Vector> roots = new HashMap<Integer, Vector>();
		for (Entry<Integer, Cluster> entry : clusters.entrySet()) {
			roots.put(entry.getKey(), entry.getValue().getMean());
		}
		return roots;
	}

	private static class Cluster {
		private Vector mean;
		private int count;

		public Cluster(int offset, int mod) {
			List<VectorValue> values = new ArrayList<VectorValue>();
			for (int i = offset; i < DEFAULT_VECTOR_SIZE; i += mod) {
				values.add(new VectorValue((short) i, (float) (0.1f * Math.pow(-1.0f, (i / mod) % 2))));
			}
			this.mean = Vector.valueOf(values);
			this.count = 0;
		}

		public void update(Vector vector) {
			++this.count;
			this.mean.add(vector, 1.0f / this.count);
		}

		public double weightedDistanceTo(Vector vector) {
			return distanceTo(vector) / Math.log(this.count + 2);
		}

		private double distanceTo(Vector vector) {
			return this.mean.add(vector, -1).abs();
		}

		public Vector getMean() {
			return this.mean;
		}
	}
}
