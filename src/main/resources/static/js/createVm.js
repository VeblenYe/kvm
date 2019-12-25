layui.use(['form'], function () {
    var form = layui.form;
    const $ = layui.$;

    $.ajax({
        url: 'getStoragePools',
        type: 'get',
        async: false,
        success: function (data) {
            //console.log(data);
            $.each(data, function (index, item) {
                //console.log(item);
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
            //console.log(data);
            $.each(data, function (index, item) {
                //console.log(item);
                $('#iso').append(new Option(item, item.id));// 下拉菜单里添加元素
            });
            layui.form.render("select");
        }
    })

    $.ajax({
        url: 'getHostInfo',
        type: 'get',
        async: false,
        success: function (data) {
            const obj = JSON.parse(data);
            const maxMem = parseInt(obj['data'][0]['hostMemory']);
            var memory = 1024;
            var id = 0;
            do {
                $('#vmMemory').append(new Option(String(memory), String(memory)));// 下拉菜单里添加元素
                memory = memory * 2;
                ++id;
            } while (memory < maxMem);

            const maxCPUs = parseInt(obj['data'][0]['hostCpus']);
            var cpu = 1;
            id = 0;
            do {
                $('#vmCPUs').append(new Option(String(cpu), String(cpu)));
                ++cpu;
                ++id;
            } while (cpu <= maxCPUs);
            layui.form.render("select");
        }
    })

    $.ajax({
        url: 'getClusterList',
        type: 'get',
        async: false,
        success: function (data) {
            //console.log(data);
            $.each(data, function (index, item) {
                //console.log(item);
                $('#cluster').append(new Option(item['clusterId'], item.id));// 下拉菜单里添加元素
            });
            $.ajax({
                url: 'getHostList',
                type: 'get',
                data: {'clusterId': "1"},
                async: false,
                success: function (data) {
                    //console.log(data);
                    $('#host').empty();
                    $.each(data, function (index, item) {
                        //console.log(item);
                        $('#host').append(new Option(item['hostId'], item.id));// 下拉菜单里添加元素
                    });
                    layui.form.render("select");
                }
            })
            layui.form.render("select");
        }
    })

    form.on('select(cluster)', function(data) {
        //event.preventDefault();
        console.log(data.value);
        $.ajax({
            url: 'getHostList',
            type: 'get',
            data: {'clusterId': data.value},
            async: false,
            success: function (data) {
                //console.log(data);
                $('#host').empty();
                $.each(data, function (index, item) {
                    //console.log(item);
                    $('#host').append(new Option(item['hostId'], item.id));// 下拉菜单里添加元素
                });
                layui.form.render("select");
            }
        })
    });

});