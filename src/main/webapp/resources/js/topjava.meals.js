const mealAjaxUrl = "profile/meals/";

// https://stackoverflow.com/a/5064235/548473
const ctx = {
    ajaxUrl: mealAjaxUrl,
    updateTable: function () {
        $.ajax({
            type: "GET",
            url: mealAjaxUrl + "filter",
            data: $("#filter").serialize()
        }).done(updateTableByData);
    }
}

function clearFilter() {
    $("#filter")[0].reset();
    $.get(mealAjaxUrl, updateTableByData);
}

$('#dateTime').datetimepicker({
    format:'yy-m-d H:m'
});
$('#startDate').datetimepicker({
    timepicker: false,
    format:'yy-m-d'
});
$('#startTime').datetimepicker({
    datepicker: false,
    format:'H:m'
});
$('#endDate').datetimepicker({
    timepicker: false,
    format:'yy-m-d'
});
$('#endTime').datetimepicker({
    datepicker: false,
    format:'H:m'
});

$(function () {
    makeEditable(
        $("#datatable").DataTable({
            "ajax": {
                "url": mealAjaxUrl,
                "dataSrc": ""
            },
            "paging": false,
            "info": true,
            "columns": [
                {
                    "data": "dateTime",
                    "render": function (date, type, row) {
                        if(type === "display") {
                            return date.replace('T', ' ').slice(0, -3);
                        }
                        return date;
                    }
                },
                {
                    "data": "description"
                },
                {
                    "data": "calories"
                },
                {
                    "orderable": false,
                    "defaultContent": "",
                    "render": renderEditBtn
                },
                {
                    "orderable": false,
                    "defaultContent": "",
                    "render": renderDeleteBtn
                }
            ],
            "order": [
                [
                    0,
                    "desc"
                ]
            ],
            "createdRow": function (row, data, dataIndex) {
                if (!data.enabled) {
                    $(row).attr("data-meal-excess", data.excess);
                }
            }
        })
    );
});