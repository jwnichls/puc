/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class edu_cmu_hcii_puc_devices_UPnP_AxisCamera_CameraControl */

#ifndef _Included_edu_cmu_hcii_puc_devices_UPnP_AxisCamera_CameraControl
#define _Included_edu_cmu_hcii_puc_devices_UPnP_AxisCamera_CameraControl
#ifdef __cplusplus
extern "C" {
#endif

#define GET_RESPONSE 1
#define IGNORE_RESPONSE 0

#define MAX_DEVICES 10

// Method ID constants

#undef METHOD_GET_PRESETS
#define METHOD_GET_PRESETS 1L

// Control method declarations

void getPan();

void setPan(int newPan);

void getTilt();

void setTilt(int newTilt);

void getZoom();

void setZoom(int newZoom);

// Java native method declarations

/*
 * Class:     edu_cmu_hcii_puc_devices_UPnP_AxisCamera_CameraControl
 * Method:    nativeInit
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_edu_cmu_hcii_puc_devices_UPnP_AxisCamera_CameraControl_nativeInit
  (JNIEnv *, jobject);

/*
 * Class:     edu_cmu_hcii_puc_devices_UPnP_AxisCamera_CameraControl
 * Method:    nativeStartCP
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_edu_cmu_hcii_puc_devices_UPnP_AxisCamera_CameraControl_nativeStartCP
  (JNIEnv *, jobject);

/*
 * Class:     edu_cmu_hcii_puc_devices_UPnP_AxisCamera_CameraControl
 * Method:    nativeStopCP
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_edu_cmu_hcii_puc_devices_UPnP_AxisCamera_CameraControl_nativeStopCP
  (JNIEnv *, jobject);

/*
 * Class:     edu_cmu_hcii_puc_devices_UPnP_AxisCamera_CameraControl
 * Method:    nativeGetPan
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_edu_cmu_hcii_puc_devices_UPnP_AxisCamera_CameraControl_nativeGetPan
  (JNIEnv *, jobject);

/*
 * Class:     edu_cmu_hcii_puc_devices_UPnP_AxisCamera_CameraControl
 * Method:    nativeSetPan
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_edu_cmu_hcii_puc_devices_UPnP_AxisCamera_CameraControl_nativeSetPan
  (JNIEnv *, jobject, jint);

/*
 * Class:     edu_cmu_hcii_puc_devices_UPnP_AxisCamera_CameraControl
 * Method:    nativeGetTilt
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_edu_cmu_hcii_puc_devices_UPnP_AxisCamera_CameraControl_nativeGetTilt
  (JNIEnv *, jobject);

/*
 * Class:     edu_cmu_hcii_puc_devices_UPnP_AxisCamera_CameraControl
 * Method:    nativeSetTilt
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_edu_cmu_hcii_puc_devices_UPnP_AxisCamera_CameraControl_nativeSetTilt
  (JNIEnv *, jobject, jint);

/*
 * Class:     edu_cmu_hcii_puc_devices_UPnP_AxisCamera_CameraControl
 * Method:    nativeGetZoom
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_edu_cmu_hcii_puc_devices_UPnP_AxisCamera_CameraControl_nativeGetZoom
  (JNIEnv *, jobject);

/*
 * Class:     edu_cmu_hcii_puc_devices_UPnP_AxisCamera_CameraControl
 * Method:    nativeSetZoom
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_edu_cmu_hcii_puc_devices_UPnP_AxisCamera_CameraControl_nativeSetZoom
  (JNIEnv *, jobject, jint);

/*
 * Class:     edu_cmu_hcii_puc_devices_UPnP_AxisCamera_CameraControl
 * Method:    nativeGetPresets
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_edu_cmu_hcii_puc_devices_UPnP_AxisCamera_CameraControl_nativeGetPresets
  (JNIEnv *, jobject);

/*
 * Class:     edu_cmu_hcii_puc_devices_UPnP_AxisCamera_CameraControl
 * Method:    nativeGoToPreset
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_edu_cmu_hcii_puc_devices_UPnP_AxisCamera_CameraControl_nativeGoToPreset
  (JNIEnv *, jobject, jint);

/*
 * Class:     edu_cmu_hcii_puc_devices_UPnP_AxisCamera_CameraControl
 * Method:    nativeSetPreset
 * Signature: (ILjava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_edu_cmu_hcii_puc_devices_UPnP_AxisCamera_CameraControl_nativeSetPreset
  (JNIEnv *, jobject, jint, jstring);

/*
 * Class:     edu_cmu_hcii_puc_devices_UPnP_AxisCamera_CameraControl
 * Method:    nativeRemovePreset
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_edu_cmu_hcii_puc_devices_UPnP_AxisCamera_CameraControl_nativeRemovePreset
  (JNIEnv *, jobject, jint);

#ifdef __cplusplus
}
#endif
#endif
