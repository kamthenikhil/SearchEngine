package com.chinappa.search.engine.service;

import com.chinappa.search.engine.dto.SearchResults;

public class WebSearchService {

	public SearchResults search(String query){
		
		WebIndexSercher indexSearcher = new WebIndexSercher();
		SearchResults searchResults = indexSearcher.searchDocuments(query);
		return searchResults;
	}
}
