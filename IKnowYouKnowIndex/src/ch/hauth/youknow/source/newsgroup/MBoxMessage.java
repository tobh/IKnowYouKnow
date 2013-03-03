package ch.hauth.youknow.source.newsgroup;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.MessagingException;
import javax.mail.internet.MimeUtility;

import org.apache.log4j.Logger;

import ch.hauth.youknow.source.Message;


public class MBoxMessage {
	private static final Logger LOGGER = Logger.getLogger(MBoxMessage.class);

	private final Map<String, String> headers;
	private final List<String> bodyLines;
	private static Pattern REFERENCE_PATTERN = Pattern.compile("<[^>]+>");
	private static Pattern CHARSET_PATTERN = Pattern.compile("(?i)charset=\"([^\"]+)\"");
	private static Pattern CHARSET_SECONDARY_PATTERN = Pattern.compile("(?i)charset=([^;]+)");
	private static Pattern GMT_PATTERN = Pattern.compile("(?i)gmt[-+]\\d\\d?$");
	private static Pattern FROM_PATTERN = Pattern.compile("^>+From ");
	private static Pattern LINE_STARTS_WITH_WHITESPACE = Pattern.compile("^\\s");

	public static final String DEFAULT_ENCODING = "ISO8859_1";
	private static final Map<String, String> ENCODINGS = new HashMap<String, String>();

	static {
		ENCODINGS.put("", "ISO8859_1");
		ENCODINGS.put("utf-7", "UTF8"); // no utf-7 support in java.io
		ENCODINGS.put("utf8", "UTF8");
		ENCODINGS.put("utf-8", "UTF8");
		ENCODINGS.put("utf-16", "UTF-16");
		ENCODINGS.put("us-ascii", "ASCII");
		ENCODINGS.put("iso-8859-1", "ISO8859_1");
		ENCODINGS.put("iso-8859-2", "ISO8859_2");
		ENCODINGS.put("iso-8859-3", "ISO8859_3");
		ENCODINGS.put("iso-8859-4", "ISO8859_4");
		ENCODINGS.put("iso-8859-5", "ISO8859_5");
		ENCODINGS.put("iso-8859-6", "ISO8859_6");
		ENCODINGS.put("iso-8859-7", "ISO8859_7");
		ENCODINGS.put("iso-8859-8", "ISO8859_8");
		ENCODINGS.put("iso-8859-9", "ISO8859_9");
		ENCODINGS.put("iso-8859-10", "ISO8859_10");
		ENCODINGS.put("iso-8859-11", "ISO8859_11");
		ENCODINGS.put("iso-8859-12", "ISO8859_12");
		ENCODINGS.put("iso-8859-13", "ISO8859_13");
		ENCODINGS.put("iso-8859-15", "ISO8859_15");
		ENCODINGS.put("iso 8859-1", "ISO8859_1");
		ENCODINGS.put("iso 8859-2", "ISO8859_2");
		ENCODINGS.put("iso 8859-3", "ISO8859_3");
		ENCODINGS.put("iso 8859-4", "ISO8859_4");
		ENCODINGS.put("iso 8859-5", "ISO8859_5");
		ENCODINGS.put("iso 8859-6", "ISO8859_6");
		ENCODINGS.put("iso 8859-7", "ISO8859_7");
		ENCODINGS.put("iso 8859-8", "ISO8859_8");
		ENCODINGS.put("iso 8859-9", "ISO8859_9");
		ENCODINGS.put("iso 8859-10", "ISO8859_10");
		ENCODINGS.put("iso 8859-11", "ISO8859_11");
		ENCODINGS.put("iso 8859-12", "ISO8859_12");
		ENCODINGS.put("iso 8859-13", "ISO8859_13");
		ENCODINGS.put("iso 8859-15", "ISO8859_15");
		ENCODINGS.put("iso8859-1", "ISO8859_1");
		ENCODINGS.put("iso8859-2", "ISO8859_2");
		ENCODINGS.put("iso8859-3", "ISO8859_3");
		ENCODINGS.put("iso8859-4", "ISO8859_4");
		ENCODINGS.put("iso8859-5", "ISO8859_5");
		ENCODINGS.put("iso8859-6", "ISO8859_6");
		ENCODINGS.put("iso8859-7", "ISO8859_7");
		ENCODINGS.put("iso8859-8", "ISO8859_8");
		ENCODINGS.put("iso8859-9", "ISO8859_9");
		ENCODINGS.put("iso8859-10", "ISO8859_10");
		ENCODINGS.put("iso8859-11", "ISO8859_11");
		ENCODINGS.put("iso8859-12", "ISO8859_12");
		ENCODINGS.put("iso8859-13", "ISO8859_13");
		ENCODINGS.put("iso8859-15", "ISO8859_15");
		ENCODINGS.put("iso8859_1", "ISO8859_1");
		ENCODINGS.put("iso8859_2", "ISO8859_2");
		ENCODINGS.put("iso8859_3", "ISO8859_3");
		ENCODINGS.put("iso8859_4", "ISO8859_4");
		ENCODINGS.put("iso8859_5", "ISO8859_5");
		ENCODINGS.put("iso8859_6", "ISO8859_6");
		ENCODINGS.put("iso8859_7", "ISO8859_7");
		ENCODINGS.put("iso8859_8", "ISO8859_8");
		ENCODINGS.put("iso8859_9", "ISO8859_9");
		ENCODINGS.put("iso8859_10", "ISO8859_10");
		ENCODINGS.put("iso8859_11", "ISO8859_11");
		ENCODINGS.put("iso8859_12", "ISO8859_12");
		ENCODINGS.put("iso8859_13", "ISO8859_13");
		ENCODINGS.put("iso8859_15", "ISO8859_15");
		ENCODINGS.put("windows-1250", "Cp1250");
		ENCODINGS.put("windows-1251", "Cp1251");
		ENCODINGS.put("windows-1252", "Cp1252");
		ENCODINGS.put("windows-1253", "Cp1253");
		ENCODINGS.put("windows-1254", "Cp1254");
		ENCODINGS.put("windows-1255", "Cp1255");
		ENCODINGS.put("windows-1256", "Cp1256");
		ENCODINGS.put("windows-1257", "Cp1257");
		ENCODINGS.put("ibm437", "IBM437");
		ENCODINGS.put("koi8_r", "KOI8_R");
		ENCODINGS.put("koi8_u", "KOI8_U");
		ENCODINGS.put("koi8-r", "KOI8_R");
		ENCODINGS.put("koi8-u", "KOI8_U");
		ENCODINGS.put("iso-2022-cn", "ISO2022CN");
		ENCODINGS.put("iso-2022-jp", "ISO2022JP");
		ENCODINGS.put("iso-2022-kr", "ISO2022KR");
		ENCODINGS.put("gb18030", "GB18030");
		ENCODINGS.put("gb2312", "GB2312");
		ENCODINGS.put("gbk", "GBK");
		ENCODINGS.put("big5", "Big5");
	}

