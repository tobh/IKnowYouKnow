package ch.hauth.youknow.frontend.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

public class Identifier implements IsSerializable {
	private Source source;
	private String id;

	public Identifier() {
		this(Source.AUTHORS_WITHOUT_THREAD_STARTERS, "");
	}

	public Identifier(Source source, String id) {
		this.source = source;
		this.id = id;
	}

	public Source getSource() {
		return this.source;
	}

	public String getId() {
		return this.id;
	}
}
