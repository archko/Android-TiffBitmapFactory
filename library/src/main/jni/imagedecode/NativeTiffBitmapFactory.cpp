using namespace std;

#ifdef __cplusplus
extern "C" {
#endif

#include "NativeTiffBitmapFactory.h"
#include <stdio.h>
#include <android/log.h>
#include <jni.h>
#include <stdlib.h>
#include <errno.h>
#include <string.h>

#define JNI_TIFF_FN(A) Java_com_archko_tiff_ ## A

struct fields_t {
    jfieldID context;
};

static fields_t fields;
static NativeDecoder *decoder = NULL;

static NativeDecoder *getNativeDecoder(JNIEnv *env, jclass thiz) {
    NativeDecoder *pDecoder = (NativeDecoder *) env->GetLongField(thiz, fields.context);
    return pDecoder;
}

static void setCacheDecoder(NativeDecoder *pDecoder) {
    if (decoder != NULL) {
        delete decoder;
        decoder = NULL;
    }
    decoder = pDecoder;
}

static NativeDecoder *getCacheDecoder() {
    return decoder;
}

static void setNativeDecoder(JNIEnv *env, jclass thiz, long decoder) {
    NativeDecoder *old = (NativeDecoder *) env->GetLongField(thiz, fields.context);
    //释放之前的
    if (old != NULL) {
        delete old;
        old = NULL;
    }
    env->SetLongField(thiz, fields.context, decoder);
}

jobject JNI_TIFF_FN(TiffBitmapFactory_nativeDecodePath)
        (JNIEnv *env, jobject thiz,
         jstring path, jobject options, jobject listener) {
    jclass clazz = env->GetObjectClass(thiz);
    //decoder = new NativeDecoder(env, clazz, path, options, listener);
    NativeDecoder *pDecoder = getCacheDecoder();
    jobject java_bitmap = pDecoder->getBitmap(path, 0, options, listener);
    //delete(decoder);

    return java_bitmap;
}

jobject JNI_TIFF_FN (TiffBitmapFactory_nativeDecodeFD)
        (JNIEnv *env, jobject obj, jint fd, jobject options,
         jobject listener) {
    jclass clazz = env->GetObjectClass(obj);
    //decoder = new NativeDecoder(env, clazz, fd, options, listener);
    jobject java_bitmap = getCacheDecoder()->getBitmap(NULL, fd, options, listener);
    //delete(decoder);

    return java_bitmap;
}

void JNI_TIFF_FN(TiffBitmapFactory_nativeCloseFd)
        (JNIEnv *env, jobject clazz) {
    if (NULL != decoder) {
        LOGI("delete decoder:%d", (long) decoder);
        delete decoder;
        decoder = NULL;
    }
}

void JNI_TIFF_FN(TiffBitmapFactory_nativeSetup)
        (JNIEnv *env, jobject thiz,
         jstring path,
         jobject options,
         jobject listener) {
    jclass clazz = env->GetObjectClass(thiz);
    fields.context = env->GetFieldID(clazz, "mNativeContext", "J");
    if (fields.context == NULL) {
        return;
    }

    NativeDecoder *pDecoder = new NativeDecoder(env, clazz, path, options, listener);
    if (pDecoder == NULL) {
        //jniThrowException(env, "java/lang/RuntimeException", "Out of memory");
        return;
    }

    LOGI("pDecoder:%d, context:%d", (long) pDecoder, fields.context);
    //env->SetLongField(thiz, fields.context, (long) pDecoder);
    //setNativeDecoder(env, clazz, (long) pDecoder);
    setCacheDecoder(pDecoder);

    env->DeleteLocalRef(clazz);
}

// ================= test jni =================
//将jstring转换成char *
char *jstringToNative(JNIEnv *env, jstring jstr) {
    if ((env)->ExceptionCheck() == JNI_TRUE || jstr == NULL) {
        (env)->ExceptionDescribe();
        (env)->ExceptionClear();
        //printf("jstringToNative函数转换时,传入的参数str为空");
        return NULL;
    }

    jbyteArray bytes = 0;
    jthrowable exc;
    char *result = 0;
    if ((env)->EnsureLocalCapacity(2) < 0) {
        return 0; /* out of memory error */
    }
    jclass jcls_str = (env)->FindClass("java/lang/String");
    jmethodID MID_String_getBytes = (env)->GetMethodID(jcls_str, "getBytes", "()[B");

    bytes = (jbyteArray) (env)->CallObjectMethod(jstr, MID_String_getBytes);
    exc = (env)->ExceptionOccurred();
    if (!exc) {
        jint len = (env)->GetArrayLength(bytes);
        result = (char *) malloc(len + 1);
        if (result == 0) {
            //JNU_ThrowByName( "java/lang/OutOfMemoryError", 	0);
            (env)->DeleteLocalRef(bytes);
            return 0;
        }
        (env)->GetByteArrayRegion(bytes, 0, len, (jbyte *) result);
        result[len] = 0; /* NULL-terminate */
    } else {
        (env)->DeleteLocalRef(exc);
    }
    (env)->DeleteLocalRef(bytes);
    return (char *) result;

}

//将char *  转换成 jstring
jstring nativeTojstring(JNIEnv *env, const char *str) {
    //定义java String类 strClass
    jclass strClass = (env)->FindClass("java/lang/String");
    //获取java String类方法String(byte[],String)的构造器,用于将本地byte[]数组转换为一个新String
    jmethodID ctorID = (env)->GetMethodID(strClass, "<init>", "([BLjava/lang/String;)V");
    //建立byte数组
    jbyteArray bytes = (env)->NewByteArray((jsize) strlen(str));
    //将char* 转换为byte数组
    (env)->SetByteArrayRegion(bytes, 0, (jsize) strlen(str), (jbyte *) str);
    //设置String, 保存语言类型,用于byte数组转换至String时的参数
    jstring encoding = (env)->NewStringUTF("utf-8");
    //将byte数组转换为java String,并输出
    return (jstring) (env)->NewObject(strClass, ctorID, bytes, encoding);
}

/*void JNI_TIFF_FN(TiffBitmapFactory_accessInstanceFiled)
        (JNIEnv *env, jobject thiz, jobject object_in) {
    jclass clazz;//JNIFieldClass类引用
    jfieldID mString_fieldID;//JNIFieldClass类对象变量mString属性ID
    jfieldID mInt_fieldID;//JNIFieldClass类对象变量mInt属性ID

    *//******获取JNIFieldClass类实例变量mString的值并修改********//*
    //1.通过JNIFieldClass类实例object_in获取Class的引用
    clazz = env->GetObjectClass(object_in);
    if (clazz == NULL) {
        LOGE("GetObjectClass failed\n");
        return;
    }

    //2.获取JNIFieldClass类的实例变量mString的属性ID
    //其中第二参数是变量的名称，第三个参数是变量类型的描述符
    mString_fieldID = env->GetFieldID(clazz, "mString", "Ljava/lang/String;");
    if (mString_fieldID == NULL) {
        LOGE("GetFieldID  mString failed\n");
        return;
    }

    //3.获取JNIFieldClass类实例变量mString的值,并打印出来
    jstring j_string = (jstring) env->GetObjectField(object_in, mString_fieldID);
    char *buf = jstringToNative(env, j_string);
    LOGE("object_in.mString : %s\n", buf);
    free(buf);


    //4.修改类实例变量mStriing的值
    char *buf_out = "Hello Java, I am JNI!";
    env->SetObjectField(object_in, mString_fieldID, nativeTojstring(env, buf_out));

    //5.释放局部引用
    env->DeleteLocalRef(j_string);

    *//******获取JNIFieldClass类实例int型变量mInt的值并修改********//*
    //6.获取JNIFieldClass类实例int型变量mString的属性ID
    mInt_fieldID = env->GetFieldID(clazz, "mInt", "I");
    if (mInt_fieldID == NULL) {
        LOGE("GetFieldID  mInt failed\n");
        return;
    }

    //7.获取JNIFieldClass实例变量mInt的值
    jint mInt = env->GetIntField(object_in, mInt_fieldID);
    LOGE("object_in.mInt : %d\n", mInt);


    //8.修改JNIFieldClass实例变量mInt的值
    env->SetIntField(object_in, mInt_fieldID, 100);

    jclass clz = env->GetObjectClass(thiz);
    jfieldID nc = env->GetFieldID(clz, "mNativeContext", "J");
    if (nc == NULL) {
        LOGE("GetFieldID  mNativeContext failed\n");
        return;
    }
    jlong mLong = env->GetLongField(thiz, nc);
    LOGE("thiz.mLong : %d\n", mLong);

    env->SetLongField(thiz, nc, 1000);

    //9.删除局部引用,即对JNIFieldClass的类引用
    env->DeleteLocalRef(clazz);
}*/


#ifdef __cplusplus
}
#endif
