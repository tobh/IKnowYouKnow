package ch.hauth.youknow.ri;

import ch.hauth.youknow.ri.content.IHaveTermStats;

public class TermScaler {
	public static float scalingFactor(final String term, final IHaveTermStats wordStats) {
		float localFactor = 0;
		int termFrequency = wordStats.getTermFrequency(term);
		if (termFrequency > 0) {
			localFactor = (float) (1 + Math.log(termFrequency));
		}
		int documentFrequency = wordStats.getDocumentFrequency(term);
		float globalFactor = 0;
		if (documentFrequency > 0) {
			globalFactor = (float) (Math.log(wordStats.getDocumentCount() / documentFrequency));
		}
		return localFactor * globalFactor;
	}
}
