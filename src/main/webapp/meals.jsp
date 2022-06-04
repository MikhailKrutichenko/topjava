<%@ page import="java.time.format.DateTimeFormatter" %>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html lang="ru">
<head>
    <title>Users</title>
</head>
<body>
<h3><a href="index.html">Home</a></h3>
<hr>
<h2>Meals</h2>
<h3><a href="meals?action=create">Add Meal</a></h3>
<table border="1">
    <tr>
        <td><h3>Date</h3></td>
        <td><h3>Description</h3></td>
        <td><h3>Calories</h3></td>
    </tr>
    <c:forEach var="meal" items="${meals}">
        <c:set var="salary" scope="session" value="${meal.isExcess()}"/>
        <tr style="${meal.isExcess() ? "color: red" : "color: black"}">
            <td>${DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm").format(meal.getDateTime())}</td>
            <td>${meal.getDescription()}</td>
            <td>${meal.getCalories()}</td>
            <td><a href="meals?action=update&id=${meal.getId()}">Update</a></td>
            <td><a href="meals?action=delete&id=${meal.getId()}">Delete</a></td>
        </tr>
    </c:forEach>
</table>
</body>
</html>
