package ch.hauth.youknow.source.terms;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;


public class TermExtractor {
	private static final Pattern WORD_PATTERN = Pattern.compile("^[a-zäüöß]{1,30}$");
	private static final String LAST_CHARACTERS_TO_REMOVE = ".,;:?!";

	public List<String> from(final String text) {
		List<String> terms = new LinkedList<String>();
		for (String possibleTerm : text.split("\\s")) {
			if (possibleTerm.isEmpty()) {
				continue;
			}

			if (LAST_CHARACTERS_TO_REMOVE.contains(possibleTerm.substring(possibleTerm.length() - 1, possibleTerm.length()))) {
				possibleTerm = possibleTerm.substring(0, possibleTerm.length() - 1);
			}

			if (WORD_PATTERN.matcher(possibleTerm).find()) {
				terms.add(possibleTerm);
			}
		}
		return terms;
	}
}
