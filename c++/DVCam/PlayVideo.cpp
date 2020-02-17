// PlayVideo.cpp : Defines the entry point for the console application.
//

#include "stdafx.h"
#include <jni.h>
#include "DVCamera.h"


struct _DVControl
{
    IAMExtDevice       *pDevice;
    IAMExtTransport    *pTransport;
    IAMTimecodeReader  *pTimecode;
    BOOL                bHasDevice;
    BOOL                bHasTransport;
    BOOL                bHasTimecode;
	long				deviceMode;
} DVControl;

// Exception-throwing function

void throwException(JNIEnv *env, char *message)
{
    jclass deviceExceptionClass;

    deviceExceptionClass = env->FindClass("edu/cmu/hcii/puc/devices/DeviceException");
    if (deviceExceptionClass == 0) { /* Unable to find the exception class, give up. */
      return;
    }
    env->ThrowNew(deviceExceptionClass, message);
}

// Initialization

JNIEXPORT void JNICALL Java_edu_cmu_hcii_puc_devices_DVCamera_init
  (JNIEnv *env, jobject obj)
{
	HRESULT hr;

	// Initialize COM
	hr = CoInitialize(NULL);

	if (hr != S_OK && hr != S_FALSE)
	{
		throwException(env, "Could not initialize COM");
		return;
	}

	// Create the System Device Enumerator.
	ICreateDevEnum *pSysDevEnum = NULL;
	hr = CoCreateInstance(CLSID_SystemDeviceEnum, NULL, CLSCTX_ALL, 
		IID_ICreateDevEnum, (void **)&pSysDevEnum);

	if (hr != S_OK)
	{
		throwException(env, "Could not create System Device Enumberator");
		return;
	}

	// Obtain a class enumerator for the video compressor category.
	IEnumMoniker *pEnumCat = NULL;
	hr = pSysDevEnum->CreateClassEnumerator(CLSID_VideoInputDeviceCategory,
		&pEnumCat, 0);

	if (hr != S_OK)
	{
		throwException(env, "Failed to obtain class enumerator");
		return;
	}

	// Get the first moniker (assume it's our camera, for now)
    IMoniker *pMoniker;
    ULONG cFetched;
    if (pEnumCat->Next(1, &pMoniker, &cFetched) == S_OK)
	{
		// Get an instance of the filter
		IBaseFilter *pFilter;
		pMoniker->BindToObject(NULL, NULL, IID_IBaseFilter, (void**)&pFilter);

		// Fill in the DVControl elements
		hr = pFilter->QueryInterface(IID_IAMExtDevice, (void**)&DVControl.pDevice);
		DVControl.bHasDevice = (SUCCEEDED(hr));

		hr = pFilter->QueryInterface(IID_IAMExtTransport, (void**)&DVControl.pTransport);
		DVControl.bHasTransport = (SUCCEEDED(hr));

		hr = pFilter->QueryInterface(IID_IAMTimecodeReader, (void**)&DVControl.pTimecode);
		DVControl.bHasTimecode = (SUCCEEDED(hr));
	}

	hr = DVControl.pDevice->GetCapability(ED_DEVCAP_DEVICE_TYPE, &(DVControl.deviceMode), NULL);
}

// Information Access

