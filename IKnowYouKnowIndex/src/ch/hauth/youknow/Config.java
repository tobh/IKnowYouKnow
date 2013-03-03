package ch.hauth.youknow;

import ch.hauth.util.config.Configuration;


public class Config {
	private static Configuration CONFIG;

	static {
		CONFIG = new Configuration();
		CONFIG.setDefault("messageStoreUrl", "chaos");
		CONFIG.setDefault("messageStoreDb", "newsgroups");
		CONFIG.setDefault("mysqlUser", "iknow");
		CONFIG.setDefault("mysqlPassword", "youknow");
		CONFIG.setDefault("messageTable", "Messages");
		CONFIG.setDefault("messagesWithoutThreadStarterTable", "MessagesWithoutThreadStarter");
		CONFIG.setDefaultInt("defaultVectorSize", 2500);
		CONFIG.setDefault("luceneIndexFile", "$HOME/newsgroups/index/");
		CONFIG.setDefault("randomIndexStoreUrl", "chaos");
		CONFIG.setDefault("randomIndexStoreDb", "newsgroups");
		CONFIG.setDefault("mysqlUser", "iknow");
		CONFIG.setDefault("mysqlPassword", "youknow");
		CONFIG.setDefault("wordContextTable", "WordContext");
		CONFIG.setDefaultInt("wordContextCacheSize", 100000);
		CONFIG.setDefault("randomIndexClusterMeansTable", "RandomIndexClusterMeans");
		CONFIG.setDefault("randomIndexClusterTablePrefix", "RandomIndexCluster");
		CONFIG.setDefaultInt("randomVectorNonZeroCount", 12);
		CONFIG.setDefaultInt("randomIndexClusterSize", 100);
		CONFIG.setDefaultInt("minDocumentFrequency", 10);
		CONFIG.setDefault("termCollectionTable", "TermCollection");
		CONFIG.setDefault("authorNewContentTable", "AuthorsNewContents");
		CONFIG.setDefault("authorWithoutThreadStarterNewContentTable", "AuthorsWithoutThreadStarterNewContents");
		CONFIG.setDefault("threadNewContentTable", "ThreadsNewContents");
		CONFIG.setDefault("threadWithoutThreadStarterNewContentTable", "ThreadsWithoutThreadStarterNewContents");
	}

	public static String get(final String key) {
		return CONFIG.get(key);
	}

	public static int getInt(final String key) {
		return CONFIG.getInt(key);
	}
}
