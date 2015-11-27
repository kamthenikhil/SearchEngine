<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"
	import="com.chinappa.search.engine.dto.SearchResult,java.util.List"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Search Engine</title>
<link rel="stylesheet" type="text/css" href="css/main.css">
<script src="javascript/main.js" type="text/javascript">
	
</script>
</head>
<body>
	<h1 class="text-aling-center">CS 172: Project Phase II</h1>
	<form action="search" method="get">
		<input id="searchBar" type="text" name="query"
			value="<%=request.getAttribute("query") == null ? "": request.getAttribute("query")%>" />
		<input type="submit" value="Search" id="searchButton" />
	</form>
	<%
		if(request
		.getAttribute("filteredRecords")!=null){
		List<SearchResult> results = (List<SearchResult>) request
		.getAttribute("filteredRecords");
	%>
	<%
		for(int i=0;i<results.size();i++){
	%>
	<br>
	<a href="<%=results.get(i).getUrl()%>" target="_blank"><%=results.get(i).getTitle()%></a>
	<p href="<%=results.get(i).getUrl()%>" target="_blank"><%=results.get(i).getSnippet()%></p>
	<%
		}
	%>
	<%
		}
	%>

	<%
		if(request
			.getAttribute("pageNumber")!=null){
			int pageNumber = (int)request
			.getAttribute("pageNumber");
	%>

	<%--For displaying Page numbers.
    The when condition does not display a link for the current page--%>
	<table cellpadding="5" cellspacing="5">
		<tr>
			<c:if test="${pageNumber != 1}">
				<c:set var="pageURL">
					<c:url value="search">
						<c:param name="pageNumber" value="${pageNumber - 1}" />
						<c:param name="query" value="${query}" />
						<c:param name="click" value="previous" />
					</c:url>
				</c:set>
				<td><a class="paginationLink" href="${pageURL}">Previous</a></td>
			</c:if>
			<c:forEach begin="1" end="${numberOfPages}" var="i">
				<c:choose>
					<c:when test="${pageNumber eq i}">
						<td><a class="paginationLinkActive">${i}</a></td>
					</c:when>
					<c:otherwise>
						<c:set var="pageURL">
							<c:url value="search">
								<c:param name="pageNumber" value="${i}" />
								<c:param name="query" value="${query}" />
							</c:url>
						</c:set>
						<td><a href="${pageURL}" class="paginationLink">${i}</a></td>
					</c:otherwise>
				</c:choose>
			</c:forEach>
			<c:if test="${pageNumber lt numberOfPages}">
				<c:set var="pageURL">
					<c:url value="search">
						<c:param name="pageNumber" value="${pageNumber+1}" />
						<c:param name="query" value="${query}" />
						<c:param name="click" value="next" />
					</c:url>
				</c:set>
				<td><a class="paginationLink" href="${pageURL}">Next</a></td>
			</c:if>
		</tr>
	</table>
	<%--For displaying Next link --%>
	<%
		}
	%>
</body>
</html>