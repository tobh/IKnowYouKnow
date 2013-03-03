package ch.hauth.youknow.source.newsgroup;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;

import org.apache.log4j.Logger;

import ch.hauth.util.data.ReadOnlyIterator;
import ch.hauth.youknow.source.Message;
import ch.hauth.youknow.source.ThreadedMessageStore;

public class MBoxReader implements Iterable<MBoxMessage> {
	private static final Logger LOGGER = Logger.getLogger(MBoxReader.class);

	private final BufferedReader reader;

	private MBoxReader(File mboxFile) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream("".getBytes())));
		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(mboxFile), MBoxMessage.DEFAULT_ENCODING));
		} catch (FileNotFoundException e) {
			LOGGER.debug("Couldn't find mbox file: " + mboxFile.getName());
		} catch (UnsupportedEncodingException e) {
			LOGGER.debug("Unsupported encoding:\n" + e);
		}
		this.reader = reader;
	}

	public static MBoxReader from(File mboxFile) {
		return new MBoxReader(mboxFile);
	}

	@Override
	public Iterator<MBoxMessage> iterator() {
		return new ReadOnlyIterator<MBoxMessage>() {
			private boolean hasFirstMessageChecked = false;
			private boolean hasNextMessage = false;

			@Override
			public boolean hasNext() {
				if (!this.hasFirstMessageChecked) {
					this.hasFirstMessageChecked = true;
					String firstLine;
					try {
						firstLine = MBoxReader.this.reader.readLine();
					} catch (IOException e) {
						this.hasNextMessage = false;
						LOGGER.debug("Couldn't read line:\n" + e);
						return false;
					}
					if (firstLine != null && firstLine.startsWith("From ")) {
						this.hasNextMessage = true;
					}
				}
				return this.hasNextMessage;
			}

			@Override
			public MBoxMessage next() {
				String header = "";
				String body = "";
				try {
					header = allLinesUntilEmptyLine();
					body = allBodyLines();
				} catch (IOException e) {
					LOGGER.debug("Couldn't read next message:\n" + e);
				}
				return new MBoxMessage(header, body);
			}

			private String allLinesUntilEmptyLine() throws IOException {
				// TODO: fix encoding
				StringBuilder sb = new StringBuilder();
				String line;
				while ((line = MBoxReader.this.reader.readLine()) != null) {
					if (line.equals("")) {
						break;
					}
					sb.append(line);
					sb.append("\n");
				}
				return sb.toString();
			}

			private String allBodyLines() throws IOException {
				this.hasNextMessage = false;
				StringBuilder sb = new StringBuilder();
				String line;
				while ((line = MBoxReader.this.reader.readLine()) != null) {
					if (line.startsWith("From ")) {
						this.hasNextMessage = true;
						break;
					}
					sb.append(line);
					sb.append("\n");
				}
				return sb.toString();
			}
		};
	}

	public static void main(String[] args) {
		ThreadedMessageStore store = new ThreadedMessageStore();
		int count = 0;
		for (MBoxMessage mboxMessage : MBoxReader.from(new File("/home/tobias/newsgroups/de.soc.wirtschaft"))) {
			Message message = mboxMessage.toMessage("de.soc.wirtschaft");
			System.out.println("MESSAGE_ID: " + message.getMessageId());
			System.out.println("NEW_CONTENT: " + message.getNewContent());
			for (Message storeMessage : store.getMessageById(message.getMessageId())) {
				System.out.println("NEW_CONTENT_FROM_STORE: " + storeMessage.getNewContent());
			}
			if (++count > 10) {
				break;
			}
		}
	}
}
