layui.use(['element', 'table', 'layer', 'form', 'tree'], function () {
    const table = layui.table;
    const layer = layui.layer;
    const $ = layui.$;
    const tree = layui.tree;

    //无连接线风格
    tree.render({
        elem: '#tree'
        ,data: data1
        ,showLine: false  //是否开启连接线
    });

    table.render({
        elem: '#host'
        , url: 'getHostInfo'
        , cols: [
            [
                {field: 'host_model', title: 'model'},
                {field: 'host_memory', title: 'memory(MB)'},
                {field: 'host_name', title: 'hostname'},
                {field: 'host_cpus', title: 'cpus'},
                {field: 'host_type', title: 'type'}
            ]
        ]
    });

    table.render({
        elem: '#vm'
        , url: 'getVmInfo'
        , cols: [
            [
                {field: 'vm_uuid', title: 'uuid'},
                {field: 'vm_memory', title: 'memory(MB)'},
                {field: 'vm_name', title: 'name'},
                {field: 'vm_cpus', title: 'cpus'},
                {field: 'vm_state', title: 'state'},
                {fixed: 'right', title: 'operation', align: 'center', toolbar: '#vmTool'} //这里的toolbar值是模板元素的选择器
            ]
        ]
        , id: "vmReload"
    });

    table.on('tool(vm)', function (obj) {
        const data = obj.data;
        if (obj.event === 'start') {
            $.ajax({
                url: 'startVm',
                async: false,
                type: "get",
                data: {'vm_uuid': data['vm_uuid']},
                success: function (req) {
                    //执行重载
                    table.reload('vmReload', {
                        page: {
                            curr: 1 //重新从第 1 页开始
                        }
                        , where: {}
                    }, 'data');
                }
            })
        } else if (obj.event === 'del') {
            layer.confirm('真的删除行么', function (index) {
                obj.del();
                layer.close(index);
                $.ajax({
                    url: 'deleteVm',
                    async: false,
                    type: "get",
                    data: {'vm_uuid': data['vm_uuid']},
                    success: function (req) {
                        //执行重载
                        table.reload('vmReload', {
                            page: {
                                curr: 1 //重新从第 1 页开始
                            }
                            , where: {}
                        }, 'data');
                    }
                })
            });
        } else if (obj.event === 'shutdown') {
            $.ajax({
                url: 'shutdownVm',
                async: false,
                type: "get",
                data: {'vm_uuid': data['vm_uuid']},
                success: function (req) {
                    //执行重载
                    table.reload('vmReload', {
                        page: {
                            curr: 1 //重新从第 1 页开始
                        }
                        , where: {}
                    }, 'data');
                }
            })
        } else if (obj.event === 'console') {
            $.ajax({
                url: 'vmConsole',
                async: false,
                type: "get",
                data: {'vm_uuid': data['vm_uuid']},
                success: function (req) {
                    layer.open({
                        type: 2,
                        title: 'vmConsole',
                        area: ['1000px', '900px'],
                        content: req,
                    })
                }
            })
        }
    });

    const active = {
        createVm: function () {
            layer.open({
                type: 2
                , title: 'createVm'
                , area: ['900px', '600px']
                , shade: 0
                , maxmin: true
                , content: 'createVm'
                , btn: ['create', 'close']
                , yes: function (index, layero) {
                    const body = layer.getChildFrame('body', index); //得到iframe页面层的BODY
                    const iframeBtn = body.find('#btn');//得到iframe页面层的提交按钮
                    iframeBtn.click();//模拟iframe页面层的提交按钮点击
                    layer.closeAll();
                    layer.msg(
                        'Wait to create VM',
                        {
                            icon: 6,
                            time: 1000,
                            shade: 1,
                            end: function () {
                                table.reload('vmReload', {
                                    page: {
                                        curr: 1 //重新从第 1 页开始
                                    }
                                    , where: {}
                                }, 'data');
                            }
                        });


                }
                , btn2: function () {
                    layer.closeAll();
                }
                , success: function (layero) {

                }
            });
        }

    };

    $('#contentMain .layui-btn').on('click', function () {
        const othis = $(this), method = othis.data('method');
        active[method] ? active[method].call(this, othis) : '';
    });
});
