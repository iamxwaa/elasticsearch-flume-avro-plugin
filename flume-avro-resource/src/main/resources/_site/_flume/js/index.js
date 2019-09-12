var _host = window.location.origin

function GET(url, dataBody, func) {
    if (typeof dataBody === "function") {
        func = dataBody
        dataBody = {}
    }
    $.ajax({
        "url": _host + url,
        "type": "GET",
        "crossDomain": true,
        "headers": {},
        "data": dataBody,
        "success": function (data) {
            func(data)
        }
    })
}

$(function () {
    var getConfig = function () {
        GET("/_flume/config", function (data) {
            $("#index").val(data.esConfig.index)
            $("#timeValue").val(data.esConfig.timeValue)
            $("#size").val(data.esConfig.size)
            $("#host").val(data.avroConfig.host)
            $("#port").val(data.avroConfig.port)
            $("#dataType").val(data.avroConfig.dataType)
        })
    }

    var timeFunc = undefined
    var needFreeze = true

    var freeze = function (a) {
        needFreeze = a
        if (a) {
            $("#index-list").attr({ "disabled": "disabled" })
            $("#channelType").attr({ "disabled": "disabled" })
            $("#timeValue").attr({ "readonly": "readonly" })
            $("#size").attr({ "readonly": "readonly" })
            $("#host").attr({ "readonly": "readonly" })
            $("#port").attr({ "readonly": "readonly" })
            $("#dataType").attr({ "readonly": "readonly" })
        } else {
            $("#index-list").removeAttr("disabled")
            $("#channelType").removeAttr("disabled")
            $("#timeValue").removeAttr("readonly")
            $("#size").removeAttr("readonly")
            $("#host").removeAttr("readonly")
            $("#port").removeAttr("readonly")
            $("#dataType").removeAttr("readonly")
        }
    }

    var getDatInfo = function () {
        GET("/_flume/info", function (data) {
            freeze(!data.isFinish)
            var state = data.isFinish ? 0 : ((data.total.value == data.sendTotal.value) && (0 != data.sendTotal.value) ? 2 : 1)
            var stateText = "-"
            if (0 == state) {
                stateText = "结束"
            } else if (1 == state) {
                stateText = "运行"
            } else if (2 == state) {
                stateText = "等待结束(" + data.waitTime + " s)"
            }
            $("#isFinish").text(stateText)
            $("#indexName").text(data.index.value)
            $("#startTime").text(data.startTime.value)
            $("#expectTime").text(data.expectTime.value)
            $("#total").text(data.total.value)
            $("#sendTotal").text(data.sendTotal.value)
            $("#percent").text(data.percent.value)
            $("#endTime").text(data.endTime.value)
            if (data.isFinish) {
                if (undefined != timeFunc) {
                    clearInterval(timeFunc)
                }
            }
        })
    }

    $("#submit").click(function () {
        if (needFreeze) {
            alert("运行中,请稍后重试.")
            return
        }
        GET("/_flume/export", {
            "host": $("#host").val(),
            "port": $("#port").val(),
            "dataType": $("#dataType").val(),
            "index": $("#index-list").val(),
            "channelType": $("#channelType").val(),
            "timeValue": $("#timeValue").val(),
            "size": $("#size").val()
        }, function (data) {
            if (undefined != timeFunc) {
                clearInterval(timeFunc)
            }
            getDatInfo()
            timeFunc = setInterval(getDatInfo, 3000)
            alert(data.status)
        })
    })

    $("#stop").click(function () {
        GET("/_flume/stop", function (data) {
            alert(data.status)
        })
    })

    var getIndices = function () {
        GET("/_cat/indices", function (data) {
            var lines = data.split("\n")
            var list = $("#index-list")
            for (var i in lines) {
                if ("" != lines[i]) {
                    var itemName = lines[i].match("(open|close)\\s{1,2}([^ ]*)")
                    if ("close" != itemName[1]) {
                        list.append("<option>" + itemName[2] + "</option>")
                    }
                }
            }
        })
    }

    //****************************启动运行****************************
    getConfig()
    getDatInfo()
    getIndices()
})