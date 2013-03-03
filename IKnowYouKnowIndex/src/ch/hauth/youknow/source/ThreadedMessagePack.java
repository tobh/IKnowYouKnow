package ch.hauth.youknow.source;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class ThreadedMessagePack {
	private final String id;
	private final List<ThreadedMessage> messagesSortedByDate;

	public ThreadedMessagePack(final String id, final Iterable<ThreadedMessage> threadedMessages) {
		this.id = id;

		List<ThreadedMessage> messages = new LinkedList<ThreadedMessage>();
		for (ThreadedMessage message : threadedMessages) {
			messages.add(message);
		}

		Comparator<ThreadedMessage> compareMessageDates = new Comparator<ThreadedMessage>() {
			@Override
			public int compare(ThreadedMessage o1, ThreadedMessage o2) {
				return o1.getDate().compareTo(o2.getDate());
			}
		};

		Collections.sort(messages, compareMessageDates);

		this.messagesSortedByDate = new ArrayList<ThreadedMessage>(messages);
	}

	public String getId() {
		return this.id;
	}

	public List<ThreadedMessage> getMessageTimeline() {
		return this.messagesSortedByDate;
	}

	public int size() {
		return this.messagesSortedByDate.size();
	}
}
