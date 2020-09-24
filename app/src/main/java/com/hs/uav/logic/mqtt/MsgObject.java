package com.hs.uav.logic.mqtt;

//@ParsePacket("index:1|num:2|seq:2|len:2|payload:this.len|pts:0")
public class MsgObject {
    public int index; // 帧序号，相同序号为同一帧
    public int num; // 当前帧分包总数
    public int seq;  // 当前帧分包序列号
    public int len;  // 有效数据长度
    public byte[] payload; // 有效数据
    public long pts;//帧时间戳
}

