/**
 * 2016-08-18
 * hqnvwriter.cpp
 * TODO API for read and write nv
 * liunianliang
 */
#include <unistd.h>
#include <utils/Log.h>
#include <cutils/log.h>
#include <jni.h>
#include <JNIHelp.h>
#include <stdlib.h>
#include "android_runtime/AndroidRuntime.h"

#include <android/log.h>
#include "nv.h"
#ifdef LOG_TAG
#undef LOG_TAG
#endif
#define LOG_TAG "NVWriter-TAG-JNI"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)

static int SHOW_LOG = 1; // 1 for show log.
static int SHOW_NV_LOG = 0; // 1 for show log.
static const char* const className = "com/huaqin/nv/NvWriter";

jint NativeInit() {
	/* Calling LSM init  */
	if(!Diag_LSM_Init(NULL)) {
		LOGI("Diag_LSM_Init() failed.");
		return -1;
	}

	LOGI("Diag_LSM_Init succeeded. \n");
	/* Register the callback for the primary processor */
	register_callback();
	return 1;
}

jstring CharTojstring(JNIEnv* env,const char* str) {
	jsize len = strlen(str);
	jclass clsstring = env->FindClass("java/lang/String");
	jstring strencode = env->NewStringUTF("utf-8");
	jmethodID mid = env->GetMethodID(clsstring,"<init>","([BLjava/lang/String;)V");
	jbyteArray barr = env->NewByteArray(len);
	env->SetByteArrayRegion(barr,0,len,(jbyte*)str);
	return (jstring)env->NewObject(clsstring,mid,barr,strencode);
}

void android_native_sync(JNIEnv *env) {
	if (SHOW_LOG) {
		LOGI("android_native_sync");
	}
	sync();
}

void android_native_writeflag_NV(JNIEnv *env,jobject this_,jint index,jchar result) {
	if (SHOW_LOG) {
		LOGI("android_native_writeflag_NV");
	}
	if (NativeInit() < 0) {
		return;
	}
	unsigned char tmp[22] = { 0 };
	unsigned char after[20] = { 0 };
	nv_items_enum_type nvId = NV_FACTORY_DATA_3_I;
	memset(tmp, 0, sizeof(tmp));
	memset(after, 0, sizeof(after));
	diag_nv_read(nvId,tmp, sizeof(tmp));
	for(int m=0;m < sizeof(tmp)-3;m++) {
		if (tmp[m+3] == NULL) {
			after[m] = ' ';
		} else {
			after[m] = tmp[m+3];
		}
	}
	LOGI("android_native_writeflag_NV index = %d\n",(int)index);
	after[sizeof(tmp)-3+1] = '\0';
	after[index] = result;
	if (SHOW_NV_LOG) {
		for (int n=0;n < sizeof(after);n++) {
			LOGI("android_native_writeflag_NV,after[%d] = %02x \n",n,after[n]);
		}
	}
	diag_nv_write(nvId,after, sizeof(after));
}

jstring android_native_readflag_NV(JNIEnv *env) {
	if (SHOW_LOG) {
		LOGI("android_native_readflag_NV");
	}
	if (NativeInit() < 0) {
		return NULL;
	}
	unsigned char tmp[23] = { 0 };
	unsigned char after[21] = { 0 };
	memset(tmp, 0, sizeof(tmp));
	memset(after, 0, sizeof(after));
	nv_items_enum_type nvId = NV_FACTORY_DATA_3_I;
	diag_nv_read(nvId,tmp, sizeof(tmp));
	for(int m=0;m < sizeof(tmp)-3;m++) {
		if (tmp[m+3] == NULL) {
			after[m] = ' ';
		} else {
			after[m] = tmp[m+3];
		}
	}
	after[sizeof(tmp)-3+1] = '\0';
	const char* p = (const char*)(char*)after;
	if (SHOW_NV_LOG) {
		for(int i=0;i < sizeof(after);i++) {
			LOGI("android_native_readflag_NV p[%d] = %02x\n",i,p[i]);
		}
	}

	jstring flag_string = CharTojstring(env,p);
	return flag_string;
}

jstring android_native_readSN_NV(JNIEnv *env) {
	if (SHOW_LOG) {
		LOGI("android_native_readSN_NV");
	}
	if (NativeInit() < 0) {
		return NULL;
	}
	unsigned char before[133] = { 0 };
	unsigned char after[131] = { 0 };
	memset(before, 0, sizeof(before));
	memset(after, 0, sizeof(after));
	nv_items_enum_type nvId = NV_FACTORY_DATA_1_I;
	diag_nv_read(nvId,before,sizeof(before));
	for(int m=0;m < sizeof(before)-3;m++) {
		if (before[m+3] == NULL) {
			after[m] = ' ';
		} else {
			after[m] = before[m+3];
		}
	}
	after[sizeof(before)-3+1] = '\0';
	const char* p = (const char*)(char*)after;
	if (SHOW_NV_LOG) {
		for(int i=0;i < sizeof(after);i++) {
			LOGI("android_native_readSN_NV p[%d] = %02x\n",i,p[i]);
		}
	}

	jstring sn_string = CharTojstring(env,p);
	return sn_string;
}

JNINativeMethod gMethods[] = {
		{ "native_sync", "()V",(void *) android_native_sync },
		{ "native_writeflag_NV", "(IC)V",(void*) android_native_writeflag_NV },
		{ "native_readflag_NV", "()Ljava/lang/String;",(void*) android_native_readflag_NV },
		{ "native_readSN_NV", "()Ljava/lang/String;",(void*) android_native_readSN_NV }
};

int registerNativeMethods(JNIEnv* env) {

	jclass clazz;
	clazz = env->FindClass(className);
	if (env->RegisterNatives(clazz, gMethods,
			sizeof(gMethods) / sizeof(gMethods[0])) < 0) {
		return -1;
	}
	return 0;
}

jint JNI_OnLoad(JavaVM* vm, void* reserved) {
	JNIEnv* env = NULL;
	jint result = -1;

	if (vm->GetEnv((void**) &env, JNI_VERSION_1_4) == JNI_OK) {
		if (NULL != env && registerNativeMethods(env) == 0) {
			result = JNI_VERSION_1_4;
		}
	}
	return result;
}
