package ch.hauth.util.config;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

public class Configuration {
	private static final Logger LOGGER = Logger.getLogger(Configuration.class);

	private static final Properties PROPERTIES = new Properties();
	private static final String HOME_DIR = System.getProperty("user.home");
	private static final String HOME_REGEXP = "(?i)^\\$HOME";
	private static final Pattern HOME_PATTERN = Pattern.compile(HOME_REGEXP);

	private final Map<String, String> defaultValues = new HashMap<String, String>();

	static {
		String configPathName = System.getenv("CONFIG_FILE");
		if (configPathName != null) {
			try {
				PROPERTIES.load(new FileInputStream(configPathName));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public int getInt(final String key) {
		return Integer.valueOf(get(key));
	}

	public String get(final String key) {
		String value = PROPERTIES.getProperty(key);
		if (value == null) {
			String defaultValue = this.defaultValues.get(key);
			if (defaultValue == null) {
				throw new NoSuchConfigKeyException(key);
			}
			value = defaultValue;
			LOGGER.debug("Used default value: " + key + " = " + value);
		} else {
			LOGGER.debug("Used configured value: " + key + " = " + value);
		}

		Matcher homeMatcher = HOME_PATTERN.matcher(value);
		if (homeMatcher.find()) {
			value = value.replaceFirst(HOME_REGEXP, HOME_DIR);
		}

		return value;
	}

	public void setDefault(final String key, final String defaultValue) {
		this.defaultValues.put(key, defaultValue);
	}

	public void setDefaultInt(final String key, final int defaultValue) {
		this.defaultValues.put(key, Integer.toString(defaultValue));
	}
}
