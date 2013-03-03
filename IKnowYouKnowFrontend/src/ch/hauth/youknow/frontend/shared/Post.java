package ch.hauth.youknow.frontend.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

public class Post implements IsSerializable {
	private String message;

	public Post() {
		this("");
	}

	public Post(String message) {
		this.message = message;
	}

	public String getMessage() {
		return this.message;
	}
}
