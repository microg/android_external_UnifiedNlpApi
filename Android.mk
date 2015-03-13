LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE := UnifiedNlpApi
LOCAL_SRC_FILES := $(call all-java-files-under, unifiednlp-api/src/main/java)
LOCAL_SRC_FILES += unifiednlp-api/src/main/aidl/org/microg/nlp/api/LocationBackend.aidl \
                   unifiednlp-api/src/main/aidl/org/microg/nlp/api/GeocoderBackend.aidl \
                   unifiednlp-api/src/main/aidl/org/microg/nlp/api/LocationCallback.aidl

include $(BUILD_STATIC_JAVA_LIBRARY)
