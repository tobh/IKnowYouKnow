package ch.hauth.util.data;

import java.util.Iterator;

public abstract class ReadOnlyIterator<T> implements Iterator<T> {
	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
}
