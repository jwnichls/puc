/*
* INTEL CONFIDENTIAL
* Copyright (c) 2002, 2003 Intel Corporation.  All rights reserved.
* 
* The source code contained or described herein and all documents
* related to the source code ("Material") are owned by Intel
* Corporation or its suppliers or licensors.  Title to the
* Material remains with Intel Corporation or its suppliers and
* licensors.  The Material contains trade secrets and proprietary
* and confidential information of Intel or its suppliers and
* licensors. The Material is protected by worldwide copyright and
* trade secret laws and treaty provisions.  No part of the Material
* may be used, copied, reproduced, modified, published, uploaded,
* posted, transmitted, distributed, or disclosed in any way without
* Intel's prior express written permission.

* No license under any patent, copyright, trade secret or other
* intellectual property right is granted to or conferred upon you
* by disclosure or delivery of the Materials, either expressly, by
* implication, inducement, estoppel or otherwise. Any license
* under such intellectual property rights must be express and
* approved by Intel in writing.
* 
* $Workfile: UPnPControlPoint.h
* $Revision: #1.0.1200.32486
* $Author:   Intel Corporation, Intel Device Builder
* $Date:     Thursday, June 12, 2003
*
*/
#ifndef __UPnPControlPoint__
#define __UPnPControlPoint__

struct UPnPDevice
{
	void* CP;
	char* DeviceType;
	char* UDN;
	
	char* LocationURL;
	char* PresentationURL;
	char* FriendlyName;
	char* ManufacturerName;
	char* ManufacturerURL;
	char* ModelName;
	char* ModelDescription;
	char* ModelNumber;
	char* ModelURL;
	
	int SCPDError;
	int SCPDLeft;
	int ReferenceCount;
	char* InterfaceToHost;
	int CacheTime;
	void *Tag;
	
	struct UPnPDevice *Parent;
	struct UPnPDevice *EmbeddedDevices;
	struct UPnPService *Services;
	struct UPnPDevice *Next;
};

struct UPnPService
{
	char* ServiceType;
	char* ServiceId;
	char* ControlURL;
	char* SubscriptionURL;
	char* SCPDURL;
	char* SubscriptionID;
	
	struct UPnPAction *Actions;
	struct UPnPStateVariable *Variables;
	struct UPnPDevice *Parent;
	struct UPnPService *Next;
};

struct UPnPStateVariable
{
	struct UPnPStateVariable *Next;
	struct UPnPService *Parent;
	
	char* Name;
	char **AllowedValues;
	int NumAllowedValues;
	char* Min;
	char* Max;
	char* Step;
};

struct UPnPAction
{
	char* Name;
	struct UPnPAction *Next;
};

struct UPnPAllowedValue
{
	struct UPnPAllowedValue *Next;
	
	char* Value;
};


void UPnPAddRef(struct UPnPDevice *device);
void UPnPRelease(struct UPnPDevice *device);

struct UPnPDevice* UPnPGetDevice1(struct UPnPDevice *device,int index);
int UPnPGetDeviceCount(struct UPnPDevice *device);
struct UPnPDevice* UPnPGetDeviceAtUDN(void *v_CP,char* UDN);

void PrintUPnPDevice(int indents, struct UPnPDevice *device);

void *UPnPCreateControlPoint(void *Chain, void(*A)(struct UPnPDevice*),void(*R)(struct UPnPDevice*));
void UPnP_CP_IPAddressListChanged(void *CPToken);
struct UPnPDevice* UPnPGetDevice(struct UPnPDevice *device, char* DeviceType, int number);
int UPnPHasAction(struct UPnPService *s, char* action);
void UPnPSubscribeForUPnPEvents(struct UPnPService *service, void(*callbackPtr)(struct UPnPService* service,int OK));
struct UPnPService *UPnPGetService(struct UPnPDevice *device, char* ServiceName, int length);
struct UPnPService *UPnPGetService_Focus(struct UPnPDevice *device);
struct UPnPService *UPnPGetService_Preset(struct UPnPDevice *device);
struct UPnPService *UPnPGetService_DigitalSecurityCameraSettings(struct UPnPDevice *device);
struct UPnPService *UPnPGetService_DigitalSecurityCameraStillImage(struct UPnPDevice *device);
struct UPnPService *UPnPGetService_PanTilt(struct UPnPDevice *device);
struct UPnPService *UPnPGetService_DigitalSecurityCameraMotionImage(struct UPnPDevice *device);

