//
// Created by meicet on 2021/7/19.
//


#include "ServerSocket.h"
#include "utils/utilbase.h"

using namespace std;

pthread_mutex_t __mutex;
jobject jObject;
jmethodID methodID;

void *taskAction(void *pVoid) { //异步线程

    ServerSocket *socket = static_cast<ServerSocket *>(pVoid);
    socket->startServer();

    return nullptr;
}

ServerSocket::ServerSocket() {
    pthread_mutex_init(&__mutex, nullptr);
}

ServerSocket::~ServerSocket() {
    delete socketListener;
    socketListener = nullptr;
    delete server;
    server = nullptr;

    pthread_mutex_destroy(&__mutex);

}

void ServerSocket::keyDataCallBack(JNIEnv *pEnv, jobject jObj) {

    if (!pEnv->IsSameObject(jObject, jObj)) {

        jObject = pEnv->NewGlobalRef(jObj);
        if (jObj) {
            jclass jClazz = pEnv->GetObjectClass(jObj);
            if (jClazz) {
                methodID = pEnv->GetMethodID(jClazz, "onFrame", "([B)V");
                LOGCATV("keyDataCallBack onReceiveCallback jObject=%p methodID=%p ", jObject,
                        methodID);
            }

        }
        pEnv->ExceptionClear();

    }


}

void ServerSocket::connect() {
    pthread_create(&mPid, nullptr, taskAction, this);

}

/**
 * 子线程中初始化服务
 */
void ServerSocket::startServer() {
    pthread_mutex_lock(&__mutex);
    socketListener = new SocketServerListener(onReceiveCallback);
    server = HP_Create_TcpServer(reinterpret_cast<ITcpServerListener *>(socketListener));
    LOGCATI("server instance=%p server=%p", socketListener, server);
    //server->SetKeepAliveTime(TCP_KEEPALIVE_TIME);
    server->Start(nullptr, 40004);//40004
    //sleep(1);
    if (server->HasStarted() || server->GetState() == SS_STARTED) {
        LOGCATI("server state=%d ", server->GetState());

        LOGCATE("server端连接的状态 State=%d autoHand=%d ConnectionCount=%d LastError=%d ErrorDesc=%s",
                server->GetState(),
                server->IsSSLAutoHandShake(),
                server->GetConnectionCount(),
                server->GetLastError(),
                server->GetLastErrorDesc()
        );

    }

    pthread_mutex_unlock(&__mutex);

}


void ServerSocket::onDestroy(JNIEnv *pEnv) {
    if (jObject) {
        pEnv->DeleteGlobalRef(jObject);
        jObject = nullptr;
    }
    if (socketListener) {
        LOGCATE("onDestroy socketListener=%p", socketListener);
        server->Stop();

    }

}

/**
 * typedef void (*OnReceiveCallback)(const BYTE *pData, int iLength)
 * @param pData 按键回调
 * @param iLength
 */
void ServerSocket::onReceiveCallback(const BYTE *pData, int iLength) {
    auto jVm = getVM();
    JNIEnv *jniEnv;
    if (jVm) {
        jint attachResult = jVm->AttachCurrentThread(&jniEnv,
                                                     nullptr); //附加当前异步线程后，会得到一个全新的env,相当于该子线程专用的env
        if (attachResult != JNI_OK) {
            return;
        }

        //反射 回传数据

        if (jObject) {
            LOGCATE("SocketServerListener jObject=%p jniEnv=%p", jObject, jniEnv);
            // auto buffer = jniEnv->NewDirectByteBuffer(&pData, iLength);奔溃
            auto buffer = jniEnv->NewByteArray(iLength);
            jniEnv->SetByteArrayRegion(buffer, 0, iLength,
                                       reinterpret_cast<const jbyte *>(pData));
            jniEnv->CallVoidMethod(jObject, methodID, buffer);
            jniEnv->ExceptionClear();
            jniEnv->DeleteLocalRef(buffer);
        } else {
            LOGCATE("SocketServerListener jObject error");
        }
        jVm->DetachCurrentThread();// 必须解除附加，否则报错
    }

}




