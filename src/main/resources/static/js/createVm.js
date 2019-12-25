layui.use(['form'], function () {
    const form = layui.form;
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
            const maxMem = parseInt(obj['data'][0]['host_memory']);
            var memory = 1024;
            var id = 0;
            do {
                $('#vmMemory').append(new Option(String(memory), String(memory)));// 下拉菜单里添加元素
                memory = memory * 2;
                ++id;
            } while (memory < maxMem);

            const maxCPUs = parseInt(obj['data'][0]['host_cpus']);
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

});