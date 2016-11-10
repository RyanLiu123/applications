#
# file create by liunianliang 20160303
#

LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := $(call all-java-files-under, src)

LOCAL_PACKAGE_NAME := FciDownload
#LOCAL_STATIC_JAVA_LIBRARIES += android-support-v4

LOCAL_CERTIFICATE := platform

#LOCAL_SDK_VERSION := current

#LOCAL_PROGUARD_FLAG_FILES := proguard.flags

include $(BUILD_PACKAGE)
