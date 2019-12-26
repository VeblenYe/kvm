package com.example.kvm.Utils;

import org.libvirt.*;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KvmUtils {
    private static KvmUtils instance = new KvmUtils();
    private static String xmldesc = "<domain type=\"kvm\">\n" +
            "    <name>%s</name>  <!--虚拟机名称-->\n" +
            "    <memory unit=\"MiB\">%d</memory>   <!--最大内存 -->\n" +
            "    <currentMemory unit=\"MiB\">%d</currentMemory>  <!--可用内存-->\n" +
            "    <vcpu>%d</vcpu>   <!--//虚拟cpu个数-->\n" +
            "    <os>\n" +
            "        <type arch=\"x86_64\" machine=\"pc\">hvm</type>\n" +
            "        <boot dev=\"hd\" /> <!-- 硬盘启动 -->\n" +
            "        <boot dev=\"cdrom\" />     <!--//光盘启动-->\n" +
            "    </os>\n" +
            "    <features>\n" +
            "        <acpi />\n" +
            "        <apic />\n" +
            "        <pae />\n" +
            "    </features>\n" +
            "    <clock offset=\"localtime\" />\n" +
            "    <on_poweroff>destroy</on_poweroff>\n" +
            "    <on_reboot>restart</on_reboot>\n" +
            "    <on_crash>destroy</on_crash>\n" +
            "    <devices>\n" +
            "        <emulator>/usr/libexec/qemu-kvm</emulator>\n" +
            "        <disk type=\"file\" device=\"disk\">\n" +
            "            <driver name=\"qemu\" type=\"qcow2\" />\n" +
            "            <source file=\"%s\" />        <!--目的镜像路径-->\n" +
            "            <target dev=\"hda\" bus=\"ide\" />\n" +
            "        </disk>\n" +
            "        <disk type=\"file\" device=\"cdrom\">\n" +
            "            <source file=\"%s\" />        <!--光盘镜像路径 -->\n" +
            "            <target dev=\"hdb\" bus=\"ide\" />\n" +
            "        </disk>\n" +
            "        <interface type=\"bridge\">       <!--虚拟机网络连接方式-->\n" +
            "            <source bridge=\"virbr0\" />      <!--当前主机网桥的名称-->\n" +
            "        </interface>\n" +
            "        <input type=\"mouse\" bus=\"ps2\" />\n" +
            "        <!--vnc方式登录，端口号自动分配，自动加1，可以通过virsh vncdisplay来查询-->\n" +
            "        <graphics type=\"vnc\" port=\"-1\" autoport=\"yes\" listen=\"0.0.0.0\" keymap=\"en-us\" />\n" +
            "    </devices>\n" +
            "</domain>";
    private static String volXml = "<volume>\n" +
            "    <name>%s.img</name>\n" +
            "    <allocation>0</allocation>\n" +
            "    <capacity unit=\"G\">%d</capacity>\n" +
            "    <target>\n" +
            "        <path>/var/lib/libvirt/images/%s.img</path>\n" +
            "        <format type='qcow2'/>\n" +
            "        <permissions>\n" +
            "        <owner>107</owner>\n" +
            "        <group>107</group>\n" +
            "        <mode>0744</mode>\n" +
            "        <label>virt_image_t</label>\n" +
            "        </permissions>\n" +
            "    </target>\n" +
            "</volume>";
    private static Connect conn = null;

    private static String connURI = "qemu:///system";

    private KvmUtils() {
        System.out.println("------------->KvmDemoUtils");
        try {
            conn = new Connect(connURI, false);
        } catch (LibvirtException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    public static KvmUtils getInstance() {
        return instance;
    }

    public static String getConnURI() {
        return connURI;
    }

    public static void setConnURI(String newConnURI) {
        if (conn != null) {
            try {
                conn.close();
            } catch (LibvirtException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        connURI = newConnURI;
        try {
            conn = new Connect(connURI, false);
        } catch (LibvirtException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public int startVm(String vmUuid) {
        Domain dom;
        try {
            dom = conn.domainLookupByUUIDString(vmUuid);
            dom.create();
        } catch (LibvirtException e) {
            e.printStackTrace();
            return -1;
        }
        return 0;
    }

    public int stopVm(String vmUuid) {
        Domain dom;
        try {
            dom = conn.domainLookupByUUIDString(vmUuid);
            dom.destroy();
        } catch (LibvirtException e) {
            e.printStackTrace();
            return -1;
        }
        return 0;
    }

    public int deleteVm(String vmUuid, boolean deleteDisk) {
        Domain dom;
        try {
            dom = conn.domainLookupByUUIDString(vmUuid);
            if (deleteDisk) {
                String rawXml = dom.getXMLDesc(0);
                DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                Document document = db.parse(new ByteArrayInputStream(rawXml.getBytes()));
                NodeList diskList = document.getElementsByTagName("disk");
                List<String> diskPaths = new ArrayList<String>();
                for (int i = 0; i < diskList.getLength(); i++) {
                    org.w3c.dom.Node node = diskList.item(i);
                    if ("disk".equals(node.getAttributes().getNamedItem("device").getTextContent())) {
                        NodeList diskChildren = node.getChildNodes();
                        for (int j = 0; j < diskChildren.getLength(); j++) {
                            org.w3c.dom.Node child = diskChildren.item(j);
                            System.out.println("---->" + child.getNodeName());
                            if (child.getNodeName().equals("source")) {
                                diskPaths.add(child.getAttributes().getNamedItem("file").getTextContent());
                            }
                        }
                    }
                }
                for (String diskPath : diskPaths) {
                    if (!diskPath.endsWith(".iso")) {
                        conn.storageVolLookupByPath(diskPath).wipe();
                        conn.storageVolLookupByPath(diskPath).delete(0);
                    }
                }
            }
            dom.undefine();
        } catch (LibvirtException e) {
            e.printStackTrace();
            return -1;
        } catch (ParserConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SAXException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return 0;
    }

    public String[] listStoragePools() {
        try {
            return conn.listStoragePools();
        } catch (LibvirtException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    public Map listIsoVolumes() {
        Map isos = new HashMap();
        try {
            for (String c : conn.listStoragePools()) {
                StoragePool po = conn.storagePoolLookupByName(c);
                for (String v : po.listVolumes()) {
                    StorageVol vol = po.storageVolLookupByName(v);
                    if (vol.getName().endsWith(".iso"))
                        isos.put(vol.getName(), vol.getPath());
                }
            }
        } catch (LibvirtException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return isos;
    }

    private int getVncPortForVm(String vmUuid) {
        Domain dom;
        try {
            dom = conn.domainLookupByUUIDString(vmUuid);
            String[] strCmd = {"virsh", "vncdisplay", dom.getName()}; //"virsh vncdisplay " + dom.getName();
            Process process = Runtime.getRuntime().exec(strCmd);
            LineNumberReader strCon = new LineNumberReader(new InputStreamReader(process.getInputStream()));
            StringBuffer sb = new StringBuffer();
            String line;
            while ((line = strCon.readLine()) != null) {
                if (line.startsWith(":")) {
                    return 5900 + Integer.parseInt(line.split(":")[1]);
                }
            }
            System.out.println("output for vnc port:" + sb);
        } catch (LibvirtException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return -1;
    }

    public int setVncProxyFile(String vmUuid) {

        int port = getVncPortForVm(vmUuid);
        if (port > 0) {
            ArrayList<String> arrayList = new ArrayList<>();
            try {
                FileReader fr = new FileReader("/root/noVNC/vnc_tokens/vnc-1.ini");
                BufferedReader bf = new BufferedReader(fr);
                // 按行读取字符串
                String str = null;
                while ((str = bf.readLine()) != null) {
                    if (str.startsWith(vmUuid))
                        continue;
                    arrayList.add(str);
                }
                fr.close();
                bf.close();
                arrayList.add(vmUuid + ": 127.0.0.1:" + port);
                FileWriter fw = new FileWriter("/root/noVNC/vnc_tokens/vnc-1.ini");
                for (int i = 0; i < arrayList.size(); i++) {
                    fw.write(arrayList.get(i) + "\n");
                }
                fw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return 0;
        }
        return -1;
    }

    public String createVm(String name, int cpu, long mem, long diskSize, String isopath, String sp) {
        String volPath = createDisk(sp, name + "_disk", diskSize);
        try {
            Domain domain = conn.domainDefineXML(String.format(xmldesc, name, mem, mem, cpu, volPath, isopath));
            return domain.getUUIDString();
        } catch (LibvirtException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return "failedss";
    }

    public String createDisk(String sp, String name, long size) {
        try {
            StoragePool pool = conn.storagePoolLookupByName(sp);
            StorageVol vol = pool.storageVolCreateXML(String.format(volXml, name, size, name), 0);
            return vol.getPath();
        } catch (LibvirtException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    public Map<String, Object> getMemoryStat(String uuid) {
        Domain domain;
        Map<String, Object> map = new HashMap<>();
        try {
            domain = conn.domainLookupByUUIDString(uuid);
            MemoryStatistic[] statistics = domain.memoryStats(2);
            DecimalFormat decimalFormat = new DecimalFormat("0");
            double memoryPercent = (double)statistics[1].getValue() / statistics[0].getValue() * 100;
            String percent = decimalFormat.format(memoryPercent) + "%";
            map.put("MemoryPercent", percent);
            return map;
        } catch (LibvirtException e) {
            e.printStackTrace();
        }
        return null;
    }
}
