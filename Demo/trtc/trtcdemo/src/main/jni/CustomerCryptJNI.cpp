//
// Created by putao on 2020/10/5.
//

#include <jni.h>
#include <string>
#include <android/log.h>
#include "TXLiteAVEncodedDataProcessingListener.h"

class TRTCCustomerEncryptor : public liteav::ITXLiteAVEncodedDataProcessingListener {
public:
    TRTCCustomerEncryptor(){
        __android_log_print(ANDROID_LOG_ERROR, "TRTCCustomerEncryptor" ,"construct :%p", this);
    }
    bool didEncodeVideo(liteav::TXLiteAVEncodedData & videoData) {
        if (videoData.processedData && encrypt_key_.size()) {
            XORData(videoData);
            return true;
        }
        return false;
    }

    bool willDecodeVideo(liteav::TXLiteAVEncodedData & videoData) {
        if (videoData.processedData && encrypt_key_.size()) {
            XORData(videoData);
            return true;
        }
        return false;
    }

    bool didEncodeAudio(liteav::TXLiteAVEncodedData & audioData) {
        if (audioData.processedData && encrypt_key_.size()) {
            XORData(audioData);
            return true;
        }
        return false;
    }

    bool willDecodeAudio(liteav::TXLiteAVEncodedData & audioData) {
        if (audioData.processedData && encrypt_key_.size()) {
            XORData(audioData);
            return true;
        }
        return false;
    }

    void XORData(liteav::TXLiteAVEncodedData & encodedData) {
        auto srcData = encodedData.originData->cdata();
        auto keySize = encrypt_key_.size();
        auto dataSize = encodedData.originData->size();
        encodedData.processedData->SetSize(dataSize);
        auto dstData = encodedData.processedData->data();
        for (int i=0; i<dataSize; ++i) {
            dstData[i] = srcData[i] ^ encrypt_key_[i % keySize];
        }
    }

    std::string encrypt_key_;
};

static TRTCCustomerEncryptor s_customerCryptor;

#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT jlong JNICALL Java_com_tencent_liteav_trtcdemo_model_manager_TRTCCustomerCrypt_nativeGetEncodedDataProcessingListener
        (JNIEnv* env, jobject thiz, jstring encryptKey) {
    if (encryptKey == NULL) return 0;
    jsize keyLen = env->GetStringUTFLength(encryptKey);

    if (keyLen != 32) return 0;

    const char *c_str = NULL;

    c_str = env->GetStringUTFChars(encryptKey, NULL);

    if (c_str == NULL)
    {
        return 0;
    }
    s_customerCryptor.encrypt_key_ = c_str;
    env->ReleaseStringUTFChars(encryptKey, c_str);

    return (jlong)&s_customerCryptor;
}

#ifdef __cplusplus
}
#endif

