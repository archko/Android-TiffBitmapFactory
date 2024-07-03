#LOCAL_ARM_MODE := arm
cmake_minimum_required(VERSION 3.2.2)
project("tiff")
set(MY_ROOT "${PROJECT_SOURCE_DIR}/../jni/tiff/")
set(ANDROID_ROOT "${PROJECT_SOURCE_DIR}/../jni")

add_library(tiff_image SHARED
        ${ANDROID_ROOT}/imagedecode/TiffImage.cpp
        ${ANDROID_ROOT}/imagedecode/JNIHelpers.cpp
        ${ANDROID_ROOT}/imagedecode/TiffImage_jni.cpp
        ${ANDROID_ROOT}/imagedecode/JavaInputStreamAdaptor.cpp
        ${ANDROID_ROOT}/imagedecode/TiffInputStream.cpp
        ${ANDROID_ROOT}/imagedecode/tiffstream.cpp
)

add_library(jpeg STATIC
        ${ANDROID_ROOT}/jpeg/jcapimin.c
        ${ANDROID_ROOT}/jpeg/jcapistd.c
        ${ANDROID_ROOT}/jpeg/jccoefct.c
        ${ANDROID_ROOT}/jpeg/jccolor.c
        ${ANDROID_ROOT}/jpeg/jcdctmgr.c
        ${ANDROID_ROOT}/jpeg/jchuff.c
        ${ANDROID_ROOT}/jpeg/jcinit.c
        ${ANDROID_ROOT}/jpeg/jcmainct.c
        ${ANDROID_ROOT}/jpeg/jcmarker.c
        ${ANDROID_ROOT}/jpeg/jcmaster.c
        ${ANDROID_ROOT}/jpeg/jcomapi.c
        ${ANDROID_ROOT}/jpeg/jcparam.c
        ${ANDROID_ROOT}/jpeg/jcphuff.c
        ${ANDROID_ROOT}/jpeg/jcprepct.c
        ${ANDROID_ROOT}/jpeg/jcsample.c
        ${ANDROID_ROOT}/jpeg/jctrans.c
        ${ANDROID_ROOT}/jpeg/jdapimin.c
        ${ANDROID_ROOT}/jpeg/jdapistd.c
        ${ANDROID_ROOT}/jpeg/jdatadst.c
        ${ANDROID_ROOT}/jpeg/jdatasrc.c
        ${ANDROID_ROOT}/jpeg/jdcoefct.c
        ${ANDROID_ROOT}/jpeg/jdcolor.c
        ${ANDROID_ROOT}/jpeg/jddctmgr.c
        ${ANDROID_ROOT}/jpeg/jdhuff.c
        ${ANDROID_ROOT}/jpeg/jdinput.c
        ${ANDROID_ROOT}/jpeg/jdmainct.c
        ${ANDROID_ROOT}/jpeg/jdmarker.c
        ${ANDROID_ROOT}/jpeg/jdmaster.c
        ${ANDROID_ROOT}/jpeg/jdmerge.c
        ${ANDROID_ROOT}/jpeg/jdphuff.c
        ${ANDROID_ROOT}/jpeg/jdpostct.c
        ${ANDROID_ROOT}/jpeg/jdsample.c
        ${ANDROID_ROOT}/jpeg/jdtrans.c
        ${ANDROID_ROOT}/jpeg/jerror.c
        ${ANDROID_ROOT}/jpeg/jfdctflt.c
        ${ANDROID_ROOT}/jpeg/jfdctfst.c
        ${ANDROID_ROOT}/jpeg/jfdctint.c
        ${ANDROID_ROOT}/jpeg/jidctflt.c
        ${ANDROID_ROOT}/jpeg/jidctfst.c
        ${ANDROID_ROOT}/jpeg/jidctint.c
        ${ANDROID_ROOT}/jpeg/jidctred.c
        ${ANDROID_ROOT}/jpeg/jquant1.c
        ${ANDROID_ROOT}/jpeg/jquant2.c
        ${ANDROID_ROOT}/jpeg/jutils.c
        ${ANDROID_ROOT}/jpeg/jmemmgr.c
        ${ANDROID_ROOT}/jpeg/jmemnobs.c
)

