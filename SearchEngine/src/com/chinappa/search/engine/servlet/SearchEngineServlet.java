package com.chinappa.search.engine.servlet;

import java.io.IOException;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.chinappa.search.engine.configuration.SearchEngineConfiguration;
import com.chinappa.search.engine.dto.SearchResult;
import com.chinappa.search.engine.dto.SearchResults;
import com.chinappa.search.engine.service.WebSearchService;

/**
 * Servlet implementation class SearchEngineServlet
 */
@WebServlet("/search")
public class SearchEngineServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public SearchEngineServlet() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see Servlet#init(ServletConfig)
	 */
	public void init(ServletConfig config) throws ServletException {

		SearchEngineConfiguration.getInstance();
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		int pageNumber = 1;
		int recordsPerPage = 10;
		if (request.getParameter("pageNumber") != null) {
			pageNumber = Integer.parseInt(request.getParameter("pageNumber"));
		}
		String query = request.getParameter("query");
		List<SearchResult> searchResultList = null;
		// if (request.getSession().getAttribute("query") != null
		// && query.equals((String) request.getSession().getAttribute(
		// "query"))) {
		// searchResultList = (List<SearchResult>) request.getSession()
		// .getAttribute("searchResults");
		// } else {
		WebSearchService searchService = new WebSearchService();
		SearchResults searchResults = searchService.search(query);
		searchResultList = searchResults.getSearchResults();
		request.getSession().setAttribute("searchResults", searchResultList);
		request.setAttribute("resultSetLength",
				searchResults.getEstimatedResultSetLength());

		int numberOfRecords = searchResultList.size();
		int numberOfPages = (int) Math.ceil(numberOfRecords * 1.0
				/ recordsPerPage);

		List<SearchResult> filteredRecords = fetchCurrentPageRecords(
				searchResultList, pageNumber, recordsPerPage);
		request.setAttribute("pageNumber", pageNumber);
		request.setAttribute("numberOfPages", numberOfPages);
		request.setAttribute("numberOfRecords", numberOfRecords);
		request.setAttribute("filteredRecords", filteredRecords);
		request.setAttribute("query", query);

		request.getSession().setAttribute("query", query);

		RequestDispatcher dispatcher = request
				.getRequestDispatcher("SearchEngine.jsp");
		dispatcher.forward(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

	public List<SearchResult> fetchCurrentPageRecords(
			List<SearchResult> totalSearchResults, int currentPageNo,
			int recordsPerPage) {
		if (totalSearchResults == null) {
			return null;
		}
		int startIndex = (currentPageNo - 1) * recordsPerPage;
		int endIndex = (startIndex + recordsPerPage > totalSearchResults.size() ? totalSearchResults
				.size() : startIndex + recordsPerPage);
		return totalSearchResults.subList(startIndex, endIndex);
	}

}