	private static final SimpleDateFormat rfc822DateFormats[] = new SimpleDateFormat[] {
		new SimpleDateFormat("EEE, d MMM yy HH:mm:ss z"),
		new SimpleDateFormat("EEE, d MMM yy HH:mm z"),
		new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z"),
		new SimpleDateFormat("EEE, d MMM yyyy HH:mm z"),
		new SimpleDateFormat("d MMM yy HH:mm z"),
		new SimpleDateFormat("d MMM yy HH:mm:ss z"),
		new SimpleDateFormat("d MMM yyyy HH:mm z"),
		new SimpleDateFormat("d MMM yyyy HH:mm:ss z"),

		// ignore timezone if everything else failed
		new SimpleDateFormat("EEE, d MMM yy HH:mm:ss"),
		new SimpleDateFormat("EEE, d MMM yy HH:mm"),
		new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss"),
		new SimpleDateFormat("EEE, d MMM yyyy HH:mm"),
		new SimpleDateFormat("d MMM yy HH:mm"),
		new SimpleDateFormat("d MMM yy HH:mm:ss"),
		new SimpleDateFormat("d MMM yyyy HH:mm"),
		new SimpleDateFormat("d MMM yyyy HH:mm:ss")
	};

	public MBoxMessage(String header, String body) {
		this.headers = extractHeaders(header);
		this.bodyLines = fixFroms(fixEncoding(body));
	}

	public Map<String, String> getHeaders() {
		return this.headers;
	}

	private String getContentEncoding() {
		String encoding = DEFAULT_ENCODING;
		String contentType = this.headers.get("content-type");
		if (contentType == null) {
			return encoding;
		}

		Matcher charsetMatcher = CHARSET_PATTERN.matcher(contentType);
		if (charsetMatcher.find()) {
			encoding = ENCODINGS.get(charsetMatcher.group(1).toLowerCase());
			if (encoding == null) {
				if (charsetMatcher.group(1).equals("")) {
					LOGGER.debug("No primary matches: " + contentType);
				}
				encoding = DEFAULT_ENCODING;
			}
		} else {
			charsetMatcher = CHARSET_SECONDARY_PATTERN.matcher(contentType);
			if (charsetMatcher.find()) {
				encoding = ENCODINGS.get(charsetMatcher.group(1).toLowerCase());
				if (encoding == null) {
					if (charsetMatcher.group(1).equals("")) {
						LOGGER.debug("No secondary matches: " + contentType);
					}
					encoding = DEFAULT_ENCODING;
				}
			}
		}
		return encoding;
	}

