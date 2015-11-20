package com.chinappa.search.engine.service;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import com.chinappa.search.engine.constant.SearchEngineConstants;
import com.chinappa.search.engine.model.SearchResult;

public class WebIndexSercher {

	public SearchResult[] searchDocuments(String queryString) {

		SearchResult[] searchResults = null;
		try {
			int hitsPerPage = 10;
			searchResults = new SearchResult[hitsPerPage];
			IndexSearcher searcher = new IndexSearcher(
					DirectoryReader.open(FSDirectory.open(new File(
							SearchEngineConstants.INDEX_DIRECTORY))));
			Query query = prepareQuery(queryString, getAnalyzer());
			TopScoreDocCollector collector = TopScoreDocCollector.create(
					hitsPerPage, true);
			searcher.search(query, collector);
			ScoreDoc[] hits = collector.topDocs().scoreDocs;
			System.out.println("Found " + hits.length + " hits.");
			for (int i = 0; i < hits.length; ++i) {
				SearchResult searchResult = new SearchResult();
				int docId = hits[i].doc;
				Document d = searcher.doc(docId);
				searchResult.setUrl(d.get(SearchEngineConstants.INDEX_FIELD));
				searchResult.setTitle(d.get(SearchEngineConstants.TITLE_FIELD));
				searchResults[i] = searchResult;
			}
		} catch (IOException e) {
		}
		return searchResults;
	}
	
	private Query prepareQuery(String queryString, Analyzer analyzer) {
		Query query = null;
		try {
			String[] fields = new String[] { SearchEngineConstants.TITLE_FIELD,
					SearchEngineConstants.METADATA_FIELD,
					SearchEngineConstants.CONTENT_FIELD };
			Map<String, Float> fieldBoost = new HashMap<String, Float>();
			fieldBoost.put(SearchEngineConstants.TITLE_FIELD, 4f);
			fieldBoost.put(SearchEngineConstants.METADATA_FIELD, 2f);
			fieldBoost.put(SearchEngineConstants.CONTENT_FIELD, 1f);
			query = new MultiFieldQueryParser(Version.LUCENE_41, fields,
					analyzer, fieldBoost).parse(queryString);
		} catch (ParseException e) {
		}
		return query;
	}
	
	private Analyzer getAnalyzer() {
		Analyzer analyzer = new StopAnalyzer(Version.LUCENE_41);
		return analyzer;
	}
}
