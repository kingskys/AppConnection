package com.kingskys.conn;

class Const {
    // 消息
    static final int MSG_TYPE_MSG = 0x01;
    // 上线
    static final int MSG_TYPE_ONLINE = 0x02;
    // 下线
    static final int MSG_TYPE_OFFLINE = 0x03;
    // 踢下线
    static final int MSG_TYPE_KICKOUT = 0x04;
    // 心跳
    static final int MSG_TYPE_HEART = 0x05;
    // 申请
    static final int MSG_TYPE_REQUEST = 0x06;
    // 回复
    static final int MSG_TYPE_RESPONSE = 0x07;

    // 数据键
    static final String MSG_KEY_DATA = "data";
    // 客户端ID 键
    static final String MSG_KEY_CLIENTID = "clientId";
    // 具体类型键
    static final String MSG_KEY_ACTION = "action";
    // 消息ID键
    static final String MSG_KEY_MSGID = "msgId";
}
