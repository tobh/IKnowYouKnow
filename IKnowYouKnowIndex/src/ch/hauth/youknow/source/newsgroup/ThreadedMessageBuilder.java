package ch.hauth.youknow.source.newsgroup;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ch.hauth.youknow.source.Message;
import ch.hauth.youknow.source.ThreadedMessage;

public class ThreadedMessageBuilder {
	private final Map<String, String> messagesWithThreadId = new HashMap<String, String>();
	private final Set<ThreadedMessage> threadedMessages = new HashSet<ThreadedMessage>();
	private final Map<String, String> threadIdMappings = new HashMap<String, String>();
	private final Map<String, Message> messageById = new HashMap<String, Message>();
	private final Map<String, Set<String>> messageToReferences = new HashMap<String, Set<String>>();

	public Set<ThreadedMessage> from(List<Message> messages) {
		for (Message message : messages) {
			this.messageById.put(message.getMessageId(), message);
			String messageId = message.getMessageId();
			Set<String> references = message.getReferences();
			addReferences(this.messageToReferences, messageId, references);
			for (String referenceId : references) {
				String[] backReference = {messageId};
				addReferences(this.messageToReferences, referenceId, Arrays.asList(backReference));
			}
		}

		while (!this.messageToReferences.isEmpty()) {
			Set<String> thread = new HashSet<String>();
			LinkedList<String> ids = new LinkedList<String>();
			String messageId = this.messageToReferences.keySet().iterator().next();
			String threadId = messageId;
			ids.add(messageId);
			thread.add(messageId);
			while (!ids.isEmpty()) {
				messageId = ids.pop();
				for (String referenceId : this.messageToReferences.get(messageId)) {
					if (!thread.contains(referenceId)) {
						ids.add(referenceId);
						thread.add(referenceId);
					}
				}
			}

			for (String id : thread) {
				Message message = this.messageById.get(id);
				if (message != null) {
					this.threadedMessages.add(new ThreadedMessage(message, threadId));
				}
			}

			for (String id : thread) {
				this.messageToReferences.remove(id);
			}
		}
		this.messageById.clear();
		this.messageToReferences.clear();

		Set<ThreadedMessage> onlyNewMessages = new HashSet<ThreadedMessage>();
		for (ThreadedMessage message : this.threadedMessages) {
			String messageId = message.getMessageId();
			String messageThreadId = message.getThreadId();
			String threadId = this.messagesWithThreadId.get(messageId);
			if (threadId == null) {
				onlyNewMessages.add(message);
			} else {
				this.threadIdMappings.put(messageThreadId, threadId);
			}
		}
		this.threadedMessages.clear();

		for (ThreadedMessage message : onlyNewMessages) {
			String messageThreadId = message.getThreadId();
			String threadId = this.threadIdMappings.get(messageThreadId);
			if (threadId == null) {
				threadId = messageThreadId;
			}
			message.setThreadId(threadId);
			this.messagesWithThreadId.put(message.getMessageId().intern(), threadId.intern());
		}
		this.threadIdMappings.clear();

		return onlyNewMessages;
	}

	private void addReferences(Map<String, Set<String>> messageToReferences, String messageId, Collection<String> newReferences) {
		Set<String> references = messageToReferences.get(messageId);
		if (references == null) {
			references = new HashSet<String>();
			messageToReferences.put(messageId, references);
		}
		references.addAll(newReferences);
	}
}