JNIEXPORT jboolean JNICALL Java_edu_cmu_hcii_puc_devices_DVCamera_nativeIsPowerOn
  (JNIEnv *env, jobject obj)
{
	long powerMode;
	HRESULT hr;
	hr = DVControl.pDevice->get_DevicePower(&powerMode);
	if (hr == S_OK)
	{
		if (powerMode == ED_POWER_ON) return JNI_TRUE;
	}
	else throwException(env, "Error getting device power state");
	return JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_edu_cmu_hcii_puc_devices_DVCamera_nativeGetDeviceMode
  (JNIEnv *env, jobject obj)
{
	HRESULT hr;
	hr = DVControl.pDevice->GetCapability(ED_DEVCAP_DEVICE_TYPE, &(DVControl.deviceMode), NULL);
	if (hr == S_OK)
	{
		if (DVControl.deviceMode == ED_DEVTYPE_VCR) return JNI_TRUE;
	}
	else throwException(env, "Error getting device mode");
	return JNI_FALSE;
}

JNIEXPORT jint JNICALL Java_edu_cmu_hcii_puc_devices_DVCamera_nativeGetMedia
  (JNIEnv *env, jobject obj)
{
	long mediaType;
	HRESULT hr;
	hr = DVControl.pTransport->GetStatus(ED_MEDIA_TYPE, &mediaType);

	if (hr == S_OK)
	{
		switch (mediaType)
		{
		case ED_MEDIA_VHS:
			return (jint) edu_cmu_hcii_puc_devices_DVCamera_MEDIA_VHS;
		case ED_MEDIA_DVC:
			return (jint) edu_cmu_hcii_puc_devices_DVCamera_MEDIA_DV;
		case ED_MEDIA_UNKNOWN:
			return (jint) edu_cmu_hcii_puc_devices_DVCamera_MEDIA_UNKNOWN;
		case ED_MEDIA_NOT_PRESENT:
			return (jint) edu_cmu_hcii_puc_devices_DVCamera_MEDIA_NONE;
		default:
			return (jint) edu_cmu_hcii_puc_devices_DVCamera_MEDIA_UNKNOWN;
		}
	}
	else throwException(env, "Error getting media type");
	return (jint) edu_cmu_hcii_puc_devices_DVCamera_MEDIA_UNKNOWN;
}

JNIEXPORT jstring JNICALL Java_edu_cmu_hcii_puc_devices_DVCamera_nativeGetAVCVersion
  (JNIEnv *env, jobject obj)
{
	LPOLESTR avcVersion;
	HRESULT hr;
	hr = DVControl.pDevice->get_ExternalDeviceVersion(&avcVersion);

	if (hr == S_OK)
	{
		if (avcVersion == NULL) avcVersion = (LPOLESTR)"";
		return env->NewStringUTF((char *)avcVersion);
	}
	else throwException(env, "Error getting AV/C version");
	return env->NewStringUTF("");
}

JNIEXPORT jfloat JNICALL Java_edu_cmu_hcii_puc_devices_DVCamera_nativeGetFrameRate
  (JNIEnv *env, jobject obj)
{
	long frameRate;
	HRESULT hr;
	hr = DVControl.pDevice->GetCapability(ED_DEVCAP_NORMAL_RATE, &frameRate, NULL);
	
	if (hr == S_OK)
	{
		if (frameRate == ED_RATE_25) return (jfloat)25.0;
		else if (frameRate == ED_RATE_2997) return (jfloat)29.97;
		else return (jfloat)0;
	}
	else throwException(env, "Error getting frame rate");
	return (jfloat)0;
}

JNIEXPORT jstring JNICALL Java_edu_cmu_hcii_puc_devices_DVCamera_nativeGetPort
  (JNIEnv *env, jobject obj)
{
	long devicePort;
	HRESULT hr;
	hr = DVControl.pDevice->get_DevicePort(&devicePort);
	
	if (hr == S_OK)
	{
		const char *portName;
		if (devicePort == DEV_PORT_1394) portName = "IEEE 1394";
		else portName = "Unknown port";
		return env->NewStringUTF(portName);
	}
	else throwException(env, "Error getting port");
	return env->NewStringUTF("");
}

JNIEXPORT jstring JNICALL Java_edu_cmu_hcii_puc_devices_DVCamera_nativeGetTimecode
  (JNIEnv *env, jobject obj)
{
	TIMECODE_SAMPLE TimecodeSample;
	TimecodeSample.timecode.dwFrames = 0;
	TimecodeSample.dwFlags = ED_DEVCAP_TIMECODE_READ;
	HRESULT hr;
	hr = DVControl.pTimecode->GetTimecode(&TimecodeSample);

	if (hr == S_OK)
	{
		// Unpack the BCD:
		int iHours, iMinutes, iSeconds, iFrames;
		iHours   = (TimecodeSample.timecode.dwFrames & 0xFF000000) >> 24;
		iMinutes = (TimecodeSample.timecode.dwFrames & 0x00FF0000) >> 16;
		iSeconds = (TimecodeSample.timecode.dwFrames & 0x0000FF00) >> 8;
		iFrames  =  TimecodeSample.timecode.dwFrames & 0x000000FF;

		char output[8];
		if (iHours < 0 || iHours > 9 || iSeconds == 0xA) 
		// This probably means there was an error reading the timecode
		{
			strcpy(output, "-:--:--");
		}
		else
		{
			sprintf(output,"%.1x:%.2x:%.2x", iHours, iMinutes, iSeconds);
		}

		return env->NewStringUTF(output);
	}
	else if (hr == 0x800704d3L) // Error code from device not being present
	{
		printf("DVControl.pTimecode->GetTimecode returned %x\n", hr);
		throwException(env, "Error getting timecode");
	}
	return env->NewStringUTF("");
}

JNIEXPORT jint JNICALL Java_edu_cmu_hcii_puc_devices_DVCamera_nativeGetTransportMode
  (JNIEnv *env, jobject obj)
{
	long mode;
	HRESULT hr;
	hr = DVControl.pTransport->get_Mode(&mode);

	if (hr == S_OK)
	{
		switch (mode)
		{
		case ED_MODE_PLAY:
			return (jint) edu_cmu_hcii_puc_devices_DVCamera_T_MODE_PLAY;
		case ED_MODE_STOP:
			return (jint) edu_cmu_hcii_puc_devices_DVCamera_T_MODE_STOP;
		case ED_MODE_FREEZE:
			return (jint) edu_cmu_hcii_puc_devices_DVCamera_T_MODE_PAUSE;
		case ED_MODE_FF:
			return (jint) edu_cmu_hcii_puc_devices_DVCamera_T_MODE_FF;
		case ED_MODE_REW:
			return (jint) edu_cmu_hcii_puc_devices_DVCamera_T_MODE_REW;
		case ED_MODE_RECORD:
			return (jint) edu_cmu_hcii_puc_devices_DVCamera_T_MODE_RECORD;
		default:
			return (jint) edu_cmu_hcii_puc_devices_DVCamera_T_MODE_STOP;
		}
	}
	else if (hr == 0x800704d3L) // Error code from device not being present
	{
		printf("DVControl.pTransport->get_Mode returned %x\n", hr);
		throwException(env, "Error getting transport mode");
	}

	return (jint) edu_cmu_hcii_puc_devices_DVCamera_T_MODE_STOP;
}

// Commands

JNIEXPORT void JNICALL Java_edu_cmu_hcii_puc_devices_DVCamera_nativePlay
  (JNIEnv *env, jobject obj)
{
	printf("Got to nativePlay...\n");

	HRESULT hr;
	hr = DVControl.pTransport->put_Mode(ED_MODE_PLAY);
	if (hr != S_OK) throwException(env, "Error setting mode to play");
}

JNIEXPORT void JNICALL Java_edu_cmu_hcii_puc_devices_DVCamera_nativeStop
  (JNIEnv *env, jobject obj)
{
	printf("Got to nativeStop...\n");

	HRESULT hr;
	hr = DVControl.pDevice->GetCapability(ED_DEVCAP_DEVICE_TYPE, &(DVControl.deviceMode), NULL);

	if (DVControl.deviceMode == ED_DEVTYPE_CAMERA)
	{
		printf("Attempting to record freeze...\n");
		hr = DVControl.pTransport->put_Mode(ED_MODE_RECORD_FREEZE);
	}
	else
	{
		printf("Attempting to stop...\n");
		hr = DVControl.pTransport->put_Mode(ED_MODE_STOP);
	}
	if (hr != S_OK) throwException(env, "Error setting mode to stop");
}

JNIEXPORT void JNICALL Java_edu_cmu_hcii_puc_devices_DVCamera_nativeRewind
  (JNIEnv *env, jobject obj)
{
	printf("Got to nativeRewind...\n");

	HRESULT hr;
	hr = DVControl.pTransport->put_Mode(ED_MODE_REW);
	if (hr != S_OK) throwException(env, "Error setting mode to rewind");
}

JNIEXPORT void JNICALL Java_edu_cmu_hcii_puc_devices_DVCamera_nativeFastForward
  (JNIEnv *env, jobject obj)
{
	printf("Got to nativeFastForward...\n");

	HRESULT hr;
	hr = DVControl.pTransport->put_Mode(ED_MODE_FF);
	if (hr != S_OK) throwException(env, "Error setting mode to fast forward");
}

JNIEXPORT void JNICALL Java_edu_cmu_hcii_puc_devices_DVCamera_nativePause
  (JNIEnv *env, jobject obj)
{
	printf("Got to nativePause...\n");

	HRESULT hr;
	hr = DVControl.pTransport->put_Mode(ED_MODE_FREEZE);
	if (hr != S_OK) throwException(env, "Error setting mode to pause");
}

JNIEXPORT void JNICALL Java_edu_cmu_hcii_puc_devices_DVCamera_nativeRecord
  (JNIEnv *env, jobject obj)
{
	printf("Got to nativeRecord...\n");

	HRESULT hr;
	hr = DVControl.pTransport->put_Mode(ED_MODE_RECORD);
	if (hr != S_OK) throwException(env, "Error setting mode to record");
}

JNIEXPORT void JNICALL Java_edu_cmu_hcii_puc_devices_DVCamera_nativeStepForward
  (JNIEnv *env, jobject obj)
{
	printf("Got to nativeStepForward...\n");

	HRESULT hr;
	hr = DVControl.pTransport->put_Mode(ED_MODE_STEP_FWD);
	if (hr != S_OK) throwException(env, "Error stepping forward");
}

JNIEXPORT void JNICALL Java_edu_cmu_hcii_puc_devices_DVCamera_nativeStepBack
  (JNIEnv *env, jobject obj)
{
	printf("Got to nativeStepBack...\n");

	HRESULT hr;
	hr = DVControl.pTransport->put_Mode(ED_MODE_STEP_REV);
	if (hr != S_OK) throwException(env, "Error stepping back");
}
