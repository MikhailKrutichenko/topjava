<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html lang="ru">
<head>
    <title>Meals</title>
</head>
<body>
<h3><a href="index.html">Home</a></h3>
<hr>
<h2>Meals</h2>
<h3><a href="meals?action=edit">Add Meal</a></h3>
<table border="1">
    <tr>
        <td><h3>Date</h3></td>
        <td><h3>Description</h3></td>
        <td><h3>Calories</h3></td>
    </tr>
    <c:set var="dateFormatter" value="${dateTimeFormatter}"/>
    <c:forEach var="meal" items="${meals}">
        <tr style="color: ${meal.isExcess() ? "red" : "green"}">
            <td>${dateFormatter.format(meal.dateTime)}</td>
            <td>${meal.description}</td>
            <td>${meal.calories}</td>
            <td><a href="meals?action=edit&id=${meal.id}">Update</a></td>
            <td><a href="meals?action=delete&id=${meal.id}">Delete</a></td>
        </tr>
    </c:forEach>
</table>
</body>
</html>
