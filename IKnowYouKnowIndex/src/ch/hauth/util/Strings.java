package ch.hauth.util;

import java.util.List;

public class Strings {
	public static final String join(final List<String> tokens, final String joinString) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < tokens.size(); ++i) {
			sb.append(tokens.get(i));
			if (i < (tokens.size() - 1)) {
				sb.append(joinString);
			}
		}
		return sb.toString();
	}
}
