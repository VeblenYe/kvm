layui.use(['form'], function () {
    var form = layui.form;
    const $ = layui.$;

    $.ajax({
        url: 'getClusterList',
        type: 'get',
        async: false,
        success: function (data) {
            //console.log(data);
            $.each(JSON.parse(data)['data'], function (index, item) {
                //console.log(item['clusterId']);
                $('#cluster').append(new Option(item['clusterName'], item['clusterId']));// 下拉菜单里添加元素
            });
            layui.form.render("select");
        }
    })

});