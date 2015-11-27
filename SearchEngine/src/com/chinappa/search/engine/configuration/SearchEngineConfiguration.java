package com.chinappa.search.engine.configuration;

import java.util.Properties;
import java.util.ResourceBundle;

import com.chinappa.information.retrieval.constant.CommonConstants;
import com.chinappa.information.retrieval.util.FileHandlerUtil;
import com.chinappa.search.engine.constant.SearchEngineConstants;

public class SearchEngineConfiguration {

	private static SearchEngineConfiguration uniqueInstance = null;
	/**
	 * The following attribute represents the location of index.
	 */
	private String indexLocation = null;
	/**
	 * The following attribute represents the location of crawled documents.
	 */
	private String documentDirectory = null;
	/**
	 * Stores URL to document file mappings.
	 */
	private Properties properties = null;
	
	private SearchEngineConfiguration(){
		init();
	}

	/**
	 * The following method returns the unique instance.
	 * 
	 * @return
	 */
	public static SearchEngineConfiguration getInstance() {
		if (uniqueInstance == null) {
			synchronized (SearchEngineConfiguration.class) {
				if (uniqueInstance == null) {
					uniqueInstance = new SearchEngineConfiguration();
				}
			}
		}
		return uniqueInstance;
	}

	/**
	 * The following method initializes the parameters required to run the
	 * crawler. The parameters are read from a property file stored in config
	 * folder. In case of a missing parameter or any errors while reading, the
	 * parameters are initialized with their default values.
	 */
	private void init() {
		ResourceBundle rb = ResourceBundle
				.getBundle(SearchEngineConstants.CONFIGURATION_FILENAME);
		String param = SearchEngineConstants.RB_INDEX_DIRECTORY;
		indexLocation = FileHandlerUtil.readStringFromResourceBundle(rb, param);
		param = SearchEngineConstants.RB_DOCUMENT_DIRECTORY;
		documentDirectory = FileHandlerUtil.readStringFromResourceBundle(rb,
				param);
		properties = FileHandlerUtil.readFromPropertiesFile(documentDirectory,
				CommonConstants.DEFAULT_MAPPINGS_FILENAME);
	}

	public String getIndexLocation() {
		return indexLocation;
	}

	public void setIndexLocation(String indexLocation) {
		this.indexLocation = indexLocation;
	}

	public String getDocumentDirectory() {
		return documentDirectory;
	}

	public void setDocumentDirectory(String documentDirectory) {
		this.documentDirectory = documentDirectory;
	}

	public Properties getProperties() {
		return properties;
	}

	public void setProperties(Properties properties) {
		this.properties = properties;
	}
}
