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
* $Date:     Friday, June 06, 2003
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
struct UPnPService *UPnPGetService_SwitchPower(struct UPnPDevice *device);
struct UPnPService *UPnPGetService_DimmingService(struct UPnPDevice *device);

extern void (*UPnPEventCallback_SwitchPower_Status)(struct UPnPService* Service,int Status);
extern void (*UPnPEventCallback_DimmingService_LoadLevelStatus)(struct UPnPService* Service,unsigned char LoadLevelStatus);

void UPnPInvoke_SwitchPower_GetStatus(struct UPnPService *service, void (*CallbackPtr)(struct UPnPService*,int ErrorCode,void *user,int ResultStatus),void* _user);
void UPnPInvoke_SwitchPower_SetTarget(struct UPnPService *service, void (*CallbackPtr)(struct UPnPService*,int ErrorCode,void *user),void* _user, int newTargetValue);
void UPnPInvoke_DimmingService_GetLoadLevelStatus(struct UPnPService *service, void (*CallbackPtr)(struct UPnPService*,int ErrorCode,void *user,unsigned char RetLoadLevelStatus),void* _user);
void UPnPInvoke_DimmingService_GetMinLevel(struct UPnPService *service, void (*CallbackPtr)(struct UPnPService*,int ErrorCode,void *user,unsigned char MinLevel),void* _user);
void UPnPInvoke_DimmingService_SetLoadLevelTarget(struct UPnPService *service, void (*CallbackPtr)(struct UPnPService*,int ErrorCode,void *user),void* _user, unsigned char NewLoadLevelTarget);

#endif
