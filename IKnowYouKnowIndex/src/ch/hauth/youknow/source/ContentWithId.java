package ch.hauth.youknow.source;

import ch.hauth.youknow.ri.content.IHaveContentWithId;
import ch.hauth.youknow.source.terms.TermCollection;

public class ContentWithId extends Content implements IHaveContentWithId {
	private final String id;

	public ContentWithId(final String id, final String text, final TermCollection termCollection) {
		super(text, termCollection);
		this.id = id;
	}

	@Override
	public String getId() {
		return this.id;
	}
}
