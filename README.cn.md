# OPC DA Windows驱动

<div style="font-size: 1.5rem;">
  <a href="./README.md">English</a> |
  <a href="./README.cn.md">中文</a>
</div>

部署于OPC服务端所在服务器，直连OPC服务并提供restful接口，可查询及写入。

## 配置说明
```yaml
opc:
  host: ${OPC_HOST:localhost} # OPC的服务，默认本地，本地采用COM连接
  prog-id: ${OPC_PROG_ID:NETxKNX.OPC.Server.3.5} # 本地OPC服务名
  group-json: ${OPC_GROUP_JSON:groupdata.json} # json地址
```
resources下的javafish.clients.opc为jeasyopc必须的配置文件，jeasyopc1.1为修改源码后的打包依赖，
源代码在jar包模式无法正确读取指定目录配置。

#### 组数据
`groupdata.json` 所有的组数据，会在程序启动的时候自动注册，如修改需要重启服务。**严格按照当前格式进行写入**，
不存在与此文件中的ID后续通过接口无法查询。

基本逻辑为：控制`group`地址，控制结果查询`group`下的基础地址。

IDE运行会读取项目根目录的文件；打包为jar包后，需要放到与jar包同级的目录中！

#### dll

读取路径配置在JCustomOpc.properties中，配置为`./lib/JCustomOpc`

动态链接库，IDE运行读取项目根目录lib下文件，打jar包，读取jar包同级的lib目录。


## 接口

### 查询
根据分组查询其所有反馈状态的值。

接口地址：`/opc/query?group=`  

请求方式：GET

结果：
```json
{
    "code": 0,
    "message": "Success",
    "data": [
        {
            "baseId": "\\NETxKNX\\10.10.0.1\\01/0/001",
            "type": "VT_BSTR",
            "status": -1
        }
    ]
}
```
说明：

| 字段     | 说明               | 
|--------|------------------| 
| baseId | 基础分组，反馈状态地址      |
| type | 类型，异常时用于开发快速判断   |
| status | 0关 1开 -1未知，可看作异常 |

### 控制
根据分组发送控制指令。

接口地址：`/opc/write`

请求方式：PUT

参数：

| 字段     | 说明               |
|--------  |------------------|
| group | 要控制的分组      |
| status | 操控的状态 0关 1开  |

结果：
```json
{
    "code": 0,
    "message": "Success",
    "data": null
}
```

### 查询OPC服务器状态

接口地址：`/opc/ping`

请求方式：GET

结果：
```json
{
    "code": 0,
    "message": "Success",
    "data": null
}
```

错误码不为0，表示有异常。