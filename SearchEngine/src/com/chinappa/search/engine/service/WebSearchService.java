package com.chinappa.search.engine.service;

import com.chinappa.information.retrieval.Indexer.WebIndexer;
import com.chinappa.search.engine.model.SearchResults;

public class WebSearchService {

	public SearchResults search(String query){
		
		WebIndexer indexer = new WebIndexer();
		SearchResults results = new SearchResults();
		results.setSearchResults(indexer.searchDocuments(query));
		return results;
	}
}
