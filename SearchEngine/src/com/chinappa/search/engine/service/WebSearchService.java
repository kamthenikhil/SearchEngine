package com.chinappa.search.engine.service;

import com.chinappa.search.engine.model.SearchResult;

public class WebSearchService {

	public SearchResult[] search(String query){
		
		WebIndexSercher indexSearcher = new WebIndexSercher();
		SearchResult[] searchResults = indexSearcher.searchDocuments(query);
		return searchResults;
	}
}
