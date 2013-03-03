package ch.hauth.youknow.frontend.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

public class DemoResult implements IsSerializable {
	private Identifier identifier;
	private double score;

	public DemoResult() {
		this(new Identifier(Source.AUTHORS_WITHOUT_THREAD_STARTERS, ""), 1.0);
	}

	public DemoResult(Identifier identifier, double score) {
		super();
		this.identifier = identifier;
		this.score = score;
	}

	public Identifier getIdentifier() {
		return this.identifier;
	}

	public double getScore() {
		return score;
	}
}
