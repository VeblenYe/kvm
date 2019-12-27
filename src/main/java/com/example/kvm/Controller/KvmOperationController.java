package com.example.kvm.Controller;


import com.alibaba.fastjson.JSONObject;
import com.example.kvm.Models.Cluster;
import com.example.kvm.Models.Host;
import com.example.kvm.Models.User;
import com.example.kvm.Models.VMachine;
import com.example.kvm.Repository.ClusterRepository;
import com.example.kvm.Repository.HostRepository;
import com.example.kvm.Repository.UserRepository;
import com.example.kvm.Repository.VMachineRepository;
import com.example.kvm.Utils.KvmUtils;
import com.example.kvm.Utils.TreeDataUtils;
import org.libvirt.Connect;
import org.libvirt.Domain;
import org.libvirt.LibvirtException;
import org.libvirt.NodeInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.persistence.Id;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Controller
public class KvmOperationController {

    @Autowired
    private ClusterRepository clusterRepository;

    @Autowired
    private HostRepository hostRepository;

    @Autowired
    private VMachineRepository vMachineRepository;

    @Autowired
    private UserRepository userRepository;

    private VMachine convertDomainInfo(Domain d) throws LibvirtException {
        VMachine vMachine = new VMachine();
        vMachine.setVmUuid(d.getUUIDString());
        vMachine.setVmName(d.getName());
        vMachine.setVmCpus(d.getInfo().nrVirtCpu);
        vMachine.setVmMemory(d.getMaxMemory() >> 10);
        vMachine.setVmMemLoad(KvmUtils.getInstance().getMemoryStat(d.getUUIDString()));
        vMachine.setVmState(MyDomainState.values()[d.getInfo().state.ordinal()].toString());
        return vMachine;
    }

