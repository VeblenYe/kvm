package com.example.kvm.Controller;


import com.example.kvm.Models.User;
import com.example.kvm.Repository.UserRepository;
import com.example.kvm.Utils.KvmUtils;
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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class KvmOperationController {

    @Autowired
    private UserRepository userRepository;

    private Map<String, Object> convertDomainInfo(Domain d) throws LibvirtException {
        Map<String, Object> dm = new HashMap<String, Object>();
        dm.put("uuid", d.getUUIDString());
        dm.put("name", d.getName());
        dm.put("cpus", d.getInfo().nrVirtCpu);
        dm.put("memory", d.getMaxMemory());
        dm.put("state", MyDomainState.values()[d.getInfo().state.ordinal()]);
        return dm;
    }

    private List<Map<String, Object>> getDomainList(Connect conn) {
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

    private void getKvmInfo(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
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
            request.setAttribute("model", nodeInfo.model);
            request.setAttribute("memory", nodeInfo.memory >> 10);
            request.setAttribute("cpus", nodeInfo.cpus);
            request.setAttribute("hostname", conn.getHostName());
            request.setAttribute("type", conn.getType());
            request.setAttribute("domlist", getDomainList(conn));
            System.out.println("numOfDefinedDomains:" + conn.numOfDefinedDomains());
            System.out.println("listDefinedDomains:" + conn.listDefinedDomains());
            for (String c : conn.listDefinedDomains())
                System.out.println("    " + c);
            System.out.println("numOfDomains:" + conn.numOfDomains());
            System.out.println("listDomainqqqs:" + conn.listDomains());
            for (int c : conn.listDomains())
                System.out.println("    -> " + c);
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
    }


    @GetMapping("/")
    public String index(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
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

    @GetMapping("/createVm")
    public void createVm(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String isopath = request.getParameter("isopath");
        String vmName = request.getParameter("name");
        int cpus = Integer.parseInt(request.getParameter("cpu"));
        long mem = Long.parseLong(request.getParameter("mem"));
        long volSize = Long.parseLong(request.getParameter("disk_size"));
        String sp = request.getParameter("sp");
        KvmUtils.getInstance().createVm(vmName, cpus, mem, volSize, isopath, sp);
        getKvmInfo(request, response);

        response.setCharacterEncoding("UTF-8");//设置将字符以"UTF-8"编码输出到客户端浏览器
        //通过设置响应头控制浏览器以UTF-8的编码显示数据，如果不加这句话，那么浏览器显示的将是乱码
        response.setHeader("content-type", "text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        out.write(request.getAttribute("model").toString());
        out.write(request.getAttribute("cpus").toString());
        out.write(request.getAttribute("memory").toString());
    }

    @GetMapping("/deleteVm")
    public String deleteVm(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String vmUuid = request.getParameter("uuid");
        KvmUtils.getInstance().deleteVm(vmUuid, true);
        getKvmInfo(request, response);
        return "host";
    }

    @GetMapping("/startVm")
    public String startVm(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String vmUuid = request.getParameter("uuid");
        KvmUtils.getInstance().startVm(vmUuid);
        getKvmInfo(request, response);
        return "host";
    }

    @GetMapping("/shutdownVm")
    public String shutdownVm(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // TODO Auto-generated method stub
        String vmUuid = request.getParameter("uuid");
        KvmUtils.getInstance().stopVm(vmUuid);
        getKvmInfo(request, response);
        return "host";
    }

    @GetMapping("/vmConsole")
    public void vmConsole(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String vmUuid = request.getParameter("uuid");
        KvmUtils.getInstance().setVncProxyFile(vmUuid);
        System.out.println("url=" + request.getRequestURL());
        java.net.URL url = new java.net.URL(request.getRequestURL().toString());

        System.out.println("url-host=" + url.getHost());
        response.sendRedirect("http://" + url.getHost() + ":6080" + "/vnc_lite.html?token=" + vmUuid);
    }

    public enum MyDomainState {
        nostate, running, blocked, paused, shutdown, shutoff, crashed, pmsuspended
    }
}
