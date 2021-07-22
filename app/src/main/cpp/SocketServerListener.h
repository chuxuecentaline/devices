//
// Created by meicet on 2021/7/20.
//

#ifndef DEVICES_SOCKETSERVERLISTENER_H
#define DEVICES_SOCKETSERVERLISTENER_H

#include <LogUtil.h>
#include <jni.h>
#include "hpsocket/SocketInterface.h"
#include <JavaVm.h>

class SocketServerListener : ITcpServerListener {
public:
    SocketServerListener(OnReceiveCallback onReceiveCallback);

    virtual ~SocketServerListener();

    OnReceiveCallback onReceiveCallback;

private:
    virtual EnHandleResult OnHandShake(ITcpServer *pSender, CONNID dwConnID);

    virtual EnHandleResult
    OnSend(ITcpServer *pSender, CONNID dwConnID, const BYTE *pData, int iLength);

    virtual EnHandleResult
    OnReceive(ITcpServer *pSender, CONNID dwConnID, const BYTE *pData, int iLength);

    virtual EnHandleResult OnReceive(ITcpServer *pSender, CONNID dwConnID, int iLength);

    virtual EnHandleResult
    OnClose(ITcpServer *pSender, CONNID dwConnID, EnSocketOperation enOperation, int iErrorCode);

    virtual EnHandleResult OnShutdown(ITcpServer *pSender);

    virtual EnHandleResult OnPrepareListen(ITcpServer *pSender, SOCKET soListen);

    virtual EnHandleResult OnAccept(ITcpServer *pSender, CONNID dwConnID, UINT_PTR soClient);


};


#endif //DEVICES_SOCKETSERVERLISTENER_H
