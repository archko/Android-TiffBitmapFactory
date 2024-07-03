#include <jni.h>
#include <android/log.h>
#include <android/bitmap.h>
#include <stdlib.h>
#include <stdio.h>
#include <tiffio.h>
#include <unistd.h>
#include "NativeExceptions.h"
#include "NativeDecoder.h"

#include "JNIHelpers.h"
/* Header for class NativeTiffBitmapFactory */

#include "Log.h"

//#ifndef _Included_com_archko_tiff_TiffBitmapFactory
//#define _Included_com_archko_tiff_TiffBitmapFactory
//#ifdef __cplusplus
//extern "C" {
//#endif
//extern "C" jobject Java_com_archko_tiff_TiffBitmapFactory_nativeDecodePath
//        (JNIEnv *, jobject, jstring, jobject, jobject);
//
//extern "C" jobject Java_com_archko_tiff_TiffBitmapFactory_nativeDecodeFD
//        (JNIEnv *, jobject, jint, jobject, jobject);
//
//extern "C" void Java_com_archko_tiff_TiffBitmapFactory_nativeClose
//        (JNIEnv *, jobject);
//
//
//#ifdef __cplusplus
//}
//#endif
//#endif

typedef struct ImageInfo {
    int width;
    int height;
    int ori;
} ImageInfo;