package com.chinappa.search.engine.model;

import com.chinappa.information.retrieval.dto.SearchResultDTO;

public class SearchResults {

	private SearchResultDTO[] searchResults;

	public SearchResultDTO[] getSearchResults() {
		return searchResults;
	}

	public void setSearchResults(SearchResultDTO[] searchResults) {
		this.searchResults = searchResults;
	}
}
