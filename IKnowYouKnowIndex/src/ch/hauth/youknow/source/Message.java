package ch.hauth.youknow.source;

import java.util.Date;
import java.util.Set;


public class Message {
	private final Set<String> messageOrigins;
	private final String messageId;
	private final String author;
	private final String topic;
	private final String content;
	private final String newContent;
	private final Date date;
	private final Set<String> references;

	public Message(Set<String> messageOrigins,
		           String messageId,
			       String author,
			       String topic,
			       String fullContent,
			       String newContent,
			       Date date,
			       Set<String> references) {
		this.messageOrigins = messageOrigins;
		this.messageId = messageId;
		this.author = author;
		this.topic = topic;
		this.content = fullContent;
		this.newContent = newContent;
		this.date = date;
		this.references = references;
		this.references.remove("");
	}

	public Set<String> getMessageOrigins() {
		return this.messageOrigins;
	}

	public String getMessageId() {
		return this.messageId;
	}

	public String getAuthor() {
		return this.author;
	}

	public String getTopic() {
		return this.topic;
	}

	public String getContent() {
		return this.content;
	}

	public String getNewContent() {
		return this.newContent;
	}

	public Date getDate() {
		return this.date;
	}

	public Set<String> getReferences() {
		return this.references;
	}

	@Override
	public int hashCode() {
		return getMessageId().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Message) {
			Message other = (Message) obj;
			return other.getMessageId().equals(getMessageId());
		}
		return false;
	}

	@Override
	public String toString() {
		return "MessageOrigin: " + getMessageOrigins() + "\n" +
			   "MessageId:" + getMessageId() + "\n" +
			   "Author: " + getAuthor() + "\n" +
			   "Topic: " + getTopic() + "\n" +
			   "Content: " + getContent() + "\n" +
			   "Date: " + getDate() + "\n" +
			   "References: " + getReferences() + "\n";
	}
}
