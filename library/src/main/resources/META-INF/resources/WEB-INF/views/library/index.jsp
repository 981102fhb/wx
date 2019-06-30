<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>图书馆首页</title>
<link href="/fhb/library/css/main.css" rel="stylesheet"/>
</head>
<body>
	<form method="get">
		<%-- param是EL表达式的内置对象，表示所有的请求参数 --%>
		<%-- ${param.keyword }使用EL表达式把名为keyword的请求参数的值获取出来 --%>
		<input name='keyword' value="${param.keyword }"/>
		<button>搜索</button>
	</form>
	<!-- 这里应该要循环从服务器返回的数据 -->
<%-- 	<%
	Page<Book> page = request.getAttribute("page");
	List<Book> content = page.getContent();
	for( Book book : content ){
	%>
		<div>
			<%=book.getName() %>
			+
		</div>
	<%} %> --%>
	
	<!-- 循环标签库 -->
	<!-- items是标签的属性，表示要遍历哪些元素 -->
	<%-- ${ page.content } 其实相当于前面的 request.getAttribute("page") + content = page.getContent() --%>
	<c:forEach items="${page.content }" var="book">
		<%-- book.name相当于是book.getName() --%>
		<div class="item">
			<img style="" alt="" src="/fhb/library/images/${book.image }" />
			<div class="name">
				${book.name }
			</div>
			<div class="buttons">
				<span onclick="document.location.href='/fhb/library/debit?bookId=${book.id}'">+</span>
			</div>
		</div>
	</c:forEach>
	
	<!-- 分页的按钮 -->
	<div>
		<c:if test="${page.number <= 0 }">
			<a>上一页</a>
		</c:if>
		<c:if test="${page.number > 0 }">
			<a href="?pageNumber=${page.number - 1 }&keyword=${param.keyword}">上一页</a>
		</c:if>
		<%-- 为什么要减一？因为number从0开始，而totalPages从1开始 --%>
		<c:if test="${page.number < page.totalPages - 1 }">
			<a href="?pageNumber=${page.number + 1 }&keyword=${param.keyword}">下一页</a>
		</c:if>
		<c:if test="${page.number >= page.totalPages - 1 }">
			<a>下一页</a>
		</c:if>
	</div>
</body>
</html>