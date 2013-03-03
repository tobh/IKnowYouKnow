package ch.hauth.util.data;

public interface IConvertTypes<E, T> {
	public T convert(final E original);
}
