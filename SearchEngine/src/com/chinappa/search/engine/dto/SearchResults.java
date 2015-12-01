package com.chinappa.search.engine.dto;

import java.util.ArrayList;
import java.util.List;

public class SearchResults {

	private long estimatedResultSetLength = 0;

	private List<SearchResult> searchResults = new ArrayList<SearchResult>();
	
	private String correctedQuery = null;

	public long getEstimatedResultSetLength() {
		return estimatedResultSetLength;
	}

	public void setEstimatedResultSetLength(long estimatedResultSetLength) {
		this.estimatedResultSetLength = estimatedResultSetLength;
	}

	public List<SearchResult> getSearchResults() {
		return searchResults;
	}

	public void setSearchResults(List<SearchResult> searchResults) {
		this.searchResults = searchResults;
	}

	public String getCorrectedQuery() {
		return correctedQuery;
	}

	public void setCorrectedQuery(String correctedQuery) {
		this.correctedQuery = correctedQuery;
	}
}