    private List<VMachine> getDomainList(Connect conn) {
        List doms = new ArrayList();
        String[] domStates = {"nostate", "running"};

        try {
            for (String c : conn.listDefinedDomains())
                doms.add(convertDomainInfo(conn.domainLookupByName(c)));
            for (int c : conn.listDomains())
                doms.add(convertDomainInfo(conn.domainLookupByID(c)));
        } catch (LibvirtException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return doms;
    }

    @GetMapping("/getVmInfo")
    @ResponseBody
    public String getVmInfo() {
        Connect conn = null;
        try {
            conn = new Connect(KvmUtils.getConnURI(), false);
        } catch (LibvirtException e) {
            System.out.println("exception caught:" + e);
            System.out.println(e.getError());
        }
        try {
            List<VMachine> allVms = getDomainList(conn);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("code", 0);
            jsonObject.put("msg", "");
            jsonObject.put("count", conn.numOfDefinedDomains());
            jsonObject.put("data", allVms);
            return jsonObject.toString();
        } catch (LibvirtException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            try {
                conn.close();
            } catch (LibvirtException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return null;
    }

    @GetMapping("/getHostInfo")
    @ResponseBody
    public String getHostInfo() {
        // TODO Auto-generated method stub
        Connect conn = null;
        try {
            conn = new Connect(KvmUtils.getConnURI(), false);
        } catch (LibvirtException e) {
            System.out.println("exception caught:" + e);
            System.out.println(e.getError());
        }
        try {
            NodeInfo nodeInfo = conn.nodeInfo();

            Host host = new Host();
            host.setHostModel(nodeInfo.model);
            host.setHostMemory(nodeInfo.memory >> 10);
            host.setHostName(conn.getHostName());
            host.setHostType(conn.getType());
            host.setHostCpus(nodeInfo.cpus);
            Host host1 = hostRepository.findByHostName(conn.getHostName());
            List<VMachine> vMachineList = vMachineRepository.findByHostId(host1.getHostId());
            host.setHostMemLoad(KvmUtils.getInstance().getHostMemoryUsage(host1.getHostMemory(), vMachineList));
            List<Host> allHosts = new ArrayList<>();
            allHosts.add(host);

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("code", 0);
            jsonObject.put("msg", "");
            jsonObject.put("count", 1);
            jsonObject.put("data", allHosts);
            return jsonObject.toString();
        } catch (LibvirtException | IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            try {
                conn.close();
            } catch (LibvirtException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return null;
    }


    @GetMapping("/getTreeData")
    @ResponseBody
    public List<TreeDataUtils> getTreeData() {
        List<Cluster> clusterList = clusterRepository.findAll();
        List<Host> hostList = hostRepository.findAll();
        List<VMachine> vMachineList = vMachineRepository.findAll();

        List<TreeDataUtils> treeDataUtilsList = new ArrayList<>();
        for (Cluster cluster : clusterList) {
            TreeDataUtils CTreeData = new TreeDataUtils();
            CTreeData.setTitle(cluster.getClusterName());
            CTreeData.setSpread(true);
            treeDataUtilsList.add(CTreeData);
            for (Host host : hostList) {
                if (host.getClusterId() == cluster.getClusterId()) {
                    TreeDataUtils HTreeData = new TreeDataUtils();
                    HTreeData.setSpread(true);
                    HTreeData.setTitle(host.getHostName());
                    CTreeData.getChildren().add(HTreeData);
                    for (VMachine vMachine : vMachineList) {
                        if (vMachine.getHostId() == host.getHostId()) {
                            TreeDataUtils VTreeData = new TreeDataUtils();
                            VTreeData.setTitle(vMachine.getVmName());
                            VTreeData.setSpread(true);
                            if (vMachine.getVmState().equalsIgnoreCase("shutoff")) {
                                CTreeData.getChildren().add(VTreeData);
                            } else if (vMachine.getVmState().equalsIgnoreCase("running")) {
                                HTreeData.getChildren().add(VTreeData);
                            }
                        }
                    }
                }
            }
        }

        return treeDataUtilsList;
    }


    @GetMapping("/")
    public String host() {
        return "host";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @PostMapping("/do_register")
    public String do_register(@RequestParam String username, @RequestParam String password) {
        User user = userRepository.findByUsername(username);
        if (user != null) {
            return "redirect:login";
        }
        user = new User();
        user.setUsername(username);
        user.setPassword(new BCryptPasswordEncoder().encode(password));
        userRepository.save(user);
        return "redirect:login";
    }

    @GetMapping("/getStoragePools")
    @ResponseBody
    public List<String> getStoragePools() {
        List<String> result = Arrays.asList(KvmUtils.getInstance().listStoragePools());
        return result;
    }

    @GetMapping("/getIsoVolumes")
    @ResponseBody
    public Map getIsoVolumes() {
        return KvmUtils.getInstance().listIsoVolumes();
    }

    @GetMapping("/getClusterList")
    @ResponseBody
    public String getClusterList() {
        List<Cluster> clusterList = clusterRepository.findAll();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("code", 0);
        jsonObject.put("msg", "");
        jsonObject.put("count", clusterList.size());
        jsonObject.put("data", clusterList);
        return jsonObject.toString();
    }

    @GetMapping("/getHostList")
    @ResponseBody
    public String getHostList(@RequestParam String clusterId) {
        List<Host> hostList = null;
        if (clusterId == null || clusterId.isEmpty()) {
            hostList =  hostRepository.findAll();
        } else {
            hostList =  hostRepository.findByClusterId(Long.parseLong(clusterId));
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("code", 0);
        jsonObject.put("msg", "");
        jsonObject.put("count", hostList.size());
        jsonObject.put("data", hostList);
        return jsonObject.toString();
    }

    @GetMapping("/getVMList")
    @ResponseBody
    public String getVMList() {
        List<VMachine> vMachineList = vMachineRepository.findAll();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("code", 0);
        jsonObject.put("msg", "");
        jsonObject.put("count", vMachineList.size());
        jsonObject.put("data", vMachineList);
        return jsonObject.toString();
    }

    @GetMapping("/createCluster")
    public String createCluster() {
        return "createCluster";
    }

    @PostMapping("/do_createCluster")
    public String do_createCluster(@RequestParam String clusterName, @RequestParam String clusterDescription) {
        Cluster cluster = new Cluster();
        cluster.setClusterName(clusterName);
        cluster.setClusterDescription(clusterDescription);
        clusterRepository.save(cluster);
        return "redirect:/";
    }

    @GetMapping("/createHost")
    public String createHost() {
        return "createHost";
    }

    @PostMapping("/do_createHost")
    public String do_createHost(HttpServletRequest request) {
        Host host = new Host();
        host.setHostCpus(Integer.parseInt(request.getParameter("HostCPUs")));
        host.setHostType(request.getParameter("HostType"));
        host.setHostName(request.getParameter("HostName"));
        host.setHostMemory(Long.parseLong(request.getParameter("HostMem")));
        host.setHostModel(request.getParameter("HostModel"));
        host.setClusterId(Long.parseLong(request.getParameter("Cluster")));
        host.setHostDescription(request.getParameter("HostDescription"));
        host.setHostIP(request.getParameter("HostIP"));
        host.setHostStatus(request.getParameter("HostStatus"));

        hostRepository.save(host);
        return "redirect:/";
    }

    @GetMapping("/createVm")
    public String createVm() {
        return "createVm";
    }

    @PostMapping("/do_createVm")
    public String do_createVm(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String isopath = request.getParameter("iso");
        String vmName = request.getParameter("VmName");
        int cpus = Integer.parseInt(request.getParameter("VmCPUs"));
        long mem = Long.parseLong(request.getParameter("VmMemory"));
        long volSize = Long.parseLong(request.getParameter("VmDisk"));
        String sp = request.getParameter("VmAddr");

        String oldURI = KvmUtils.getConnURI();
        KvmUtils.setConnURI(hostRepository.findByHostId(Long.parseLong(request.getParameter("Host"))).getHostIP());
        String uuid = KvmUtils.getInstance().createVm(vmName, cpus, mem, volSize, isopath, sp);
        KvmUtils.setConnURI(oldURI);

        VMachine vMachine = new VMachine();
        vMachine.setVmState("shutoff");
        vMachine.setVmMemory(mem);
        vMachine.setVmName(vmName);
        vMachine.setVmCpus(cpus);
        vMachine.setVmUuid(uuid);
        vMachine.setHostId(Long.parseLong(request.getParameter("Host")));
        vMachine.setClusterId(Long.parseLong(request.getParameter("Cluster")));
        vMachineRepository.save(vMachine);

        return "redirect:/";
    }

    @GetMapping("/deleteVm")
    @ResponseBody
    public String deleteVm(@RequestParam String vm_uuid) {
        VMachine vMachine = vMachineRepository.findByVmUuid(vm_uuid);
        if (vMachine.getVmState().equalsIgnoreCase("shutoff")) {
            KvmUtils.getInstance().deleteVm(vm_uuid, true);
            vMachineRepository.delete(vMachine);
            return "success";
        } else {
            return "failed";
        }
    }

    @GetMapping("/deleteCluster")
    @ResponseBody
    public String deleteCluster(@RequestParam String cluster_id) {
        List<Host> hostList = hostRepository.findByClusterId(Long.parseLong(cluster_id));
        if (!hostList.isEmpty()) {
            return "failed";
        }
        List<VMachine> vMachineList = vMachineRepository.findByClusterId(Long.parseLong(cluster_id));
        if (!vMachineList.isEmpty()) {
            return "failed";
        }
        clusterRepository.delete(clusterRepository.findByClusterId(Long.parseLong(cluster_id)));
        return "success";
    }

    @GetMapping("/deleteHost")
    @ResponseBody
    public String deleteHost(@RequestParam String host_id) {
        List<VMachine> vMachineList = vMachineRepository.findByHostId(Long.parseLong(host_id));
        if (!vMachineList.isEmpty()) {
            return "failed";
        }
        hostRepository.delete(hostRepository.findByHostId(Long.parseLong(host_id)));
        return "success";
    }

    @GetMapping("/startVm")
    @ResponseBody
    public String startVm(@RequestParam String vm_uuid) {
        KvmUtils.getInstance().startVm(vm_uuid);
        VMachine vMachine = vMachineRepository.findByVmUuid(vm_uuid);
        vMachine.setVmState("running");
        vMachineRepository.save(vMachine);
        return "success";
    }

    @GetMapping("/shutdownVm")
    @ResponseBody
    public String shutdownVm(@RequestParam String vm_uuid) {
        KvmUtils.getInstance().stopVm(vm_uuid);
        VMachine vMachine = vMachineRepository.findByVmUuid(vm_uuid);
        vMachine.setVmState("shutoff");
        vMachineRepository.save(vMachine);
        return "success";
    }

    @GetMapping("/vmConsole")
    @ResponseBody
    public String vmConsole(@RequestParam String vm_uuid) {
        Host host = hostRepository.findByHostId(vMachineRepository.findByVmUuid(vm_uuid).getHostId());
        KvmUtils.getInstance().setVncProxyFile(vm_uuid);
        return "http://" + host.getHostIP() + ":6080" + "/vnc_lite.html?token=" + vm_uuid;
    }

    @GetMapping("/link")
    public String link(@RequestParam String hostIP) {
        KvmUtils.setConnURI(hostIP);
        return "host";
    }

    public enum MyDomainState {
        nostate, running, blocked, paused, shutdown, shutoff, crashed, pmsuspended
    }
}
