//
// Created by meicet on 2021/7/20.
//


#include "SocketServerListener.h"

SocketServerListener::SocketServerListener(OnReceiveCallback onReceiveCallback) : onReceiveCallback(onReceiveCallback) {

}

SocketServerListener::~SocketServerListener() {

}

EnHandleResult SocketServerListener::OnHandShake(ITcpServer *pSender, CONNID dwConnID) {
    LOGCATE("server 三次握手 dwConnID=%lu ", dwConnID);
    return HR_OK;
}

EnHandleResult
SocketServerListener::OnSend(ITcpServer *pSender, CONNID dwConnID, const BYTE *pData, int iLength) {
    LOGCATE("server 发送数据 dwConnID=%lu pData=%s iLength=%d", dwConnID, pData, iLength);
    return HR_OK;
}

EnHandleResult
SocketServerListener::OnReceive(ITcpServer *pSender, CONNID dwConnID, const BYTE *pData,
                                int iLength) {
    LOGCATE("server 接收数据 dwConnID=%lu pData=%p iLength=%d", dwConnID, pData, iLength);
    onReceiveCallback(pData, iLength);
    return HR_OK;
}

EnHandleResult SocketServerListener::OnReceive(ITcpServer *pSender, CONNID dwConnID, int iLength) {
    LOGCATE("server OnReceive dwConnID=%lu iLength=%d", dwConnID, iLength);
    return HR_OK;
}

EnHandleResult
SocketServerListener::OnClose(ITcpServer *pSender, CONNID dwConnID, EnSocketOperation enOperation,
                              int iErrorCode) {
    LOGCATE("server 关闭 dwConnID=%lu iErrorCode=%d", dwConnID, iErrorCode);
    return HR_OK;
}

EnHandleResult SocketServerListener::OnShutdown(ITcpServer *pSender) {
    LOGCATE("server 中断 pSender=%p ", pSender);
    return HR_OK;
}

EnHandleResult SocketServerListener::OnPrepareListen(ITcpServer *pSender, SOCKET soListen) {
    LOGCATE("server 准备 pSender=%p soListen=%d", pSender, soListen);

    return HR_OK;
}

EnHandleResult
SocketServerListener::OnAccept(ITcpServer *pSender, CONNID dwConnID, UINT_PTR soClient) {
    LOGCATE("server 接受时 pSender=%p soClient=%ld", pSender, soClient);
    return HR_OK;
}







