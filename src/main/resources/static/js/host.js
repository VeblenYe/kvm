layui.use(['element', 'table', 'layer'], function () {
    const table = layui.table;
    const layer = layui.layer;
    const $ = layui.$;

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
                {fixed: 'right', align:'center', toolbar: '#vmTool'} //这里的toolbar值是模板元素的选择器
            ]
        ]
        , id: "vmReload"
    });

    table.on('tool(vm)', function(obj){
        const data = obj.data;
        if(obj.event === 'start'){
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
        } else if(obj.event === 'del'){
            layer.confirm('真的删除行么', function(index){
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
        } else if(obj.event === 'shutdown'){
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
        }
    });
});