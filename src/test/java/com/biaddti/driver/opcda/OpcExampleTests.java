package com.biaddti.driver.opcda;

import javafish.clients.opc.JOpc;
import javafish.clients.opc.browser.JOpcBrowser;
import javafish.clients.opc.component.OpcGroup;
import javafish.clients.opc.component.OpcItem;
import javafish.clients.opc.exception.ComponentNotFoundException;
import javafish.clients.opc.exception.SynchReadException;
import javafish.clients.opc.variant.Variant;
import org.junit.jupiter.api.Test;

public class OpcExampleTests {

    @Test
    public void test() throws Exception {
        JOpc.coInitialize();

        // List All Servers
        String[] opcServers = JOpcBrowser.getOpcServers("localhost");
        for (String server : opcServers) {
            System.out.println("[server]:" + server);
        }
        System.out.println(repeat("-", 100));

        JOpc jopc = new JOpc("localhost", "NETxKNX.OPC.Server.3.5",
                "JCustomOPC");
        jopc.connect();

        // ping
        System.out.println("Client is connected: " + jopc.ping());
        System.out.println(repeat("-", 100));

        // -List All Branches
        JOpcBrowser jbrowser = new JOpcBrowser("localhost",
                "NETxKNX.OPC.Server.3.5", "JOPCBrowser1");
        jbrowser.connect();
        String[] branches = jbrowser.getOpcBranch("");
        for (String branch : branches) {
            System.out.println("[branch]:" + branch);
        }
        System.out.println(repeat("-", 100));

        // List All Leafs under[???] - 无效
/*        String[] items = jbrowser.getOpcItems("", true);
        if (items != null) {
            for (int i = 0; i < items.length; i++) {
                String item = items[i];
                System.out.println(item);
            }
        }
        System.out.println(repeat("-",100));*/

        // 同步读取group
        OpcItem item1 = new OpcItem("\\NETxKNX\\10.10.0.1\\01/0/001", true, "");
        OpcItem item2 = new OpcItem("\\NETxKNX\\10.10.0.1\\01/0/002", true, "");
        OpcItem item3 = new OpcItem("\\NETxKNX\\10.10.0.1\\01/0/003", true, "");
        OpcItem item4 = new OpcItem("\\NETxKNX\\10.10.0.1\\01/7/004", true, "");
        OpcGroup group = new OpcGroup("group1", true, 10, 0.0f);
        group.addItem(item1);
        group.addItem(item2);
        group.addItem(item3);
        jopc.addGroup(group);

        // 同步读取item
        OpcGroup group11 = new OpcGroup("group11", true, 500, 0.0f);
        group11.addItem(item1);
        jopc.addGroup(group11);

        OpcGroup group111 = new OpcGroup("group111", true, 1000, 0.0f);
        group111.addItem(item4);
        jopc.addGroup(group111);

        jopc.registerGroups();
        OpcGroup responseGroup = jopc.synchReadGroup(group);
        System.out.println(responseGroup);

        OpcItem responseItem = jopc.synchReadItem(group11, item1);
        System.out.println(responseItem);

        System.out.println(repeat("-", 100));

        // 同步写入 - 有效
/*        Variant varin = new Variant(false);
        item1.setValue(varin);
        jopc.synchWriteItem(group11, item1);*/

        // 异步20读取 -- 相同asynch10Read失败
        System.out.println("异步20读取");
        jopc.asynch20Read(group111);
        OpcGroup downGroup;
        long start = System.currentTimeMillis();
        while ((System.currentTimeMillis() - start) < 10000) {
            jopc.ping();
            downGroup = jopc.getDownloadGroup();
            if (downGroup != null) {
                System.out.println(downGroup);
            }

            if ((System.currentTimeMillis() - start) >= 6000) {
                // 设置瘫痪后无效
                jopc.setGroupActivity(group111, false);
                System.out.println("---------");
            }

            synchronized (OpcExampleTests.class) {
                OpcExampleTests.class.wait(1000);
            }
        }
        jopc.asynch20Unadvise(group111);
        System.out.println(repeat("-", 100));


        //  异步订阅式读取-无效
        // https://github.com/luoyan35714/OPC_Client/blob/master/OPC_Client_Jeasyopc/src/main/java/com/freud/opc/jeasyopc/JeasyopcTest11.java


        // uninitialize COM components
        JOpc.coUninitialize();
    }

    private static void synchReadItem(JOpc jopc, OpcGroup group, OpcItem item)
            throws ComponentNotFoundException, SynchReadException {
        // read again
        OpcItem responseItem = jopc.synchReadItem(group, item);
        System.out.println(responseItem);
        System.out.println(!responseItem.isQuality() ? "Quality: BAD!!!"
                : "Quality: GOOD");
        // processing
        if (!responseItem.isQuality()) {
            System.out
                    .println("This next processing is WRONG!!! You haven't quality!!!");
        }
        System.out.println("Processing: Data type: "
                + Variant.getVariantName(responseItem.getDataType())
                + " Value: " + responseItem.getValue());

    }

    public static String repeat(String str, int n) {
        StringBuilder strBuilder = new StringBuilder(str);
        for (int i = 0; i < n; ++i) {
            strBuilder.append(str);
        }
        str = strBuilder.toString();
        return str;
    }
}
