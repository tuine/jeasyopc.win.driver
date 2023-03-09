package com.biaddti.driver.opcda.controller;


import com.alibaba.fastjson.JSONObject;
import com.biaddti.driver.opcda.service.OpcDaService;
import com.biaddti.driver.opcda.util.Result;
import com.biaddti.driver.opcda.util.ResultCode;
import com.biaddti.driver.opcda.util.ResultGenerator;
import javafish.clients.opc.JOpc;
import javafish.clients.opc.component.OpcGroup;
import javafish.clients.opc.component.OpcItem;
import javafish.clients.opc.exception.*;
import javafish.clients.opc.variant.Variant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.sql.Array;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * opc操作
 */
@RestController
@RequestMapping("opc")
@RequiredArgsConstructor
@Slf4j
public class OpcController {

    private final OpcDaService opcDaService;

    /**
     * 查询
     *
     * @return /
     */
    @GetMapping("query")
    public Result query(@RequestParam("group") String groupId) {
        JOpc jopc = opcDaService.getOpcInstance();
        if (Objects.isNull(jopc)) {
            return ResultGenerator.fail(ResultCode.INTERNAL_SERVER_ERROR.getCode(), "OPC服务异常");
        }
        // 获取分组
        OpcGroup baseGroupById = opcDaService.getBaseGroupById(groupId);
        if (Objects.isNull(baseGroupById)) {
            return ResultGenerator.fail(ResultCode.FAIL.getCode(), "未查询到该分组");
        }

        OpcGroup responseGroup = null;
        try {
            responseGroup = jopc.synchReadGroup(baseGroupById);
        } catch (SynchReadException e) {
            log.error("查询组状态失败，查询ID:{}", groupId, e);
            return ResultGenerator.fail(ResultCode.INTERNAL_SERVER_ERROR.getCode(), "查询异常【" + e.getMessage() + "】");
        }
        ArrayList<OpcItem> items = responseGroup.getItems();
        ArrayList<Object> objects = new ArrayList<>();
        for (OpcItem item : items) {
            JSONObject jsonObject = new JSONObject();
            Variant value = item.getValue();
            jsonObject.put("baseId", item.getItemName());
            int returnValue;
            if (!item.isQuality()) {
                returnValue = -1;
            } else {
                if (Variant.VT_BOOL == item.getDataType()) {
                    returnValue = value.getBoolean() ? 1 : 0;
                } else {
                    returnValue = -1;
                }
            }
            jsonObject.put("type", Variant.getVariantName(item.getDataType()));
            jsonObject.put("status", returnValue);
            objects.add(jsonObject);
        }

        return ResultGenerator.success(objects);
    }

    @PutMapping("write")
    public Result write(@RequestBody JSONObject params) {
        String id = params.getString("group");
        if (Objects.isNull(id)) {
            return ResultGenerator.fail(ResultCode.FAIL.getCode(), "请输入要修改的组地址");
        }
        // 获取分组
        OpcGroup group = opcDaService.getGroupById(id);
        if (Objects.isNull(group)) {
            return ResultGenerator.fail(ResultCode.FAIL.getCode(), "该分组不存在");
        }
        Integer status = params.getInteger("status");
        if (Objects.isNull(status)) {
            return ResultGenerator.fail(ResultCode.FAIL.getCode(), "请输入要修改的状态0/1");
        }
        JOpc jopc = opcDaService.getOpcInstance();
        if (Objects.isNull(jopc)) {
            return ResultGenerator.fail(ResultCode.INTERNAL_SERVER_ERROR.getCode(), "OPC服务异常");
        }
        // 获取当前分组的唯一item
        OpcItem opcItem = group.getItems().get(0);
        Variant varin = new Variant(!status.equals(0));
        opcItem.setValue(varin);
        try {
            jopc.synchWriteItem(group, opcItem);
        } catch (SynchWriteException e) {
            log.error("写入组状态失败，用户参数:{}", params, e);
            return ResultGenerator.fail(ResultCode.INTERNAL_SERVER_ERROR.getCode(), "控制异常【" + e.getMessage() + "】");
        }

        return ResultGenerator.success();
    }

    @GetMapping("ping")
    public Result ping() {
        JOpc jopc = opcDaService.getOpcInstance();
        if (Objects.isNull(jopc)) {
            return ResultGenerator.fail(ResultCode.INTERNAL_SERVER_ERROR.getCode(), "OPC服务异常");
        }
        return ResultGenerator.success();
    }
}
