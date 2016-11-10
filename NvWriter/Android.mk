# 
# file creaet by liunianliang for handler nv read and write
#
# 2016.08.18
#
LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_MODULE := NvWriter

LOCAL_JNI_SHARED_LIBRARIES := libhqnvwriter_jni

LOCAL_SRC_FILES := $(call all-java-files-under, src)

LOCAL_CERTIFICATE := platform

include $(BUILD_JAVA_LIBRARY)
#include $(BUILD_STATIC_JAVA_LIBRARY)

#----------------------------------------------------
# Nv read and write jni
#
include $(CLEAR_VARS)
LOCAL_MODULE_TAGS := optional

LOCAL_MODULE := libhqnvwriter_jni

LOCAL_SRC_FILES := $(call all-cpp-files-under,jni)

LOCAL_C_INCLUDES += \
	vendor/qcom/proprietary/fastmmi/libmmi \
	external/libcxx/include \
	external/skia/include/core \
	external/libxml2/include \
	external/icu/icu4c/source/common \
	$(QC_PROP_ROOT)/diag/include \
	$(QC_PROP_ROOT)/diag/src/ \
	$(TARGET_OUT_HEADERS)/common/inc

LOCAL_SHARED_LIBRARIES := \
    libutils \
    libcutils \
    libc \
    libmmi \
    libdiag

include $(BUILD_SHARED_LIBRARY)