extern void (*UPnPEventCallback_Focus_IrisPosition)(struct UPnPService* Service,unsigned int IrisPosition);
extern void (*UPnPEventCallback_Focus_FocusPosition)(struct UPnPService* Service,unsigned int FocusPosition);
extern void (*UPnPEventCallback_Focus_ZoomPosition)(struct UPnPService* Service,unsigned int ZoomPosition);
extern void (*UPnPEventCallback_DigitalSecurityCameraSettings_Brightness)(struct UPnPService* Service,unsigned int Brightness);
extern void (*UPnPEventCallback_DigitalSecurityCameraSettings_FixedWhiteBalance)(struct UPnPService* Service,unsigned int FixedWhiteBalance);
extern void (*UPnPEventCallback_DigitalSecurityCameraSettings_ColorSaturation)(struct UPnPService* Service,unsigned int ColorSaturation);
extern void (*UPnPEventCallback_DigitalSecurityCameraStillImage_DefaultResolution)(struct UPnPService* Service,char* DefaultResolution);
extern void (*UPnPEventCallback_DigitalSecurityCameraStillImage_DefaultEncoding)(struct UPnPService* Service,char* DefaultEncoding);
extern void (*UPnPEventCallback_DigitalSecurityCameraStillImage_DefaultCompressionLevel)(struct UPnPService* Service,char* DefaultCompressionLevel);
extern void (*UPnPEventCallback_PanTilt_TiltPosition)(struct UPnPService* Service,short TiltPosition);
extern void (*UPnPEventCallback_PanTilt_PanPosition)(struct UPnPService* Service,short PanPosition);
extern void (*UPnPEventCallback_DigitalSecurityCameraMotionImage_MaxBandwith)(struct UPnPService* Service,unsigned int MaxBandwith);
extern void (*UPnPEventCallback_DigitalSecurityCameraMotionImage_DefaultResolution)(struct UPnPService* Service,char* DefaultResolution);
extern void (*UPnPEventCallback_DigitalSecurityCameraMotionImage_DefaultEncoding)(struct UPnPService* Service,char* DefaultEncoding);
extern void (*UPnPEventCallback_DigitalSecurityCameraMotionImage_DefaultCompressionLevel)(struct UPnPService* Service,char* DefaultCompressionLevel);
extern void (*UPnPEventCallback_DigitalSecurityCameraMotionImage_TargetFrameRate)(struct UPnPService* Service,unsigned int TargetFrameRate);

