<%@ page contentType="text/html;charset=UTF-8" %>
<html lang="ru">
<head>
    <title>Edit meal</title>
</head>
<body>
<h3><a href="index.html">Home</a></h3>
<hr>
<form action="meals?action=${param.action}&id=${param.id}" method="POST">
    <table>
        <tr>
            <td>DateTime:</td>
            <td><input type="datetime-local" name="dateTime"></td>
        </tr>
        <tr>
            <td>Descriptions:</td>
            <td><input type="text" name="description"></td>
        </tr>
        <tr>
            <td>Calories:</td>
            <td><input type="text" name="calories"/></td>
        </tr>
    </table>
    <br/>
    <input type="submit" value="Submit"/>
    <input type="reset" value="Cancel">
</form>
</body>
</html>