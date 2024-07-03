#include <jni.h>
#include <android/log.h>
#include <android/bitmap.h>
#include <stdlib.h>
#include <stdio.h>
#include <tiffio.h>
#include <unistd.h>
#include "NativeExceptions.h"
#include "NativeDecoder.h"
/* Header for class NativeTiffBitmapFactory */

#include "Log.h"

#ifndef _Included_org_beyka_tiffbitmapfactory_TiffBitmapFactory
#define _Included_org_beyka_tiffbitmapfactory_TiffBitmapFactory
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     com_example_beyka_tiffexample_TiffBitmapFactory
 * Method:    nativeDecodePath
 * Signature: (Ljava/lang/String;I)Landroid/graphics/Bitmap;
 */
extern "C" jobject Java_org_beyka_tiffbitmapfactory_TiffBitmapFactory_nativeDecodePath
        (JNIEnv *, jobject, jstring, jobject, jobject);

/*
 * Class:     com_example_beyka_tiffexample_TiffBitmapFactory
 * Method:    nativeDecodePath
 * Signature: (Ljava/lang/String;I)Landroid/graphics/Bitmap;
 */
extern "C" jobject Java_org_beyka_tiffbitmapfactory_TiffBitmapFactory_nativeDecodeFD
        (JNIEnv *, jobject, jint, jobject, jobject);

/*
 * Class:     com_example_beyka_tiffexample_TiffBitmapFactory
 * Method:    nativeCloseFd
 * Signature: (Ljava/lang/String;I)Landroid/graphics/Bitmap;
 */
extern "C" void Java_org_beyka_tiffbitmapfactory_TiffBitmapFactory_nativeCloseFd
        (JNIEnv *, jobject);


#ifdef __cplusplus
}
#endif
#endif
