package ch.hauth.util.data;

import java.util.LinkedHashMap;

public class LeastRecentlyUsedCache<K, V> extends LinkedHashMap<K, V> {
	private static final long serialVersionUID = 6172127353024749757L;

	private final int cacheSize;

	public LeastRecentlyUsedCache(final int cacheSize) {
		super((int) Math.ceil((cacheSize / 0.75) + 1), 0.75f, true);
		this.cacheSize = cacheSize;
 	}

	@Override
	protected boolean removeEldestEntry(java.util.Map.Entry<K, V> eldest) {
		return this.cacheSize < size();
	}
}
