package ch.hauth.youknow.source;

import ch.hauth.youknow.ri.content.ICreateContents;
import ch.hauth.youknow.ri.content.IHaveContent;
import ch.hauth.youknow.source.terms.TermCollection;

public class ContentCreator implements ICreateContents {
	private final TermCollection termCollection;

	public ContentCreator(final TermCollection termCollection) {
		this.termCollection = termCollection;
	}

	@Override
	public IHaveContent from(String text) {
		return new Content(text, this.termCollection);
	}

}
