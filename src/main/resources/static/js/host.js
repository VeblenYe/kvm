layui.use(['element', 'table', 'layer', 'form', 'tree'], function () {
    const table = layui.table;
    const layer = layui.layer;
    const $ = layui.$;
    const tree = layui.tree;
    const element = layui.element;

    $.ajax({
        url: "getTreeData",
        type: "get",
        async: false,
        success: function (req) {
            tree.render({
                elem: '#tree',
                id: 'treeId'
                , data: req
                , showLine: false  //是否开启连接线
                , click: function (obj) {
                    console.log(obj.data); //得到当前点击的节点数据
                    console.log(obj.state); //得到当前节点的展开状态：open、close、normal
                    console.log(obj.elem); //得到当前节点元素

                    console.log(obj.data.children); //当前节点下是否有子节点
                }
            });
        }
    });


    table.render({
        elem: '#cluster'
        , id: 'clusterReload'
        , url: 'getClusterList'
        , cols: [
            [
                {field: 'clusterName', title: 'clusterName'},
                {field: 'clusterDescription', title: 'clusterDescription'},
                {fixed: 'right', title: 'operation', align: 'center', toolbar: '#clusterTool'}
            ]
        ]
    });

    table.render({
        elem: '#host'
        , id: 'hostReload'
        , url: 'getHostInfo'
        , cols: [
            [
                {field: 'hostName', title: 'hostname'},
                {field: 'hostModel', title: 'model'},
                {field: 'hostMemory', title: 'memory(MB)'},

                {
                    field: 'hostMemLoad', title: 'memLoad(MB)', templet: function (data) {
                        return '<div class="layui-progress layui-progress-big" lay-showpercent="true" lay-filter="demo">' +
                            '<div class="layui-progress-bar layui-bg-green" lay-percent="' +
                            data.hostMemLoad + '"></div></div>'
                    }
                },

                {field: 'hostCpus', title: 'cpus'},
                {field: 'hostType', title: 'type'}
            ]
        ]
    });

    table.render({
        elem: '#hostUnlink'
        , id: 'hostUnlinkReload'
        , url: 'getHostList'
        , where: {
            clusterId: ''
        }
        , cols: [
            [
                {field: 'hostName', title: 'hostname'},
                {field: 'hostModel', title: 'model'},
                {field: 'hostMemory', title: 'memory(MB)'},
                {field: 'hostCpus', title: 'cpus'},
                {field: 'hostType', title: 'type'},
                {field: 'hostIP', title: 'IP'},
                {fixed: 'right', title: 'operation', align: 'center', toolbar: '#hostTool'}
            ]
        ]
    });

    table.render({
        elem: '#vmUnlink'
        , id: 'vmUnlinkReload'
        , url: 'getVMList'
        , cols: [
            [
                {field: 'vmName', title: 'vmName'},
                {field: 'vmUuid', title: 'uuid', hide: true},
                {field: 'vmMemory', title: 'memory(MB)'},
                {field: 'vmCpus', title: 'cpus'},
                {field: 'vmState', title: 'state'}
            ]
        ]
    });


    table.render({
        elem: '#vm'
        , url: 'getVmInfo'
        , cols: [
            [
                {field: 'vmName', title: 'name'},
                {field: 'vmUuid', title: 'uuid', hide: true},
                {field: 'vmMemory', title: 'memory(MB)'},
                {
                    field: 'vmMemLoad', title: 'memLoad(MB)', templet: function (data) {
                        element.progress('demo', data.vmMemLoad);
                        return '<div class="layui-progress layui-progress-big" lay-showpercent="true" lay-filter="demo">' +
                            '<div class="layui-progress-bar layui-bg-green" lay-percent="' +
                            data['vmMemLoad'] + '"></div></div>'
                    }
                },
                {field: 'vmCpus', title: 'cpus'},
                {field: 'vmState', title: 'state'},
                {fixed: 'right', title: 'operation', align: 'center', toolbar: '#vmTool'} //这里的toolbar值是模板元素的选择器
            ]
        ]
        , id: "vmReload"
        , done: function (res, currentCount) {
            console.log("hello");
            element.render();
        }
    });

    table.on('tool(cluster)', function (obj) {
        const data = obj.data;
        console.log(data);
        if (obj.event === "del") {
            layer.confirm('confirm to delete Cluster?', function (index) {
                $.ajax({
                    url: 'deleteCluster',
                    async: false,
                    type: "get",
                    data: {'cluster_id': data['clusterId']},
                    success: function (req) {
                        //执行重载
                        if (req === "failed") {
                            layer.msg("The cluster still contains hosts or virtual machines");
                        }
                        table.reload('clusterReload', {
                            page: {
                                curr: 1 //重新从第 1 页开始
                            }
                            , where: {}
                        }, 'data');
                        $.ajax({
                            url: "getTreeData",
                            type: "get",
                            async: false,
                            success: function (req) {
                                tree.reload('treeId', {
                                    data: req
                                });
                            }
                        })
                    }
                });
                obj.del();
                layer.close(index);
            });
        }
    });

    table.on('tool(vm)', function (obj) {
        const data = obj.data;
        if (obj.event === 'start') {
            $.ajax({
                url: 'startVm',
                async: false,
                type: "get",
                data: {'vm_uuid': data['vmUuid']},
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
            layer.confirm('confirm to delete VM?', function (index) {
                $.ajax({
                    url: 'deleteVm',
                    async: false,
                    type: "get",
                    data: {'vm_uuid': data['vmUuid']},
                    success: function (req) {
                        //执行重载
                        if (req === "failed") {
                            layer.msg("Please shutoff the VM first");
                        }
                        table.reload('vmReload', {
                            page: {
                                curr: 1 //重新从第 1 页开始
                            }
                            , where: {}
                        }, 'data');
                        table.reload('vmUnlinkReload', {
                            page: {
                                curr: 1 //重新从第 1 页开始
                            }
                            , where: {}
                        }, 'data');
                        $.ajax({
                            url: "getTreeData",
                            type: "get",
                            async: false,
                            success: function (req) {
                                tree.reload('treeId', {
                                    data: req
                                });
                            }
                        })
                    }
                });
                obj.del();
                layer.close(index);
            });
        } else if (obj.event === 'shutdown') {
            $.ajax({
                url: 'shutdownVm',
                async: false,
                type: "get",
                data: {'vm_uuid': data['vmUuid']},
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
                data: {'vm_uuid': data['vmUuid']},
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
        table.reload('vmUnlinkReload', {
            page: {
                curr: 1 //重新从第 1 页开始
            }
            , where: {}
        }, 'data');
        $.ajax({
            url: "getTreeData",
            type: "get",
            async: false,
            success: function (req) {
                tree.reload('treeId', {
                    data: req
                });
            }
        })
    });

    table.on('tool(hostUnlink)', function (obj) {
        const data = obj.data;
        //console.log(data);
        if (obj.event === 'link') {
            console.log(data['hostIP']);
            $.ajax({
                url: 'link',
                async: false,
                data: {'hostIP': data['hostIP']},
                success: function (req) {
                    table.reload('hostReload', {
                        page: {
                            curr: 1 //重新从第 1 页开始
                        }
                        , where: {}
                    }, 'data');
                    table.reload('vmReload', {
                        page: {
                            curr: 1 //重新从第 1 页开始
                        }
                        , where: {}
                    }, 'data');
                }
            })
        }
        else if (obj.event === 'del') {
            layer.confirm('confirm to delete Host?', function (index) {
                $.ajax({
                    url: 'deleteHost',
                    async: false,
                    type: "get",
                    data: {'host_id': data['hostId']},
                    success: function (req) {
                        //执行重载
                        if (req === "failed") {
                            layer.msg("The host still contains virtual machines");
                        }
                        table.reload('hostUnlinkReload', {
                            page: {
                                curr: 1 //重新从第 1 页开始
                            }
                            , where: {}
                        }, 'data');
                        $.ajax({
                            url: "getTreeData",
                            type: "get",
                            async: false,
                            success: function (req) {
                                tree.reload('treeId', {
                                    data: req
                                });
                            }
                        })
                    }
                });
                obj.del();
                layer.close(index);
            });
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
                            shade: 0.5,
                            end: function () {
                                table.reload('vmReload', {
                                    page: {
                                        curr: 1 //重新从第 1 页开始
                                    }
                                    , where: {}
                                }, 'data');
                                table.reload('vmUnlinkReload', {
                                    page: {
                                        curr: 1 //重新从第 1 页开始
                                    }
                                    , where: {}
                                }, 'data');
                                $.ajax({
                                    url: "getTreeData",
                                    type: "get",
                                    async: false,
                                    success: function (req) {
                                        tree.reload('treeId', {
                                            data: req
                                        });
                                    }
                                })
                            }
                        });


                }
                , btn2: function () {
                    layer.closeAll();
                }
                , success: function (layero) {

                }
            });
        },
        createHost: function () {
            layer.open({
                type: 2
                , title: 'createHost'
                , area: ['900px', '600px']
                , shade: 0
                , maxmin: true
                , content: 'createHost'
                , btn: ['create', 'close']
                , yes: function (index, layero) {
                    const body = layer.getChildFrame('body', index); //得到iframe页面层的BODY
                    const iframeBtn = body.find('#host-btn');//得到iframe页面层的提交按钮
                    iframeBtn.click();//模拟iframe页面层的提交按钮点击
                    layer.closeAll();
                    layer.msg(
                        'Wait to create Host',
                        {
                            icon: 6,
                            time: 1000,
                            shade: 0.5,
                            end: function () {
                                table.reload('hostUnlinkReload', {
                                    page: {
                                        curr: 1 //重新从第 1 页开始
                                    }
                                    , where: {}
                                }, 'data');
                                $.ajax({
                                    url: "getTreeData",
                                    type: "get",
                                    async: false,
                                    success: function (req) {
                                        tree.reload('treeId', {
                                            data: req
                                        });
                                    }
                                })
                            }
                        });
                }
                , btn2: function () {
                    layer.closeAll();
                }
            });
        },
        createCluster: function () {
            layer.open({
                type: 2
                , title: 'createCluster'
                , area: ['900px', '600px']
                , shade: 0
                , maxmin: true
                , content: 'createCluster'
                , btn: ['create', 'close']
                , yes: function (index, layero) {
                    const body = layer.getChildFrame('body', index); //得到iframe页面层的BODY
                    const iframeBtn = body.find('#btn');//得到iframe页面层的提交按钮
                    iframeBtn.click();//模拟iframe页面层的提交按钮点击
                    layer.closeAll();
                    layer.msg(
                        'Wait to create Cluster',
                        {
                            icon: 6,
                            time: 1000,
                            shade: 0.5,
                            end: function () {
                                table.reload('clusterReload', {
                                    page: {
                                        curr: 1 //重新从第 1 页开始
                                    }
                                    , where: {}
                                }, 'data');
                                $.ajax({
                                    url: "getTreeData",
                                    type: "get",
                                    async: false,
                                    success: function (req) {
                                        tree.reload('treeId', {
                                            data: req
                                        });
                                    }
                                })
                            }
                        });
                }
                , btn2: function () {
                    layer.closeAll();
                }
            });
        }
    };

    $('#contentMain .layui-btn').on('click', function () {
        const othis = $(this), method = othis.data('method');
        active[method] ? active[method].call(this, othis) : '';
    });
});