void UPnPInvoke_Focus_SetAbsFocusPosition(struct UPnPService *service, void (*CallbackPtr)(struct UPnPService*,int ErrorCode,void *user),void* _user, unsigned int FocusSteps);
void UPnPInvoke_Focus_SetRelZoomPosition(struct UPnPService *service, void (*CallbackPtr)(struct UPnPService*,int ErrorCode,void *user),void* _user, int ZoomSteps);
void UPnPInvoke_Focus_SetAbsZoomPosition(struct UPnPService *service, void (*CallbackPtr)(struct UPnPService*,int ErrorCode,void *user),void* _user, unsigned int ZoomSteps);
void UPnPInvoke_Focus_GetIrisPosition(struct UPnPService *service, void (*CallbackPtr)(struct UPnPService*,int ErrorCode,void *user,unsigned int RetIrisSteps),void* _user);
void UPnPInvoke_Focus_SetAbsIrisPosition(struct UPnPService *service, void (*CallbackPtr)(struct UPnPService*,int ErrorCode,void *user),void* _user, unsigned int IrisSteps);
void UPnPInvoke_Focus_GetFocusPosition(struct UPnPService *service, void (*CallbackPtr)(struct UPnPService*,int ErrorCode,void *user,unsigned int RetFocusSteps),void* _user);
void UPnPInvoke_Focus_GetZoomPosition(struct UPnPService *service, void (*CallbackPtr)(struct UPnPService*,int ErrorCode,void *user,unsigned int RetZoomSteps),void* _user);
void UPnPInvoke_Focus_SetRelIrisPosition(struct UPnPService *service, void (*CallbackPtr)(struct UPnPService*,int ErrorCode,void *user),void* _user, int IrisSteps);
void UPnPInvoke_Focus_SetRelFocusPosition(struct UPnPService *service, void (*CallbackPtr)(struct UPnPService*,int ErrorCode,void *user),void* _user, int FocusSteps);
void UPnPInvoke_Preset_GetPresetList(struct UPnPService *service, void (*CallbackPtr)(struct UPnPService*,int ErrorCode,void *user,char* RetPresetList),void* _user);
void UPnPInvoke_Preset_RemovePreset(struct UPnPService *service, void (*CallbackPtr)(struct UPnPService*,int ErrorCode,void *user),void* _user, unsigned char PresetNumber);
void UPnPInvoke_Preset_GoToPreset(struct UPnPService *service, void (*CallbackPtr)(struct UPnPService*,int ErrorCode,void *user),void* _user, unsigned char PresetNumber);
void UPnPInvoke_Preset_SetPreset(struct UPnPService *service, void (*CallbackPtr)(struct UPnPService*,int ErrorCode,void *user),void* _user, unsigned char PresetNumber, char* PresetName);
void UPnPInvoke_DigitalSecurityCameraSettings_GetColorSaturation(struct UPnPService *service, void (*CallbackPtr)(struct UPnPService*,int ErrorCode,void *user,unsigned int RetColorSaturation),void* _user);
void UPnPInvoke_DigitalSecurityCameraSettings_SetBrightness(struct UPnPService *service, void (*CallbackPtr)(struct UPnPService*,int ErrorCode,void *user),void* _user, unsigned int NewBrightness);
void UPnPInvoke_DigitalSecurityCameraSettings_GetFixedWhiteBalance(struct UPnPService *service, void (*CallbackPtr)(struct UPnPService*,int ErrorCode,void *user,unsigned int RetFixedWhiteBalance),void* _user);
void UPnPInvoke_DigitalSecurityCameraSettings_IncreaseBrightness(struct UPnPService *service, void (*CallbackPtr)(struct UPnPService*,int ErrorCode,void *user),void* _user);
void UPnPInvoke_DigitalSecurityCameraSettings_DecreaseBrightness(struct UPnPService *service, void (*CallbackPtr)(struct UPnPService*,int ErrorCode,void *user),void* _user);
void UPnPInvoke_DigitalSecurityCameraSettings_DecreaseColorSaturation(struct UPnPService *service, void (*CallbackPtr)(struct UPnPService*,int ErrorCode,void *user),void* _user);
void UPnPInvoke_DigitalSecurityCameraSettings_SetColorSaturation(struct UPnPService *service, void (*CallbackPtr)(struct UPnPService*,int ErrorCode,void *user),void* _user, unsigned int NewColorSaturation);
void UPnPInvoke_DigitalSecurityCameraSettings_SetFixedWhiteBalance(struct UPnPService *service, void (*CallbackPtr)(struct UPnPService*,int ErrorCode,void *user),void* _user, unsigned int NewFixedWhiteBalance);
void UPnPInvoke_DigitalSecurityCameraSettings_IncreaseColorSaturation(struct UPnPService *service, void (*CallbackPtr)(struct UPnPService*,int ErrorCode,void *user),void* _user);
void UPnPInvoke_DigitalSecurityCameraSettings_GetBrightness(struct UPnPService *service, void (*CallbackPtr)(struct UPnPService*,int ErrorCode,void *user,unsigned int RetBrightness),void* _user);
void UPnPInvoke_DigitalSecurityCameraStillImage_SetDefaultCompressionLevel(struct UPnPService *service, void (*CallbackPtr)(struct UPnPService*,int ErrorCode,void *user),void* _user, char* ReqCompressionLevel);
void UPnPInvoke_DigitalSecurityCameraStillImage_GetAvailableEncodings(struct UPnPService *service, void (*CallbackPtr)(struct UPnPService*,int ErrorCode,void *user,char* RetAvailableEncodings),void* _user);
void UPnPInvoke_DigitalSecurityCameraStillImage_GetImagePresentationURL(struct UPnPService *service, void (*CallbackPtr)(struct UPnPService*,int ErrorCode,void *user,char* RetImagePresentationURL),void* _user, char* ReqEncoding, char* ReqCompression, char* ReqResolution);
void UPnPInvoke_DigitalSecurityCameraStillImage_GetAvailableResolutions(struct UPnPService *service, void (*CallbackPtr)(struct UPnPService*,int ErrorCode,void *user,char* RetAvailableResolutions),void* _user);
void UPnPInvoke_DigitalSecurityCameraStillImage_GetImageURL(struct UPnPService *service, void (*CallbackPtr)(struct UPnPService*,int ErrorCode,void *user,char* RetImageURL),void* _user, char* ReqEncoding, char* ReqCompression, char* ReqResolution);
void UPnPInvoke_DigitalSecurityCameraStillImage_GetDefaultImageURL(struct UPnPService *service, void (*CallbackPtr)(struct UPnPService*,int ErrorCode,void *user,char* RetImageURL),void* _user);
void UPnPInvoke_DigitalSecurityCameraStillImage_SetDefaultEncoding(struct UPnPService *service, void (*CallbackPtr)(struct UPnPService*,int ErrorCode,void *user),void* _user, char* ReqEncoding);
void UPnPInvoke_DigitalSecurityCameraStillImage_GetAvailableCompressionLevels(struct UPnPService *service, void (*CallbackPtr)(struct UPnPService*,int ErrorCode,void *user,char* RetAvailableCompressionLevels),void* _user);
void UPnPInvoke_DigitalSecurityCameraStillImage_GetDefaultEncoding(struct UPnPService *service, void (*CallbackPtr)(struct UPnPService*,int ErrorCode,void *user,char* RetEncoding),void* _user);
void UPnPInvoke_DigitalSecurityCameraStillImage_SetDefaultResolution(struct UPnPService *service, void (*CallbackPtr)(struct UPnPService*,int ErrorCode,void *user),void* _user, char* ReqResolution);
void UPnPInvoke_DigitalSecurityCameraStillImage_GetDefaultCompressionLevel(struct UPnPService *service, void (*CallbackPtr)(struct UPnPService*,int ErrorCode,void *user,char* RetCompressionLevel),void* _user);
void UPnPInvoke_DigitalSecurityCameraStillImage_GetDefaultImagePresentationURL(struct UPnPService *service, void (*CallbackPtr)(struct UPnPService*,int ErrorCode,void *user,char* RetImagePresentationURL),void* _user);
void UPnPInvoke_DigitalSecurityCameraStillImage_GetDefaultResolution(struct UPnPService *service, void (*CallbackPtr)(struct UPnPService*,int ErrorCode,void *user,char* RetResolution),void* _user);
void UPnPInvoke_PanTilt_GetTiltPosition(struct UPnPService *service, void (*CallbackPtr)(struct UPnPService*,int ErrorCode,void *user,short RetTilt),void* _user);
void UPnPInvoke_PanTilt_GetPanPosition(struct UPnPService *service, void (*CallbackPtr)(struct UPnPService*,int ErrorCode,void *user,short RetPan),void* _user);
void UPnPInvoke_PanTilt_SetRelTiltPosition(struct UPnPService *service, void (*CallbackPtr)(struct UPnPService*,int ErrorCode,void *user),void* _user, short NewTilt);
void UPnPInvoke_PanTilt_SetAbsTiltPosition(struct UPnPService *service, void (*CallbackPtr)(struct UPnPService*,int ErrorCode,void *user),void* _user, short NewTilt);
void UPnPInvoke_PanTilt_SetRelPanPosition(struct UPnPService *service, void (*CallbackPtr)(struct UPnPService*,int ErrorCode,void *user),void* _user, short NewPan);
void UPnPInvoke_PanTilt_SetAbsPanPosition(struct UPnPService *service, void (*CallbackPtr)(struct UPnPService*,int ErrorCode,void *user),void* _user, short NewPan);
void UPnPInvoke_DigitalSecurityCameraMotionImage_GetDefaultVideoPresentationURL(struct UPnPService *service, void (*CallbackPtr)(struct UPnPService*,int ErrorCode,void *user,char* RetVideoPresentationURL),void* _user);
void UPnPInvoke_DigitalSecurityCameraMotionImage_SetDefaultCompressionLevel(struct UPnPService *service, void (*CallbackPtr)(struct UPnPService*,int ErrorCode,void *user),void* _user, char* ReqCompressionLevel);
void UPnPInvoke_DigitalSecurityCameraMotionImage_GetAvailableEncodings(struct UPnPService *service, void (*CallbackPtr)(struct UPnPService*,int ErrorCode,void *user,char* RetAvailableEncodings),void* _user);
void UPnPInvoke_DigitalSecurityCameraMotionImage_GetAvailableResolutions(struct UPnPService *service, void (*CallbackPtr)(struct UPnPService*,int ErrorCode,void *user,char* RetAvailableResolutions),void* _user);
void UPnPInvoke_DigitalSecurityCameraMotionImage_GetVideoURL(struct UPnPService *service, void (*CallbackPtr)(struct UPnPService*,int ErrorCode,void *user,char* RetVideoURL),void* _user, char* ReqEncoding, char* ReqCompression, char* ReqResolution);
void UPnPInvoke_DigitalSecurityCameraMotionImage_SetDefaultEncoding(struct UPnPService *service, void (*CallbackPtr)(struct UPnPService*,int ErrorCode,void *user),void* _user, char* ReqEncoding);
void UPnPInvoke_DigitalSecurityCameraMotionImage_GetAvailableCompressionLevels(struct UPnPService *service, void (*CallbackPtr)(struct UPnPService*,int ErrorCode,void *user,char* RetAvailableCompressionLevels),void* _user);
void UPnPInvoke_DigitalSecurityCameraMotionImage_GetDefaultVideoURL(struct UPnPService *service, void (*CallbackPtr)(struct UPnPService*,int ErrorCode,void *user,char* RetVideoURL),void* _user);
void UPnPInvoke_DigitalSecurityCameraMotionImage_GetDefaultEncoding(struct UPnPService *service, void (*CallbackPtr)(struct UPnPService*,int ErrorCode,void *user,char* RetEncoding),void* _user);
void UPnPInvoke_DigitalSecurityCameraMotionImage_SetDefaultResolution(struct UPnPService *service, void (*CallbackPtr)(struct UPnPService*,int ErrorCode,void *user),void* _user, char* ReqResolution);
void UPnPInvoke_DigitalSecurityCameraMotionImage_GetDefaultCompressionLevel(struct UPnPService *service, void (*CallbackPtr)(struct UPnPService*,int ErrorCode,void *user,char* RetCompressionLevel),void* _user);
void UPnPInvoke_DigitalSecurityCameraMotionImage_GetVideoPresentationURL(struct UPnPService *service, void (*CallbackPtr)(struct UPnPService*,int ErrorCode,void *user,char* RetVideoPresentationURL),void* _user, char* ReqEncoding, char* ReqCompression, char* ReqResolution);
void UPnPInvoke_DigitalSecurityCameraMotionImage_GetDefaultResolution(struct UPnPService *service, void (*CallbackPtr)(struct UPnPService*,int ErrorCode,void *user,char* RetResolution),void* _user);

#endif
