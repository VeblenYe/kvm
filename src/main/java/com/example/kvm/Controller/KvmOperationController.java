package com.example.kvm.Controller;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
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
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonArrayFormatVisitor;
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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

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
        vMachine.setVmMemory(d.getMaxMemory());
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
            conn = new Connect("qemu:///system", false);
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
            conn = new Connect("qemu:///system", false);
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
            List<Host> allHosts = new ArrayList<>();
            allHosts.add(host);

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("code", 0);
            jsonObject.put("msg", "");
            jsonObject.put("count", 1);
            jsonObject.put("data", allHosts);
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


    @GetMapping("/getTreeData")
    @ResponseBody
    public String getTreeData() {
        JSONObject jsonObject = new JSONObject();
        List<Cluster> clusterList = clusterRepository.findAll();
        List<Host> hostList = hostRepository.findAll();
        List<VMachine> vMachineList = vMachineRepository.findAll();
        for (Cluster cluster : clusterList) {
            for (Host host : hostList) {
                if (host.getClusterId() == cluster.getClusterId()) {
                    for (VMachine vMachine : vMachineList) {
                        if (vMachine.getHostId() == host.getHostId()) {

                        }
                    }
                }
            }
        }
        return "";
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

    @GetMapping("/createVm")
    public String createVm() {
        return "createVm";
    }

    @PostMapping("/do_createVm")
    public String createVm(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String isopath = request.getParameter("iso");
        String vmName = request.getParameter("VmName");
        int cpus = Integer.parseInt(request.getParameter("VmCPUs"));
        long mem = Long.parseLong(request.getParameter("VmMemory"));
        long volSize = Long.parseLong(request.getParameter("VmDisk"));
        String sp = request.getParameter("VmAddr");
        KvmUtils.getInstance().createVm(vmName, cpus, mem, volSize, isopath, sp);
        return "redirect:/";
    }

    @GetMapping("/deleteVm")
    @ResponseBody
    public String deleteVm(@RequestParam String vm_uuid) {
        KvmUtils.getInstance().deleteVm(vm_uuid, true);
        return "success";
    }

    @GetMapping("/startVm")
    @ResponseBody
    public String startVm(@RequestParam String vm_uuid) {
        KvmUtils.getInstance().startVm(vm_uuid);
        return "success";
    }

    @GetMapping("/shutdownVm")
    @ResponseBody
    public String shutdownVm(@RequestParam String vm_uuid) {
        KvmUtils.getInstance().stopVm(vm_uuid);
        return "success";
    }

    @GetMapping("/vmConsole")
    @ResponseBody
    public String vmConsole(@RequestParam String vm_uuid, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String vmUuid = vm_uuid;
        KvmUtils.getInstance().setVncProxyFile(vmUuid);
        System.out.println("url=" + request.getRequestURL());
        java.net.URL url = new java.net.URL(request.getRequestURL().toString());
        System.out.println("url-host=" + url.getHost());
        return "http://" + url.getHost() + ":6080" + "/vnc_lite.html?token=" + vmUuid;
    }

    public enum MyDomainState {
        nostate, running, blocked, paused, shutdown, shutoff, crashed, pmsuspended
    }
}
