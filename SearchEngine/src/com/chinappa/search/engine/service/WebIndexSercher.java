package com.chinappa.search.engine.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermFreqVector;
import org.apache.lucene.index.TermPositionVector;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.jsoup.Jsoup;

import com.chinappa.information.retrieval.constant.CommonConstants;
import com.chinappa.information.retrieval.util.FileHandlerUtil;
import com.chinappa.search.engine.configuration.SearchEngineConfiguration;
import com.chinappa.search.engine.dto.SearchResult;
import com.chinappa.search.engine.dto.SearchResults;

public class WebIndexSercher {

	public SearchResults searchDocuments(String queryString) {

		SearchResults searchResults = null;
		List<SearchResult> searchResultList = null;
		IndexReader reader = null;
		IndexSearcher searcher = null;
		try {
			int hitsPerPage = 100;
			searchResults = new SearchResults();
			searchResultList = new ArrayList<SearchResult>();
			Directory index = FSDirectory
					.open(new File(SearchEngineConfiguration.getInstance()
							.getIndexLocation()));
			Query query = prepareQuery(queryString, getAnalyzer());
			TopScoreDocCollector collector = TopScoreDocCollector.create(
					hitsPerPage, true);

			reader = IndexReader.open(index);
			searcher = new IndexSearcher(reader);
			searcher.search(query, collector);
			searcher.setDefaultFieldSortScoring(true, false);

			ScoreDoc[] hits = collector.topDocs().scoreDocs;
			Map<String, ArrayList<Integer>> termPosMap = new HashMap<String, ArrayList<Integer>>();
			String[] queryTerms = queryString.split("\\s+");
			searchResults.setEstimatedResultSetLength(hits.length);
			// fetchEstimatedResultSetLength(queryTerms, reader)
			for (int i = 0; i < hits.length; ++i) {
				SearchResult searchResult = new SearchResult();
				int docId = hits[i].doc;

				Document document = searcher.doc(docId);
				String snippet = generateSnippet(reader, termPosMap,
						queryTerms, docId, document);
				searchResult.setSnippet(highlightSnippet(snippet, queryTerms));
				searchResult.setUrl(document.get(CommonConstants.INDEX_FIELD));
				searchResult
						.setTitle(document.get(CommonConstants.TITLE_FIELD));
				searchResultList.add(searchResult);
			}
			searchResults.setSearchResults(searchResultList);
		} catch (IOException e) {
			System.out.println(e.getMessage());
		} finally {
			if (searcher != null) {
				try {
					searcher.close();
				} catch (IOException e) {
				}
			}
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
				}
			}

		}
		return searchResults;
	}

	private String generateSnippet(IndexReader reader,
			Map<String, ArrayList<Integer>> termPosMap, String[] queryTerms,
			int docId, Document document) throws IOException {
		String htmlData = FileHandlerUtil
				.fetchFromCompressedHTMLFile(
						SearchEngineConfiguration.getInstance()
								.getDocumentDirectory(),
						SearchEngineConfiguration
								.getInstance()
								.getProperties()
								.getProperty(
										document.get(CommonConstants.INDEX_FIELD)));
		org.jsoup.nodes.Document doc = Jsoup.parse(htmlData);
		StringBuilder snippet = new StringBuilder();
		termPosMap = fetchTermWindowForField(reader, queryTerms, docId,
				CommonConstants.CONTENT_FIELD);
		int startIndex = fetchMinimumTermWindow(termPosMap, 20);
		String content = FileHandlerUtil.fetchDocumentText(doc);
		if (startIndex != Integer.MAX_VALUE) {
			String[] contentTerms = content.split("\\s+");
			int endIndex = contentTerms.length - 1;
			if (startIndex < startIndex - 20) {
				startIndex = startIndex - 20;
			} else {
				startIndex = 0;
			}
			if (endIndex > startIndex + 20) {
				endIndex = startIndex + 20;
			}
			snippet.append(CommonConstants.FULL_STOP
					+ CommonConstants.FULL_STOP);
			for (int j = startIndex; j < endIndex; j++) {
				snippet.append(contentTerms[j]);
				snippet.append(CommonConstants.SPACE);
			}
			snippet.append(CommonConstants.FULL_STOP
					+ CommonConstants.FULL_STOP);
			snippet.toString();
		}
		boolean containsSnippetContainQueryTerm = false;
		for (String queryterm : queryTerms) {
			if (snippet.toString().toLowerCase()
					.contains(queryterm.toLowerCase())) {
				containsSnippetContainQueryTerm = containsSnippetContainQueryTerm | true;
			}
		}
		if (!containsSnippetContainQueryTerm) {
			String description = FileHandlerUtil.fetchDocumentMetadata(doc,
					CommonConstants.HTML_META_DESCRIPTION);
			snippet = new StringBuilder(description);
		}
		return snippet.toString();
	}

	private int fetchMinimumTermWindow(
			Map<String, ArrayList<Integer>> termPosMap, int maxLength) {

		ArrayList<Integer> collatedTermPositions = new ArrayList<Integer>();

		for (String queryTerm : termPosMap.keySet()) {
			ArrayList<Integer> termPositions = termPosMap.get(queryTerm);
			if (termPositions != null && termPositions.size() > 0) {
				for (int position : termPositions) {
					collatedTermPositions.add(position);
				}
			}
		}
		int numberOfTerms = Integer.MIN_VALUE;
		int startIndex = Integer.MAX_VALUE;

		if (collatedTermPositions.size() > 0) {
			Collections.sort(collatedTermPositions);
			startIndex = collatedTermPositions.get(0);
			for (int i = 0; i < collatedTermPositions.size(); i++) {
				for (int j = i + 1; j < collatedTermPositions.size(); j++) {
					if (collatedTermPositions.get(j)
							- collatedTermPositions.get(i) < maxLength) {
						if (j - i + 1 > numberOfTerms) {
							numberOfTerms = j - i + 1;
							startIndex = collatedTermPositions.get(i);
						}
					}
				}
			}
		}
		return startIndex;
	}

	private Map<String, ArrayList<Integer>> fetchTermWindowForField(
			IndexReader reader, String[] queryTerms, int docId, String field)
			throws IOException {
		Map<String, ArrayList<Integer>> termPosMap = new HashMap<String, ArrayList<Integer>>();
		TermFreqVector tfvector = reader.getTermFreqVector(docId, field);
		TermPositionVector tpvector = (TermPositionVector) tfvector;
		// int[] windowTerms = null;
		if (tfvector != null && tpvector != null) {
			for (String queryTerm : queryTerms) {

				if (!termPosMap.containsKey(queryTerm)) {
					termPosMap.put(queryTerm, new ArrayList<Integer>());
				}
				int termidx = tfvector.indexOf(queryTerm);
				int[] termposx = tpvector.getTermPositions(termidx);
				// TermVectorOffsetInfo[] tvoffsetinfo = tpvector
				// .getOffsets(termidx);

				for (int j = 0; j < termposx.length; j++) {
					termPosMap.get(queryTerm).add(termposx[j]);
				}
				// for (int j = 0; j < tvoffsetinfo.length; j++) {
				// int offsetStart = tvoffsetinfo[j].getStartOffset();
				// int offsetEnd = tvoffsetinfo[j].getEndOffset();
				// System.out.println("offsets : " + offsetStart + " "
				// + offsetEnd);
				// }
			}
			// windowTerms =
			// SnippetGenerationUtil.fetchMinimumWindow(termPosMap);
		}
		return termPosMap;
	}

	private String highlightSnippet(String snippet, String[] queryTerms) {

		for (String queryTerm : queryTerms) {
			String capitalized = Character.toUpperCase(queryTerm.toLowerCase()
					.charAt(0)) + queryTerm.toLowerCase().substring(1);
			snippet = snippet.replaceAll(" " + queryTerm.toLowerCase() + "|"
					+ queryTerm.toLowerCase() + " ",
					" <b>" + queryTerm.toUpperCase() + "</b> ");
			snippet = snippet.replaceAll(" " + queryTerm.toUpperCase() + "|"
					+ queryTerm.toUpperCase() + " ",
					" <b>" + queryTerm.toUpperCase() + "</b> ");
			snippet = snippet.replaceAll(" " + capitalized + "|" + capitalized
					+ " ", " <b>" + capitalized + "</b> ");
		}
		return snippet;

	}

	private long fetchEstimatedResultSetLength(String[] queryterms,
			IndexReader reader) throws IOException {
		long totalNumberOfDocs = reader.numDocs();
		long resultSetLength = totalNumberOfDocs;

		for (String queryTerm : queryterms) {
			Term termInstance = new Term(CommonConstants.CONTENT_FIELD,
					queryTerm);
			double factor = reader.docFreq(termInstance) * resultSetLength
					/ totalNumberOfDocs;
			resultSetLength = (long) Math.floor(factor);
		}
		return resultSetLength;
	}

	private Query prepareQuery(String queryString, Analyzer analyzer) {
		Query query = null;
		try {
			String[] fields = new String[] { CommonConstants.TITLE_FIELD,
					CommonConstants.METADATA_FIELD,
					CommonConstants.ANCHOR_TEXT_FIELD,
					CommonConstants.CONTENT_FIELD };
			Map<String, Float> fieldBoost = new HashMap<String, Float>();
			fieldBoost.put(CommonConstants.TITLE_FIELD, 4f);
			fieldBoost.put(CommonConstants.METADATA_FIELD, 3f);
			fieldBoost.put(CommonConstants.ANCHOR_TEXT_FIELD, 2f);
			fieldBoost.put(CommonConstants.CONTENT_FIELD, 1f);
			query = new MultiFieldQueryParser(Version.LUCENE_36, fields,
					analyzer, fieldBoost).parse(queryString);
		} catch (ParseException e) {
		}
		return query;
	}

	private Analyzer getAnalyzer() {
		Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_36);
		return analyzer;
	}
}
