package ch.hauth.youknow.frontend.client;

import ch.hauth.youknow.frontend.shared.DemoResult;
import ch.hauth.youknow.frontend.shared.Identifier;
import ch.hauth.youknow.frontend.shared.Post;
import ch.hauth.youknow.frontend.shared.ResultInfo;
import ch.hauth.youknow.frontend.shared.Source;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * The async counterpart of <code>DemoService</code>.
 */
public interface DemoServiceAsync {
	void load(AsyncCallback<Void> callback) throws IllegalArgumentException;
	void processPost(Source source, Post post, AsyncCallback<DemoResult[]> callback) throws IllegalArgumentException;
	void search(Identifier identifier, AsyncCallback<ResultInfo[]> callback) throws IllegalArgumentException;
}
