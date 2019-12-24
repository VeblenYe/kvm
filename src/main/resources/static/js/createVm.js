layui.use(['form'], function () {
    const form = layui.form;
    const $ = layui.$;

    $.ajax({
        url: 'getStoragePools',
        type: 'get',
        async: false,
        success: function (data) {
            console.log(data);
            $.each(data, function (index, item) {
                console.log(item);
                $('#vmAddr').append(new Option(item, item.id));// 下拉菜单里添加元素
            });
            layui.form.render("select");
        }
    })

    $.ajax({
        url: 'getIsoVolumes',
        type: 'get',
        async: false,
        success: function (data) {
            console.log(data);
            $.each(data, function (index, item) {
                console.log(item);
                $('#iso').append(new Option(item, item.id));// 下拉菜单里添加元素
            });
            layui.form.render("select");
        }
    })

});