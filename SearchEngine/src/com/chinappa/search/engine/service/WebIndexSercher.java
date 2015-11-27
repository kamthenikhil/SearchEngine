package com.chinappa.search.engine.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.AtomicReader;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Terms;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.jsoup.Jsoup;

import com.chinappa.information.retrieval.util.FileHandlerUtil;
import com.chinappa.search.engine.configuration.SearchEngineConfiguration;
import com.chinappa.search.engine.constant.SearchEngineConstants;
import com.chinappa.search.engine.dto.SearchResult;

public class WebIndexSercher {

	public List<SearchResult> searchDocuments(String queryString) {

		List<SearchResult> searchResults = null;
		try {
			int hitsPerPage = 100000;
			searchResults = new ArrayList<SearchResult>();
			Directory index = FSDirectory
					.open(new File(SearchEngineConfiguration.getInstance()
							.getIndexLocation()));
			IndexSearcher searcher = new IndexSearcher(
					DirectoryReader.open(index));
			Query query = prepareQuery(queryString, getAnalyzer());
			TopScoreDocCollector collector = TopScoreDocCollector.create(
					hitsPerPage, true);
			searcher.search(query, collector);
			AtomicReader atomicReader = DirectoryReader.open(index).leaves()
					.get(0).reader();
			ScoreDoc[] hits = collector.topDocs().scoreDocs;
			System.out.println("Found " + hits.length + " hits.");
			Map<String, int[]> termPosMap = new HashMap<String, int[]>();
			for (int i = 0; i < hits.length; ++i) {
				SearchResult searchResult = new SearchResult();
				int docId = hits[i].doc;
				Document document = searcher.doc(docId);

				String url = document.get(SearchEngineConstants.INDEX_FIELD);

				searchResult.setUrl(url);

				searchResult.setSnippet(generateSnippet(url, docId));

				Object filenameObject = SearchEngineConfiguration.getInstance()
						.getProperties().get("url");
				if (filenameObject != null) {
					String filename = (String) filenameObject;
					org.jsoup.nodes.Document doc = Jsoup.parse(FileHandlerUtil
							.fetchFromCompressedHTMLFile(
									SearchEngineConfiguration.getInstance()
											.getDocumentDirectory(), filename));
					String content = FileHandlerUtil.fetchDocumentText(doc);
				}

				Terms titleTerms = atomicReader.getTermVector(docId,
						SearchEngineConstants.TITLE_FIELD);
				Terms metadataTerms = atomicReader.getTermVector(docId,
						SearchEngineConstants.METADATA_FIELD);
				Terms contentTerms = atomicReader.getTermVector(docId,
						SearchEngineConstants.CONTENT_FIELD);
				//
				// if (terms != null) {
				// TermsEnum termsEnum = terms.iterator(TermsEnum.EMPTY);
				// BytesRef term;
				// while ((term = termsEnum.next()) != null) {
				// String docTerm = term.utf8ToString();
				// if (queryString.contains(docTerm)) {
				// DocsAndPositionsEnum docPosEnum = termsEnum
				// .docsAndPositions(
				// atomicReader.getLiveDocs(), null,
				// DocsAndPositionsEnum.FLAG_OFFSETS);
				// docPosEnum.nextDoc();
				// int freq = docPosEnum.freq();
				// int[] posArray = new int[freq];
				// for (int j = 0; j < freq; j++) {
				// int position = docPosEnum.nextPosition();
				// posArray[j] = position;
				// }
				// termPosMap.put(docTerm, posArray);
				// }
				// }
				// }
				searchResult.setTitle(document
						.get(SearchEngineConstants.TITLE_FIELD));
				System.out.println(searchResult.getUrl());
				searchResults.add(searchResult);
			}
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
		return searchResults;
	}

	private String generateSnippet(String url, int docId) {

		return null;
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
