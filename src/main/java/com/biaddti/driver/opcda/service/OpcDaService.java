package com.biaddti.driver.opcda.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.biaddti.driver.opcda.configuration.OpcConfig;
import com.biaddti.driver.opcda.util.Utils;
import javafish.clients.opc.JOpc;
import javafish.clients.opc.browser.JOpcBrowser;
import javafish.clients.opc.component.OpcGroup;
import javafish.clients.opc.component.OpcItem;
import javafish.clients.opc.exception.HostException;
import javafish.clients.opc.exception.NotFoundServersException;
import javafish.clients.opc.exception.UnableAddGroupException;
import javafish.clients.opc.exception.UnableAddItemException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @author tuine
 * @date 2022-10-27
 */
@Component
@Slf4j
public class OpcDaService {

    @Resource
    private OpcConfig opcConfig;
    private JOpc jopc;
    // 暂时用不到browser
//    private JOpcBrowser jOpcBrowser;
    private boolean isConnecting = false;
    public OpcGroup group1;

    // 存储groups
    static Map<String, OpcGroup> groupSourceMap = new ConcurrentHashMap<>();
    static Map<String, OpcGroup> baseGroupSourceMap = new ConcurrentHashMap<>();

    @PostConstruct
    private void initOpc() {
        boolean b = opcInit();
        if (!b) {
            log.error("JOPC 连接失败，开始重连。");
            reconnect();
        } else {
            log.info("JOPC 连接成功");
        }
    }

    /**
     * 初始化opc服务
     */
    private boolean opcInit() {
        log.info("JOPC 开始初始化");

        try {
            JOpc.coInitialize();

            // List All Servers
            String[] opcServers = JOpcBrowser.getOpcServers(opcConfig.getHost());
            for (String server : opcServers) {
                log.info("-----[server]-----:" + server);
            }

            log.info("建立JOPC服务对象");
            jopc = new JOpc(opcConfig.getHost(), opcConfig.getProgId(), "JOPC1");
            jopc.connect();
//            jOpcBrowser = new JOpcBrowser(opcConfig.getHost(), opcConfig.getProgId(), "JOPCBrowser1");
//            jOpcBrowser.connect();

            // 初始化组
            String jsonContent = Utils.readFile(opcConfig.getGroupJson());
            if (jsonContent.isEmpty()) {
                throw new RuntimeException("JOPC 请选择正确的json文件");
            }
            JSONArray groups = JSON.parseArray(jsonContent);
            for (Object group : groups) {
                JSONObject groupJsonObj = (JSONObject) group;
                String groupId = groupJsonObj.getString("group");

                log.info("JOPC 新增组：{}", groupId);
                OpcGroup opcGroup = new OpcGroup(groupId, true, 100, 0.0f);
                OpcItem groupItem = new OpcItem(groupId, true, "");
                opcGroup.addItem(groupItem);

                OpcGroup opcBaseGroup = new OpcGroup(groupId + "-base", true, 100, 0.0f);
                JSONArray baseGroups = groupJsonObj.getJSONArray("base");
                for (Object baseGroup : baseGroups) {
                    String baseGroupId = (String) baseGroup;
                    OpcItem baseGroupItem = new OpcItem(baseGroupId, true, "");
                    opcBaseGroup.addItem(baseGroupItem);
                    log.info("JOPC 新增组下底层分组：{}", baseGroupId);
                }
                jopc.addGroup(opcGroup);
                jopc.addGroup(opcBaseGroup);
                groupSourceMap.put(groupId, opcGroup);
                baseGroupSourceMap.put(groupId, opcBaseGroup);
            }
            log.info("JOPC 开始注册分组");
            jopc.registerGroups();
            log.info("JOPC 注册分组成功");

        } catch (Exception e) {
            log.error("JOPC 连接异常", e);
            return false;
        }

        return true;
    }

    /**
     * 断开连接
     */
    @PreDestroy
    private void coUninitialize() {
        log.info("JOPC 断开连接");
        JOpc.coUninitialize();
    }

    /**
     * 获取实例 - 暂时用不到
     *
     * @return /
     */
    public JOpcBrowser getBrowerInstance() {
        /*if (jOpcBrowser.ping()) {
            return jOpcBrowser;
        }
        reconnect()*/
        ;
        return null;
    }


    /**
     * 获取实例
     *
     * @return /
     */
    public JOpc getOpcInstance() {
        if (jopc.ping()) {
            return jopc;
        }
        reconnect();
        return null;
    }

    /**
     * 重连
     */
    public void reconnect() {
        if (isConnecting) {
            return;
        }
        isConnecting = true;

        try {
            reconnectHandler();
        } finally {
            isConnecting = false;
        }
    }

    /**
     * 根据ID获取分组
     *
     * @param id 分组ID
     * @return /
     */
    public OpcGroup getGroupById(String id) {
        return groupSourceMap.get(id);
    }

    /**
     * 根据ID获取分组
     *
     * @param id 分组ID
     * @return /
     */
    public OpcGroup getBaseGroupById(String id) {
        return baseGroupSourceMap.get(id);
    }

    private synchronized void reconnectHandler() {
        long i = 1;
        while (true) {
            coUninitialize();

            boolean b = opcInit();
//            if (b && jopc.ping() && jOpcBrowser.ping()) {
            if (b && jopc.ping()) {
                log.info("JOPC 重连成功");
                break;
            }
            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException e) {
                continue;
            }
            log.warn("JOPC 尝试第{}次连接", ++i);
        }
    }
}
