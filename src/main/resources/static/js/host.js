layui.use(['element', 'table'], function () {
    var table = layui.table;
    var $ = layui.$;

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
    });

    table.on('tool(vm)', function(obj){
        var data = obj.data;
        if(obj.event === 'start'){

        } else if(obj.event === 'del'){
            layer.confirm('真的删除行么', function(index){
                obj.del();
                layer.close(index);
            });
        } else if(obj.event === 'shutdown'){
            layer.alert('编辑行：<br>'+ JSON.stringify(data))
        }
    });
});