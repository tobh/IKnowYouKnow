package ch.hauth.youknow.frontend.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

public class ResultInfo implements IsSerializable {
	private String title;
	private String info;

	public ResultInfo() {
		this("", "");
	}

	public ResultInfo(String title, String info) {
		this.title = title;
		this.info = info;
	}

	public String getTitle() {
		return this.title;
	}

	public String getInfo() {
		return this.info;
	}
}
