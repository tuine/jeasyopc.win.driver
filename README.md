# OPC DA Windows Driver

<div style="font-size: 1.5rem;">
  <a href="./README.md">English</a> |
  <a href="./README.cn.md">中文</a>
</div>

Deployed on the server where the OPC service is located, it directly connects to the OPC service and provides a 
RESTful interface for querying and writing.

## config intro
```yaml
opc:
  host: ${OPC_HOST:localhost} # default local, local use COM connection
  prog-id: ${OPC_PROG_ID:NETxKNX.OPC.Server.3.5}
  group-json: ${OPC_GROUP_JSON:groupdata.json} # location of the json file
```
The "javafish.clients.opc" under the "resources" folder is a necessary configuration file for JeasyOPC. The JeasyOPC 1.1
version is a packaged dependency with modified source code, as the source code cannot correctly read the 
specified directory configuration in jar packaging mode.

#### group data
All group data in `groupdata.json` will be automatically registered when the program starts. Any modifications to 
this file require a restart of the service. Please strictly follow the current format for writing to the file. 
If an ID is not listed in this file, it will not be able to be queried through the interface.

Basic logic：control the address of the `group` and to query the results of the control based on the `base address` under the group.

When running in the IDE, the file in the root directory of the project will be read. After packaging as a jar file, 
it needs to be placed in the same directory as the jar file.

#### dll

The configuration for the reading path is set in the JCustomOpc.properties file and is configured as 
`./lib/JCustomOpc`.

For dynamic linking libraries, when running the IDE, the files are read from the lib directory in 
the project's root directory. After packaging into a JAR file, the files are read from the lib directory at 
the same level as the JAR file.

## api

### query
Query the values of all feedback states according to the group.

URL：`/opc/query?group=`  

method：GET

result：
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
desc：

| field  | desc             | 
|--------|------------------| 
| baseId | Basic grouping, feedback status address      |
| type   | Type used for quick judgment during development when an exception occurs.   |
| status | 0 means off, 1 means on, and -1 means unknown or can be considered as an exception. |

### control
Send control commands based on groups.

url：`/opc/write`

method：PUT

param：

| field  | desc                                    |
|--------|-----------------------------------------|
| group  | The group that needs to be controlled.  |
| status | The state to be controlled: 0 for off, 1 for on.     |

result：
```json
{
    "code": 0,
    "message": "Success",
    "data": null
}
```

### Retrieve OPC server status.

url：`/opc/ping`

method：GET

result：
```json
{
    "code": 0,
    "message": "Success",
    "data": null
}
```

An error code not equal to 0 indicates an exception.