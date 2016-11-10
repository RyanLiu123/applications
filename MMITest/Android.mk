#
# create by liunianliang for MMITest
#

LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := $(call all-java-files-under, src)
LOCAL_PRIVILEGED_MODULE := true
LOCAL_PACKAGE_NAME := MMITest

LOCAL_JAVA_LIBRARIES := telephony-common mediatek-framework huaqin-framework
LOCAL_STATIC_JAVA_LIBRARIES := com.mediatek.systemui.ext

LOCAL_CERTIFICATE := platform

include $(BUILD_PACKAGE)

include $(call all-makefiles-under,$(LOCAL_PATH))
