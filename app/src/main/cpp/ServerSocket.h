//
// Created by meicet on 2021/7/19.
//

#ifndef DEVICES_SERVERSOCKET_H
#define DEVICES_SERVERSOCKET_H

#pragma once

#include <sys/types.h>
#include <pthread.h>
#include <jni.h>
#include "SocketServerListener.h"
#include <SocketInterface.h>
#include <HPSocket.h>
#include <LogUtil.h>
#include <unistd.h>

class ServerSocket {

public:
    ServerSocket();

    virtual ~ServerSocket();

    void startServer();

    void onDestroy(JNIEnv *pEnv);

    void keyDataCallBack(JNIEnv *pEnv, jobject jObject);

    void connect();

    //typedef void (*OnReceiveCallback)(const BYTE *pData, int iLength)
    static void onReceiveCallback(const BYTE *pData, int iLength);


private:
    pthread_t mPid;
    SocketServerListener *socketListener = 0;
    ITcpServer *server = 0;

};


#endif //DEVICES_SERVERSOCKET_H
