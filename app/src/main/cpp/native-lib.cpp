#pragma once

#include <string>
#include <LogUtil.h>
#include "ServerSocket.h"

extern "C"
JNIEXPORT jlong JNICALL
Java_com_meicet_network_1hp_ApiNet_nativeCreateServerNet(JNIEnv *env, jobject thiz) {

    auto *instance = new ServerSocket();
    instance->connect();
    return reinterpret_cast<jlong>(instance);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_meicet_network_1hp_ApiNet_nativeServerDestroy(JNIEnv *env, jobject thiz,
                                                       jlong api_net_id) {
    ServerSocket *socket = reinterpret_cast<ServerSocket *>(api_net_id);
    LOGCATE("Destroy socket 回收的地址为：%p", socket);
    if (socket) {
        socket->onDestroy(env);
        delete socket;
    }


}

extern "C"
JNIEXPORT void JNICALL
Java_com_meicet_network_1hp_ApiNet_nativeStopServerNet(JNIEnv *env, jobject thiz) {


}extern "C"
JNIEXPORT void JNICALL
Java_com_meicet_network_1hp_ApiNet_nativeKeyDataCallBack(JNIEnv *env, jobject thiz,
                                                         jlong api_net_server_id,
                                                         jobject frame_data_callback) {

    ServerSocket *socket = reinterpret_cast<ServerSocket *>(api_net_server_id);
    socket->keyDataCallBack(env, frame_data_callback);
}