	private Map<String, String> extractHeaders(String header) {
		String[] headerLines = header.split("\n");
		List<String> headers = new ArrayList<String>(headerLines.length);
		String currentHeader = "";
		for (String headerLine : headerLines) {
			String line;
			try {
				line = MimeUtility.decodeText(headerLine);
			} catch (UnsupportedEncodingException e) {
				line = headerLine;
			}
			if (LINE_STARTS_WITH_WHITESPACE.matcher(line).lookingAt() || !line.contains(":")) {
				currentHeader += " " + line.trim();
			} else {
				if (!currentHeader.equals("")) {
					headers.add(currentHeader);
				}
				currentHeader = line.trim();
			}
		}

		Map<String, String> headerMap = new HashMap<String, String>();
		for (String headerLine : headers) {
			int splitPoint = headerLine.indexOf(":");
			if (splitPoint == -1) {
				LOGGER.debug("Problem with headerline: " + headerLine);
			}
			String key = headerLine.substring(0, splitPoint).toLowerCase().trim();
			String value = headerLine.substring(splitPoint + 1).trim();
			headerMap.put(key, value);
		}
		return Collections.unmodifiableMap(headerMap);
	}

	private String fixEncoding(String original) {
		String transferEncoding = this.headers.get("content-transfer-encoding");
		// ugly hack to handle 8bit encoding
		if (transferEncoding == null || transferEncoding.equals("8bit")) {
			return original;
		}

		try {
			StringWriter writer = new StringWriter();
			InputStream is = MimeUtility.decode(new ByteArrayInputStream(original.getBytes()), transferEncoding);
			BufferedReader reader = new BufferedReader(new InputStreamReader(is, getContentEncoding()));
			char[] buffer = new char[1024];
			int n;
			while ((n = reader.read(buffer)) != -1) {
				writer.write(buffer, 0, n);
			}
			return writer.toString();
		} catch (MessagingException e) {
		} catch (UnsupportedEncodingException e) {
		} catch (IOException e) {
		}

		return original;
	}

	private List<String> fixFroms(final String body) {
		List<String> lines = new LinkedList<String>();
		Scanner scanner = new Scanner(body);
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			if (FROM_PATTERN.matcher(line).lookingAt()) {
				line = line.substring(1);
			}
			lines.add(line);
		}
		return lines;
	}

	private void constructContents(final StringBuilder contentBuilder, final StringBuilder newContentBuilder) {
		for (String line : this.bodyLines) {
			contentBuilder.append(line);
			contentBuilder.append("\n");
			if (!line.startsWith(">")) {
				newContentBuilder.append(line);
				newContentBuilder.append("\n");
			}
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		for (String key : getHeaders().keySet()) {
			sb.append(key + ": ");
			sb.append(getHeaders().get(key) + "\n");
		}
		sb.append("\n");
		for (String line : this.bodyLines) {
			sb.append(line);
			sb.append("\n");
		}
		return sb.toString();
	}

	public Message toMessage(String messageOrigin) {
		String messageId = this.headers.get(Newsgroups.MESSAGE_ID);
		String author = this.headers.get(Newsgroups.AUTHOR);
		String topic = this.headers.get(Newsgroups.TOPIC);

		StringBuilder contentBuilder = new StringBuilder();
		StringBuilder newContentBuilder = new StringBuilder();
		constructContents(contentBuilder, newContentBuilder);
		String content = contentBuilder.toString();
		String newContent = newContentBuilder.toString();

		String date = this.headers.get(Newsgroups.DATE);
		if (messageOrigin == null ||
			messageId == null ||
			author == null ||
			topic == null ||
			content == null ||
			date == null) {
			return null;
		}

		Set<String> messageOrigins = new HashSet<String>();
		messageOrigins.add(messageOrigin);
		messageOrigins.addAll(Arrays.asList(this.headers.get(Newsgroups.MESSAGE_NEWSGROUPS).split(",")));

		Date messageDate = stringToDate(date);
		if (messageDate == null) {
			return null;
		}

		String references = this.headers.get(Newsgroups.REFERENCES);
		if (references == null) {
			references = "";
		}

		return new Message(messageOrigins,
				           messageId,
				           author,
				           topic,
				           content,
				           newContent,
				           messageDate,
				           splitReferenceString(references));
	}

	private Date stringToDate(String dateString) {
		if (GMT_PATTERN.matcher(dateString).find()) {
			dateString += ":00";
		}

		Date date = null;
		for (SimpleDateFormat format : rfc822DateFormats) {
			try {
				date = format.parse(dateString);
				break;
			} catch (ParseException e) {
				// do nothing
			}
		}
		if (date == null) {
			LOGGER.debug("Couldn't parse '" + dateString + "'");
		}
		return date;
	}

	private Set<String> splitReferenceString(String references) {
		Matcher referenceMatcher = REFERENCE_PATTERN.matcher(references);
		Set<String> uniqueReferences = new HashSet<String>();
		while (referenceMatcher.find()) {
			uniqueReferences.add(referenceMatcher.group());
		}
		return uniqueReferences;
	}
}
