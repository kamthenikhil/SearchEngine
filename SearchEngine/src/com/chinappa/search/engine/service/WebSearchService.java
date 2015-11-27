package com.chinappa.search.engine.service;

import java.util.List;

import com.chinappa.search.engine.dto.SearchResult;

public class WebSearchService {

	public List<SearchResult> search(String query){
		
		WebIndexSercher indexSearcher = new WebIndexSercher();
		List<SearchResult> searchResults = indexSearcher.searchDocuments(query);
		return searchResults;
	}
}
