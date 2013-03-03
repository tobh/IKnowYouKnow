package ch.hauth.util.config;

public class NoSuchConfigKeyException extends RuntimeException {
	private static final long serialVersionUID = 832439795833874894L;

	public NoSuchConfigKeyException(final String key) {
		super("Could not find key: '" + key + "'");
	}
}
