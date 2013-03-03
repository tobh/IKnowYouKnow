package ch.hauth.youknow.frontend.client;

import ch.hauth.youknow.frontend.shared.DemoResult;
import ch.hauth.youknow.frontend.shared.Identifier;
import ch.hauth.youknow.frontend.shared.Post;
import ch.hauth.youknow.frontend.shared.ResultInfo;
import ch.hauth.youknow.frontend.shared.Source;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * The client side stub for the RPC service.
 */
@RemoteServiceRelativePath("demo")
public interface DemoService extends RemoteService {
	void load() throws IllegalArgumentException;
	DemoResult[] processPost(Source source, Post post) throws IllegalArgumentException;
	ResultInfo[] search(Identifier identifier) throws IllegalArgumentException;
}
