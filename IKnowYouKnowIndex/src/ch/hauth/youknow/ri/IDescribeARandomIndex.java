package ch.hauth.youknow.ri;

import ch.hauth.youknow.ri.content.IHaveContent;
import ch.hauth.youknow.ri.content.IHaveContentWithId;

public interface IDescribeARandomIndex {
	public String getWordContextSource();
	public String getDocumentSource();
	public Iterable<? extends IHaveContent> getContentsForWordContext();
	public Iterable<? extends IHaveContentWithId> getContentsForDocuments();
}