add_library(tiff STATIC
        ${MY_ROOT}/libtiff/tif_aux.c
        ${MY_ROOT}/libtiff/tif_close.c
        ${MY_ROOT}/libtiff/tif_codec.c
        ${MY_ROOT}/libtiff/tif_color.c
        ${MY_ROOT}/libtiff/tif_compress.c
        ${MY_ROOT}/libtiff/tif_dir.c
        ${MY_ROOT}/libtiff/tif_dirinfo.c
        ${MY_ROOT}/libtiff/tif_dirread.c
        ${MY_ROOT}/libtiff/tif_dirwrite.c
        ${MY_ROOT}/libtiff/tif_dumpmode.c
        ${MY_ROOT}/libtiff/tif_error.c
        ${MY_ROOT}/libtiff/tif_extension.c
        ${MY_ROOT}/libtiff/tif_fax3.c
        ${MY_ROOT}/libtiff/tif_fax3sm.c
        ${MY_ROOT}/libtiff/tif_flush.c
        ${MY_ROOT}/libtiff/tif_getimage.c
        ${MY_ROOT}/libtiff/tif_jbig.c
        ${MY_ROOT}/libtiff/tif_jpeg.c
        ${MY_ROOT}/libtiff/tif_jpeg_12.c
        ${MY_ROOT}/libtiff/tif_luv.c
        ${MY_ROOT}/libtiff/tif_lzma.c
        ${MY_ROOT}/libtiff/tif_lzw.c
        ${MY_ROOT}/libtiff/tif_next.c
        ${MY_ROOT}/libtiff/tif_ojpeg.c
        ${MY_ROOT}/libtiff/tif_open.c
        ${MY_ROOT}/libtiff/tif_packbits.c
        ${MY_ROOT}/libtiff/tif_pixarlog.c
        ${MY_ROOT}/libtiff/tif_predict.c
        ${MY_ROOT}/libtiff/tif_print.c
        ${MY_ROOT}/libtiff/tif_read.c
        ${MY_ROOT}/libtiff/tif_strip.c
        ${MY_ROOT}/libtiff/tif_swab.c
        ${MY_ROOT}/libtiff/tif_thunder.c
        ${MY_ROOT}/libtiff/tif_tile.c
        ${MY_ROOT}/libtiff/tif_version.c
        ${MY_ROOT}/libtiff/tif_warning.c
        ${MY_ROOT}/libtiff/tif_write.c
        ${MY_ROOT}/libtiff/tif_zip.c
)

#jpeg lib
target_include_directories(tiff PUBLIC ${MY_ROOT}/libtiff/)

target_include_directories(tiff PUBLIC ${MY_ROOT}/libtiff/tiff_config_headers/arm64-v8a/)
target_include_directories(tiff PUBLIC ${MY_ROOT}/contrib/stream/)
target_include_directories(tiff PUBLIC ${ANDROID_ROOT}/jpeg/)

# -DDEBUGLVL=6 -DRUNTIME_DEBUG_ONLY
target_compile_definitions(tiff PRIVATE -DHAVE_PTHREAD -DNO_LIBGCC_HOOKS -DHAVE_STDINT_H -DHAVE_NAMESPACES -DHAVE_STDINCLUDES)
#mbstate and wchar
target_compile_definitions(tiff PRIVATE -DHAS_WCHAR -DHAVE_WCHAR_H -DHAVE_MBSTATE_T -DHAS_MBSTATE)
#for ernno
target_compile_definitions(tiff PRIVATE -DUNIX)


#tiff_image Android
target_include_directories(tiff_image PUBLIC ${MY_ROOT}/libtiff/)

target_compile_definitions(tiff_image PRIVATE -DORION_FOR_ANDROID)
target_compile_options(tiff_image PRIVATE -fexceptions -frtti)


target_link_libraries(tiff_image tiff jpeg -lm -llog -ljnigraphics)