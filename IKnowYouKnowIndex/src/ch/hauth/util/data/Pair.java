package ch.hauth.util.data;

public class Pair<T, E> {
	private final T first;
	private final E second;

	public Pair(final T first, final E second) {
		this.first = first;
		this.second = second;
	}

	public T getFirst() {
		return this.first;
	}

	public E getSecond() {
		return this.second;
	}
}
