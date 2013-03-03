package ch.hauth.youknow.source;

import java.util.Date;
import java.util.Set;


public class ThreadedMessage extends Message {
	private String threadId;

	public ThreadedMessage(final Message message, final String threadId) {
		this(message.getMessageOrigins(),
		     message.getMessageId(),
		     message.getAuthor(),
		     message.getTopic(),
		     message.getContent(),
		     message.getNewContent(),
		     message.getDate(),
		     message.getReferences(),
		     threadId);
	}

	public ThreadedMessage(Set<String> messageOrigins,
	           			   String messageId,
	           			   String author,
	           			   String topic,
	           			   String fullContent,
	           			   String newContent,
	           			   Date date,
	           			   Set<String> references,
	           			   String threadId) {
		super(messageOrigins, messageId, author, topic, fullContent, newContent, date, references);
		this.threadId = threadId;
	}

	public String getThreadId() {
		return this.threadId;
	}

	public void setThreadId(final String threadId) {
		this.threadId = threadId;
	}

	@Override
	public int hashCode() {
		return getMessageId().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ThreadedMessage) {
			ThreadedMessage other = (ThreadedMessage) obj;
			return other.getMessageId().equals(getMessageId());
		}
		return false;
	}

	@Override
	public String toString() {
		return super.toString() + "ThreadId: " + getThreadId() + "\n";
	}
}
