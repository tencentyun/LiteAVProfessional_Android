//
// Created by guanyifeng on 7/8/21.
//

#include <jni.h>
#include "ITRTCAudioPacketListener.h"
#include <android/log.h>
#include <string>
#include <cstring>

using namespace liteav;

static JavaVM *gJavaVM;
static jobject gCallBackObj;
static jmethodID gCallBackMid;
static JNIEnv *gEnv;
/**
 * DEMO使用，用于测试网络层回调
 */
class CustomerAudioPacketListenerJNI : public ITRTCAudioPacketListener {
public:
    CustomerAudioPacketListenerJNI() {
        __android_log_print(ANDROID_LOG_WARN, "CustomerAudioPkgListenerJNI" ,"construct :%p", this);
    }

    virtual bool onRecvAudioPacket(TRTCAudioPacket &data) {
        auto extraBuff = data.extraData;
        if (extraBuff) {
            CallJavaCallback(data.userId, extraBuff->cdata(), extraBuff->size());
            size_t size = extraBuff->size();
            std::string strlog;
            auto cdata = extraBuff->cdata();
            for (int i = 0; i < size; i++) {
                char tmp[10];
                sprintf(tmp, "%x", cdata[i]);
                strlog.append(tmp);
            }
            __android_log_print(ANDROID_LOG_INFO, "CustomerAudioPkgListenerJNI" ,"onRecvAudioPacket :%s %s", data.userId, strlog.c_str());
        }
        return true;
    }

    virtual bool onSendAudioPacket(TRTCAudioPacket &data) {
        if (msgLen != 0) {
            auto extraBuff = data.extraData;
            if (extraBuff) {
                __android_log_print(ANDROID_LOG_INFO, "CustomerAudioPkgListenerJNI" ,"onSendAudioPacket :%d", msgLen);

                extraBuff->SetSize(msgLen);
                memcpy(extraBuff->data(), msg, msgLen);
                msgLen = 0;
                return true;
            }
        }
        return false;
    }

    uint8_t msg[80];
    size_t msgLen;
private:
    void CallJavaCallback(const char *userId, const uint8_t *data, size_t dataSize) {
        // 将当前线程添加到Java虚拟机上，返回一个属于当前线程的JNIEnv指针env
        gJavaVM->AttachCurrentThread(&gEnv, NULL);
        if (gEnv != NULL) {
            JNIEnv *env = gEnv;
            jstring jstr = env->NewStringUTF(userId);
            jbyteArray jbyteArray1 = env->NewByteArray(dataSize);
            if (jbyteArray1) {
                env->SetByteArrayRegion(jbyteArray1, 0, dataSize, reinterpret_cast<const jbyte *>(data));
                // 回调到java层
                env->CallVoidMethod(gCallBackObj, gCallBackMid, jstr, jbyteArray1);
                env->DeleteLocalRef(jbyteArray1);
            }
            env->DeleteLocalRef(jstr);
        }
    }
};

static CustomerAudioPacketListenerJNI sCustomerAudioPkgListenerJni;
//static
#ifdef __cplusplus
extern "C" {
#endif
JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {
    __android_log_print(ANDROID_LOG_INFO, "CustomerAudioPkgListenerJNI" ,"JNI_OnLoad :%p", vm);
    gJavaVM = vm;
    return JNI_VERSION_1_6;
}

JNIEXPORT jlong JNICALL
Java_com_tencent_liteav_trtcdemo_model_manager_TRTCAudioPkgListener_nativeGetCustomerAudioPkgListener(JNIEnv *env, jobject thiz) {
    if (thiz) {
        gCallBackObj = env->NewGlobalRef(thiz);
        if (gCallBackObj) {
            jclass cls = env->GetObjectClass(thiz);
            gCallBackMid = env->GetMethodID(cls, "onReceiveAudioPacketExtraData", "(Ljava/lang/String;[B)V");
        }
    }
    return (jlong)&sCustomerAudioPkgListenerJni;
}

JNIEXPORT void JNICALL
Java_com_tencent_liteav_trtcdemo_model_manager_TRTCAudioPkgListener_nativeBindMsgToAudioPkg(JNIEnv *env, jobject thiz, jbyteArray msg) {
    jbyte *buff = env->GetByteArrayElements(msg, 0);
    jsize size = env->GetArrayLength(msg);
    memcpy(sCustomerAudioPkgListenerJni.msg, buff, size);
    sCustomerAudioPkgListenerJni.msgLen = size;
    env->ReleaseByteArrayElements(msg, buff, 0);
}
#ifdef __cplusplus
};
#endif