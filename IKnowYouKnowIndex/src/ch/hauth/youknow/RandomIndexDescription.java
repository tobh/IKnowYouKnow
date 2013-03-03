package ch.hauth.youknow;

import ch.hauth.youknow.ri.IDescribeARandomIndex;
import ch.hauth.youknow.ri.content.IHaveContent;
import ch.hauth.youknow.ri.content.IHaveContentWithId;
import ch.hauth.youknow.source.ContentSource;

public class RandomIndexDescription implements IDescribeARandomIndex {
	private final ContentSource wordContextSource;
	private final ContentSource documentSource;

	public RandomIndexDescription(final ContentSource wordContextSource, final ContentSource documentSource) {
		this.wordContextSource = wordContextSource;
		this.documentSource = documentSource;
	}

	@Override
	public String getWordContextSource() {
		return this.wordContextSource.toString();
	}

	@Override
	public String getDocumentSource() {
		return this.documentSource.toString();
	}

	@Override
	public Iterable<? extends IHaveContent> getContentsForWordContext() {
		return this.wordContextSource.getContents();
	}

	@Override
	public Iterable<? extends IHaveContentWithId> getContentsForDocuments() {
		return this.documentSource.getContents();
	}
}
