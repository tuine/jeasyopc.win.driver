package com.biaddti.driver.opcda.enums;

import lombok.Getter;

/**
 * 消息类型
 */
@Getter
public enum MessageTypeEnum {

    HEARTBEAT,
    CONTROL,
    QUERY
}
