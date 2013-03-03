package ch.hauth.util.data;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class Sequence {
	public static <In, Out> Iterable<Out> map(final Iterable<In> originals, final IConvertTypes<In, Out> converter) {
		return new Mapper<In, Out>(originals, converter);
	}

	public static <In, Out> Iterable<Out> flatmap(final Iterable<In> originals, final IConvertTypes<In, Iterable<Out>> converter) {
		return flatten(map(originals, converter));
	}

	public static <T> Iterable<T> flatten(final Iterable<Iterable<T>> nested) {
		return toIterable(new NestedIterator<T>(nested));
	}

	public static <T> Iterable<T> filter(final Iterable<T> originals, final IAmAPredicate<T> predicate) {
		return new Filter<T>(originals, predicate);
	}

	public static <T> List<T> toList(final Iterable<T> originals) {
		List<T> values = new LinkedList<T>();
		for (T value : originals) {
			values.add(value);
		}
		return values;
	}

	public static <T> Iterable<T> toIterable(final Iterator<T> iter) {
		return new Iterable<T>() {
			@Override
			public Iterator<T> iterator() {
				return iter;
			}
		};
	}

	public static <T> Iterable<T> emptyIterable() {
		Iterator<T> iter = emptyIterator();
		return toIterable(iter);
	}

	public static <T> Iterator<T> emptyIterator() {
		return new ReadOnlyIterator<T>() {
			@Override
			public boolean hasNext() {
				return false;
			}

			@Override
			public T next() {
				return null;
			}
		};
	}

	private static class Mapper<In, Out> implements Iterable<Out> {
		private final Iterable<In> originals;
		private final IConvertTypes<In, Out> converter;

		public Mapper(final Iterable<In> originals, final IConvertTypes<In, Out> converter) {
			this.originals = originals;
			this.converter = converter;
		}

		@Override
		public Iterator<Out> iterator() {
			return new MapperIterator(this.originals.iterator());
		}

		private class MapperIterator extends ReadOnlyIterator<Out> {
			private final Iterator<In> originalIter;

			public MapperIterator(final Iterator<In> originalIter) {
				this.originalIter = originalIter;
			}

			@Override
			public boolean hasNext() {
				return this.originalIter.hasNext();
			}

			@Override
			public Out next() {
				In original = this.originalIter.next();
				return Mapper.this.converter.convert(original);
			}
		}
	}

	private static class NestedIterator<T> extends ReadOnlyIterator<T> {
		Iterator<Iterable<T>> outerIterator;
		Iterator<T> innerIterator = null;

		public NestedIterator(final Iterable<Iterable<T>> outer) {
			this.outerIterator = outer.iterator();
		}

		@Override
		public boolean hasNext() {
			if (this.innerIterator == null || !this.innerIterator.hasNext()) {
				if (this.outerIterator.hasNext()) {
					this.innerIterator = this.outerIterator.next().iterator();
					return hasNext();
				}
				return false;
			}
			return this.innerIterator.hasNext();
		}

		@Override
		public T next() {
			return this.innerIterator.next();
		}
	}

	private static class Filter<T> implements Iterable<T> {
		private final Iterable<T> originals;
		private final IAmAPredicate<T> predicate;

		public Filter(final Iterable<T> originals, final IAmAPredicate<T> predicate) {
			this.originals = originals;
			this.predicate = predicate;
		}

		@Override
		public Iterator<T> iterator() {
			return new ReadOnlyIterator<T>() {
				private final Iterator<T> originalIter = Filter.this.originals.iterator();
				private boolean hasNext = false;
				private T nextValue = null;
				private boolean checkedNextValue = false;

				@Override
				public boolean hasNext() {
					if (!this.checkedNextValue) {
						if (this.originalIter.hasNext()) {
							T possibleNextValue = this.originalIter.next();
							if (Filter.this.predicate.check(possibleNextValue)) {
								this.nextValue = possibleNextValue;
								this.hasNext = true;
							} else {
								hasNext();
							}
						} else {
							this.hasNext = false;
						}
					}
					return this.hasNext;
				}

				@Override
				public T next() {
					this.checkedNextValue = false;
					return this.nextValue;
				}
			};
		}
	}
}
