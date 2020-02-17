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
* $Workfile: UPnPControlPoint.c
* $Revision: #1.0.1200.32486
* $Author:   Intel Corporation, Intel Device Builder
* $Date:     Thursday, June 12, 2003
*
*/
#ifndef MICROSTACK_NO_STDAFX
#include "stdafx.h"
#endif
#include <stdio.h>
#include <stdlib.h>
#include <stddef.h>
#include <string.h>
#include <winsock.h>
#include <wininet.h>
#include <windows.h>
#include <winioctl.h>
#include <winbase.h>
#include "ILibParsers.h"
#include "ILibHTTPClient.h"
#include "ILibSSDPClient.h"
#include "ILibMiniWebServer.h"
#include "UPnPControlPoint.h"
#define sem_t HANDLE
#define sem_init(x,y,z) *x=CreateSemaphore(NULL,z,FD_SETSIZE,NULL)
#define sem_destroy(x) (CloseHandle(*x)==0?1:0)
#define sem_wait(x) WaitForSingleObject(*x,INFINITE)
#define sem_trywait(x) ((WaitForSingleObject(*x,0)==WAIT_OBJECT_0)?0:1)
#define sem_post(x) ReleaseSemaphore(*x,1,NULL)
#define strncasecmp(x,y,z) _strnicmp(x,y,z)
#define INVALID_DATA 0
#define DEBUGSTATEMENT(x)
#define LVL3DEBUG(x)

const char *UPNPCP_SOAP_Header = "POST %s HTTP/1.0\r\nHost: %s:%d\r\nUser-Agent: WINDOWS, UPnP/1.0, Intel MicroStack/1.0.1200\r\nSOAPACTION: \"%s#%s\"\r\nContent-Type: text/xml\r\nContent-Length: %d\r\n\r\n";
const char *UPNPCP_SOAP_BodyHead = "<?xml version=\"1.0\" encoding=\"utf-8\"?><s:Envelope s:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\" xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\"><s:Body><u:";
const char *UPNPCP_SOAP_BodyTail = "></s:Body></s:Envelope>";

void UPnPRenew(void *state);
void UPnPSSDP_Sink(void *sender, char* UDN, int Alive, char* LocationURL, int Timeout,void *cp);

struct CustomUserData
{
	int Timeout;
	char* buffer;
};
struct UPnPCP
{
	void (*PreSelect)(void* object,fd_set *readset, fd_set *writeset, fd_set *errorset, int* blocktime);
	void (*PostSelect)(void* object,int slct, fd_set *readset, fd_set *writeset, fd_set *errorset);
	void (*Destroy)(void* object);
	void (*DiscoverSink)(struct UPnPDevice *device);
	void (*RemoveSink)(struct UPnPDevice *device);
	
	void (*EventCallback_Focus_IrisPosition)(struct UPnPService* Service,unsigned int value);
	void (*EventCallback_Focus_FocusPosition)(struct UPnPService* Service,unsigned int value);
	void (*EventCallback_Focus_ZoomPosition)(struct UPnPService* Service,unsigned int value);
	void (*EventCallback_DigitalSecurityCameraSettings_Brightness)(struct UPnPService* Service,unsigned int value);
	void (*EventCallback_DigitalSecurityCameraSettings_FixedWhiteBalance)(struct UPnPService* Service,unsigned int value);
	void (*EventCallback_DigitalSecurityCameraSettings_ColorSaturation)(struct UPnPService* Service,unsigned int value);
	void (*EventCallback_DigitalSecurityCameraStillImage_DefaultResolution)(struct UPnPService* Service,char* value);
	void (*EventCallback_DigitalSecurityCameraStillImage_DefaultEncoding)(struct UPnPService* Service,char* value);
	void (*EventCallback_DigitalSecurityCameraStillImage_DefaultCompressionLevel)(struct UPnPService* Service,char* value);
	void (*EventCallback_PanTilt_TiltPosition)(struct UPnPService* Service,short value);
	void (*EventCallback_PanTilt_PanPosition)(struct UPnPService* Service,short value);
	void (*EventCallback_DigitalSecurityCameraMotionImage_MaxBandwith)(struct UPnPService* Service,unsigned int value);
	void (*EventCallback_DigitalSecurityCameraMotionImage_DefaultResolution)(struct UPnPService* Service,char* value);
	void (*EventCallback_DigitalSecurityCameraMotionImage_DefaultEncoding)(struct UPnPService* Service,char* value);
	void (*EventCallback_DigitalSecurityCameraMotionImage_DefaultCompressionLevel)(struct UPnPService* Service,char* value);
	void (*EventCallback_DigitalSecurityCameraMotionImage_TargetFrameRate)(struct UPnPService* Service,unsigned int value);
	
	struct UDNMapNode *UDN_Head;
	struct LifeTimeMonitorStruct *LifeTimeMonitor;
	
	void *HTTP;
	void *SSDP;
	void *WebServer;
	
	sem_t DeviceLock;
	void* SIDTable;
	
	void *Chain;
	int RecheckFlag;
	int AddressListLength;
	int *AddressList;
};
void (*UPnPEventCallback_Focus_IrisPosition)(struct UPnPService* Service,unsigned int value);
void (*UPnPEventCallback_Focus_FocusPosition)(struct UPnPService* Service,unsigned int value);
void (*UPnPEventCallback_Focus_ZoomPosition)(struct UPnPService* Service,unsigned int value);
void (*UPnPEventCallback_DigitalSecurityCameraSettings_Brightness)(struct UPnPService* Service,unsigned int value);
void (*UPnPEventCallback_DigitalSecurityCameraSettings_FixedWhiteBalance)(struct UPnPService* Service,unsigned int value);
void (*UPnPEventCallback_DigitalSecurityCameraSettings_ColorSaturation)(struct UPnPService* Service,unsigned int value);
void (*UPnPEventCallback_DigitalSecurityCameraStillImage_DefaultResolution)(struct UPnPService* Service,char* value);
void (*UPnPEventCallback_DigitalSecurityCameraStillImage_DefaultEncoding)(struct UPnPService* Service,char* value);
void (*UPnPEventCallback_DigitalSecurityCameraStillImage_DefaultCompressionLevel)(struct UPnPService* Service,char* value);
void (*UPnPEventCallback_PanTilt_TiltPosition)(struct UPnPService* Service,short value);
void (*UPnPEventCallback_PanTilt_PanPosition)(struct UPnPService* Service,short value);
void (*UPnPEventCallback_DigitalSecurityCameraMotionImage_MaxBandwith)(struct UPnPService* Service,unsigned int value);
void (*UPnPEventCallback_DigitalSecurityCameraMotionImage_DefaultResolution)(struct UPnPService* Service,char* value);
void (*UPnPEventCallback_DigitalSecurityCameraMotionImage_DefaultEncoding)(struct UPnPService* Service,char* value);
void (*UPnPEventCallback_DigitalSecurityCameraMotionImage_DefaultCompressionLevel)(struct UPnPService* Service,char* value);
void (*UPnPEventCallback_DigitalSecurityCameraMotionImage_TargetFrameRate)(struct UPnPService* Service,unsigned int value);

struct InvokeStruct
{
	struct UPnPService *Service;
	void *CallbackPtr;
	void *User;
};
struct UPnPServiceInfo
{
	char* serviceType;
	char* SCPDURL;
	char* controlURL;
	char* eventSubURL;
	char* serviceId;
	struct UPnPServiceInfo *Next;
};
struct UPnP_Stack
{
	void *data;
	struct UPnP_Stack *next;
};

struct UDNMapNode
{
	char* UDN;
	char* RootUDN;
	long TimeStamp;
	struct UPnPDevice *device;
	struct UDNMapNode *Next;
	struct UDNMapNode *Previous;
};

void UPnPDestructUPnPService(struct UPnPService *service)
{
	struct UPnPAction *a1,*a2;
	struct UPnPStateVariable *sv1,*sv2;
	int i;
	
	a1 = service->Actions;
	while(a1!=NULL)
	{
		a2 = a1->Next;
		FREE(a1->Name);
		FREE(a1);
		a1 = a2;
	}
	
	sv1 = service->Variables;
	while(sv1!=NULL)
	{
		sv2 = sv1->Next;
		FREE(sv1->Name);
		if(sv1->Min!=NULL) {FREE(sv1->Min);}
		if(sv1->Max!=NULL) {FREE(sv1->Max);}
		if(sv1->Step!=NULL) {FREE(sv1->Step);}
		for(i=0;i<sv1->NumAllowedValues;++i)
		{
			FREE(sv1->AllowedValues[i]);
		}
		if(sv1->AllowedValues!=NULL) {FREE(sv1->AllowedValues);}
		FREE(sv1);
		sv1 = sv2;
	}
	if(service->ControlURL!=NULL) {FREE(service->ControlURL);}
	if(service->SCPDURL!=NULL) {FREE(service->SCPDURL);}
	if(service->ServiceId!=NULL) {FREE(service->ServiceId);}
	if(service->ServiceType!=NULL) {FREE(service->ServiceType);}
	if(service->SubscriptionURL!=NULL) {FREE(service->SubscriptionURL);}
	if(service->SubscriptionID!=NULL)
	{
		ILibLifeTime_Remove(((struct UPnPCP*)service->Parent->CP)->LifeTimeMonitor,service);
		ILibDeleteEntry(((struct UPnPCP*)service->Parent->CP)->SIDTable,service->SubscriptionID,(int)strlen(service->SubscriptionID));
		FREE(service->SubscriptionID);
		service->SubscriptionID = NULL;
	}
	
	FREE(service);
}
void UPnPDestructUPnPDevice(struct UPnPDevice *device)
{
	struct UPnPDevice *d1,*d2;
	struct UPnPService *s1,*s2;
	
	d1 = device->EmbeddedDevices;
	while(d1!=NULL)
	{
		d2 = d1->Next;
		UPnPDestructUPnPDevice(d1);
		d1 = d2;
	}
	
	s1 = device->Services;
	while(s1!=NULL)
	{
		s2 = s1->Next;
		UPnPDestructUPnPService(s1);
		s1 = s2;
	}
	
	LVL3DEBUG(printf("\r\n\r\nDevice Destructed\r\n");)
	if(device->PresentationURL!=NULL) {FREE(device->PresentationURL);}
	if(device->ManufacturerName!=NULL) {FREE(device->ManufacturerName);}
	if(device->ManufacturerURL!=NULL) {FREE(device->ManufacturerURL);}
	if(device->ModelName!=NULL) {FREE(device->ModelName);}
	if(device->ModelNumber!=NULL) {FREE(device->ModelNumber);}
	if(device->ModelURL!=NULL) {FREE(device->ModelURL);}
	if(device->ModelDescription!=NULL) {FREE(device->ModelDescription);}
	if(device->DeviceType!=NULL) {FREE(device->DeviceType);}
	if(device->FriendlyName!=NULL) {FREE(device->FriendlyName);}
	if(device->LocationURL!=NULL) {FREE(device->LocationURL);}
	if(device->UDN!=NULL) {FREE(device->UDN);}
	if(device->InterfaceToHost!=NULL) {FREE(device->InterfaceToHost);}
	
	FREE(device);
}

void UPnPAddRef(struct UPnPDevice *device)
{
	struct UPnPCP *CP = (struct UPnPCP*)device->CP;
	struct UPnPDevice *d = device;
	sem_wait(&(CP->DeviceLock));
	while(d->Parent!=NULL) {d = d->Parent;}
	++d->ReferenceCount;
	sem_post(&(CP->DeviceLock));
}
void UPnPRelease(struct UPnPDevice *device)
{
	struct UPnPCP *CP = (struct UPnPCP*)device->CP;
	struct UPnPDevice *d = device;
	sem_wait(&(CP->DeviceLock));
	while(d->Parent!=NULL) {d = d->Parent;}
	--d->ReferenceCount;
	if(d->ReferenceCount==0)
	{
		UPnPDestructUPnPDevice(d);
	}
	sem_post(&(CP->DeviceLock));
}
void UPnPDeviceDescriptionInterruptSink(void *sender, void *user1, void *user2)
{
	struct CustomUserData *cd = (struct CustomUserData*)user1;
	FREE(cd->buffer);
	FREE(user1);
}
void UPnPPush(struct UPnP_Stack **pp_Top, void *data)
{
	struct UPnP_Stack *frame = (struct UPnP_Stack*)MALLOC(sizeof(struct UPnP_Stack));
	frame->data = data;
	frame->next = *pp_Top;
	*pp_Top = frame;
}
void *UPnPPop(struct UPnP_Stack **pp_Top)
{
	struct UPnP_Stack *frame = *pp_Top;
	void *RetVal = NULL;
	
	if(frame!=NULL)
	{
		*pp_Top = frame->next;
		RetVal = frame->data;
		FREE(frame);
	}
	return(RetVal);
}
void *UPnPPeek(struct UPnP_Stack **pp_Top)
{
	struct UPnP_Stack *frame = *pp_Top;
	void *RetVal = NULL;
	
	if(frame!=NULL)
	{
		RetVal = (*pp_Top)->data;
	}
	return(RetVal);
}
void UPnPFlush(struct UPnP_Stack **pp_Top)
{
	while(UPnPPop(pp_Top)!=NULL) {}
	*pp_Top = NULL;
}

void UPnPAttachRootUDNToUDN(void *v_CP,char* UDN, char* RootUDN)
{
	struct UDNMapNode *node;
	struct UPnPCP* CP = (struct UPnPCP*)v_CP;
	if(CP->UDN_Head==NULL) {return;}
	
	sem_wait(&(CP->DeviceLock));
	node = CP->UDN_Head;
	while(node!=NULL)
	{
		if(strcmp(node->UDN,UDN)==0)
		{
			if(node->RootUDN!=NULL)
			{
				FREE(node->RootUDN);
			}
			node->RootUDN = MALLOC(1+strlen(RootUDN));
			sprintf(node->RootUDN,"%s",RootUDN);
			break;
		}
		node=node->Next;
	}
	sem_post(&(CP->DeviceLock));
}
void UPnPAttachDeviceToUDN(void *v_CP,char* UDN, struct UPnPDevice *device)
{
	struct UDNMapNode *node;
	struct UPnPCP* CP = (struct UPnPCP*)v_CP;
	if(CP->UDN_Head==NULL) {return;}
	
	sem_wait(&(CP->DeviceLock));
	node = CP->UDN_Head;
	while(node!=NULL)
	{
		if(strcmp(node->UDN,UDN)==0)
		{
			node->device = device;
			if(device->Parent==NULL) {++device->ReferenceCount;}
			break;
		}
		node=node->Next;
	}
	sem_post(&(CP->DeviceLock));
}
void UPnPRemoveUDN(void *v_CP,char* UDN)
{
	struct UPnPCP* CP = (struct UPnPCP*)v_CP;
	struct UPnPDevice *device = NULL;
	struct UDNMapNode *node,*prevNode;
	if(CP->UDN_Head==NULL) {return;}
	
	sem_wait(&(CP->DeviceLock));
	node = CP->UDN_Head;
	prevNode = NULL;
	while (node!=NULL)
	{
		if(strcmp(node->UDN,UDN)==0)
		{
			device = node->device;
			node->device = NULL;
			if(node->RootUDN!=NULL)
			{
				FREE(node->RootUDN);
				node->RootUDN = NULL;
			}
			if(prevNode!=NULL)
			{
				prevNode->Next = node->Next;
				if(node->Next!=NULL)
				{
					node->Next->Previous = prevNode;
				}
			}
			else
			{
				CP->UDN_Head = node->Next;
				if(node->Next!=NULL)
				{
					node->Next->Previous = NULL;
				}
			}
			FREE(node->UDN);
			FREE(node);
			break;
		}
		prevNode = node;
		node=node->Next;
	}
	sem_post(&(CP->DeviceLock));
	
	if(device!=NULL) {UPnPRelease(device);}
}
void UPnPAddUDN(void *v_CP,char *UDN)
{
	struct UPnPCP* CP = (struct UPnPCP*)v_CP;
	struct UDNMapNode *node;
	int has = 0;
	
	sem_wait(&(CP->DeviceLock));
	node = CP->UDN_Head;
	while(node!=NULL)
	{
		if(strcmp(node->UDN,UDN)==0)
		{
			has = -1;
			break;
		}
		node=node->Next;
	}
	if(has==0)
	{
		node = (struct UDNMapNode*)MALLOC(sizeof(struct UDNMapNode));
		node->UDN = (char*)MALLOC((int)strlen(UDN)+1);
		memcpy(node->UDN,UDN,(int)strlen(UDN));
		node->UDN[(int)strlen(UDN)] = '\0';
		node->device = NULL;
		node->RootUDN = NULL;
		node->Next = CP->UDN_Head;
		CP->UDN_Head = node;
		CP->UDN_Head->Previous = NULL;
		if(CP->UDN_Head->Next!=NULL)
		{
			CP->UDN_Head->Next->Previous = CP->UDN_Head;
		}
	}
	sem_post(&(CP->DeviceLock));
}
struct UPnPDevice* UPnPGetDeviceAtUDN(void *v_CP,char* UDN)
{
	struct UDNMapNode *node;
	struct UPnPDevice *RetVal = NULL;
	struct UPnPCP* CP = (struct UPnPCP*)v_CP;
	if(CP->UDN_Head==NULL) {return(NULL);}
	
	sem_wait(&(CP->DeviceLock));
	node = CP->UDN_Head;
	while(node!=NULL)
	{
		if(strcmp(node->UDN,UDN)==0)
		{
			RetVal = node->device;
			if(RetVal!=NULL)
			{
				while(RetVal->Parent!=NULL) {RetVal = RetVal->Parent;}
				++RetVal->ReferenceCount;
				RetVal = node->device;
			}
			break;
		}
		node=node->Next;
	}
	sem_post(&(CP->DeviceLock));
	
	return(RetVal);
}
char* UPnPGetRootUDNAtUDN(void *v_CP,char* UDN)
{
	struct UDNMapNode *node;
	char *RetVal = NULL;
	struct UPnPCP* CP = (struct UPnPCP*)v_CP;
	if(CP->UDN_Head==NULL) {return(NULL);}
	
	sem_wait(&(CP->DeviceLock));
	node = CP->UDN_Head;
	while(node!=NULL)
	{
		if(strcmp(node->UDN,UDN)==0)
		{
			RetVal = node->RootUDN;
			break;
		}
		node=node->Next;
	}
	sem_post(&(CP->DeviceLock));
	
	return(RetVal);
}
int UPnPHasUDN(void *v_CP,char *UDN)
{
	struct UDNMapNode *node;
	struct UPnPCP* CP = (struct UPnPCP*)v_CP;
	int RetVal = 0;
	if(CP->UDN_Head==NULL) {return(0);}
	
	sem_wait(&(CP->DeviceLock));
	node = CP->UDN_Head;
	while(node!=NULL)
	{
		if(strcmp(node->UDN,UDN)==0)
		{
			RetVal = -1;
			break;
		}
		node=node->Next;
	}
	sem_post(&(CP->DeviceLock));
	
	return(RetVal);
}
struct packetheader *UPnPBuildPacket(char* IP, int Port, char* Path, char* cmd)
{
	struct packetheader *RetVal = ILibCreateEmptyPacket();
	char* HostLine = (char*)MALLOC((int)strlen(IP)+7);
	int HostLineLength = sprintf(HostLine,"%s:%d",IP,Port);
	ILibSetDirective(RetVal,cmd,(int)strlen(cmd),Path,(int)strlen(Path));
	ILibAddHeaderLine(RetVal,"Host",4,HostLine,HostLineLength);
	ILibAddHeaderLine(RetVal,"User-Agent",10,"WINDOWS, UPnP/1.0, Intel MicroStack/1.0.1200",44);
	FREE(HostLine);
	return(RetVal);
}

void UPnPRemoveServiceFromDevice(struct UPnPDevice *device, struct UPnPService *service)
{
	struct UPnPService *s = device->Services;
	
	if(s==service)
	{
		device->Services = s->Next;
		UPnPDestructUPnPService(service);
		return;
	}
	while(s->Next!=NULL)
	{
		if(s->Next == service)
		{
			s->Next = s->Next->Next;
			UPnPDestructUPnPService(service);
			return;
		}
		s = s->Next;
	}
}

void UPnPProcessDevice(struct UPnPDevice *device)
{
	int OK = 0;
	struct UPnPService  *s,*s2;
	struct UPnPDevice *EmbeddedDevice = device->EmbeddedDevices;
	while(EmbeddedDevice!=NULL)
	{
		UPnPProcessDevice(EmbeddedDevice);
		EmbeddedDevice = EmbeddedDevice->Next;
	}
	
	if(strncmp(device->DeviceType,"urn:schemas-upnp-org:device:DigitalSecurityCamera:1.0",53)==0)
	{
		s = device->Services;
		while(s!=NULL)
		{
			OK = 0;
			if(strncmp(s->ServiceType,"urn:schemas-upnp-org:service:Focus:1.0",38)==0)
			{
				OK = 1;
			}
			if(strncmp(s->ServiceType,"urn:schemas-upnp-org:service:Preset:1.0",39)==0)
			{
				OK = 1;
			}
			if(strncmp(s->ServiceType,"urn:schemas-upnp-org:service:DigitalSecurityCameraSettings:1.0",62)==0)
			{
				OK = 1;
			}
			if(strncmp(s->ServiceType,"urn:schemas-upnp-org:service:DigitalSecurityCameraStillImage:1.0",64)==0)
			{
				OK = 1;
			}
			if(strncmp(s->ServiceType,"urn:schemas-upnp-org:service:PanTilt:1.0",40)==0)
			{
				OK = 1;
			}
			if(strncmp(s->ServiceType,"urn:schemas-upnp-org:service:DigitalSecurityCameraMotionImage:1.0",65)==0)
			{
				OK = 1;
			}
			s2 = s->Next;
			if(OK==0) {UPnPRemoveServiceFromDevice(device,s);}
			s = s2;
		}
	}
	
}

void UPnPPrintUPnPDevice(int indents, struct UPnPDevice *device)
{
	struct UPnPService *s;
	struct UPnPDevice *d;
	struct UPnPAction *a;
	int x=0;
	
	for(x=0;x<indents;++x) {printf(" ");}
	printf("Device: %s\r\n",device->DeviceType);
	
	for(x=0;x<indents;++x) {printf(" ");}
	printf("Friendly: %s\r\n",device->FriendlyName);
	
	s = device->Services;
	while(s!=NULL)
	{
		for(x=0;x<indents;++x) {printf(" ");}
		printf("   Service: %s\r\n",s->ServiceType);
		a = s->Actions;
		while(a!=NULL)
		{
			for(x=0;x<indents;++x) {printf(" ");}
			printf("      Action: %s\r\n",a->Name);
			a = a->Next;
		}
		s = s->Next;
	}
	
	d = device->EmbeddedDevices;
	while(d!=NULL)
	{
		UPnPPrintUPnPDevice(indents+5,d);
		d = d->Next;
	}
}
struct UPnPService *UPnPGetService(struct UPnPDevice *device, char* ServiceName, int length)
{
	struct UPnPService *RetService = NULL;
	struct UPnPService *s = device->Services;
	while(s!=NULL)
	{
		if((int)strlen(s->ServiceType)==length)
		{
			if(strncmp(s->ServiceType,ServiceName,length)==0)
			{
				RetService = s;
				break;
			}
		}
		s = s->Next;
	}
	
	return(RetService);
}
struct UPnPService *UPnPGetService_Focus(struct UPnPDevice *device)
{
	return(UPnPGetService(device,"urn:schemas-upnp-org:service:Focus:1.0",38));
}
struct UPnPService *UPnPGetService_Preset(struct UPnPDevice *device)
{
	return(UPnPGetService(device,"urn:schemas-upnp-org:service:Preset:1.0",39));
}
struct UPnPService *UPnPGetService_DigitalSecurityCameraSettings(struct UPnPDevice *device)
{
	return(UPnPGetService(device,"urn:schemas-upnp-org:service:DigitalSecurityCameraSettings:1.0",62));
}
struct UPnPService *UPnPGetService_DigitalSecurityCameraStillImage(struct UPnPDevice *device)
{
	return(UPnPGetService(device,"urn:schemas-upnp-org:service:DigitalSecurityCameraStillImage:1.0",64));
}
struct UPnPService *UPnPGetService_PanTilt(struct UPnPDevice *device)
{
	return(UPnPGetService(device,"urn:schemas-upnp-org:service:PanTilt:1.0",40));
}
struct UPnPService *UPnPGetService_DigitalSecurityCameraMotionImage(struct UPnPDevice *device)
{
	return(UPnPGetService(device,"urn:schemas-upnp-org:service:DigitalSecurityCameraMotionImage:1.0",65));
}
struct UPnPDevice *UPnPGetDevice2(struct UPnPDevice *device, int index, int *c_Index)
{
	struct UPnPDevice *RetVal = NULL;
	struct UPnPDevice *e_Device = NULL;
	int currentIndex = *c_Index;
	
	if(strncmp(device->DeviceType,"urn:schemas-upnp-org:device:DigitalSecurityCamera:1.0",53)==0)
	{
		++currentIndex;
		if(currentIndex==index)
		{
			*c_Index = currentIndex;
			return(device);
		}
	}
	
	e_Device = device->EmbeddedDevices;
	while(e_Device!=NULL)
	{
		RetVal = UPnPGetDevice2(e_Device,index,&currentIndex);
		if(RetVal!=NULL)
		{
			break;
		}
		e_Device = e_Device->Next;
	}
	
	*c_Index = currentIndex;
	return(RetVal);
}
struct UPnPDevice* UPnPGetDevice1(struct UPnPDevice *device,int index)
{
	int c_Index = -1;
	return(UPnPGetDevice2(device,index,&c_Index));
}
int UPnPGetDeviceCount(struct UPnPDevice *device)
{
	int RetVal = 0;
	struct UPnPDevice *e_Device = device->EmbeddedDevices;
	
	while(e_Device!=NULL)
	{
		RetVal += UPnPGetDeviceCount(e_Device);
		e_Device = e_Device->Next;
	}
	
	if(strncmp(device->DeviceType,"urn:schemas-upnp-org:device:DigitalSecurityCamera:1.0",53)==0)
	{
		++RetVal;
	}
	return(RetVal);
}

int UPnPGetErrorCode(char *buffer, int length)
{
	int RetVal = 500;
	struct ILibXMLNode *xml,*rootXML;
	
	char *temp;
	int tempLength;
	
	rootXML = xml = ILibParseXML(buffer,0,length);
	ILibProcessXMLNodeList(xml);
	
	while(xml!=NULL)
	{
		if(xml->NameLength==8 && memcmp(xml->Name,"Envelope",8)==0)
		{
			xml = xml->Next;
			while(xml!=NULL)
			{
				if(xml->NameLength==4 && memcmp(xml->Name,"Body",4)==0)
				{
					xml = xml->Next;
					while(xml!=NULL)
					{
						if(xml->NameLength==5 && memcmp(xml->Name,"Fault",5)==0)
						{
							xml = xml->Next;
							while(xml!=NULL)
							{
								if(xml->NameLength==6 && memcmp(xml->Name,"detail",6)==0)
								{
									xml = xml->Next;
									while(xml!=NULL)
									{
										if(xml->NameLength==9 && memcmp(xml->Name,"UPnPError",9)==0)
										{
											xml = xml->Next;
											while(xml!=NULL)
											{
												if(xml->NameLength==9 && memcmp(xml->Name,"errorCode",9)==0)
												{
													tempLength = ILibReadInnerXML(xml,&temp);
													temp[tempLength] = 0;
													RetVal =atoi(temp);
													xml = NULL;
												}
												if(xml!=NULL) {xml = xml->Peer;}
											}
										}
										if(xml!=NULL) {xml = xml->Peer;}
									}
								}
								if(xml!=NULL) {xml = xml->Peer;}
							}
						}
						if(xml!=NULL) {xml = xml->Peer;}
					}
				}
				if(xml!=NULL) {xml = xml->Peer;}
			}
		}
		if(xml!=NULL) {xml = xml->Peer;}
	}
	ILibDestructXMLNodeList(rootXML);
	return(RetVal);
}
void UPnPProcessSCPD(char* buffer, int length, struct UPnPService *service)
{
	struct UPnPAction *action;
	struct UPnPStateVariable *sv = NULL;
	struct UPnPAllowedValue *av = NULL;
	struct UPnPAllowedValue *avs = NULL;
	
	struct ILibXMLNode *xml,*rootXML;
	int flg2,flg3,flg4;
	
	char* tempString;
	int tempStringLength;
	
	rootXML = xml = ILibParseXML(buffer,0,length);
	ILibProcessXMLNodeList(xml);
	
	xml = xml->Next;
	while(xml!=NULL)
	{
		if(xml->NameLength==10 && memcmp(xml->Name,"actionList",10)==0)
		{
			xml = xml->Next;
			flg2 = 0;
			while(flg2==0)
			{
				if(xml->NameLength==6 && memcmp(xml->Name,"action",6)==0)
				{
					action = (struct UPnPAction*)MALLOC(sizeof(struct UPnPAction));
					action->Name = NULL;
					action->Next = service->Actions;
					service->Actions = action;
					
					xml = xml->Next;
					flg3 = 0;
					while(flg3==0)
					{
						if(xml->NameLength==4 && memcmp(xml->Name,"name",4)==0)
						{
							tempStringLength = ILibReadInnerXML(xml,&tempString);
							action->Name = (char*)MALLOC(1+tempStringLength);
							memcpy(action->Name,tempString,tempStringLength);
							action->Name[tempStringLength] = '\0';
						}
						if(xml->Peer==NULL)
						{
							flg3 = -1;
							xml = xml->Parent;
						}
						else
						{
							xml = xml->Peer;
						}
					}
				}
				if(xml->Peer==NULL)
				{
					flg2 = -1;
					xml = xml->Parent;
				}
				else
				{
					xml = xml->Peer;
				}
			}
		}
		if(xml->NameLength==17 && memcmp(xml->Name,"serviceStateTable",17)==0)
		{
			if(xml->Next->StartTag!=0)
			{
				xml = xml->Next;
				flg2 = 0;
				while(flg2==0)
				{
					if(xml->NameLength==13 && memcmp(xml->Name,"stateVariable",13)==0)
					{
						sv = (struct UPnPStateVariable*)MALLOC(sizeof(struct UPnPStateVariable));
						sv->AllowedValues = NULL;
						sv->NumAllowedValues = 0;
						sv->Max = NULL;
						sv->Min = NULL;
						sv->Step = NULL;
						sv->Name = NULL;
						sv->Next = service->Variables;
						service->Variables = sv;
						sv->Parent = service;
						
						xml = xml->Next;
						flg3 = 0;
						while(flg3==0)
						{
							if(xml->NameLength==4 && memcmp(xml->Name,"name",4)==0)
							{
								tempStringLength = ILibReadInnerXML(xml,&tempString);
								sv->Name = (char*)MALLOC(1+tempStringLength);
								memcpy(sv->Name,tempString,tempStringLength);
								sv->Name[tempStringLength] = '\0';
							}
							if(xml->NameLength==16 && memcmp(xml->Name,"allowedValueList",16)==0)
							{
								if(xml->Next->StartTag!=0)
								{
									avs = NULL;
									xml = xml->Next;
									flg4 = 0;
									while(flg4==0)
									{
										if(xml->NameLength==12 && memcmp(xml->Name,"allowedValue",12)==0)
										{
											av = (struct UPnPAllowedValue*)MALLOC(sizeof(struct UPnPAllowedValue));
											av->Next = avs;
											avs = av;
											
											tempStringLength = ILibReadInnerXML(xml,&tempString);
											av->Value = (char*)MALLOC(1+tempStringLength);
											memcpy(av->Value,tempString,tempStringLength);
											av->Value[tempStringLength] = '\0';
										}
										if(xml->Peer!=NULL)
										{
											xml = xml->Peer;
										}
										else
										{
											xml = xml->Parent;
											flg4 = -1;
										}
									}
									av = avs;
									while(av!=NULL)
									{
										++sv->NumAllowedValues;
										av = av->Next;
									}
									av = avs;
									sv->AllowedValues = (char**)MALLOC(sv->NumAllowedValues*sizeof(char*));
									for(flg4=0;flg4<sv->NumAllowedValues;++flg4)
									{
										sv->AllowedValues[flg4] = av->Value;
										av = av->Next;
									}
									av = avs;
									while(av!=NULL)
									{
										avs = av->Next;
										FREE(av);
										av = avs;
									}
								}
							}
							if(xml->NameLength==17 && memcmp(xml->Name,"allowedValueRange",17)==0)
							{
								if(xml->Next->StartTag!=0)
								{
									xml = xml->Next;
									flg4 = 0;
									while(flg4==0)
									{
										if(xml->NameLength==7)
										{
											if(memcmp(xml->Name,"minimum",7)==0)
											{
												tempStringLength = ILibReadInnerXML(xml,&tempString);
												sv->Min = (char*)MALLOC(1+tempStringLength);
												memcpy(sv->Min,tempString,tempStringLength);
												sv->Min[tempStringLength] = '\0';
											}
											else if(memcmp(xml->Name,"maximum",7)==0)
											{
												tempStringLength = ILibReadInnerXML(xml,&tempString);
												sv->Max = (char*)MALLOC(1+tempStringLength);
												memcpy(sv->Max,tempString,tempStringLength);
												sv->Max[tempStringLength] = '\0';
											}
										}
										if(xml->NameLength==4 && memcmp(xml->Name,"step",4)==0)
										{
											tempStringLength = ILibReadInnerXML(xml,&tempString);
											sv->Step = (char*)MALLOC(1+tempStringLength);
											memcpy(sv->Step,tempString,tempStringLength);
											sv->Step[tempStringLength] = '\0';
										}
										if(xml->Peer!=NULL)
										{
											xml = xml->Peer;
										}
										else
										{
											xml = xml->Parent;
											flg4 = -1;
										}
									}
								}
							}
							if(xml->Peer!=NULL)
							{
								xml = xml->Peer;
							}
							else
							{
								flg3 = -1;
								xml = xml->Parent;
							}
						}
					}
					if(xml->Peer!=NULL)
					{
						xml = xml->Peer;
					}
					else
					{
						xml = xml->Parent;
						flg2 = -1;
					}
				}
			}
		}
		xml = xml->Peer;
	}
	
	ILibDestructXMLNodeList(rootXML);
}
void UPnPDeviceExpired(struct UPnPDevice *device)
{
	LVL3DEBUG(printf("Device[%s] failed to re-advertise in a timely manner\r\n",device->FriendlyName);)
	while(device->Parent!=NULL) {device = device->Parent;}
	UPnPSSDP_Sink(NULL, device->UDN, 0, NULL, 0,device->CP);
}
void UPnPFinishProcessingDevice(struct UPnPCP* CP, struct UPnPDevice *RootDevice)
{
	char *RootUDN = RootDevice->UDN;
	int Timeout = RootDevice->CacheTime;
	struct UPnPDevice *RetDevice;
	int i=0;
	
	RootDevice->ReferenceCount = 0;
	UPnPAttachDeviceToUDN(CP,RootDevice->UDN,RootDevice);
	do
	{
		RetDevice = UPnPGetDevice1(RootDevice,i++);
		if(RetDevice!=NULL)
		{
			UPnPAddUDN(CP,RetDevice->UDN);
			UPnPAttachRootUDNToUDN(CP,RetDevice->UDN,RootUDN);
			UPnPAttachDeviceToUDN(CP,RetDevice->UDN,RetDevice);
			if(CP->DiscoverSink!=NULL)
			{
				CP->DiscoverSink(RetDevice);
			}
		}
	}while(RetDevice!=NULL);
	RetDevice = UPnPGetDeviceAtUDN(CP,RootUDN);
	if(RetDevice!=NULL)
	{
		ILibLifeTime_Add(CP->LifeTimeMonitor,RetDevice,Timeout,&UPnPDeviceExpired,NULL);
		UPnPRelease(RetDevice);
	}
}
void UPnPSCPD_Sink(void *reader, struct packetheader *header, int IsInterrupt,char* buffer, int *p_BeginPointer, int EndPointer, int done, void* dv, void* sv)
{
	struct UPnPDevice *device;
	struct UPnPService *service = (struct UPnPService*)sv;
	struct UPnPCP *CP = service->Parent->CP;
	
	if(!(header==NULL || header->StatusCode!=200) && done!=0)
	{
		UPnPProcessSCPD(buffer,EndPointer, service);
		
		device = service->Parent;
		while(device->Parent!=NULL)
		{
			device = device->Parent;
		}
		--device->SCPDLeft;
		if(device->SCPDLeft==0)
		{
			if(device->SCPDError==0)
			{
				UPnPFinishProcessingDevice(CP,device);
			}
			else
			{
				UPnPDestructUPnPDevice(device);
			}
		}
	}
	else
	{
		if(done!=0 && (header==NULL || header->StatusCode!=200))
		{
			device = service->Parent;
			while(device->Parent!=NULL)
			{
				device = device->Parent;
			}
			--device->SCPDLeft;
			if(device->SCPDError==0)
			{
				device->SCPDError=1;
				if(IsInterrupt==0)
				{
					ILibDeleteRequests(CP->HTTP,dv);
				}
			}
			if(device->SCPDLeft==0)
			{
				UPnPDestructUPnPDevice(device);
			}
		}
	}
}
void UPnPCalculateSCPD_FetchCount(struct UPnPDevice *device)
{
	int count = 0;
	struct UPnPDevice *root;
	struct UPnPDevice *e_Device = device->EmbeddedDevices;
	struct UPnPService *s;
	
	while(e_Device!=NULL)
	{
		UPnPCalculateSCPD_FetchCount(e_Device);
		e_Device = e_Device->Next;
	}
	
	s = device->Services;
	while(s!=NULL)
	{
		++count;
		s = s->Next;
	}
	
	root = device;
	while(root->Parent!=NULL)
	{
		root = root->Parent;
	}
	root->SCPDLeft += count;
}
void UPnPSCPD_Fetch(struct UPnPDevice *device)
{
	struct UPnPDevice *e_Device = device->EmbeddedDevices;
	struct UPnPService *s;
	char *IP,*Path;
	unsigned short Port;
	struct packetheader *p;
	struct sockaddr_in addr;
	
	while(e_Device!=NULL)
	{
		UPnPSCPD_Fetch(e_Device);
		e_Device = e_Device->Next;
	}
	
	s = device->Services;
	while(s!=NULL)
	{
		ILibParseUri(s->SCPDURL,&IP,&Port,&Path);
		DEBUGSTATEMENT(printf("SCPD: %s Port: %d Path: %s\r\n",IP,Port,Path));
		p = UPnPBuildPacket(IP,Port,Path,"GET");
		
		memset((char *)&addr, 0,sizeof(addr));
		addr.sin_family = AF_INET;
		addr.sin_addr.s_addr = inet_addr(IP);
		addr.sin_port = htons(Port);
		
		ILibAddRequest(((struct UPnPCP*)device->CP)->HTTP, p,&addr, &UPnPSCPD_Sink, device, s);
		
		FREE(IP);
		FREE(Path);
		s = s->Next;
	}
}
struct UPnPDevice* UPnPProcessDeviceXML_device(struct ILibXMLNode *xml, void *v_CP,const char *BaseURL, int Timeout, int RecvAddr)
{
	struct ILibXMLNode *tempNode;
	int flg,flg2;
	char *tempString;
	int tempStringLength;
	struct parser_result *tpr;
	
	char* ServiceType = NULL;
	int ServiceTypeLength = 0;
	char* SCPDURL = NULL;
	int SCPDURLLength = 0;
	char* EventSubURL = NULL;
	int EventSubURLLength = 0;
	char* ControlURL = NULL;
	int ControlURLLength = 0;
	
	struct UPnPDevice *tempDevice;
	struct UPnPService *TempService;
	struct UPnPDevice *device = (struct UPnPDevice*)MALLOC(sizeof(struct UPnPDevice));
	device->CP = v_CP;
	device->CacheTime = Timeout;
	device->Tag = NULL;
	device->InterfaceToHost = (char*)MALLOC(16);
	sprintf(device->InterfaceToHost,"%d.%d.%d.%d",(RecvAddr&0xFF),((RecvAddr>>8)&0xFF),((RecvAddr>>16)&0xFF),((RecvAddr>>24)&0xFF));
	device->DeviceType = NULL;
	device->UDN = NULL;
	device->LocationURL = NULL;
	device->FriendlyName = NULL;
	device->Parent = NULL;
	device->EmbeddedDevices = NULL;
	device->Services = NULL;
	device->Next = NULL;
	device->LocationURL = NULL;
	device->PresentationURL = NULL;
	device->FriendlyName = NULL;
	device->ManufacturerName = NULL;
	device->ManufacturerURL = NULL;
	device->ModelName = NULL;
	device->ModelDescription = NULL;
	device->ModelNumber = NULL;
	device->ModelURL = NULL;
	device->SCPDLeft = 0;
	device->SCPDError =0;
	
	xml = xml->Next;
	while(xml!=NULL)
	{
		if(xml->NameLength==10 && memcmp(xml->Name,"deviceList",10)==0)
		{
			if(xml->Next->StartTag!=0)
			{
				xml = xml->Next;
				flg2 = 0;
				while(flg2==0)
				{
					if(xml->NameLength==6 && memcmp(xml->Name,"device",6)==0)
					{
						tempDevice = UPnPProcessDeviceXML_device(xml,v_CP,BaseURL,Timeout, RecvAddr);
						tempDevice->Parent = device;
						tempDevice->Next = device->EmbeddedDevices;
						device->EmbeddedDevices = tempDevice;
					}
					if(xml->Peer==NULL)
					{
						flg2 = 1;
						xml = xml->Parent;
					}
					else
					{
						xml = xml->Peer;
					}
				}
			}
		} else
		if(xml->NameLength==3 && memcmp(xml->Name,"UDN",3)==0)
		{
			tempStringLength = ILibReadInnerXML(xml,&tempString);
			if(tempStringLength>5)
			{
				if(memcmp(tempString,"uuid:",5)==0)
				{
					tempString += 5;
					tempStringLength -= 5;
				}
				device->UDN = (char*)MALLOC(tempStringLength+1);
				memcpy(device->UDN,tempString,tempStringLength);
				device->UDN[tempStringLength] = '\0';
			}
		} else
		if(xml->NameLength==10 && memcmp(xml->Name,"deviceType",10) == 0)
		{
			tempStringLength = ILibReadInnerXML(xml,&tempString);
			
			device->DeviceType = (char*)MALLOC(tempStringLength+1);
			memcpy(device->DeviceType,tempString,tempStringLength);
			device->DeviceType[tempStringLength] = '\0';
		} else
		if(xml->NameLength==12 && memcmp(xml->Name,"friendlyName",12) == 0)
		{
			tempStringLength = ILibReadInnerXML(xml,&tempString);
			device->FriendlyName = (char*)MALLOC(1+tempStringLength);
			memcpy(device->FriendlyName,tempString,tempStringLength);
			device->FriendlyName[tempStringLength] = '\0';
		} else
		if(xml->NameLength==12 && memcmp(xml->Name,"manufacturer",12) == 0)
		{
			tempStringLength = ILibReadInnerXML(xml,&tempString);
			device->ManufacturerName = (char*)MALLOC(1+tempStringLength);
			memcpy(device->ManufacturerName,tempString,tempStringLength);
			device->ManufacturerName[tempStringLength] = '\0';
		} else
		if(xml->NameLength==15 && memcmp(xml->Name,"manufacturerURL",15) == 0)
		{
			tempStringLength = ILibReadInnerXML(xml,&tempString);
			device->ManufacturerURL = (char*)MALLOC(1+tempStringLength);
			memcpy(device->ManufacturerURL,tempString,tempStringLength);
			device->ManufacturerURL[tempStringLength] = '\0';
		} else
		if(xml->NameLength==16 && memcmp(xml->Name,"modelDescription",16) == 0)
		{
			tempStringLength = ILibReadInnerXML(xml,&tempString);
			device->ModelDescription = (char*)MALLOC(1+tempStringLength);
			memcpy(device->ModelDescription,tempString,tempStringLength);
			device->ModelDescription[tempStringLength] = '\0';
		} else
		if(xml->NameLength==9 && memcmp(xml->Name,"modelName",9) == 0)
		{
			tempStringLength = ILibReadInnerXML(xml,&tempString);
			device->ModelName = (char*)MALLOC(1+tempStringLength);
			memcpy(device->ModelName,tempString,tempStringLength);
			device->ModelName[tempStringLength] = '\0';
		} else
		if(xml->NameLength==11 && memcmp(xml->Name,"modelNumber",11) == 0)
		{
			tempStringLength = ILibReadInnerXML(xml,&tempString);
			device->ModelNumber = (char*)MALLOC(1+tempStringLength);
			memcpy(device->ModelNumber,tempString,tempStringLength);
			device->ModelNumber[tempStringLength] = '\0';
		} else
		if(xml->NameLength==8 && memcmp(xml->Name,"modelURL",8) == 0)
		{
			tempStringLength = ILibReadInnerXML(xml,&tempString);
			device->ModelURL = (char*)MALLOC(1+tempStringLength);
			memcpy(device->ModelURL,tempString,tempStringLength);
			device->ModelURL[tempStringLength] = '\0';
		} else
		if(xml->NameLength==15 && memcmp(xml->Name,"presentationURL",15) == 0)
		{
			tempStringLength = ILibReadInnerXML(xml,&tempString);
			tempString[tempStringLength] = 0;
			tpr = ILibParseString(tempString,0,tempStringLength,"://",3);
			if(tpr->NumResults==1)
			{
				/* RelativeURL */
				if(tempString[0]=='/')
				{
					device->PresentationURL = (char*)MALLOC(1+strlen(BaseURL)+tempStringLength);
					memcpy(device->PresentationURL,BaseURL,strlen(BaseURL));
					strcpy(device->PresentationURL+strlen(BaseURL),tempString+1);
				}
				else
				{
					device->PresentationURL = (char*)MALLOC(2+strlen(BaseURL)+tempStringLength);
					memcpy(device->PresentationURL,BaseURL,strlen(BaseURL));
					strcpy(device->PresentationURL+strlen(BaseURL),tempString);
				}
			}
			else
			{
				/* AbsoluteURL */
				device->PresentationURL = (char*)MALLOC(1+tempStringLength);
				memcpy(device->PresentationURL,tempString,tempStringLength);
				device->PresentationURL[tempStringLength] = '\0';
			}
			ILibDestructParserResults(tpr);
		} else
		if(xml->NameLength==11 && memcmp(xml->Name,"serviceList",11)==0)
		{
			xml = xml->Next;
			tempNode = xml;
			while(xml!=NULL)
			{
				if(xml->NameLength==7 && memcmp(xml->Name,"service",7)==0)
				{
					ServiceType = NULL;
					ServiceTypeLength = 0;
					SCPDURL = NULL;
					SCPDURLLength = 0;
					EventSubURL = NULL;
					EventSubURLLength = 0;
					ControlURL = NULL;
					ControlURLLength = 0;
					
					xml = xml->Next;
					flg = 0;
					while(flg==0)
					{
						if(xml->NameLength==11 && memcmp(xml->Name,"serviceType",11)==0)
						{
							ServiceTypeLength = ILibReadInnerXML(xml,&ServiceType);
						} else
						if(xml->NameLength==7 && memcmp(xml->Name,"SCPDURL",7) == 0)
						{
							SCPDURLLength = ILibReadInnerXML(xml,&SCPDURL);
						} else
						if(xml->NameLength==10 && memcmp(xml->Name,"controlURL",10) == 0)
						{
							ControlURLLength = ILibReadInnerXML(xml,&ControlURL);
						} else
						if(xml->NameLength==11 && memcmp(xml->Name,"eventSubURL",11) == 0)
						{
							EventSubURLLength = ILibReadInnerXML(xml,&EventSubURL);
						}
						
						if(xml->Peer!=NULL)
						{
							xml = xml->Peer;
						}
						else
						{
							flg = 1;
							xml = xml->Parent;
						}
					}
					
					/* Finished Parsing the ServiceSection, build the Service */
					ServiceType[ServiceTypeLength] = '\0';
					SCPDURL[SCPDURLLength] = '\0';
					EventSubURL[EventSubURLLength] = '\0';
					ControlURL[ControlURLLength] = '\0';
					
					TempService = (struct UPnPService*)MALLOC(sizeof(struct UPnPService));
					TempService->SubscriptionID = NULL;
					TempService->ServiceId = NULL;
					TempService->Actions = NULL;
					TempService->Variables = NULL;
					TempService->Next = NULL;
					TempService->Parent = device;
					if(EventSubURLLength>=7 && memcmp(EventSubURL,"http://",6)==0)
					{
						/* Explicit */
						TempService->SubscriptionURL = (char*)MALLOC(EventSubURLLength+1);
						memcpy(TempService->SubscriptionURL,EventSubURL,EventSubURLLength);
						TempService->SubscriptionURL[EventSubURLLength] = '\0';
					}
					else
					{
						/* Relative */
						if(memcmp(EventSubURL,"/",1)!=0)
						{
							TempService->SubscriptionURL = (char*)MALLOC(EventSubURLLength+(int)strlen(BaseURL)+1);
							memcpy(TempService->SubscriptionURL,BaseURL,(int)strlen(BaseURL));
							memcpy(TempService->SubscriptionURL+(int)strlen(BaseURL),EventSubURL,EventSubURLLength);
							TempService->SubscriptionURL[EventSubURLLength+(int)strlen(BaseURL)] = '\0';
						}
						else
						{
							TempService->SubscriptionURL = (char*)MALLOC(EventSubURLLength+(int)strlen(BaseURL)+1);
							memcpy(TempService->SubscriptionURL,BaseURL,(int)strlen(BaseURL));
							memcpy(TempService->SubscriptionURL+(int)strlen(BaseURL),EventSubURL+1,EventSubURLLength-1);
							TempService->SubscriptionURL[EventSubURLLength+(int)strlen(BaseURL)-1] = '\0';
						}
					}
					if(ControlURLLength>=7 && memcmp(ControlURL,"http://",6)==0)
					{
						/* Explicit */
						TempService->ControlURL = (char*)MALLOC(ControlURLLength+1);
						memcpy(TempService->ControlURL,ControlURL,ControlURLLength);
						TempService->ControlURL[ControlURLLength] = '\0';
					}
					else
					{
						/* Relative */
						if(memcmp(ControlURL,"/",1)!=0)
						{
							TempService->ControlURL = (char*)MALLOC(ControlURLLength+(int)strlen(BaseURL)+1);
							memcpy(TempService->ControlURL,BaseURL,(int)strlen(BaseURL));
							memcpy(TempService->ControlURL+(int)strlen(BaseURL),ControlURL,ControlURLLength);
							TempService->ControlURL[ControlURLLength+(int)strlen(BaseURL)] = '\0';
						}
						else
						{
							TempService->ControlURL = (char*)MALLOC(ControlURLLength+(int)strlen(BaseURL)+1);
							memcpy(TempService->ControlURL,BaseURL,(int)strlen(BaseURL));
							memcpy(TempService->ControlURL+(int)strlen(BaseURL),ControlURL+1,ControlURLLength-1);
							TempService->ControlURL[ControlURLLength+(int)strlen(BaseURL)-1] = '\0';
						}
					}
					if(SCPDURLLength>=7 && memcmp(SCPDURL,"http://",6)==0)
					{
						/* Explicit */
						TempService->SCPDURL = (char*)MALLOC(SCPDURLLength+1);
						memcpy(TempService->SCPDURL,SCPDURL,SCPDURLLength);
						TempService->SCPDURL[SCPDURLLength] = '\0';
					}
					else
					{
						/* Relative */
						if(memcmp(SCPDURL,"/",1)!=0)
						{
							TempService->SCPDURL = (char*)MALLOC(SCPDURLLength+(int)strlen(BaseURL)+1);
							memcpy(TempService->SCPDURL,BaseURL,(int)strlen(BaseURL));
							memcpy(TempService->SCPDURL+(int)strlen(BaseURL),SCPDURL,SCPDURLLength);
							TempService->SCPDURL[SCPDURLLength+(int)strlen(BaseURL)] = '\0';
						}
						else
						{
							TempService->SCPDURL = (char*)MALLOC(SCPDURLLength+(int)strlen(BaseURL)+1);
							memcpy(TempService->SCPDURL,BaseURL,(int)strlen(BaseURL));
							memcpy(TempService->SCPDURL+(int)strlen(BaseURL),SCPDURL+1,SCPDURLLength-1);
							TempService->SCPDURL[SCPDURLLength+(int)strlen(BaseURL)-1] = '\0';
						}
					}
					
					TempService->ServiceType = (char*)MALLOC(ServiceTypeLength+1);
					sprintf(TempService->ServiceType,ServiceType,ServiceTypeLength);
					TempService->Next = device->Services;
					device->Services = TempService;
					
					DEBUGSTATEMENT(printf("ServiceType: %s\r\nSCPDURL: %s\r\nEventSubURL: %s\r\nControl URL: %s\r\n",ServiceType,SCPDURL,EventSubURL,ControlURL));
				}
				xml = xml->Peer;
			}
			xml = tempNode;
		} // End of ServiceList
		xml = xml->Peer;
	} // End of While
	
	return(device);
}

void UPnPProcessDeviceXML(void *v_CP,char* buffer, int BufferSize, char* LocationURL, int RecvAddr, int Timeout)
{
	struct UPnPDevice *RootDevice = NULL;
	
	char* IP;
	unsigned short Port;
	char* Path;
	
	char* BaseURL = NULL;
	
	struct ILibXMLNode *rootXML;
	struct ILibXMLNode *xml;
	char* tempString;
	int tempStringLength;
	
	rootXML = ILibParseXML(buffer,0,BufferSize);
	ILibProcessXMLNodeList(rootXML);
	
	xml = rootXML;
	xml = xml->Next;
	while(xml!=NULL)
	{
		if(xml->NameLength==7 && memcmp(xml->Name,"URLBase",7)==0)
		{
			tempStringLength = ILibReadInnerXML(xml,&tempString);
			if(tempString[tempStringLength-1]!='/')
			{
				BaseURL = (char*)MALLOC(2+tempStringLength);
				memcpy(BaseURL,tempString,tempStringLength);
				BaseURL[tempStringLength] = '/';
				BaseURL[tempStringLength+1] = '\0';
			}
			else
			{
				BaseURL = (char*)MALLOC(1+tempStringLength);
				memcpy(BaseURL,tempString,tempStringLength);
				BaseURL[tempStringLength] = '\0';
			}
			break;
		}
		xml = xml->Peer;
	}
	
	if(BaseURL==NULL)
	{
		ILibParseUri(LocationURL,&IP,&Port,&Path);
		BaseURL = (char*)MALLOC(18+(int)strlen(IP));
		sprintf(BaseURL,"http://%s:%d/",IP,Port);
		
		FREE(IP);
		FREE(Path);
	}
	
	DEBUGSTATEMENT(printf("BaseURL: %s\r\n",BaseURL));
	
	xml = rootXML;
	xml = xml->Next;
	while(xml->NameLength!=6 && memcmp(xml->Name,"device",6)!=0 && xml!=NULL)
	{
		xml = xml->Peer;
	}
	if(xml==NULL)
	{
		/* Error */
		ILibDestructXMLNodeList(rootXML);
		return;
	}
	
	RootDevice = UPnPProcessDeviceXML_device(xml,v_CP,BaseURL,Timeout,RecvAddr);
	FREE(BaseURL);
	ILibDestructXMLNodeList(rootXML);
	
	/* Add Root Device to UDNTable */
	UPnPAddUDN(v_CP,RootDevice->UDN);
	
	/* Save reference to LocationURL in the RootDevice */
	RootDevice->LocationURL = (char*)MALLOC(strlen(LocationURL)+1);
	sprintf(RootDevice->LocationURL,"%s",LocationURL);
	
	/* Trim Object Structure */
	UPnPProcessDevice(RootDevice);
	RootDevice->SCPDLeft = 0;
	UPnPCalculateSCPD_FetchCount(RootDevice);
	if(RootDevice->SCPDLeft==0)
	{
		UPnPFinishProcessingDevice(v_CP,RootDevice);
	}
	else
	{
		UPnPSCPD_Fetch(RootDevice);
	}
}

void UPnPHTTP_Sink_DeviceDescription(void *reader, struct packetheader *header, int IsInterrupt,char* buffer, int *p_BeginPointer, int EndPointer, int done, void* user, void* cp)
{
	struct CustomUserData *customData = (struct CustomUserData*)user;
	if(header!=NULL && done!=0)
	{
		UPnPProcessDeviceXML(cp,buffer,EndPointer-(*p_BeginPointer),customData->buffer,header->ReceivingAddress,customData->Timeout);
	}
	if(done!=0)
	{
		FREE(customData->buffer);
		FREE(user);
	}
}
void UPnP_FlushRequest(struct UPnPDevice *device)
{
	struct UPnPDevice *ed = device->EmbeddedDevices;
	struct UPnPService *s = device->Services;
	
	while(ed!=NULL)
	{
		UPnP_FlushRequest(ed);
		ed = ed->Next;
	}
	while(s!=NULL)
	{
		ILibDeleteRequests(((struct UPnPCP*)device->CP)->HTTP, s);
		s = s->Next;
	}
}
void UPnPSSDP_Sink(void *sender, char* UDN, int Alive, char* LocationURL, int Timeout,void *cp)
{
	struct CustomUserData *customData;
	char* buffer;
	char* IP;
	unsigned short Port;
	char* Path;
	struct packetheader *p;
	struct sockaddr_in addr;
	
	struct UPnPDevice *device,*tempDevice;
	int i=0;
	
	if(Alive!=0)
	{
		/* Hello */
		DEBUGSTATEMENT(printf("DigitalSecurityCamera Hello\r\n"));
		DEBUGSTATEMENT(printf("LocationURL: %s\r\n",LocationURL));
		if(UPnPHasUDN(cp,LocationURL)==0)
		{
			UPnPAddUDN(cp,LocationURL);
			if(UPnPHasUDN(cp,UDN)==0)
			{
				UPnPAddUDN(cp,UDN);
				ILibParseUri(LocationURL,&IP,&Port,&Path);
				DEBUGSTATEMENT(printf("IP: %s Port: %d Path: %s\r\n",IP,Port,Path));
				p = UPnPBuildPacket(IP,Port,Path,"GET");
				
				memset((char *)&addr, 0,sizeof(addr));
				addr.sin_family = AF_INET;
				addr.sin_addr.s_addr = inet_addr(IP);
				addr.sin_port = htons(Port);
				
				buffer = (char*)MALLOC((int)strlen(LocationURL)+1);
				strcpy(buffer,LocationURL);
				
				customData = (struct CustomUserData*)MALLOC(sizeof(struct CustomUserData));
				customData->Timeout = Timeout;
				customData->buffer = buffer;
				
				ILibAddRequest(((struct UPnPCP*)cp)->HTTP, p,&addr, &UPnPHTTP_Sink_DeviceDescription,customData, cp);
				
				FREE(IP);
				FREE(Path);
			}
		}
		else
		{
			// Periodic Notify Packets
			if(UPnPHasUDN(cp,UDN)!=0)
			{
				buffer = UPnPGetRootUDNAtUDN(cp,UDN);
				if(buffer!=NULL)
				{
					device = UPnPGetDeviceAtUDN(cp,buffer);
					if(device!=NULL)
					{
						//Extend LifetimeMonitor duration
						ILibLifeTime_Remove(((struct UPnPCP*)cp)->LifeTimeMonitor,device);
						ILibLifeTime_Add(((struct UPnPCP*)cp)->LifeTimeMonitor,device,Timeout,&UPnPDeviceExpired,NULL);
						UPnPRelease(device);
					}
				}
			}
		}
	}
	else
	{
		/* Bye Bye */
		DEBUGSTATEMENT(printf("DigitalSecurityCamera ByeBye\r\n"));
		device = UPnPGetDeviceAtUDN(cp,UDN);
		if(device!=NULL)
		{
			ILibLifeTime_Remove(((struct UPnPCP*)cp)->LifeTimeMonitor,device);
			UPnPRemoveUDN(cp,device->LocationURL);
			do
			{
				tempDevice = UPnPGetDevice1(device,i++);
				if(tempDevice!=NULL)
				{
					UPnP_FlushRequest(tempDevice);
					if(((struct UPnPCP*)cp)->RemoveSink!=NULL)
					{
						((struct UPnPCP*)cp)->RemoveSink(tempDevice);
					}
				}
			} while(tempDevice!=NULL);
			UPnPRelease(device);
			UPnPRemoveUDN(cp,UDN);
		}
	}
}
void UPnPFocus_EventSink(char* buffer, int bufferlength, struct UPnPService *service)
{
	struct ILibXMLNode *xml,*rootXML;
	char *tempString;
	int tempStringLength;
	int flg,flg2;
	
	unsigned int IrisPosition = 0;
	unsigned int FocusPosition = 0;
	unsigned int ZoomPosition = 0;
	unsigned long TempULong;
	
	/* Parse SOAP */
	rootXML = xml = ILibParseXML(buffer,0,bufferlength);
	ILibProcessXMLNodeList(xml);
	
	while(xml!=NULL)
	{
		if(xml->NameLength==11 && memcmp(xml->Name,"propertyset",11)==0)
		{
			if(xml->Next->StartTag!=0)
			{
				flg = 0;
				xml = xml->Next;
				while(flg==0)
				{
					if(xml->NameLength==8 && memcmp(xml->Name,"property",8)==0)
					{
						xml = xml->Next;
						flg2 = 0;
						while(flg2==0)
						{
							if(xml->NameLength==12 && memcmp(xml->Name,"IrisPosition",12) == 0)
							{
								tempStringLength = ILibReadInnerXML(xml,&tempString);
								if(ILibGetULong(tempString,tempStringLength,&TempULong)==0)
								{
									IrisPosition = (unsigned int) TempULong;
								}
								if(UPnPEventCallback_Focus_IrisPosition != NULL)
								{
									UPnPEventCallback_Focus_IrisPosition(service,IrisPosition);
								}
							}
							if(xml->NameLength==13 && memcmp(xml->Name,"FocusPosition",13) == 0)
							{
								tempStringLength = ILibReadInnerXML(xml,&tempString);
								if(ILibGetULong(tempString,tempStringLength,&TempULong)==0)
								{
									FocusPosition = (unsigned int) TempULong;
								}
								if(UPnPEventCallback_Focus_FocusPosition != NULL)
								{
									UPnPEventCallback_Focus_FocusPosition(service,FocusPosition);
								}
							}
							if(xml->NameLength==12 && memcmp(xml->Name,"ZoomPosition",12) == 0)
							{
								tempStringLength = ILibReadInnerXML(xml,&tempString);
								if(ILibGetULong(tempString,tempStringLength,&TempULong)==0)
								{
									ZoomPosition = (unsigned int) TempULong;
								}
								if(UPnPEventCallback_Focus_ZoomPosition != NULL)
								{
									UPnPEventCallback_Focus_ZoomPosition(service,ZoomPosition);
								}
							}
							if(xml->Peer!=NULL)
							{
								xml = xml->Peer;
							}
							else
							{
								flg2 = -1;
								xml = xml->Parent;
							}
						}
					}
					if(xml->Peer!=NULL)
					{
						xml = xml->Peer;
					}
					else
					{
						flg = -1;
						xml = xml->Parent;
					}
				}
			}
		}
		xml = xml->Peer;
	}
	
	ILibDestructXMLNodeList(rootXML);
}
void UPnPPreset_EventSink(char* buffer, int bufferlength, struct UPnPService *service)
{
	struct ILibXMLNode *xml,*rootXML;
	int flg,flg2;
	
	
	/* Parse SOAP */
	rootXML = xml = ILibParseXML(buffer,0,bufferlength);
	ILibProcessXMLNodeList(xml);
	
	while(xml!=NULL)
	{
		if(xml->NameLength==11 && memcmp(xml->Name,"propertyset",11)==0)
		{
			if(xml->Next->StartTag!=0)
			{
				flg = 0;
				xml = xml->Next;
				while(flg==0)
				{
					if(xml->NameLength==8 && memcmp(xml->Name,"property",8)==0)
					{
						xml = xml->Next;
						flg2 = 0;
						while(flg2==0)
						{
							if(xml->Peer!=NULL)
							{
								xml = xml->Peer;
							}
							else
							{
								flg2 = -1;
								xml = xml->Parent;
							}
						}
					}
					if(xml->Peer!=NULL)
					{
						xml = xml->Peer;
					}
					else
					{
						flg = -1;
						xml = xml->Parent;
					}
				}
			}
		}
		xml = xml->Peer;
	}
	
	ILibDestructXMLNodeList(rootXML);
}
void UPnPDigitalSecurityCameraSettings_EventSink(char* buffer, int bufferlength, struct UPnPService *service)
{
	struct ILibXMLNode *xml,*rootXML;
	char *tempString;
	int tempStringLength;
	int flg,flg2;
	
	unsigned int Brightness = 0;
	unsigned int FixedWhiteBalance = 0;
	unsigned int ColorSaturation = 0;
	unsigned long TempULong;
	
	/* Parse SOAP */
	rootXML = xml = ILibParseXML(buffer,0,bufferlength);
	ILibProcessXMLNodeList(xml);
	
	while(xml!=NULL)
	{
		if(xml->NameLength==11 && memcmp(xml->Name,"propertyset",11)==0)
		{
			if(xml->Next->StartTag!=0)
			{
				flg = 0;
				xml = xml->Next;
				while(flg==0)
				{
					if(xml->NameLength==8 && memcmp(xml->Name,"property",8)==0)
					{
						xml = xml->Next;
						flg2 = 0;
						while(flg2==0)
						{
							if(xml->NameLength==10 && memcmp(xml->Name,"Brightness",10) == 0)
							{
								tempStringLength = ILibReadInnerXML(xml,&tempString);
								if(ILibGetULong(tempString,tempStringLength,&TempULong)==0)
								{
									Brightness = (unsigned int) TempULong;
								}
								if(UPnPEventCallback_DigitalSecurityCameraSettings_Brightness != NULL)
								{
									UPnPEventCallback_DigitalSecurityCameraSettings_Brightness(service,Brightness);
								}
							}
							if(xml->NameLength==17 && memcmp(xml->Name,"FixedWhiteBalance",17) == 0)
							{
								tempStringLength = ILibReadInnerXML(xml,&tempString);
								if(ILibGetULong(tempString,tempStringLength,&TempULong)==0)
								{
									FixedWhiteBalance = (unsigned int) TempULong;
								}
								if(UPnPEventCallback_DigitalSecurityCameraSettings_FixedWhiteBalance != NULL)
								{
									UPnPEventCallback_DigitalSecurityCameraSettings_FixedWhiteBalance(service,FixedWhiteBalance);
								}
							}
							if(xml->NameLength==15 && memcmp(xml->Name,"ColorSaturation",15) == 0)
							{
								tempStringLength = ILibReadInnerXML(xml,&tempString);
								if(ILibGetULong(tempString,tempStringLength,&TempULong)==0)
								{
									ColorSaturation = (unsigned int) TempULong;
								}
								if(UPnPEventCallback_DigitalSecurityCameraSettings_ColorSaturation != NULL)
								{
									UPnPEventCallback_DigitalSecurityCameraSettings_ColorSaturation(service,ColorSaturation);
								}
							}
							if(xml->Peer!=NULL)
							{
								xml = xml->Peer;
							}
							else
							{
								flg2 = -1;
								xml = xml->Parent;
							}
						}
					}
					if(xml->Peer!=NULL)
					{
						xml = xml->Peer;
					}
					else
					{
						flg = -1;
						xml = xml->Parent;
					}
				}
			}
		}
		xml = xml->Peer;
	}
	
	ILibDestructXMLNodeList(rootXML);
}
void UPnPDigitalSecurityCameraStillImage_EventSink(char* buffer, int bufferlength, struct UPnPService *service)
{
	struct ILibXMLNode *xml,*rootXML;
	char *tempString;
	int tempStringLength;
	int flg,flg2;
	
	char* DefaultResolution = 0;
	char* DefaultEncoding = 0;
	char* DefaultCompressionLevel = 0;
	
	/* Parse SOAP */
	rootXML = xml = ILibParseXML(buffer,0,bufferlength);
	ILibProcessXMLNodeList(xml);
	
	while(xml!=NULL)
	{
		if(xml->NameLength==11 && memcmp(xml->Name,"propertyset",11)==0)
		{
			if(xml->Next->StartTag!=0)
			{
				flg = 0;
				xml = xml->Next;
				while(flg==0)
				{
					if(xml->NameLength==8 && memcmp(xml->Name,"property",8)==0)
					{
						xml = xml->Next;
						flg2 = 0;
						while(flg2==0)
						{
							if(xml->NameLength==17 && memcmp(xml->Name,"DefaultResolution",17) == 0)
							{
								tempStringLength = ILibReadInnerXML(xml,&tempString);
								tempString[tempStringLength] = '\0';
								DefaultResolution = tempString;
								if(UPnPEventCallback_DigitalSecurityCameraStillImage_DefaultResolution != NULL)
								{
									UPnPEventCallback_DigitalSecurityCameraStillImage_DefaultResolution(service,DefaultResolution);
								}
							}
							if(xml->NameLength==15 && memcmp(xml->Name,"DefaultEncoding",15) == 0)
							{
								tempStringLength = ILibReadInnerXML(xml,&tempString);
								tempString[tempStringLength] = '\0';
								DefaultEncoding = tempString;
								if(UPnPEventCallback_DigitalSecurityCameraStillImage_DefaultEncoding != NULL)
								{
									UPnPEventCallback_DigitalSecurityCameraStillImage_DefaultEncoding(service,DefaultEncoding);
								}
							}
							if(xml->NameLength==23 && memcmp(xml->Name,"DefaultCompressionLevel",23) == 0)
							{
								tempStringLength = ILibReadInnerXML(xml,&tempString);
								tempString[tempStringLength] = '\0';
								DefaultCompressionLevel = tempString;
								if(UPnPEventCallback_DigitalSecurityCameraStillImage_DefaultCompressionLevel != NULL)
								{
									UPnPEventCallback_DigitalSecurityCameraStillImage_DefaultCompressionLevel(service,DefaultCompressionLevel);
								}
							}
							if(xml->Peer!=NULL)
							{
								xml = xml->Peer;
							}
							else
							{
								flg2 = -1;
								xml = xml->Parent;
							}
						}
					}
					if(xml->Peer!=NULL)
					{
						xml = xml->Peer;
					}
					else
					{
						flg = -1;
						xml = xml->Parent;
					}
				}
			}
		}
		xml = xml->Peer;
	}
	
	ILibDestructXMLNodeList(rootXML);
}
void UPnPPanTilt_EventSink(char* buffer, int bufferlength, struct UPnPService *service)
{
	struct ILibXMLNode *xml,*rootXML;
	char *tempString;
	int tempStringLength;
	int flg,flg2;
	
	short TiltPosition = 0;
	short PanPosition = 0;
	long TempLong;
	
	/* Parse SOAP */
	rootXML = xml = ILibParseXML(buffer,0,bufferlength);
	ILibProcessXMLNodeList(xml);
	
	while(xml!=NULL)
	{
		if(xml->NameLength==11 && memcmp(xml->Name,"propertyset",11)==0)
		{
			if(xml->Next->StartTag!=0)
			{
				flg = 0;
				xml = xml->Next;
				while(flg==0)
				{
					if(xml->NameLength==8 && memcmp(xml->Name,"property",8)==0)
					{
						xml = xml->Next;
						flg2 = 0;
						while(flg2==0)
						{
							if(xml->NameLength==12 && memcmp(xml->Name,"TiltPosition",12) == 0)
							{
								tempStringLength = ILibReadInnerXML(xml,&tempString);
								if(ILibGetLong(tempString,tempStringLength,&TempLong)==0)
								{
									TiltPosition = (short) TempLong;
								}
								if(UPnPEventCallback_PanTilt_TiltPosition != NULL)
								{
									UPnPEventCallback_PanTilt_TiltPosition(service,TiltPosition);
								}
							}
							if(xml->NameLength==11 && memcmp(xml->Name,"PanPosition",11) == 0)
							{
								tempStringLength = ILibReadInnerXML(xml,&tempString);
								if(ILibGetLong(tempString,tempStringLength,&TempLong)==0)
								{
									PanPosition = (short) TempLong;
								}
								if(UPnPEventCallback_PanTilt_PanPosition != NULL)
								{
									UPnPEventCallback_PanTilt_PanPosition(service,PanPosition);
								}
							}
							if(xml->Peer!=NULL)
							{
								xml = xml->Peer;
							}
							else
							{
								flg2 = -1;
								xml = xml->Parent;
							}
						}
					}
					if(xml->Peer!=NULL)
					{
						xml = xml->Peer;
					}
					else
					{
						flg = -1;
						xml = xml->Parent;
					}
				}
			}
		}
		xml = xml->Peer;
	}
	
	ILibDestructXMLNodeList(rootXML);
}
void UPnPDigitalSecurityCameraMotionImage_EventSink(char* buffer, int bufferlength, struct UPnPService *service)
{
	struct ILibXMLNode *xml,*rootXML;
	char *tempString;
	int tempStringLength;
	int flg,flg2;
	
	unsigned int MaxBandwith = 0;
	char* DefaultResolution = 0;
	char* DefaultEncoding = 0;
	char* DefaultCompressionLevel = 0;
	unsigned int TargetFrameRate = 0;
	unsigned long TempULong;
	
	/* Parse SOAP */
	rootXML = xml = ILibParseXML(buffer,0,bufferlength);
	ILibProcessXMLNodeList(xml);
	
	while(xml!=NULL)
	{
		if(xml->NameLength==11 && memcmp(xml->Name,"propertyset",11)==0)
		{
			if(xml->Next->StartTag!=0)
			{
				flg = 0;
				xml = xml->Next;
				while(flg==0)
				{
					if(xml->NameLength==8 && memcmp(xml->Name,"property",8)==0)
					{
						xml = xml->Next;
						flg2 = 0;
						while(flg2==0)
						{
							if(xml->NameLength==11 && memcmp(xml->Name,"MaxBandwith",11) == 0)
							{
								tempStringLength = ILibReadInnerXML(xml,&tempString);
								if(ILibGetULong(tempString,tempStringLength,&TempULong)==0)
								{
									MaxBandwith = (unsigned int) TempULong;
								}
								if(UPnPEventCallback_DigitalSecurityCameraMotionImage_MaxBandwith != NULL)
								{
									UPnPEventCallback_DigitalSecurityCameraMotionImage_MaxBandwith(service,MaxBandwith);
								}
							}
							if(xml->NameLength==17 && memcmp(xml->Name,"DefaultResolution",17) == 0)
							{
								tempStringLength = ILibReadInnerXML(xml,&tempString);
								tempString[tempStringLength] = '\0';
								DefaultResolution = tempString;
								if(UPnPEventCallback_DigitalSecurityCameraMotionImage_DefaultResolution != NULL)
								{
									UPnPEventCallback_DigitalSecurityCameraMotionImage_DefaultResolution(service,DefaultResolution);
								}
							}
							if(xml->NameLength==15 && memcmp(xml->Name,"DefaultEncoding",15) == 0)
							{
								tempStringLength = ILibReadInnerXML(xml,&tempString);
								tempString[tempStringLength] = '\0';
								DefaultEncoding = tempString;
								if(UPnPEventCallback_DigitalSecurityCameraMotionImage_DefaultEncoding != NULL)
								{
									UPnPEventCallback_DigitalSecurityCameraMotionImage_DefaultEncoding(service,DefaultEncoding);
								}
							}
							if(xml->NameLength==23 && memcmp(xml->Name,"DefaultCompressionLevel",23) == 0)
							{
								tempStringLength = ILibReadInnerXML(xml,&tempString);
								tempString[tempStringLength] = '\0';
								DefaultCompressionLevel = tempString;
								if(UPnPEventCallback_DigitalSecurityCameraMotionImage_DefaultCompressionLevel != NULL)
								{
									UPnPEventCallback_DigitalSecurityCameraMotionImage_DefaultCompressionLevel(service,DefaultCompressionLevel);
								}
							}
							if(xml->NameLength==15 && memcmp(xml->Name,"TargetFrameRate",15) == 0)
							{
								tempStringLength = ILibReadInnerXML(xml,&tempString);
								if(ILibGetULong(tempString,tempStringLength,&TempULong)==0)
								{
									TargetFrameRate = (unsigned int) TempULong;
								}
								if(UPnPEventCallback_DigitalSecurityCameraMotionImage_TargetFrameRate != NULL)
								{
									UPnPEventCallback_DigitalSecurityCameraMotionImage_TargetFrameRate(service,TargetFrameRate);
								}
							}
							if(xml->Peer!=NULL)
							{
								xml = xml->Peer;
							}
							else
							{
								flg2 = -1;
								xml = xml->Parent;
							}
						}
					}
					if(xml->Peer!=NULL)
					{
						xml = xml->Peer;
					}
					else
					{
						flg = -1;
						xml = xml->Parent;
					}
				}
			}
		}
		xml = xml->Peer;
	}
	
	ILibDestructXMLNodeList(rootXML);
}
void UPnPOnEventSink(void *ReaderObject, struct packetheader *header, char* buffer, int *BeginPointer, int BufferSize, int done, void* user)
{
	int type_length;
	char* sid = NULL;
	void* value = NULL;
	struct UPnPService *service = NULL;
	struct packetheader_field_node *field = NULL;
	struct packetheader *resp;
	if(done!=0)
	{
		resp = ILibCreateEmptyPacket();
		ILibAddHeaderLine(resp,"Server",6,"WINDOWS, UPnP/1.0, Intel MicroStack/1.0.1200",44);
		field = header->FirstField;
		while(field!=NULL)
		{
			if(field->FieldLength==3)
			{
				if(strncasecmp(field->Field,"SID",3)==0)
				{
					sid = (char*)MALLOC(field->FieldDataLength+1);
					sprintf(sid,"%s",field->FieldData);
					value = ILibGetEntry(((struct UPnPCP*)user)->SIDTable,field->FieldData,field->FieldDataLength);
					break;
				}
			}
			field = field->NextField;
		}
		
		if(value==NULL)
		{
			/* Not a valid SID */
			ILibSetStatusCode(resp,412,"Failed",6);
			if(sid!=NULL) {FREE(sid);}
		}
		else
		{
			ILibSetStatusCode(resp,200,"OK",2);
			service = (struct UPnPService*)value;
			
			type_length = (int)strlen(service->ServiceType);
			if(type_length>35 && strncmp("urn:schemas-upnp-org:service:Focus:",service->ServiceType,35)==0)
			{
				UPnPFocus_EventSink(buffer, BufferSize, service);
			}
			else
			if(type_length>36 && strncmp("urn:schemas-upnp-org:service:Preset:",service->ServiceType,36)==0)
			{
				UPnPPreset_EventSink(buffer, BufferSize, service);
			}
			else
			if(type_length>59 && strncmp("urn:schemas-upnp-org:service:DigitalSecurityCameraSettings:",service->ServiceType,59)==0)
			{
				UPnPDigitalSecurityCameraSettings_EventSink(buffer, BufferSize, service);
			}
			else
			if(type_length>61 && strncmp("urn:schemas-upnp-org:service:DigitalSecurityCameraStillImage:",service->ServiceType,61)==0)
			{
				UPnPDigitalSecurityCameraStillImage_EventSink(buffer, BufferSize, service);
			}
			else
			if(type_length>37 && strncmp("urn:schemas-upnp-org:service:PanTilt:",service->ServiceType,37)==0)
			{
				UPnPPanTilt_EventSink(buffer, BufferSize, service);
			}
			else
			if(type_length>62 && strncmp("urn:schemas-upnp-org:service:DigitalSecurityCameraMotionImage:",service->ServiceType,62)==0)
			{
				UPnPDigitalSecurityCameraMotionImage_EventSink(buffer, BufferSize, service);
			}
		}
		ILibMiniWebServerSend(ReaderObject,resp);
		ILibDestructPacket(resp);
		ILibMiniWebServerCloseSession(ReaderObject);
		if(sid!=NULL){FREE(sid);}
	}
}
void UPnPOnSubscribeSink(void *ReaderObject, struct packetheader *header, int IsInterrupt,char* buffer, int *BeginPointer, int BufferSize, int done, void* user, void *vcp)
{
	struct UPnPService *s;
	struct packetheader_field_node *field;
	struct parser_result *p;
	struct UPnPCP *cp = (struct UPnPCP*)vcp;
	
	if(done!=0)
	{
		s = (struct UPnPService*)user;
		if(header!=NULL)
		{
			if(header->StatusCode==200)
			{
				/* Successful */
				field = header->FirstField;
				while(field!=NULL)
				{
					if(field->FieldLength==3 && strncasecmp(field->Field,"SID",3)==0)
					{
						s->SubscriptionID = (char*)MALLOC(1+field->FieldDataLength);
						strcpy(s->SubscriptionID,field->FieldData);
						ILibAddEntry(cp->SIDTable,field->FieldData,field->FieldDataLength,s);
					} else
					if(field->FieldLength==7 && strncasecmp(field->Field,"TIMEOUT",7)==0)
					{
						p = ILibParseString(field->FieldData,0,field->FieldDataLength,"-",1);
						p->LastResult->data[p->LastResult->datalength] = '\0';
						UPnPAddRef(s->Parent);
						ILibLifeTime_Add(cp->LifeTimeMonitor,s,atoi(p->LastResult->data)/2,&UPnPRenew,NULL);
						ILibDestructParserResults(p);
					}
					field = field->NextField;
				}
			}
		}
		UPnPRelease(s->Parent);
	}
}

void UPnPRenew(void *state)
{
	struct UPnPService *service = (struct UPnPService*)state;
	char *IP;
	char *Path;
	unsigned short Port;
	struct packetheader *p;
	char* TempString;
	struct sockaddr_in destaddr;
	
	ILibParseUri(service->SubscriptionURL,&IP,&Port,&Path);
	p = ILibCreateEmptyPacket();
	
	ILibSetDirective(p,"SUBSCRIBE",9,Path,(int)strlen(Path));
	
	TempString = (char*)MALLOC((int)strlen(IP)+7);
	sprintf(TempString,"%s:%d",IP,Port);
	
	ILibAddHeaderLine(p,"HOST",4,TempString,(int)strlen(TempString));
	FREE(TempString);
	
	ILibAddHeaderLine(p,"SID",3,service->SubscriptionID,(int)strlen(service->SubscriptionID));
	ILibAddHeaderLine(p,"TIMEOUT",7,"Second-180",10);
	ILibAddHeaderLine(p,"User-Agent",10,"WINDOWS, UPnP/1.0, Intel MicroStack/1.0.1200",44);
	
	memset((char *)&destaddr, 0,sizeof(destaddr));
	destaddr.sin_family = AF_INET;
	destaddr.sin_addr.s_addr = inet_addr(IP);
	destaddr.sin_port = htons(Port);
	
	ILibAddRequest(((struct UPnPCP*)service->Parent->CP)->HTTP, p,&destaddr, &UPnPOnSubscribeSink,(void*)service, service->Parent->CP);
	
	FREE(IP);
	FREE(Path);
}

struct UPnPDevice* UPnPGetDevice(struct UPnPDevice *device, char* DeviceType, int number)
{
	int counter = 0;
	
	device = device->EmbeddedDevices;
	while(device != NULL)
	{
		if(strlen(device->DeviceType)>=strlen(DeviceType))
		{
			if(memcmp(device->DeviceType,DeviceType,strlen(DeviceType))==0)
			{
				if(number == (++counter)) return(device);
			}
		}
		device = device->Next;
	}
	return(NULL);
}
int UPnPHasAction(struct UPnPService *s, char* action)
{
	struct UPnPAction *a = s->Actions;
	
	while(a!=NULL)
	{
		if(strcmp(action,a->Name)==0) return(-1);
		a = a->Next;
	}
	return(0);
}
void UPnPStopCP(void *v_CP)
{
	int i;
	struct UPnPDevice *RetDevice;
	struct UDNMapNode *mn,*mn2;
	struct UPnPCP *CP= (struct UPnPCP*)v_CP;
	sem_destroy(&(CP->DeviceLock));
	
	mn = CP->UDN_Head;
	while(mn!=NULL)
	{
		mn2 = mn->Next;
		if(mn->device!=NULL && mn->RootUDN==NULL)
		{
			i = 0;
			if(CP->RemoveSink!=NULL)
			{
				do
				{
					RetDevice = UPnPGetDevice1(mn->device,i++);
					if(RetDevice!=NULL)
					{
						CP->RemoveSink(RetDevice);
					}
				}while(RetDevice!=NULL);
			}
			UPnPDestructUPnPDevice(mn->device);
		}
		if(mn->RootUDN!=NULL){FREE(mn->RootUDN);}
		FREE(mn->UDN);
		FREE(mn);
		mn = mn2;
	}
	ILibDestroyHashTree(CP->SIDTable);
	FREE(CP->AddressList);
}
void UPnP_CP_IPAddressListChanged(void *CPToken)
{
	((struct UPnPCP*)CPToken)->RecheckFlag = 1;
	ILibForceUnBlockChain(((struct UPnPCP*)CPToken)->Chain);
}
void UPnPCP_ProcessDeviceRemoval(struct UPnPCP* CP, struct UPnPDevice *device)
{
	struct UPnPDevice *temp = device->EmbeddedDevices;
	struct UPnPService *s;
	
	while(temp!=NULL)
	{
		UPnPCP_ProcessDeviceRemoval(CP,temp);
		temp = temp->Next;
	}
	
	s = device->Services;
	while(s!=NULL)
	{
		ILibLifeTime_Remove(CP->LifeTimeMonitor,s);
		s = s->Next;
	}
}

void UPnPCP_PreSelect(void *CPToken,fd_set *readset, fd_set *writeset, fd_set *errorset, int *blocktime)
{
	struct UDNMapNode *mn,*mn2;
	struct UPnPCP *CP= (struct UPnPCP*)CPToken;
	int *IPAddressList;
	int NumAddressList;
	int i;
	int found;
	
	if(CP->RecheckFlag!=0)
	{
		CP->RecheckFlag = 0;
		
		NumAddressList = ILibGetLocalIPAddressList(&IPAddressList);
		
		mn = CP->UDN_Head;
		while(mn!=NULL)
		{
			mn2 = mn->Next;
			found = 0;
			for(i=0;i<NumAddressList;++i)
			{
				if(mn->device!=NULL && IPAddressList[i]==inet_addr(mn->device->InterfaceToHost))
				{
					found = 1;
					break;
				}
			}
			if(found==0)
			{
				// Clear LifeTime for services contained
				UPnPCP_ProcessDeviceRemoval(CP,mn->device);
				CP->RemoveSink(mn->device);
				UPnPDestructUPnPDevice(mn->device);
				
				if(mn->Previous==NULL)
				{
					// This is the head
					CP->UDN_Head = mn->Next;
					if(CP->UDN_Head!=NULL)
					{
						CP->UDN_Head->Previous = NULL;
					}
				}
				else
				{
					mn->Previous->Next = mn->Next;
					if(mn->Next!=NULL)
					{
						mn->Next->Previous = mn->Previous;
					}
				}
				
				FREE(mn->UDN);
				FREE(mn);
			}
			mn = mn2;
		}
		
		ILibSSDP_IPAddressListChanged(CP->SSDP);
		FREE(CP->AddressList);
		CP->AddressListLength = NumAddressList;
		CP->AddressList = IPAddressList;
	}
}
void *UPnPCreateControlPoint(void *Chain, void(*A)(struct UPnPDevice*),void(*R)(struct UPnPDevice*))
{
	struct UPnPCP *cp = (struct UPnPCP*)MALLOC(sizeof(struct UPnPCP));
	
	cp->Destroy = &UPnPStopCP;
	cp->PostSelect = NULL;
	cp->PreSelect = &UPnPCP_PreSelect;
	cp->DiscoverSink = A;
	cp->RemoveSink = R;
	
	sem_init(&(cp->DeviceLock),0,1);
	cp->UDN_Head = NULL;
	cp->WebServer = ILibCreateMiniWebServer(Chain,5,&UPnPOnEventSink,cp);
	cp->SIDTable = ILibInitHashTree();
	
	cp->SSDP = ILibCreateSSDPClientModule(Chain,"urn:schemas-upnp-org:device:DigitalSecurityCamera:1.0", 53, &UPnPSSDP_Sink,cp);
	cp->HTTP = ILibCreateHTTPClientModule(Chain,5);
	ILibAddToChain(Chain,cp);
	cp->LifeTimeMonitor = ILibCreateLifeTime(Chain);
	
	cp->Chain = Chain;
	cp->RecheckFlag = 0;
	cp->AddressListLength = ILibGetLocalIPAddressList(&(cp->AddressList));
	return((void*)cp);
}
void UPnPInvoke_Focus_SetAbsFocusPosition_Sink(void *reader, struct packetheader *header, int IsInterrupt,char* buffer, int *p_BeginPointer, int EndPointer, int done, void* _service, void *state)
{
	struct UPnPService *Service = (struct UPnPService*)_service;
	struct InvokeStruct *_InvokeData = (struct InvokeStruct*)state;
	if(done==0){return;}
	if(_InvokeData->CallbackPtr==NULL)
	{
		UPnPRelease(Service->Parent);
		FREE(_InvokeData);
		return;
	}
	else
	{
		if(header==NULL)
		{
			/* Connection Failed */
			((void (*)(struct UPnPService*,int,void*))_InvokeData->CallbackPtr)(Service,-1,_InvokeData->User);
			UPnPRelease(Service->Parent);
			FREE(_InvokeData);
			return;
		}
		else if(header->StatusCode!=200)
		{
			/* SOAP Fault */
			((void (*)(struct UPnPService*,int,void*))_InvokeData->CallbackPtr)(Service,UPnPGetErrorCode(buffer,EndPointer-(*p_BeginPointer)),_InvokeData->User);
			UPnPRelease(Service->Parent);
			FREE(_InvokeData);
			return;
		}
	}
	
	((void (*)(struct UPnPService*,int,void*))_InvokeData->CallbackPtr)(Service,0,_InvokeData->User);
	UPnPRelease(Service->Parent);
	FREE(_InvokeData);
}
void UPnPInvoke_Focus_SetAbsFocusPosition(struct UPnPService *service,void (*CallbackPtr)(struct UPnPService*,int,void*), void* user, unsigned int FocusSteps)
{
	int headerLength;
	char *headerBuffer;
	char *SoapBodyTemplate;
	char* buffer;
	int bufferLength;
	char* IP;
	unsigned short Port;
	char* Path;
	struct sockaddr_in addr;
	struct InvokeStruct *invoke_data = (struct InvokeStruct*)MALLOC(sizeof(struct InvokeStruct));
	
	if(service==NULL)
	{
		FREE(invoke_data);
		return;
	}
	buffer = (char*)MALLOC((int)strlen(service->ServiceType)+305);
	SoapBodyTemplate = "%sSetAbsFocusPosition xmlns:u=\"%s\"><FocusSteps>%u</FocusSteps></u:SetAbsFocusPosition%s";
	bufferLength = sprintf(buffer,SoapBodyTemplate,UPNPCP_SOAP_BodyHead,service->ServiceType, FocusSteps,UPNPCP_SOAP_BodyTail);
	
	UPnPAddRef(service->Parent);
	ILibParseUri(service->ControlURL,&IP,&Port,&Path);
	
	headerBuffer = (char*)MALLOC(174 + (int)strlen(Path) + (int)strlen(IP) + (int)strlen(service->ServiceType));
	headerLength = sprintf(headerBuffer,UPNPCP_SOAP_Header,Path,IP,Port,service->ServiceType,"SetAbsFocusPosition",bufferLength);
	
	memset((char *)&addr, 0, sizeof(addr));
	addr.sin_family = AF_INET;
	addr.sin_addr.s_addr = inet_addr(IP);
	addr.sin_port = htons(Port);
	
	invoke_data->CallbackPtr = CallbackPtr;
	invoke_data->User = user;
	ILibAddRequest_DirectEx(((struct UPnPCP*)service->Parent->CP)->HTTP, headerBuffer,headerLength,buffer,bufferLength,&addr, &UPnPInvoke_Focus_SetAbsFocusPosition_Sink,service, invoke_data);	
	
	FREE(IP);
	FREE(Path);
}
void UPnPInvoke_Focus_SetRelZoomPosition_Sink(void *reader, struct packetheader *header, int IsInterrupt,char* buffer, int *p_BeginPointer, int EndPointer, int done, void* _service, void *state)
{
	struct UPnPService *Service = (struct UPnPService*)_service;
	struct InvokeStruct *_InvokeData = (struct InvokeStruct*)state;
	if(done==0){return;}
	if(_InvokeData->CallbackPtr==NULL)
	{
		UPnPRelease(Service->Parent);
		FREE(_InvokeData);
		return;
	}
	else
	{
		if(header==NULL)
		{
			/* Connection Failed */
			((void (*)(struct UPnPService*,int,void*))_InvokeData->CallbackPtr)(Service,-1,_InvokeData->User);
			UPnPRelease(Service->Parent);
			FREE(_InvokeData);
			return;
		}
		else if(header->StatusCode!=200)
		{
			/* SOAP Fault */
			((void (*)(struct UPnPService*,int,void*))_InvokeData->CallbackPtr)(Service,UPnPGetErrorCode(buffer,EndPointer-(*p_BeginPointer)),_InvokeData->User);
			UPnPRelease(Service->Parent);
			FREE(_InvokeData);
			return;
		}
	}
	
	((void (*)(struct UPnPService*,int,void*))_InvokeData->CallbackPtr)(Service,0,_InvokeData->User);
	UPnPRelease(Service->Parent);
	FREE(_InvokeData);
}
void UPnPInvoke_Focus_SetRelZoomPosition(struct UPnPService *service,void (*CallbackPtr)(struct UPnPService*,int,void*), void* user, int ZoomSteps)
{
	int headerLength;
	char *headerBuffer;
	char *SoapBodyTemplate;
	char* buffer;
	int bufferLength;
	char* IP;
	unsigned short Port;
	char* Path;
	struct sockaddr_in addr;
	struct InvokeStruct *invoke_data = (struct InvokeStruct*)MALLOC(sizeof(struct InvokeStruct));
	
	if(service==NULL)
	{
		FREE(invoke_data);
		return;
	}
	buffer = (char*)MALLOC((int)strlen(service->ServiceType)+302);
	SoapBodyTemplate = "%sSetRelZoomPosition xmlns:u=\"%s\"><ZoomSteps>%d</ZoomSteps></u:SetRelZoomPosition%s";
	bufferLength = sprintf(buffer,SoapBodyTemplate,UPNPCP_SOAP_BodyHead,service->ServiceType, ZoomSteps,UPNPCP_SOAP_BodyTail);
	
	UPnPAddRef(service->Parent);
	ILibParseUri(service->ControlURL,&IP,&Port,&Path);
	
	headerBuffer = (char*)MALLOC(173 + (int)strlen(Path) + (int)strlen(IP) + (int)strlen(service->ServiceType));
	headerLength = sprintf(headerBuffer,UPNPCP_SOAP_Header,Path,IP,Port,service->ServiceType,"SetRelZoomPosition",bufferLength);
	
	memset((char *)&addr, 0, sizeof(addr));
	addr.sin_family = AF_INET;
	addr.sin_addr.s_addr = inet_addr(IP);
	addr.sin_port = htons(Port);
	
	invoke_data->CallbackPtr = CallbackPtr;
	invoke_data->User = user;
	ILibAddRequest_DirectEx(((struct UPnPCP*)service->Parent->CP)->HTTP, headerBuffer,headerLength,buffer,bufferLength,&addr, &UPnPInvoke_Focus_SetRelZoomPosition_Sink,service, invoke_data);	
	
	FREE(IP);
	FREE(Path);
}
void UPnPInvoke_Focus_SetAbsZoomPosition_Sink(void *reader, struct packetheader *header, int IsInterrupt,char* buffer, int *p_BeginPointer, int EndPointer, int done, void* _service, void *state)
{
	struct UPnPService *Service = (struct UPnPService*)_service;
	struct InvokeStruct *_InvokeData = (struct InvokeStruct*)state;
	if(done==0){return;}
	if(_InvokeData->CallbackPtr==NULL)
	{
		UPnPRelease(Service->Parent);
		FREE(_InvokeData);
		return;
	}
	else
	{
		if(header==NULL)
		{
			/* Connection Failed */
			((void (*)(struct UPnPService*,int,void*))_InvokeData->CallbackPtr)(Service,-1,_InvokeData->User);
			UPnPRelease(Service->Parent);
			FREE(_InvokeData);
			return;
		}
		else if(header->StatusCode!=200)
		{
			/* SOAP Fault */
			((void (*)(struct UPnPService*,int,void*))_InvokeData->CallbackPtr)(Service,UPnPGetErrorCode(buffer,EndPointer-(*p_BeginPointer)),_InvokeData->User);
			UPnPRelease(Service->Parent);
			FREE(_InvokeData);
			return;
		}
	}
	
	((void (*)(struct UPnPService*,int,void*))_InvokeData->CallbackPtr)(Service,0,_InvokeData->User);
	UPnPRelease(Service->Parent);
	FREE(_InvokeData);
}
void UPnPInvoke_Focus_SetAbsZoomPosition(struct UPnPService *service,void (*CallbackPtr)(struct UPnPService*,int,void*), void* user, unsigned int ZoomSteps)
{
	int headerLength;
	char *headerBuffer;
	char *SoapBodyTemplate;
	char* buffer;
	int bufferLength;
	char* IP;
	unsigned short Port;
	char* Path;
	struct sockaddr_in addr;
	struct InvokeStruct *invoke_data = (struct InvokeStruct*)MALLOC(sizeof(struct InvokeStruct));
	
	if(service==NULL)
	{
		FREE(invoke_data);
		return;
	}
	buffer = (char*)MALLOC((int)strlen(service->ServiceType)+301);
	SoapBodyTemplate = "%sSetAbsZoomPosition xmlns:u=\"%s\"><ZoomSteps>%u</ZoomSteps></u:SetAbsZoomPosition%s";
	bufferLength = sprintf(buffer,SoapBodyTemplate,UPNPCP_SOAP_BodyHead,service->ServiceType, ZoomSteps,UPNPCP_SOAP_BodyTail);
	
	UPnPAddRef(service->Parent);
	ILibParseUri(service->ControlURL,&IP,&Port,&Path);
	
	headerBuffer = (char*)MALLOC(173 + (int)strlen(Path) + (int)strlen(IP) + (int)strlen(service->ServiceType));
	headerLength = sprintf(headerBuffer,UPNPCP_SOAP_Header,Path,IP,Port,service->ServiceType,"SetAbsZoomPosition",bufferLength);
	
	memset((char *)&addr, 0, sizeof(addr));
	addr.sin_family = AF_INET;
	addr.sin_addr.s_addr = inet_addr(IP);
	addr.sin_port = htons(Port);
	
	invoke_data->CallbackPtr = CallbackPtr;
	invoke_data->User = user;
	ILibAddRequest_DirectEx(((struct UPnPCP*)service->Parent->CP)->HTTP, headerBuffer,headerLength,buffer,bufferLength,&addr, &UPnPInvoke_Focus_SetAbsZoomPosition_Sink,service, invoke_data);	
	
	FREE(IP);
	FREE(Path);
}
void UPnPInvoke_Focus_GetIrisPosition_Sink(void *reader, struct packetheader *header, int IsInterrupt,char* buffer, int *p_BeginPointer, int EndPointer, int done, void* _service, void *state)
{
	struct UPnPService *Service = (struct UPnPService*)_service;
	struct InvokeStruct *_InvokeData = (struct InvokeStruct*)state;
	int ArgLeft = 1;
	struct ILibXMLNode *xml;
	struct ILibXMLNode *__xml;
	char *tempBuffer;
	int tempBufferLength;
	unsigned long TempULong;
	unsigned int RetIrisSteps = 0;
	
	if(done==0){return;}
	if(_InvokeData->CallbackPtr==NULL)
	{
		UPnPRelease(Service->Parent);
		FREE(_InvokeData);
		return;
	}
	else
	{
		if(header==NULL)
		{
			/* Connection Failed */
			((void (*)(struct UPnPService*,int,void*,unsigned int))_InvokeData->CallbackPtr)(Service,IsInterrupt==0?-1:IsInterrupt,_InvokeData->User,INVALID_DATA);
			UPnPRelease(Service->Parent);
			FREE(_InvokeData);
			return;
		}
		else if(header->StatusCode!=200)
		{
			/* SOAP Fault */
			((void (*)(struct UPnPService*,int,void*,unsigned int))_InvokeData->CallbackPtr)(Service,UPnPGetErrorCode(buffer,EndPointer-(*p_BeginPointer)),_InvokeData->User,INVALID_DATA);
			UPnPRelease(Service->Parent);
			FREE(_InvokeData);
			return;
		}
	}
	
	__xml = xml = ILibParseXML(buffer,0,EndPointer-(*p_BeginPointer));
	ILibProcessXMLNodeList(xml);
	while(xml!=NULL)
	{
		if(xml->NameLength==23 && memcmp(xml->Name,"GetIrisPositionResponse",23)==0)
		{
			xml = xml->Next;
			while(xml!=NULL)
			{
				if(xml->NameLength==12 && memcmp(xml->Name,"RetIrisSteps",12) == 0)
				{
					tempBufferLength = ILibReadInnerXML(xml,&tempBuffer);
					--ArgLeft;
					if(ILibGetULong(tempBuffer,tempBufferLength,&TempULong)==0)
					{
						RetIrisSteps = (unsigned int) TempULong;
					}
				}
				xml = xml->Peer;
			}
		}
		if(xml!=NULL) {xml = xml->Next;}
	}
	ILibDestructXMLNodeList(__xml);
	
	if(ArgLeft!=0)
	{
		((void (*)(struct UPnPService*,int,void*,unsigned int))_InvokeData->CallbackPtr)(Service,-2,_InvokeData->User,INVALID_DATA);
	}
	else
	{
		((void (*)(struct UPnPService*,int,void*,unsigned int))_InvokeData->CallbackPtr)(Service,0,_InvokeData->User,RetIrisSteps);
	}
	UPnPRelease(Service->Parent);
	FREE(_InvokeData);
}
void UPnPInvoke_Focus_GetIrisPosition(struct UPnPService *service,void (*CallbackPtr)(struct UPnPService*,int,void*,unsigned int), void* user)
{
	int headerLength;
	char *headerBuffer;
	char *SoapBodyTemplate;
	char* buffer;
	int bufferLength;
	char* IP;
	unsigned short Port;
	char* Path;
	struct sockaddr_in addr;
	struct InvokeStruct *invoke_data = (struct InvokeStruct*)MALLOC(sizeof(struct InvokeStruct));
	
	if(service==NULL)
	{
		FREE(invoke_data);
		return;
	}
	buffer = (char*)MALLOC((int)strlen(service->ServiceType)+262);
	SoapBodyTemplate = "%sGetIrisPosition xmlns:u=\"%s\"></u:GetIrisPosition%s";
	bufferLength = sprintf(buffer,SoapBodyTemplate,UPNPCP_SOAP_BodyHead,service->ServiceType,UPNPCP_SOAP_BodyTail);
	
	UPnPAddRef(service->Parent);
	ILibParseUri(service->ControlURL,&IP,&Port,&Path);
	
	headerBuffer = (char*)MALLOC(170 + (int)strlen(Path) + (int)strlen(IP) + (int)strlen(service->ServiceType));
	headerLength = sprintf(headerBuffer,UPNPCP_SOAP_Header,Path,IP,Port,service->ServiceType,"GetIrisPosition",bufferLength);
	
	memset((char *)&addr, 0, sizeof(addr));
	addr.sin_family = AF_INET;
	addr.sin_addr.s_addr = inet_addr(IP);
	addr.sin_port = htons(Port);
	
	invoke_data->CallbackPtr = CallbackPtr;
	invoke_data->User = user;
	ILibAddRequest_DirectEx(((struct UPnPCP*)service->Parent->CP)->HTTP, headerBuffer,headerLength,buffer,bufferLength,&addr, &UPnPInvoke_Focus_GetIrisPosition_Sink,service, invoke_data);	
	
	FREE(IP);
	FREE(Path);
}
void UPnPInvoke_Focus_SetAbsIrisPosition_Sink(void *reader, struct packetheader *header, int IsInterrupt,char* buffer, int *p_BeginPointer, int EndPointer, int done, void* _service, void *state)
{
	struct UPnPService *Service = (struct UPnPService*)_service;
	struct InvokeStruct *_InvokeData = (struct InvokeStruct*)state;
	if(done==0){return;}
	if(_InvokeData->CallbackPtr==NULL)
	{
		UPnPRelease(Service->Parent);
		FREE(_InvokeData);
		return;
	}
	else
	{
		if(header==NULL)
		{
			/* Connection Failed */
			((void (*)(struct UPnPService*,int,void*))_InvokeData->CallbackPtr)(Service,-1,_InvokeData->User);
			UPnPRelease(Service->Parent);
			FREE(_InvokeData);
			return;
		}
		else if(header->StatusCode!=200)
		{
			/* SOAP Fault */
			((void (*)(struct UPnPService*,int,void*))_InvokeData->CallbackPtr)(Service,UPnPGetErrorCode(buffer,EndPointer-(*p_BeginPointer)),_InvokeData->User);
			UPnPRelease(Service->Parent);
			FREE(_InvokeData);
			return;
		}
	}
	
	((void (*)(struct UPnPService*,int,void*))_InvokeData->CallbackPtr)(Service,0,_InvokeData->User);
	UPnPRelease(Service->Parent);
	FREE(_InvokeData);
}
void UPnPInvoke_Focus_SetAbsIrisPosition(struct UPnPService *service,void (*CallbackPtr)(struct UPnPService*,int,void*), void* user, unsigned int IrisSteps)
{
	int headerLength;
	char *headerBuffer;
	char *SoapBodyTemplate;
	char* buffer;
	int bufferLength;
	char* IP;
	unsigned short Port;
	char* Path;
	struct sockaddr_in addr;
	struct InvokeStruct *invoke_data = (struct InvokeStruct*)MALLOC(sizeof(struct InvokeStruct));
	
	if(service==NULL)
	{
		FREE(invoke_data);
		return;
	}
	buffer = (char*)MALLOC((int)strlen(service->ServiceType)+301);
	SoapBodyTemplate = "%sSetAbsIrisPosition xmlns:u=\"%s\"><IrisSteps>%u</IrisSteps></u:SetAbsIrisPosition%s";
	bufferLength = sprintf(buffer,SoapBodyTemplate,UPNPCP_SOAP_BodyHead,service->ServiceType, IrisSteps,UPNPCP_SOAP_BodyTail);
	
	UPnPAddRef(service->Parent);
	ILibParseUri(service->ControlURL,&IP,&Port,&Path);
	
	headerBuffer = (char*)MALLOC(173 + (int)strlen(Path) + (int)strlen(IP) + (int)strlen(service->ServiceType));
	headerLength = sprintf(headerBuffer,UPNPCP_SOAP_Header,Path,IP,Port,service->ServiceType,"SetAbsIrisPosition",bufferLength);
	
	memset((char *)&addr, 0, sizeof(addr));
	addr.sin_family = AF_INET;
	addr.sin_addr.s_addr = inet_addr(IP);
	addr.sin_port = htons(Port);
	
	invoke_data->CallbackPtr = CallbackPtr;
	invoke_data->User = user;
	ILibAddRequest_DirectEx(((struct UPnPCP*)service->Parent->CP)->HTTP, headerBuffer,headerLength,buffer,bufferLength,&addr, &UPnPInvoke_Focus_SetAbsIrisPosition_Sink,service, invoke_data);	
	
	FREE(IP);
	FREE(Path);
}
void UPnPInvoke_Focus_GetFocusPosition_Sink(void *reader, struct packetheader *header, int IsInterrupt,char* buffer, int *p_BeginPointer, int EndPointer, int done, void* _service, void *state)
{
	struct UPnPService *Service = (struct UPnPService*)_service;
	struct InvokeStruct *_InvokeData = (struct InvokeStruct*)state;
	int ArgLeft = 1;
	struct ILibXMLNode *xml;
	struct ILibXMLNode *__xml;
	char *tempBuffer;
	int tempBufferLength;
	unsigned long TempULong;
	unsigned int RetFocusSteps = 0;
	
	if(done==0){return;}
	if(_InvokeData->CallbackPtr==NULL)
	{
		UPnPRelease(Service->Parent);
		FREE(_InvokeData);
		return;
	}
	else
	{
		if(header==NULL)
		{
			/* Connection Failed */
			((void (*)(struct UPnPService*,int,void*,unsigned int))_InvokeData->CallbackPtr)(Service,IsInterrupt==0?-1:IsInterrupt,_InvokeData->User,INVALID_DATA);
			UPnPRelease(Service->Parent);
			FREE(_InvokeData);
			return;
		}
		else if(header->StatusCode!=200)
		{
			/* SOAP Fault */
			((void (*)(struct UPnPService*,int,void*,unsigned int))_InvokeData->CallbackPtr)(Service,UPnPGetErrorCode(buffer,EndPointer-(*p_BeginPointer)),_InvokeData->User,INVALID_DATA);
			UPnPRelease(Service->Parent);
			FREE(_InvokeData);
			return;
		}
	}
	
	__xml = xml = ILibParseXML(buffer,0,EndPointer-(*p_BeginPointer));
	ILibProcessXMLNodeList(xml);
	while(xml!=NULL)
	{
		if(xml->NameLength==24 && memcmp(xml->Name,"GetFocusPositionResponse",24)==0)
		{
			xml = xml->Next;
			while(xml!=NULL)
			{
				if(xml->NameLength==13 && memcmp(xml->Name,"RetFocusSteps",13) == 0)
				{
					tempBufferLength = ILibReadInnerXML(xml,&tempBuffer);
					--ArgLeft;
					if(ILibGetULong(tempBuffer,tempBufferLength,&TempULong)==0)
					{
						RetFocusSteps = (unsigned int) TempULong;
					}
				}
				xml = xml->Peer;
			}
		}
		if(xml!=NULL) {xml = xml->Next;}
	}
	ILibDestructXMLNodeList(__xml);
	
	if(ArgLeft!=0)
	{
		((void (*)(struct UPnPService*,int,void*,unsigned int))_InvokeData->CallbackPtr)(Service,-2,_InvokeData->User,INVALID_DATA);
	}
	else
	{
		((void (*)(struct UPnPService*,int,void*,unsigned int))_InvokeData->CallbackPtr)(Service,0,_InvokeData->User,RetFocusSteps);
	}
	UPnPRelease(Service->Parent);
	FREE(_InvokeData);
}
void UPnPInvoke_Focus_GetFocusPosition(struct UPnPService *service,void (*CallbackPtr)(struct UPnPService*,int,void*,unsigned int), void* user)
{
	int headerLength;
	char *headerBuffer;
	char *SoapBodyTemplate;
	char* buffer;
	int bufferLength;
	char* IP;
	unsigned short Port;
	char* Path;
	struct sockaddr_in addr;
	struct InvokeStruct *invoke_data = (struct InvokeStruct*)MALLOC(sizeof(struct InvokeStruct));
	
	if(service==NULL)
	{
		FREE(invoke_data);
		return;
	}
	buffer = (char*)MALLOC((int)strlen(service->ServiceType)+264);
	SoapBodyTemplate = "%sGetFocusPosition xmlns:u=\"%s\"></u:GetFocusPosition%s";
	bufferLength = sprintf(buffer,SoapBodyTemplate,UPNPCP_SOAP_BodyHead,service->ServiceType,UPNPCP_SOAP_BodyTail);
	
	UPnPAddRef(service->Parent);
	ILibParseUri(service->ControlURL,&IP,&Port,&Path);
	
	headerBuffer = (char*)MALLOC(171 + (int)strlen(Path) + (int)strlen(IP) + (int)strlen(service->ServiceType));
	headerLength = sprintf(headerBuffer,UPNPCP_SOAP_Header,Path,IP,Port,service->ServiceType,"GetFocusPosition",bufferLength);
	
	memset((char *)&addr, 0, sizeof(addr));
	addr.sin_family = AF_INET;
	addr.sin_addr.s_addr = inet_addr(IP);
	addr.sin_port = htons(Port);
	
	invoke_data->CallbackPtr = CallbackPtr;
	invoke_data->User = user;
	ILibAddRequest_DirectEx(((struct UPnPCP*)service->Parent->CP)->HTTP, headerBuffer,headerLength,buffer,bufferLength,&addr, &UPnPInvoke_Focus_GetFocusPosition_Sink,service, invoke_data);	
	
	FREE(IP);
	FREE(Path);
}
void UPnPInvoke_Focus_GetZoomPosition_Sink(void *reader, struct packetheader *header, int IsInterrupt,char* buffer, int *p_BeginPointer, int EndPointer, int done, void* _service, void *state)
{
	struct UPnPService *Service = (struct UPnPService*)_service;
	struct InvokeStruct *_InvokeData = (struct InvokeStruct*)state;
	int ArgLeft = 1;
	struct ILibXMLNode *xml;
	struct ILibXMLNode *__xml;
	char *tempBuffer;
	int tempBufferLength;
	unsigned long TempULong;
	unsigned int RetZoomSteps = 0;
	
	if(done==0){return;}
	if(_InvokeData->CallbackPtr==NULL)
	{
		UPnPRelease(Service->Parent);
		FREE(_InvokeData);
		return;
	}
	else
	{
		if(header==NULL)
		{
			/* Connection Failed */
			((void (*)(struct UPnPService*,int,void*,unsigned int))_InvokeData->CallbackPtr)(Service,IsInterrupt==0?-1:IsInterrupt,_InvokeData->User,INVALID_DATA);
			UPnPRelease(Service->Parent);
			FREE(_InvokeData);
			return;
		}
		else if(header->StatusCode!=200)
		{
			/* SOAP Fault */
			((void (*)(struct UPnPService*,int,void*,unsigned int))_InvokeData->CallbackPtr)(Service,UPnPGetErrorCode(buffer,EndPointer-(*p_BeginPointer)),_InvokeData->User,INVALID_DATA);
			UPnPRelease(Service->Parent);
			FREE(_InvokeData);
			return;
		}
	}
	
	__xml = xml = ILibParseXML(buffer,0,EndPointer-(*p_BeginPointer));
	ILibProcessXMLNodeList(xml);
	while(xml!=NULL)
	{
		if(xml->NameLength==23 && memcmp(xml->Name,"GetZoomPositionResponse",23)==0)
		{
			xml = xml->Next;
			while(xml!=NULL)
			{
				if(xml->NameLength==12 && memcmp(xml->Name,"RetZoomSteps",12) == 0)
				{
					tempBufferLength = ILibReadInnerXML(xml,&tempBuffer);
					--ArgLeft;
					if(ILibGetULong(tempBuffer,tempBufferLength,&TempULong)==0)
					{
						RetZoomSteps = (unsigned int) TempULong;
					}
				}
				xml = xml->Peer;
			}
		}
		if(xml!=NULL) {xml = xml->Next;}
	}
	ILibDestructXMLNodeList(__xml);
	
	if(ArgLeft!=0)
	{
		((void (*)(struct UPnPService*,int,void*,unsigned int))_InvokeData->CallbackPtr)(Service,-2,_InvokeData->User,INVALID_DATA);
	}
	else
	{
		((void (*)(struct UPnPService*,int,void*,unsigned int))_InvokeData->CallbackPtr)(Service,0,_InvokeData->User,RetZoomSteps);
	}
	UPnPRelease(Service->Parent);
	FREE(_InvokeData);
}
void UPnPInvoke_Focus_GetZoomPosition(struct UPnPService *service,void (*CallbackPtr)(struct UPnPService*,int,void*,unsigned int), void* user)
{
	int headerLength;
	char *headerBuffer;
	char *SoapBodyTemplate;
	char* buffer;
	int bufferLength;
	char* IP;
	unsigned short Port;
	char* Path;
	struct sockaddr_in addr;
	struct InvokeStruct *invoke_data = (struct InvokeStruct*)MALLOC(sizeof(struct InvokeStruct));
	
	if(service==NULL)
	{
		FREE(invoke_data);
		return;
	}
	buffer = (char*)MALLOC((int)strlen(service->ServiceType)+262);
	SoapBodyTemplate = "%sGetZoomPosition xmlns:u=\"%s\"></u:GetZoomPosition%s";
	bufferLength = sprintf(buffer,SoapBodyTemplate,UPNPCP_SOAP_BodyHead,service->ServiceType,UPNPCP_SOAP_BodyTail);
	
	UPnPAddRef(service->Parent);
	ILibParseUri(service->ControlURL,&IP,&Port,&Path);
	
	headerBuffer = (char*)MALLOC(170 + (int)strlen(Path) + (int)strlen(IP) + (int)strlen(service->ServiceType));
	headerLength = sprintf(headerBuffer,UPNPCP_SOAP_Header,Path,IP,Port,service->ServiceType,"GetZoomPosition",bufferLength);
	
	memset((char *)&addr, 0, sizeof(addr));
	addr.sin_family = AF_INET;
	addr.sin_addr.s_addr = inet_addr(IP);
	addr.sin_port = htons(Port);
	
	invoke_data->CallbackPtr = CallbackPtr;
	invoke_data->User = user;
	ILibAddRequest_DirectEx(((struct UPnPCP*)service->Parent->CP)->HTTP, headerBuffer,headerLength,buffer,bufferLength,&addr, &UPnPInvoke_Focus_GetZoomPosition_Sink,service, invoke_data);	
	
	FREE(IP);
	FREE(Path);
}
void UPnPInvoke_Focus_SetRelIrisPosition_Sink(void *reader, struct packetheader *header, int IsInterrupt,char* buffer, int *p_BeginPointer, int EndPointer, int done, void* _service, void *state)
{
	struct UPnPService *Service = (struct UPnPService*)_service;
	struct InvokeStruct *_InvokeData = (struct InvokeStruct*)state;
	if(done==0){return;}
	if(_InvokeData->CallbackPtr==NULL)
	{
		UPnPRelease(Service->Parent);
		FREE(_InvokeData);
		return;
	}
	else
	{
		if(header==NULL)
		{
			/* Connection Failed */
			((void (*)(struct UPnPService*,int,void*))_InvokeData->CallbackPtr)(Service,-1,_InvokeData->User);
			UPnPRelease(Service->Parent);
			FREE(_InvokeData);
			return;
		}
		else if(header->StatusCode!=200)
		{
			/* SOAP Fault */
			((void (*)(struct UPnPService*,int,void*))_InvokeData->CallbackPtr)(Service,UPnPGetErrorCode(buffer,EndPointer-(*p_BeginPointer)),_InvokeData->User);
			UPnPRelease(Service->Parent);
			FREE(_InvokeData);
			return;
		}
	}
	
	((void (*)(struct UPnPService*,int,void*))_InvokeData->CallbackPtr)(Service,0,_InvokeData->User);
	UPnPRelease(Service->Parent);
	FREE(_InvokeData);
}
void UPnPInvoke_Focus_SetRelIrisPosition(struct UPnPService *service,void (*CallbackPtr)(struct UPnPService*,int,void*), void* user, int IrisSteps)
{
	int headerLength;
	char *headerBuffer;
	char *SoapBodyTemplate;
	char* buffer;
	int bufferLength;
	char* IP;
	unsigned short Port;
	char* Path;
	struct sockaddr_in addr;
	struct InvokeStruct *invoke_data = (struct InvokeStruct*)MALLOC(sizeof(struct InvokeStruct));
	
	if(service==NULL)
	{
		FREE(invoke_data);
		return;
	}
	buffer = (char*)MALLOC((int)strlen(service->ServiceType)+302);
	SoapBodyTemplate = "%sSetRelIrisPosition xmlns:u=\"%s\"><IrisSteps>%d</IrisSteps></u:SetRelIrisPosition%s";
	bufferLength = sprintf(buffer,SoapBodyTemplate,UPNPCP_SOAP_BodyHead,service->ServiceType, IrisSteps,UPNPCP_SOAP_BodyTail);
	
	UPnPAddRef(service->Parent);
	ILibParseUri(service->ControlURL,&IP,&Port,&Path);
	
	headerBuffer = (char*)MALLOC(173 + (int)strlen(Path) + (int)strlen(IP) + (int)strlen(service->ServiceType));
	headerLength = sprintf(headerBuffer,UPNPCP_SOAP_Header,Path,IP,Port,service->ServiceType,"SetRelIrisPosition",bufferLength);
	
	memset((char *)&addr, 0, sizeof(addr));
	addr.sin_family = AF_INET;
	addr.sin_addr.s_addr = inet_addr(IP);
	addr.sin_port = htons(Port);
	
	invoke_data->CallbackPtr = CallbackPtr;
	invoke_data->User = user;
	ILibAddRequest_DirectEx(((struct UPnPCP*)service->Parent->CP)->HTTP, headerBuffer,headerLength,buffer,bufferLength,&addr, &UPnPInvoke_Focus_SetRelIrisPosition_Sink,service, invoke_data);	
	
	FREE(IP);
	FREE(Path);
}
void UPnPInvoke_Focus_SetRelFocusPosition_Sink(void *reader, struct packetheader *header, int IsInterrupt,char* buffer, int *p_BeginPointer, int EndPointer, int done, void* _service, void *state)
{
	struct UPnPService *Service = (struct UPnPService*)_service;
	struct InvokeStruct *_InvokeData = (struct InvokeStruct*)state;
	if(done==0){return;}
	if(_InvokeData->CallbackPtr==NULL)
	{
		UPnPRelease(Service->Parent);
		FREE(_InvokeData);
		return;
	}
	else
	{
		if(header==NULL)
		{
			/* Connection Failed */
			((void (*)(struct UPnPService*,int,void*))_InvokeData->CallbackPtr)(Service,-1,_InvokeData->User);
			UPnPRelease(Service->Parent);
			FREE(_InvokeData);
			return;
		}
		else if(header->StatusCode!=200)
		{
			/* SOAP Fault */
			((void (*)(struct UPnPService*,int,void*))_InvokeData->CallbackPtr)(Service,UPnPGetErrorCode(buffer,EndPointer-(*p_BeginPointer)),_InvokeData->User);
			UPnPRelease(Service->Parent);
			FREE(_InvokeData);
			return;
		}
	}
	
	((void (*)(struct UPnPService*,int,void*))_InvokeData->CallbackPtr)(Service,0,_InvokeData->User);
	UPnPRelease(Service->Parent);
	FREE(_InvokeData);
}
void UPnPInvoke_Focus_SetRelFocusPosition(struct UPnPService *service,void (*CallbackPtr)(struct UPnPService*,int,void*), void* user, int FocusSteps)
{
	int headerLength;
	char *headerBuffer;
	char *SoapBodyTemplate;
	char* buffer;
	int bufferLength;
	char* IP;
	unsigned short Port;
	char* Path;
	struct sockaddr_in addr;
	struct InvokeStruct *invoke_data = (struct InvokeStruct*)MALLOC(sizeof(struct InvokeStruct));
	
	if(service==NULL)
	{
		FREE(invoke_data);
		return;
	}
	buffer = (char*)MALLOC((int)strlen(service->ServiceType)+306);
	SoapBodyTemplate = "%sSetRelFocusPosition xmlns:u=\"%s\"><FocusSteps>%d</FocusSteps></u:SetRelFocusPosition%s";
	bufferLength = sprintf(buffer,SoapBodyTemplate,UPNPCP_SOAP_BodyHead,service->ServiceType, FocusSteps,UPNPCP_SOAP_BodyTail);
	
	UPnPAddRef(service->Parent);
	ILibParseUri(service->ControlURL,&IP,&Port,&Path);
	
	headerBuffer = (char*)MALLOC(174 + (int)strlen(Path) + (int)strlen(IP) + (int)strlen(service->ServiceType));
	headerLength = sprintf(headerBuffer,UPNPCP_SOAP_Header,Path,IP,Port,service->ServiceType,"SetRelFocusPosition",bufferLength);
	
	memset((char *)&addr, 0, sizeof(addr));
	addr.sin_family = AF_INET;
	addr.sin_addr.s_addr = inet_addr(IP);
	addr.sin_port = htons(Port);
	
	invoke_data->CallbackPtr = CallbackPtr;
	invoke_data->User = user;
	ILibAddRequest_DirectEx(((struct UPnPCP*)service->Parent->CP)->HTTP, headerBuffer,headerLength,buffer,bufferLength,&addr, &UPnPInvoke_Focus_SetRelFocusPosition_Sink,service, invoke_data);	
	
	FREE(IP);
	FREE(Path);
}
void UPnPInvoke_Preset_GetPresetList_Sink(void *reader, struct packetheader *header, int IsInterrupt,char* buffer, int *p_BeginPointer, int EndPointer, int done, void* _service, void *state)
{
	struct UPnPService *Service = (struct UPnPService*)_service;
	struct InvokeStruct *_InvokeData = (struct InvokeStruct*)state;
	int ArgLeft = 1;
	struct ILibXMLNode *xml;
	struct ILibXMLNode *__xml;
	char *tempBuffer;
	int tempBufferLength;
	char* RetPresetList = NULL;
	
	if(done==0){return;}
	if(_InvokeData->CallbackPtr==NULL)
	{
		UPnPRelease(Service->Parent);
		FREE(_InvokeData);
		return;
	}
	else
	{
		if(header==NULL)
		{
			/* Connection Failed */
			((void (*)(struct UPnPService*,int,void*,char*))_InvokeData->CallbackPtr)(Service,IsInterrupt==0?-1:IsInterrupt,_InvokeData->User,INVALID_DATA);
			UPnPRelease(Service->Parent);
			FREE(_InvokeData);
			return;
		}
		else if(header->StatusCode!=200)
		{
			/* SOAP Fault */
			((void (*)(struct UPnPService*,int,void*,char*))_InvokeData->CallbackPtr)(Service,UPnPGetErrorCode(buffer,EndPointer-(*p_BeginPointer)),_InvokeData->User,INVALID_DATA);
			UPnPRelease(Service->Parent);
			FREE(_InvokeData);
			return;
		}
	}
	
	__xml = xml = ILibParseXML(buffer,0,EndPointer-(*p_BeginPointer));
	ILibProcessXMLNodeList(xml);
	while(xml!=NULL)
	{
		if(xml->NameLength==21 && memcmp(xml->Name,"GetPresetListResponse",21)==0)
		{
			xml = xml->Next;
			while(xml!=NULL)
			{
				if(xml->NameLength==13 && memcmp(xml->Name,"RetPresetList",13) == 0)
				{
					tempBufferLength = ILibReadInnerXML(xml,&tempBuffer);
					--ArgLeft;
					if(tempBufferLength!=0)
					{
						tempBuffer[tempBufferLength] = '\0';
						RetPresetList = tempBuffer;
					}
				}
				xml = xml->Peer;
			}
		}
		if(xml!=NULL) {xml = xml->Next;}
	}
	ILibDestructXMLNodeList(__xml);
	
	if(ArgLeft!=0)
	{
		((void (*)(struct UPnPService*,int,void*,char*))_InvokeData->CallbackPtr)(Service,-2,_InvokeData->User,INVALID_DATA);
	}
	else
	{
		((void (*)(struct UPnPService*,int,void*,char*))_InvokeData->CallbackPtr)(Service,0,_InvokeData->User,RetPresetList);
	}
	UPnPRelease(Service->Parent);
	FREE(_InvokeData);
}
void UPnPInvoke_Preset_GetPresetList(struct UPnPService *service,void (*CallbackPtr)(struct UPnPService*,int,void*,char*), void* user)
{
	int headerLength;
	char *headerBuffer;
	char *SoapBodyTemplate;
	char* buffer;
	int bufferLength;
	char* IP;
	unsigned short Port;
	char* Path;
	struct sockaddr_in addr;
	struct InvokeStruct *invoke_data = (struct InvokeStruct*)MALLOC(sizeof(struct InvokeStruct));
	
	if(service==NULL)
	{
		FREE(invoke_data);
		return;
	}
	buffer = (char*)MALLOC((int)strlen(service->ServiceType)+258);
	SoapBodyTemplate = "%sGetPresetList xmlns:u=\"%s\"></u:GetPresetList%s";
	bufferLength = sprintf(buffer,SoapBodyTemplate,UPNPCP_SOAP_BodyHead,service->ServiceType,UPNPCP_SOAP_BodyTail);
	
	UPnPAddRef(service->Parent);
	ILibParseUri(service->ControlURL,&IP,&Port,&Path);
	
	headerBuffer = (char*)MALLOC(168 + (int)strlen(Path) + (int)strlen(IP) + (int)strlen(service->ServiceType));
	headerLength = sprintf(headerBuffer,UPNPCP_SOAP_Header,Path,IP,Port,service->ServiceType,"GetPresetList",bufferLength);
	
	memset((char *)&addr, 0, sizeof(addr));
	addr.sin_family = AF_INET;
	addr.sin_addr.s_addr = inet_addr(IP);
	addr.sin_port = htons(Port);
	
	invoke_data->CallbackPtr = CallbackPtr;
	invoke_data->User = user;
	ILibAddRequest_DirectEx(((struct UPnPCP*)service->Parent->CP)->HTTP, headerBuffer,headerLength,buffer,bufferLength,&addr, &UPnPInvoke_Preset_GetPresetList_Sink,service, invoke_data);	
	
	FREE(IP);
	FREE(Path);
}
void UPnPInvoke_Preset_RemovePreset_Sink(void *reader, struct packetheader *header, int IsInterrupt,char* buffer, int *p_BeginPointer, int EndPointer, int done, void* _service, void *state)
{
	struct UPnPService *Service = (struct UPnPService*)_service;
	struct InvokeStruct *_InvokeData = (struct InvokeStruct*)state;
	if(done==0){return;}
	if(_InvokeData->CallbackPtr==NULL)
	{
		UPnPRelease(Service->Parent);
		FREE(_InvokeData);
		return;
	}
	else
	{
		if(header==NULL)
		{
			/* Connection Failed */
			((void (*)(struct UPnPService*,int,void*))_InvokeData->CallbackPtr)(Service,-1,_InvokeData->User);
			UPnPRelease(Service->Parent);
			FREE(_InvokeData);
			return;
		}
		else if(header->StatusCode!=200)
		{
			/* SOAP Fault */
			((void (*)(struct UPnPService*,int,void*))_InvokeData->CallbackPtr)(Service,UPnPGetErrorCode(buffer,EndPointer-(*p_BeginPointer)),_InvokeData->User);
			UPnPRelease(Service->Parent);
			FREE(_InvokeData);
			return;
		}
	}
	
	((void (*)(struct UPnPService*,int,void*))_InvokeData->CallbackPtr)(Service,0,_InvokeData->User);
	UPnPRelease(Service->Parent);
	FREE(_InvokeData);
}
void UPnPInvoke_Preset_RemovePreset(struct UPnPService *service,void (*CallbackPtr)(struct UPnPService*,int,void*), void* user, unsigned char PresetNumber)
{
	int headerLength;
	char *headerBuffer;
	char *SoapBodyTemplate;
	char* buffer;
	int bufferLength;
	char* IP;
	unsigned short Port;
	char* Path;
	struct sockaddr_in addr;
	struct InvokeStruct *invoke_data = (struct InvokeStruct*)MALLOC(sizeof(struct InvokeStruct));
	
	if(service==NULL)
	{
		FREE(invoke_data);
		return;
	}
	buffer = (char*)MALLOC((int)strlen(service->ServiceType)+288);
	SoapBodyTemplate = "%sRemovePreset xmlns:u=\"%s\"><PresetNumber>%u</PresetNumber></u:RemovePreset%s";
	bufferLength = sprintf(buffer,SoapBodyTemplate,UPNPCP_SOAP_BodyHead,service->ServiceType, PresetNumber,UPNPCP_SOAP_BodyTail);
	
	UPnPAddRef(service->Parent);
	ILibParseUri(service->ControlURL,&IP,&Port,&Path);
	
	headerBuffer = (char*)MALLOC(167 + (int)strlen(Path) + (int)strlen(IP) + (int)strlen(service->ServiceType));
	headerLength = sprintf(headerBuffer,UPNPCP_SOAP_Header,Path,IP,Port,service->ServiceType,"RemovePreset",bufferLength);
	
	memset((char *)&addr, 0, sizeof(addr));
	addr.sin_family = AF_INET;
	addr.sin_addr.s_addr = inet_addr(IP);
	addr.sin_port = htons(Port);
	
	invoke_data->CallbackPtr = CallbackPtr;
	invoke_data->User = user;
	ILibAddRequest_DirectEx(((struct UPnPCP*)service->Parent->CP)->HTTP, headerBuffer,headerLength,buffer,bufferLength,&addr, &UPnPInvoke_Preset_RemovePreset_Sink,service, invoke_data);	
	
	FREE(IP);
	FREE(Path);
}
void UPnPInvoke_Preset_GoToPreset_Sink(void *reader, struct packetheader *header, int IsInterrupt,char* buffer, int *p_BeginPointer, int EndPointer, int done, void* _service, void *state)
{
	struct UPnPService *Service = (struct UPnPService*)_service;
	struct InvokeStruct *_InvokeData = (struct InvokeStruct*)state;
	if(done==0){return;}
	if(_InvokeData->CallbackPtr==NULL)
	{
		UPnPRelease(Service->Parent);
		FREE(_InvokeData);
		return;
	}
	else
	{
		if(header==NULL)
		{
			/* Connection Failed */
			((void (*)(struct UPnPService*,int,void*))_InvokeData->CallbackPtr)(Service,-1,_InvokeData->User);
			UPnPRelease(Service->Parent);
			FREE(_InvokeData);
			return;
		}
		else if(header->StatusCode!=200)
		{
			/* SOAP Fault */
			((void (*)(struct UPnPService*,int,void*))_InvokeData->CallbackPtr)(Service,UPnPGetErrorCode(buffer,EndPointer-(*p_BeginPointer)),_InvokeData->User);
			UPnPRelease(Service->Parent);
			FREE(_InvokeData);
			return;
		}
	}
	
	((void (*)(struct UPnPService*,int,void*))_InvokeData->CallbackPtr)(Service,0,_InvokeData->User);
	UPnPRelease(Service->Parent);
	FREE(_InvokeData);
}
void UPnPInvoke_Preset_GoToPreset(struct UPnPService *service,void (*CallbackPtr)(struct UPnPService*,int,void*), void* user, unsigned char PresetNumber)
{
	int headerLength;
	char *headerBuffer;
	char *SoapBodyTemplate;
	char* buffer;
	int bufferLength;
	char* IP;
	unsigned short Port;
	char* Path;
	struct sockaddr_in addr;
	struct InvokeStruct *invoke_data = (struct InvokeStruct*)MALLOC(sizeof(struct InvokeStruct));
	
	if(service==NULL)
	{
		FREE(invoke_data);
		return;
	}
	buffer = (char*)MALLOC((int)strlen(service->ServiceType)+284);
	SoapBodyTemplate = "%sGoToPreset xmlns:u=\"%s\"><PresetNumber>%u</PresetNumber></u:GoToPreset%s";
	bufferLength = sprintf(buffer,SoapBodyTemplate,UPNPCP_SOAP_BodyHead,service->ServiceType, PresetNumber,UPNPCP_SOAP_BodyTail);
	
	UPnPAddRef(service->Parent);
	ILibParseUri(service->ControlURL,&IP,&Port,&Path);
	
	headerBuffer = (char*)MALLOC(165 + (int)strlen(Path) + (int)strlen(IP) + (int)strlen(service->ServiceType));
	headerLength = sprintf(headerBuffer,UPNPCP_SOAP_Header,Path,IP,Port,service->ServiceType,"GoToPreset",bufferLength);
	
	memset((char *)&addr, 0, sizeof(addr));
	addr.sin_family = AF_INET;
	addr.sin_addr.s_addr = inet_addr(IP);
	addr.sin_port = htons(Port);
	
	invoke_data->CallbackPtr = CallbackPtr;
	invoke_data->User = user;
	ILibAddRequest_DirectEx(((struct UPnPCP*)service->Parent->CP)->HTTP, headerBuffer,headerLength,buffer,bufferLength,&addr, &UPnPInvoke_Preset_GoToPreset_Sink,service, invoke_data);	
	
	FREE(IP);
	FREE(Path);
}
void UPnPInvoke_Preset_SetPreset_Sink(void *reader, struct packetheader *header, int IsInterrupt,char* buffer, int *p_BeginPointer, int EndPointer, int done, void* _service, void *state)
{
	struct UPnPService *Service = (struct UPnPService*)_service;
	struct InvokeStruct *_InvokeData = (struct InvokeStruct*)state;
	if(done==0){return;}
	if(_InvokeData->CallbackPtr==NULL)
	{
		UPnPRelease(Service->Parent);
		FREE(_InvokeData);
		return;
	}
	else
	{
		if(header==NULL)
		{
			/* Connection Failed */
			((void (*)(struct UPnPService*,int,void*))_InvokeData->CallbackPtr)(Service,-1,_InvokeData->User);
			UPnPRelease(Service->Parent);
			FREE(_InvokeData);
			return;
		}
		else if(header->StatusCode!=200)
		{
			/* SOAP Fault */
			((void (*)(struct UPnPService*,int,void*))_InvokeData->CallbackPtr)(Service,UPnPGetErrorCode(buffer,EndPointer-(*p_BeginPointer)),_InvokeData->User);
			UPnPRelease(Service->Parent);
			FREE(_InvokeData);
			return;
		}
	}
	
	((void (*)(struct UPnPService*,int,void*))_InvokeData->CallbackPtr)(Service,0,_InvokeData->User);
	UPnPRelease(Service->Parent);
	FREE(_InvokeData);
}
void UPnPInvoke_Preset_SetPreset(struct UPnPService *service,void (*CallbackPtr)(struct UPnPService*,int,void*), void* user, unsigned char PresetNumber, char* PresetName)
{
	int headerLength;
	char *headerBuffer;
	char *SoapBodyTemplate;
	char* buffer;
	int bufferLength;
	char* IP;
	unsigned short Port;
	char* Path;
	struct sockaddr_in addr;
	struct InvokeStruct *invoke_data = (struct InvokeStruct*)MALLOC(sizeof(struct InvokeStruct));
	
	if(service==NULL)
	{
		FREE(invoke_data);
		return;
	}
	buffer = (char*)MALLOC((int)strlen(service->ServiceType)+(int)strlen(PresetName)+307);
	SoapBodyTemplate = "%sSetPreset xmlns:u=\"%s\"><PresetNumber>%u</PresetNumber><PresetName>%s</PresetName></u:SetPreset%s";
	bufferLength = sprintf(buffer,SoapBodyTemplate,UPNPCP_SOAP_BodyHead,service->ServiceType, PresetNumber, PresetName,UPNPCP_SOAP_BodyTail);
	
	UPnPAddRef(service->Parent);
	ILibParseUri(service->ControlURL,&IP,&Port,&Path);
	
	headerBuffer = (char*)MALLOC(164 + (int)strlen(Path) + (int)strlen(IP) + (int)strlen(service->ServiceType));
	headerLength = sprintf(headerBuffer,UPNPCP_SOAP_Header,Path,IP,Port,service->ServiceType,"SetPreset",bufferLength);
	
	memset((char *)&addr, 0, sizeof(addr));
	addr.sin_family = AF_INET;
	addr.sin_addr.s_addr = inet_addr(IP);
	addr.sin_port = htons(Port);
	
	invoke_data->CallbackPtr = CallbackPtr;
	invoke_data->User = user;
	ILibAddRequest_DirectEx(((struct UPnPCP*)service->Parent->CP)->HTTP, headerBuffer,headerLength,buffer,bufferLength,&addr, &UPnPInvoke_Preset_SetPreset_Sink,service, invoke_data);	
	
	FREE(IP);
	FREE(Path);
}
void UPnPInvoke_DigitalSecurityCameraSettings_GetColorSaturation_Sink(void *reader, struct packetheader *header, int IsInterrupt,char* buffer, int *p_BeginPointer, int EndPointer, int done, void* _service, void *state)
{
	struct UPnPService *Service = (struct UPnPService*)_service;
	struct InvokeStruct *_InvokeData = (struct InvokeStruct*)state;
	int ArgLeft = 1;
	struct ILibXMLNode *xml;
	struct ILibXMLNode *__xml;
	char *tempBuffer;
	int tempBufferLength;
	unsigned long TempULong;
	unsigned int RetColorSaturation = 0;
	
	if(done==0){return;}
	if(_InvokeData->CallbackPtr==NULL)
	{
		UPnPRelease(Service->Parent);
		FREE(_InvokeData);
		return;
	}
	else
	{
		if(header==NULL)
		{
			/* Connection Failed */
			((void (*)(struct UPnPService*,int,void*,unsigned int))_InvokeData->CallbackPtr)(Service,IsInterrupt==0?-1:IsInterrupt,_InvokeData->User,INVALID_DATA);
			UPnPRelease(Service->Parent);
			FREE(_InvokeData);
			return;
		}
		else if(header->StatusCode!=200)
		{
			/* SOAP Fault */
			((void (*)(struct UPnPService*,int,void*,unsigned int))_InvokeData->CallbackPtr)(Service,UPnPGetErrorCode(buffer,EndPointer-(*p_BeginPointer)),_InvokeData->User,INVALID_DATA);
			UPnPRelease(Service->Parent);
			FREE(_InvokeData);
			return;
		}
	}
	
	__xml = xml = ILibParseXML(buffer,0,EndPointer-(*p_BeginPointer));
	ILibProcessXMLNodeList(xml);
	while(xml!=NULL)
	{
		if(xml->NameLength==26 && memcmp(xml->Name,"GetColorSaturationResponse",26)==0)
		{
			xml = xml->Next;
			while(xml!=NULL)
			{
				if(xml->NameLength==18 && memcmp(xml->Name,"RetColorSaturation",18) == 0)
				{
					tempBufferLength = ILibReadInnerXML(xml,&tempBuffer);
					--ArgLeft;
					if(ILibGetULong(tempBuffer,tempBufferLength,&TempULong)==0)
					{
						RetColorSaturation = (unsigned int) TempULong;
					}
				}
				xml = xml->Peer;
			}
		}
		if(xml!=NULL) {xml = xml->Next;}
	}
	ILibDestructXMLNodeList(__xml);
	
	if(ArgLeft!=0)
	{
		((void (*)(struct UPnPService*,int,void*,unsigned int))_InvokeData->CallbackPtr)(Service,-2,_InvokeData->User,INVALID_DATA);
	}
	else
	{
		((void (*)(struct UPnPService*,int,void*,unsigned int))_InvokeData->CallbackPtr)(Service,0,_InvokeData->User,RetColorSaturation);
	}
	UPnPRelease(Service->Parent);
	FREE(_InvokeData);
}
void UPnPInvoke_DigitalSecurityCameraSettings_GetColorSaturation(struct UPnPService *service,void (*CallbackPtr)(struct UPnPService*,int,void*,unsigned int), void* user)
{
	int headerLength;
	char *headerBuffer;
	char *SoapBodyTemplate;
	char* buffer;
	int bufferLength;
	char* IP;
	unsigned short Port;
	char* Path;
	struct sockaddr_in addr;
	struct InvokeStruct *invoke_data = (struct InvokeStruct*)MALLOC(sizeof(struct InvokeStruct));
	
	if(service==NULL)
	{
		FREE(invoke_data);
		return;
	}
	buffer = (char*)MALLOC((int)strlen(service->ServiceType)+268);
	SoapBodyTemplate = "%sGetColorSaturation xmlns:u=\"%s\"></u:GetColorSaturation%s";
	bufferLength = sprintf(buffer,SoapBodyTemplate,UPNPCP_SOAP_BodyHead,service->ServiceType,UPNPCP_SOAP_BodyTail);
	
	UPnPAddRef(service->Parent);
	ILibParseUri(service->ControlURL,&IP,&Port,&Path);
	
	headerBuffer = (char*)MALLOC(173 + (int)strlen(Path) + (int)strlen(IP) + (int)strlen(service->ServiceType));
	headerLength = sprintf(headerBuffer,UPNPCP_SOAP_Header,Path,IP,Port,service->ServiceType,"GetColorSaturation",bufferLength);
	
	memset((char *)&addr, 0, sizeof(addr));
	addr.sin_family = AF_INET;
	addr.sin_addr.s_addr = inet_addr(IP);
	addr.sin_port = htons(Port);
	
	invoke_data->CallbackPtr = CallbackPtr;
	invoke_data->User = user;
	ILibAddRequest_DirectEx(((struct UPnPCP*)service->Parent->CP)->HTTP, headerBuffer,headerLength,buffer,bufferLength,&addr, &UPnPInvoke_DigitalSecurityCameraSettings_GetColorSaturation_Sink,service, invoke_data);	
	
	FREE(IP);
	FREE(Path);
}
void UPnPInvoke_DigitalSecurityCameraSettings_SetBrightness_Sink(void *reader, struct packetheader *header, int IsInterrupt,char* buffer, int *p_BeginPointer, int EndPointer, int done, void* _service, void *state)
{
	struct UPnPService *Service = (struct UPnPService*)_service;
	struct InvokeStruct *_InvokeData = (struct InvokeStruct*)state;
	if(done==0){return;}
	if(_InvokeData->CallbackPtr==NULL)
	{
		UPnPRelease(Service->Parent);
		FREE(_InvokeData);
		return;
	}
	else
	{
		if(header==NULL)
		{
			/* Connection Failed */
			((void (*)(struct UPnPService*,int,void*))_InvokeData->CallbackPtr)(Service,-1,_InvokeData->User);
			UPnPRelease(Service->Parent);
			FREE(_InvokeData);
			return;
		}
		else if(header->StatusCode!=200)
		{
			/* SOAP Fault */
			((void (*)(struct UPnPService*,int,void*))_InvokeData->CallbackPtr)(Service,UPnPGetErrorCode(buffer,EndPointer-(*p_BeginPointer)),_InvokeData->User);
			UPnPRelease(Service->Parent);
			FREE(_InvokeData);
			return;
		}
	}
	
	((void (*)(struct UPnPService*,int,void*))_InvokeData->CallbackPtr)(Service,0,_InvokeData->User);
	UPnPRelease(Service->Parent);
	FREE(_InvokeData);
}
void UPnPInvoke_DigitalSecurityCameraSettings_SetBrightness(struct UPnPService *service,void (*CallbackPtr)(struct UPnPService*,int,void*), void* user, unsigned int NewBrightness)
{
	int headerLength;
	char *headerBuffer;
	char *SoapBodyTemplate;
	char* buffer;
	int bufferLength;
	char* IP;
	unsigned short Port;
	char* Path;
	struct sockaddr_in addr;
	struct InvokeStruct *invoke_data = (struct InvokeStruct*)MALLOC(sizeof(struct InvokeStruct));
	
	if(service==NULL)
	{
		FREE(invoke_data);
		return;
	}
	buffer = (char*)MALLOC((int)strlen(service->ServiceType)+299);
	SoapBodyTemplate = "%sSetBrightness xmlns:u=\"%s\"><NewBrightness>%u</NewBrightness></u:SetBrightness%s";
	bufferLength = sprintf(buffer,SoapBodyTemplate,UPNPCP_SOAP_BodyHead,service->ServiceType, NewBrightness,UPNPCP_SOAP_BodyTail);
	
	UPnPAddRef(service->Parent);
	ILibParseUri(service->ControlURL,&IP,&Port,&Path);
	
	headerBuffer = (char*)MALLOC(168 + (int)strlen(Path) + (int)strlen(IP) + (int)strlen(service->ServiceType));
	headerLength = sprintf(headerBuffer,UPNPCP_SOAP_Header,Path,IP,Port,service->ServiceType,"SetBrightness",bufferLength);
	
	memset((char *)&addr, 0, sizeof(addr));
	addr.sin_family = AF_INET;
	addr.sin_addr.s_addr = inet_addr(IP);
	addr.sin_port = htons(Port);
	
	invoke_data->CallbackPtr = CallbackPtr;
	invoke_data->User = user;
	ILibAddRequest_DirectEx(((struct UPnPCP*)service->Parent->CP)->HTTP, headerBuffer,headerLength,buffer,bufferLength,&addr, &UPnPInvoke_DigitalSecurityCameraSettings_SetBrightness_Sink,service, invoke_data);	
	
	FREE(IP);
	FREE(Path);
}
void UPnPInvoke_DigitalSecurityCameraSettings_GetFixedWhiteBalance_Sink(void *reader, struct packetheader *header, int IsInterrupt,char* buffer, int *p_BeginPointer, int EndPointer, int done, void* _service, void *state)
{
	struct UPnPService *Service = (struct UPnPService*)_service;
	struct InvokeStruct *_InvokeData = (struct InvokeStruct*)state;
	int ArgLeft = 1;
	struct ILibXMLNode *xml;
	struct ILibXMLNode *__xml;
	char *tempBuffer;
	int tempBufferLength;
	unsigned long TempULong;
	unsigned int RetFixedWhiteBalance = 0;
	
	if(done==0){return;}
	if(_InvokeData->CallbackPtr==NULL)
	{
		UPnPRelease(Service->Parent);
		FREE(_InvokeData);
		return;
	}
	else
	{
		if(header==NULL)
		{
			/* Connection Failed */
			((void (*)(struct UPnPService*,int,void*,unsigned int))_InvokeData->CallbackPtr)(Service,IsInterrupt==0?-1:IsInterrupt,_InvokeData->User,INVALID_DATA);
			UPnPRelease(Service->Parent);
			FREE(_InvokeData);
			return;
		}
		else if(header->StatusCode!=200)
		{
			/* SOAP Fault */
			((void (*)(struct UPnPService*,int,void*,unsigned int))_InvokeData->CallbackPtr)(Service,UPnPGetErrorCode(buffer,EndPointer-(*p_BeginPointer)),_InvokeData->User,INVALID_DATA);
			UPnPRelease(Service->Parent);
			FREE(_InvokeData);
			return;
		}
	}
	
	__xml = xml = ILibParseXML(buffer,0,EndPointer-(*p_BeginPointer));
	ILibProcessXMLNodeList(xml);
	while(xml!=NULL)
	{
		if(xml->NameLength==28 && memcmp(xml->Name,"GetFixedWhiteBalanceResponse",28)==0)
		{
			xml = xml->Next;
			while(xml!=NULL)
			{
				if(xml->NameLength==20 && memcmp(xml->Name,"RetFixedWhiteBalance",20) == 0)
				{
					tempBufferLength = ILibReadInnerXML(xml,&tempBuffer);
					--ArgLeft;
					if(ILibGetULong(tempBuffer,tempBufferLength,&TempULong)==0)
					{
						RetFixedWhiteBalance = (unsigned int) TempULong;
					}
				}
				xml = xml->Peer;
			}
		}
		if(xml!=NULL) {xml = xml->Next;}
	}
	ILibDestructXMLNodeList(__xml);
	
	if(ArgLeft!=0)
	{
		((void (*)(struct UPnPService*,int,void*,unsigned int))_InvokeData->CallbackPtr)(Service,-2,_InvokeData->User,INVALID_DATA);
	}
	else
	{
		((void (*)(struct UPnPService*,int,void*,unsigned int))_InvokeData->CallbackPtr)(Service,0,_InvokeData->User,RetFixedWhiteBalance);
	}
	UPnPRelease(Service->Parent);
	FREE(_InvokeData);
}
void UPnPInvoke_DigitalSecurityCameraSettings_GetFixedWhiteBalance(struct UPnPService *service,void (*CallbackPtr)(struct UPnPService*,int,void*,unsigned int), void* user)
{
	int headerLength;
	char *headerBuffer;
	char *SoapBodyTemplate;
	char* buffer;
	int bufferLength;
	char* IP;
	unsigned short Port;
	char* Path;
	struct sockaddr_in addr;
	struct InvokeStruct *invoke_data = (struct InvokeStruct*)MALLOC(sizeof(struct InvokeStruct));
	
	if(service==NULL)
	{
		FREE(invoke_data);
		return;
	}
	buffer = (char*)MALLOC((int)strlen(service->ServiceType)+272);
	SoapBodyTemplate = "%sGetFixedWhiteBalance xmlns:u=\"%s\"></u:GetFixedWhiteBalance%s";
	bufferLength = sprintf(buffer,SoapBodyTemplate,UPNPCP_SOAP_BodyHead,service->ServiceType,UPNPCP_SOAP_BodyTail);
	
	UPnPAddRef(service->Parent);
	ILibParseUri(service->ControlURL,&IP,&Port,&Path);
	
	headerBuffer = (char*)MALLOC(175 + (int)strlen(Path) + (int)strlen(IP) + (int)strlen(service->ServiceType));
	headerLength = sprintf(headerBuffer,UPNPCP_SOAP_Header,Path,IP,Port,service->ServiceType,"GetFixedWhiteBalance",bufferLength);
	
	memset((char *)&addr, 0, sizeof(addr));
	addr.sin_family = AF_INET;
	addr.sin_addr.s_addr = inet_addr(IP);
	addr.sin_port = htons(Port);
	
	invoke_data->CallbackPtr = CallbackPtr;
	invoke_data->User = user;
	ILibAddRequest_DirectEx(((struct UPnPCP*)service->Parent->CP)->HTTP, headerBuffer,headerLength,buffer,bufferLength,&addr, &UPnPInvoke_DigitalSecurityCameraSettings_GetFixedWhiteBalance_Sink,service, invoke_data);	
	
	FREE(IP);
	FREE(Path);
}
void UPnPInvoke_DigitalSecurityCameraSettings_IncreaseBrightness_Sink(void *reader, struct packetheader *header, int IsInterrupt,char* buffer, int *p_BeginPointer, int EndPointer, int done, void* _service, void *state)
{
	struct UPnPService *Service = (struct UPnPService*)_service;
	struct InvokeStruct *_InvokeData = (struct InvokeStruct*)state;
	if(done==0){return;}
	if(_InvokeData->CallbackPtr==NULL)
	{
		UPnPRelease(Service->Parent);
		FREE(_InvokeData);
		return;
	}
	else
	{
		if(header==NULL)
		{
			/* Connection Failed */
			((void (*)(struct UPnPService*,int,void*))_InvokeData->CallbackPtr)(Service,-1,_InvokeData->User);
			UPnPRelease(Service->Parent);
			FREE(_InvokeData);
			return;
		}
		else if(header->StatusCode!=200)
		{
			/* SOAP Fault */
			((void (*)(struct UPnPService*,int,void*))_InvokeData->CallbackPtr)(Service,UPnPGetErrorCode(buffer,EndPointer-(*p_BeginPointer)),_InvokeData->User);
			UPnPRelease(Service->Parent);
			FREE(_InvokeData);
			return;
		}
	}
	
	((void (*)(struct UPnPService*,int,void*))_InvokeData->CallbackPtr)(Service,0,_InvokeData->User);
	UPnPRelease(Service->Parent);
	FREE(_InvokeData);
}
void UPnPInvoke_DigitalSecurityCameraSettings_IncreaseBrightness(struct UPnPService *service,void (*CallbackPtr)(struct UPnPService*,int,void*), void* user)
{
	int headerLength;
	char *headerBuffer;
	char *SoapBodyTemplate;
	char* buffer;
	int bufferLength;
	char* IP;
	unsigned short Port;
	char* Path;
	struct sockaddr_in addr;
	struct InvokeStruct *invoke_data = (struct InvokeStruct*)MALLOC(sizeof(struct InvokeStruct));
	
	if(service==NULL)
	{
		FREE(invoke_data);
		return;
	}
	buffer = (char*)MALLOC((int)strlen(service->ServiceType)+268);
	SoapBodyTemplate = "%sIncreaseBrightness xmlns:u=\"%s\"></u:IncreaseBrightness%s";
	bufferLength = sprintf(buffer,SoapBodyTemplate,UPNPCP_SOAP_BodyHead,service->ServiceType,UPNPCP_SOAP_BodyTail);
	
	UPnPAddRef(service->Parent);
	ILibParseUri(service->ControlURL,&IP,&Port,&Path);
	
	headerBuffer = (char*)MALLOC(173 + (int)strlen(Path) + (int)strlen(IP) + (int)strlen(service->ServiceType));
	headerLength = sprintf(headerBuffer,UPNPCP_SOAP_Header,Path,IP,Port,service->ServiceType,"IncreaseBrightness",bufferLength);
	
	memset((char *)&addr, 0, sizeof(addr));
	addr.sin_family = AF_INET;
	addr.sin_addr.s_addr = inet_addr(IP);
	addr.sin_port = htons(Port);
	
	invoke_data->CallbackPtr = CallbackPtr;
	invoke_data->User = user;
	ILibAddRequest_DirectEx(((struct UPnPCP*)service->Parent->CP)->HTTP, headerBuffer,headerLength,buffer,bufferLength,&addr, &UPnPInvoke_DigitalSecurityCameraSettings_IncreaseBrightness_Sink,service, invoke_data);	
	
	FREE(IP);
	FREE(Path);
}
void UPnPInvoke_DigitalSecurityCameraSettings_DecreaseBrightness_Sink(void *reader, struct packetheader *header, int IsInterrupt,char* buffer, int *p_BeginPointer, int EndPointer, int done, void* _service, void *state)
{
	struct UPnPService *Service = (struct UPnPService*)_service;
	struct InvokeStruct *_InvokeData = (struct InvokeStruct*)state;
	if(done==0){return;}
	if(_InvokeData->CallbackPtr==NULL)
	{
		UPnPRelease(Service->Parent);
		FREE(_InvokeData);
		return;
	}
	else
	{
		if(header==NULL)
		{
			/* Connection Failed */
			((void (*)(struct UPnPService*,int,void*))_InvokeData->CallbackPtr)(Service,-1,_InvokeData->User);
			UPnPRelease(Service->Parent);
			FREE(_InvokeData);
			return;
		}
		else if(header->StatusCode!=200)
		{
			/* SOAP Fault */
			((void (*)(struct UPnPService*,int,void*))_InvokeData->CallbackPtr)(Service,UPnPGetErrorCode(buffer,EndPointer-(*p_BeginPointer)),_InvokeData->User);
			UPnPRelease(Service->Parent);
			FREE(_InvokeData);
			return;
		}
	}
	
	((void (*)(struct UPnPService*,int,void*))_InvokeData->CallbackPtr)(Service,0,_InvokeData->User);
	UPnPRelease(Service->Parent);
	FREE(_InvokeData);
}
void UPnPInvoke_DigitalSecurityCameraSettings_DecreaseBrightness(struct UPnPService *service,void (*CallbackPtr)(struct UPnPService*,int,void*), void* user)
{
	int headerLength;
	char *headerBuffer;
	char *SoapBodyTemplate;
	char* buffer;
	int bufferLength;
	char* IP;
	unsigned short Port;
	char* Path;
	struct sockaddr_in addr;
	struct InvokeStruct *invoke_data = (struct InvokeStruct*)MALLOC(sizeof(struct InvokeStruct));
	
	if(service==NULL)
	{
		FREE(invoke_data);
		return;
	}
	buffer = (char*)MALLOC((int)strlen(service->ServiceType)+268);
	SoapBodyTemplate = "%sDecreaseBrightness xmlns:u=\"%s\"></u:DecreaseBrightness%s";
	bufferLength = sprintf(buffer,SoapBodyTemplate,UPNPCP_SOAP_BodyHead,service->ServiceType,UPNPCP_SOAP_BodyTail);
	
	UPnPAddRef(service->Parent);
	ILibParseUri(service->ControlURL,&IP,&Port,&Path);
	
	headerBuffer = (char*)MALLOC(173 + (int)strlen(Path) + (int)strlen(IP) + (int)strlen(service->ServiceType));
	headerLength = sprintf(headerBuffer,UPNPCP_SOAP_Header,Path,IP,Port,service->ServiceType,"DecreaseBrightness",bufferLength);
	
	memset((char *)&addr, 0, sizeof(addr));
	addr.sin_family = AF_INET;
	addr.sin_addr.s_addr = inet_addr(IP);
	addr.sin_port = htons(Port);
	
	invoke_data->CallbackPtr = CallbackPtr;
	invoke_data->User = user;
	ILibAddRequest_DirectEx(((struct UPnPCP*)service->Parent->CP)->HTTP, headerBuffer,headerLength,buffer,bufferLength,&addr, &UPnPInvoke_DigitalSecurityCameraSettings_DecreaseBrightness_Sink,service, invoke_data);	
	
	FREE(IP);
	FREE(Path);
}
void UPnPInvoke_DigitalSecurityCameraSettings_DecreaseColorSaturation_Sink(void *reader, struct packetheader *header, int IsInterrupt,char* buffer, int *p_BeginPointer, int EndPointer, int done, void* _service, void *state)
{
	struct UPnPService *Service = (struct UPnPService*)_service;
	struct InvokeStruct *_InvokeData = (struct InvokeStruct*)state;
	if(done==0){return;}
	if(_InvokeData->CallbackPtr==NULL)
	{
		UPnPRelease(Service->Parent);
		FREE(_InvokeData);
		return;
	}
	else
	{
		if(header==NULL)
		{
			/* Connection Failed */
			((void (*)(struct UPnPService*,int,void*))_InvokeData->CallbackPtr)(Service,-1,_InvokeData->User);
			UPnPRelease(Service->Parent);
			FREE(_InvokeData);
			return;
		}
		else if(header->StatusCode!=200)
		{
			/* SOAP Fault */
			((void (*)(struct UPnPService*,int,void*))_InvokeData->CallbackPtr)(Service,UPnPGetErrorCode(buffer,EndPointer-(*p_BeginPointer)),_InvokeData->User);
			UPnPRelease(Service->Parent);
			FREE(_InvokeData);
			return;
		}
	}
	
	((void (*)(struct UPnPService*,int,void*))_InvokeData->CallbackPtr)(Service,0,_InvokeData->User);
	UPnPRelease(Service->Parent);
	FREE(_InvokeData);
}
void UPnPInvoke_DigitalSecurityCameraSettings_DecreaseColorSaturation(struct UPnPService *service,void (*CallbackPtr)(struct UPnPService*,int,void*), void* user)
{
	int headerLength;
	char *headerBuffer;
	char *SoapBodyTemplate;
	char* buffer;
	int bufferLength;
	char* IP;
	unsigned short Port;
	char* Path;
	struct sockaddr_in addr;
	struct InvokeStruct *invoke_data = (struct InvokeStruct*)MALLOC(sizeof(struct InvokeStruct));
	
	if(service==NULL)
	{
		FREE(invoke_data);
		return;
	}
	buffer = (char*)MALLOC((int)strlen(service->ServiceType)+278);
	SoapBodyTemplate = "%sDecreaseColorSaturation xmlns:u=\"%s\"></u:DecreaseColorSaturation%s";
	bufferLength = sprintf(buffer,SoapBodyTemplate,UPNPCP_SOAP_BodyHead,service->ServiceType,UPNPCP_SOAP_BodyTail);
	
	UPnPAddRef(service->Parent);
	ILibParseUri(service->ControlURL,&IP,&Port,&Path);
	
	headerBuffer = (char*)MALLOC(178 + (int)strlen(Path) + (int)strlen(IP) + (int)strlen(service->ServiceType));
	headerLength = sprintf(headerBuffer,UPNPCP_SOAP_Header,Path,IP,Port,service->ServiceType,"DecreaseColorSaturation",bufferLength);
	
	memset((char *)&addr, 0, sizeof(addr));
	addr.sin_family = AF_INET;
	addr.sin_addr.s_addr = inet_addr(IP);
	addr.sin_port = htons(Port);
	
	invoke_data->CallbackPtr = CallbackPtr;
	invoke_data->User = user;
	ILibAddRequest_DirectEx(((struct UPnPCP*)service->Parent->CP)->HTTP, headerBuffer,headerLength,buffer,bufferLength,&addr, &UPnPInvoke_DigitalSecurityCameraSettings_DecreaseColorSaturation_Sink,service, invoke_data);	
	
	FREE(IP);
	FREE(Path);
}
void UPnPInvoke_DigitalSecurityCameraSettings_SetColorSaturation_Sink(void *reader, struct packetheader *header, int IsInterrupt,char* buffer, int *p_BeginPointer, int EndPointer, int done, void* _service, void *state)
{
	struct UPnPService *Service = (struct UPnPService*)_service;
	struct InvokeStruct *_InvokeData = (struct InvokeStruct*)state;
	if(done==0){return;}
	if(_InvokeData->CallbackPtr==NULL)
	{
		UPnPRelease(Service->Parent);
		FREE(_InvokeData);
		return;
	}
	else
	{
		if(header==NULL)
		{
			/* Connection Failed */
			((void (*)(struct UPnPService*,int,void*))_InvokeData->CallbackPtr)(Service,-1,_InvokeData->User);
			UPnPRelease(Service->Parent);
			FREE(_InvokeData);
			return;
		}
		else if(header->StatusCode!=200)
		{
			/* SOAP Fault */
			((void (*)(struct UPnPService*,int,void*))_InvokeData->CallbackPtr)(Service,UPnPGetErrorCode(buffer,EndPointer-(*p_BeginPointer)),_InvokeData->User);
			UPnPRelease(Service->Parent);
			FREE(_InvokeData);
			return;
		}
	}
	
	((void (*)(struct UPnPService*,int,void*))_InvokeData->CallbackPtr)(Service,0,_InvokeData->User);
	UPnPRelease(Service->Parent);
	FREE(_InvokeData);
}
void UPnPInvoke_DigitalSecurityCameraSettings_SetColorSaturation(struct UPnPService *service,void (*CallbackPtr)(struct UPnPService*,int,void*), void* user, unsigned int NewColorSaturation)
{
	int headerLength;
	char *headerBuffer;
	char *SoapBodyTemplate;
	char* buffer;
	int bufferLength;
	char* IP;
	unsigned short Port;
	char* Path;
	struct sockaddr_in addr;
	struct InvokeStruct *invoke_data = (struct InvokeStruct*)MALLOC(sizeof(struct InvokeStruct));
	
	if(service==NULL)
	{
		FREE(invoke_data);
		return;
	}
	buffer = (char*)MALLOC((int)strlen(service->ServiceType)+319);
	SoapBodyTemplate = "%sSetColorSaturation xmlns:u=\"%s\"><NewColorSaturation>%u</NewColorSaturation></u:SetColorSaturation%s";
	bufferLength = sprintf(buffer,SoapBodyTemplate,UPNPCP_SOAP_BodyHead,service->ServiceType, NewColorSaturation,UPNPCP_SOAP_BodyTail);
	
	UPnPAddRef(service->Parent);
	ILibParseUri(service->ControlURL,&IP,&Port,&Path);
	
	headerBuffer = (char*)MALLOC(173 + (int)strlen(Path) + (int)strlen(IP) + (int)strlen(service->ServiceType));
	headerLength = sprintf(headerBuffer,UPNPCP_SOAP_Header,Path,IP,Port,service->ServiceType,"SetColorSaturation",bufferLength);
	
	memset((char *)&addr, 0, sizeof(addr));
	addr.sin_family = AF_INET;
	addr.sin_addr.s_addr = inet_addr(IP);
	addr.sin_port = htons(Port);
	
	invoke_data->CallbackPtr = CallbackPtr;
	invoke_data->User = user;
	ILibAddRequest_DirectEx(((struct UPnPCP*)service->Parent->CP)->HTTP, headerBuffer,headerLength,buffer,bufferLength,&addr, &UPnPInvoke_DigitalSecurityCameraSettings_SetColorSaturation_Sink,service, invoke_data);	
	
	FREE(IP);
	FREE(Path);
}
void UPnPInvoke_DigitalSecurityCameraSettings_SetFixedWhiteBalance_Sink(void *reader, struct packetheader *header, int IsInterrupt,char* buffer, int *p_BeginPointer, int EndPointer, int done, void* _service, void *state)
{
	struct UPnPService *Service = (struct UPnPService*)_service;
	struct InvokeStruct *_InvokeData = (struct InvokeStruct*)state;
	if(done==0){return;}
	if(_InvokeData->CallbackPtr==NULL)
	{
		UPnPRelease(Service->Parent);
		FREE(_InvokeData);
		return;
	}
	else
	{
		if(header==NULL)
		{
			/* Connection Failed */
			((void (*)(struct UPnPService*,int,void*))_InvokeData->CallbackPtr)(Service,-1,_InvokeData->User);
			UPnPRelease(Service->Parent);
			FREE(_InvokeData);
			return;
		}
		else if(header->StatusCode!=200)
		{
			/* SOAP Fault */
			((void (*)(struct UPnPService*,int,void*))_InvokeData->CallbackPtr)(Service,UPnPGetErrorCode(buffer,EndPointer-(*p_BeginPointer)),_InvokeData->User);
			UPnPRelease(Service->Parent);
			FREE(_InvokeData);
			return;
		}
	}
	
	((void (*)(struct UPnPService*,int,void*))_InvokeData->CallbackPtr)(Service,0,_InvokeData->User);
	UPnPRelease(Service->Parent);
	FREE(_InvokeData);
}
void UPnPInvoke_DigitalSecurityCameraSettings_SetFixedWhiteBalance(struct UPnPService *service,void (*CallbackPtr)(struct UPnPService*,int,void*), void* user, unsigned int NewFixedWhiteBalance)
{
	int headerLength;
	char *headerBuffer;
	char *SoapBodyTemplate;
	char* buffer;
	int bufferLength;
	char* IP;
	unsigned short Port;
	char* Path;
	struct sockaddr_in addr;
	struct InvokeStruct *invoke_data = (struct InvokeStruct*)MALLOC(sizeof(struct InvokeStruct));
	
	if(service==NULL)
	{
		FREE(invoke_data);
		return;
	}
	buffer = (char*)MALLOC((int)strlen(service->ServiceType)+327);
	SoapBodyTemplate = "%sSetFixedWhiteBalance xmlns:u=\"%s\"><NewFixedWhiteBalance>%u</NewFixedWhiteBalance></u:SetFixedWhiteBalance%s";
	bufferLength = sprintf(buffer,SoapBodyTemplate,UPNPCP_SOAP_BodyHead,service->ServiceType, NewFixedWhiteBalance,UPNPCP_SOAP_BodyTail);
	
	UPnPAddRef(service->Parent);
	ILibParseUri(service->ControlURL,&IP,&Port,&Path);
	
	headerBuffer = (char*)MALLOC(175 + (int)strlen(Path) + (int)strlen(IP) + (int)strlen(service->ServiceType));
	headerLength = sprintf(headerBuffer,UPNPCP_SOAP_Header,Path,IP,Port,service->ServiceType,"SetFixedWhiteBalance",bufferLength);
	
	memset((char *)&addr, 0, sizeof(addr));
	addr.sin_family = AF_INET;
	addr.sin_addr.s_addr = inet_addr(IP);
	addr.sin_port = htons(Port);
	
	invoke_data->CallbackPtr = CallbackPtr;
	invoke_data->User = user;
	ILibAddRequest_DirectEx(((struct UPnPCP*)service->Parent->CP)->HTTP, headerBuffer,headerLength,buffer,bufferLength,&addr, &UPnPInvoke_DigitalSecurityCameraSettings_SetFixedWhiteBalance_Sink,service, invoke_data);	
	
	FREE(IP);
	FREE(Path);
}
void UPnPInvoke_DigitalSecurityCameraSettings_IncreaseColorSaturation_Sink(void *reader, struct packetheader *header, int IsInterrupt,char* buffer, int *p_BeginPointer, int EndPointer, int done, void* _service, void *state)
{
	struct UPnPService *Service = (struct UPnPService*)_service;
	struct InvokeStruct *_InvokeData = (struct InvokeStruct*)state;
	if(done==0){return;}
	if(_InvokeData->CallbackPtr==NULL)
	{
		UPnPRelease(Service->Parent);
		FREE(_InvokeData);
		return;
	}
	else
	{
		if(header==NULL)
		{
			/* Connection Failed */
			((void (*)(struct UPnPService*,int,void*))_InvokeData->CallbackPtr)(Service,-1,_InvokeData->User);
			UPnPRelease(Service->Parent);
			FREE(_InvokeData);
			return;
		}
		else if(header->StatusCode!=200)
		{
			/* SOAP Fault */
			((void (*)(struct UPnPService*,int,void*))_InvokeData->CallbackPtr)(Service,UPnPGetErrorCode(buffer,EndPointer-(*p_BeginPointer)),_InvokeData->User);
			UPnPRelease(Service->Parent);
			FREE(_InvokeData);
			return;
		}
	}
	
	((void (*)(struct UPnPService*,int,void*))_InvokeData->CallbackPtr)(Service,0,_InvokeData->User);
	UPnPRelease(Service->Parent);
	FREE(_InvokeData);
}
void UPnPInvoke_DigitalSecurityCameraSettings_IncreaseColorSaturation(struct UPnPService *service,void (*CallbackPtr)(struct UPnPService*,int,void*), void* user)
{
	int headerLength;
	char *headerBuffer;
	char *SoapBodyTemplate;
	char* buffer;
	int bufferLength;
	char* IP;
	unsigned short Port;
	char* Path;
	struct sockaddr_in addr;
	struct InvokeStruct *invoke_data = (struct InvokeStruct*)MALLOC(sizeof(struct InvokeStruct));
	
	if(service==NULL)
	{
		FREE(invoke_data);
		return;
	}
	buffer = (char*)MALLOC((int)strlen(service->ServiceType)+278);
	SoapBodyTemplate = "%sIncreaseColorSaturation xmlns:u=\"%s\"></u:IncreaseColorSaturation%s";
	bufferLength = sprintf(buffer,SoapBodyTemplate,UPNPCP_SOAP_BodyHead,service->ServiceType,UPNPCP_SOAP_BodyTail);
	
	UPnPAddRef(service->Parent);
	ILibParseUri(service->ControlURL,&IP,&Port,&Path);
	
	headerBuffer = (char*)MALLOC(178 + (int)strlen(Path) + (int)strlen(IP) + (int)strlen(service->ServiceType));
	headerLength = sprintf(headerBuffer,UPNPCP_SOAP_Header,Path,IP,Port,service->ServiceType,"IncreaseColorSaturation",bufferLength);
	
	memset((char *)&addr, 0, sizeof(addr));
	addr.sin_family = AF_INET;
	addr.sin_addr.s_addr = inet_addr(IP);
	addr.sin_port = htons(Port);
	
	invoke_data->CallbackPtr = CallbackPtr;
	invoke_data->User = user;
	ILibAddRequest_DirectEx(((struct UPnPCP*)service->Parent->CP)->HTTP, headerBuffer,headerLength,buffer,bufferLength,&addr, &UPnPInvoke_DigitalSecurityCameraSettings_IncreaseColorSaturation_Sink,service, invoke_data);	
	
	FREE(IP);
	FREE(Path);
}
void UPnPInvoke_DigitalSecurityCameraSettings_GetBrightness_Sink(void *reader, struct packetheader *header, int IsInterrupt,char* buffer, int *p_BeginPointer, int EndPointer, int done, void* _service, void *state)
{
	struct UPnPService *Service = (struct UPnPService*)_service;
	struct InvokeStruct *_InvokeData = (struct InvokeStruct*)state;
	int ArgLeft = 1;
	struct ILibXMLNode *xml;
	struct ILibXMLNode *__xml;
	char *tempBuffer;
	int tempBufferLength;
	unsigned long TempULong;
	unsigned int RetBrightness = 0;
	
	if(done==0){return;}
	if(_InvokeData->CallbackPtr==NULL)
	{
		UPnPRelease(Service->Parent);
		FREE(_InvokeData);
		return;
	}
	else
	{
		if(header==NULL)
		{
			/* Connection Failed */
			((void (*)(struct UPnPService*,int,void*,unsigned int))_InvokeData->CallbackPtr)(Service,IsInterrupt==0?-1:IsInterrupt,_InvokeData->User,INVALID_DATA);
			UPnPRelease(Service->Parent);
			FREE(_InvokeData);
			return;
		}
		else if(header->StatusCode!=200)
		{
			/* SOAP Fault */
			((void (*)(struct UPnPService*,int,void*,unsigned int))_InvokeData->CallbackPtr)(Service,UPnPGetErrorCode(buffer,EndPointer-(*p_BeginPointer)),_InvokeData->User,INVALID_DATA);
			UPnPRelease(Service->Parent);
			FREE(_InvokeData);
			return;
		}
	}
	
	__xml = xml = ILibParseXML(buffer,0,EndPointer-(*p_BeginPointer));
	ILibProcessXMLNodeList(xml);
	while(xml!=NULL)
	{
		if(xml->NameLength==21 && memcmp(xml->Name,"GetBrightnessResponse",21)==0)
		{
			xml = xml->Next;
			while(xml!=NULL)
			{
				if(xml->NameLength==13 && memcmp(xml->Name,"RetBrightness",13) == 0)
				{
					tempBufferLength = ILibReadInnerXML(xml,&tempBuffer);
					--ArgLeft;
					if(ILibGetULong(tempBuffer,tempBufferLength,&TempULong)==0)
					{
						RetBrightness = (unsigned int) TempULong;
					}
				}
				xml = xml->Peer;
			}
		}
		if(xml!=NULL) {xml = xml->Next;}
	}
	ILibDestructXMLNodeList(__xml);
	
	if(ArgLeft!=0)
	{
		((void (*)(struct UPnPService*,int,void*,unsigned int))_InvokeData->CallbackPtr)(Service,-2,_InvokeData->User,INVALID_DATA);
	}
	else
	{
		((void (*)(struct UPnPService*,int,void*,unsigned int))_InvokeData->CallbackPtr)(Service,0,_InvokeData->User,RetBrightness);
	}
	UPnPRelease(Service->Parent);
	FREE(_InvokeData);
}
void UPnPInvoke_DigitalSecurityCameraSettings_GetBrightness(struct UPnPService *service,void (*CallbackPtr)(struct UPnPService*,int,void*,unsigned int), void* user)
{
	int headerLength;
	char *headerBuffer;
	char *SoapBodyTemplate;
	char* buffer;
	int bufferLength;
	char* IP;
	unsigned short Port;
	char* Path;
	struct sockaddr_in addr;
	struct InvokeStruct *invoke_data = (struct InvokeStruct*)MALLOC(sizeof(struct InvokeStruct));
	
	if(service==NULL)
	{
		FREE(invoke_data);
		return;
	}
	buffer = (char*)MALLOC((int)strlen(service->ServiceType)+258);
	SoapBodyTemplate = "%sGetBrightness xmlns:u=\"%s\"></u:GetBrightness%s";
	bufferLength = sprintf(buffer,SoapBodyTemplate,UPNPCP_SOAP_BodyHead,service->ServiceType,UPNPCP_SOAP_BodyTail);
	
	UPnPAddRef(service->Parent);
	ILibParseUri(service->ControlURL,&IP,&Port,&Path);
	
	headerBuffer = (char*)MALLOC(168 + (int)strlen(Path) + (int)strlen(IP) + (int)strlen(service->ServiceType));
	headerLength = sprintf(headerBuffer,UPNPCP_SOAP_Header,Path,IP,Port,service->ServiceType,"GetBrightness",bufferLength);
	
	memset((char *)&addr, 0, sizeof(addr));
	addr.sin_family = AF_INET;
	addr.sin_addr.s_addr = inet_addr(IP);
	addr.sin_port = htons(Port);
	
	invoke_data->CallbackPtr = CallbackPtr;
	invoke_data->User = user;
	ILibAddRequest_DirectEx(((struct UPnPCP*)service->Parent->CP)->HTTP, headerBuffer,headerLength,buffer,bufferLength,&addr, &UPnPInvoke_DigitalSecurityCameraSettings_GetBrightness_Sink,service, invoke_data);	
	
	FREE(IP);
	FREE(Path);
}
void UPnPInvoke_DigitalSecurityCameraStillImage_SetDefaultCompressionLevel_Sink(void *reader, struct packetheader *header, int IsInterrupt,char* buffer, int *p_BeginPointer, int EndPointer, int done, void* _service, void *state)
{
	struct UPnPService *Service = (struct UPnPService*)_service;
	struct InvokeStruct *_InvokeData = (struct InvokeStruct*)state;
	if(done==0){return;}
	if(_InvokeData->CallbackPtr==NULL)
	{
		UPnPRelease(Service->Parent);
		FREE(_InvokeData);
		return;
	}
	else
	{
		if(header==NULL)
		{
			/* Connection Failed */
			((void (*)(struct UPnPService*,int,void*))_InvokeData->CallbackPtr)(Service,-1,_InvokeData->User);
			UPnPRelease(Service->Parent);
			FREE(_InvokeData);
			return;
		}
		else if(header->StatusCode!=200)
		{
			/* SOAP Fault */
			((void (*)(struct UPnPService*,int,void*))_InvokeData->CallbackPtr)(Service,UPnPGetErrorCode(buffer,EndPointer-(*p_BeginPointer)),_InvokeData->User);
			UPnPRelease(Service->Parent);
			FREE(_InvokeData);
			return;
		}
	}
	
	((void (*)(struct UPnPService*,int,void*))_InvokeData->CallbackPtr)(Service,0,_InvokeData->User);
	UPnPRelease(Service->Parent);
	FREE(_InvokeData);
}
void UPnPInvoke_DigitalSecurityCameraStillImage_SetDefaultCompressionLevel(struct UPnPService *service,void (*CallbackPtr)(struct UPnPService*,int,void*), void* user, char* ReqCompressionLevel)
{
	int headerLength;
	char *headerBuffer;
	char *SoapBodyTemplate;
	char* buffer;
	int bufferLength;
	char* IP;
	unsigned short Port;
	char* Path;
	struct sockaddr_in addr;
	struct InvokeStruct *invoke_data = (struct InvokeStruct*)MALLOC(sizeof(struct InvokeStruct));
	
	if(service==NULL)
	{
		FREE(invoke_data);
		return;
	}
	buffer = (char*)MALLOC((int)strlen(service->ServiceType)+(int)strlen(ReqCompressionLevel)+327);
	SoapBodyTemplate = "%sSetDefaultCompressionLevel xmlns:u=\"%s\"><ReqCompressionLevel>%s</ReqCompressionLevel></u:SetDefaultCompressionLevel%s";
	bufferLength = sprintf(buffer,SoapBodyTemplate,UPNPCP_SOAP_BodyHead,service->ServiceType, ReqCompressionLevel,UPNPCP_SOAP_BodyTail);
	
	UPnPAddRef(service->Parent);
	ILibParseUri(service->ControlURL,&IP,&Port,&Path);
	
	headerBuffer = (char*)MALLOC(181 + (int)strlen(Path) + (int)strlen(IP) + (int)strlen(service->ServiceType));
	headerLength = sprintf(headerBuffer,UPNPCP_SOAP_Header,Path,IP,Port,service->ServiceType,"SetDefaultCompressionLevel",bufferLength);
	
	memset((char *)&addr, 0, sizeof(addr));
	addr.sin_family = AF_INET;
	addr.sin_addr.s_addr = inet_addr(IP);
	addr.sin_port = htons(Port);
	
	invoke_data->CallbackPtr = CallbackPtr;
	invoke_data->User = user;
	ILibAddRequest_DirectEx(((struct UPnPCP*)service->Parent->CP)->HTTP, headerBuffer,headerLength,buffer,bufferLength,&addr, &UPnPInvoke_DigitalSecurityCameraStillImage_SetDefaultCompressionLevel_Sink,service, invoke_data);	
	
	FREE(IP);
	FREE(Path);
}
void UPnPInvoke_DigitalSecurityCameraStillImage_GetAvailableEncodings_Sink(void *reader, struct packetheader *header, int IsInterrupt,char* buffer, int *p_BeginPointer, int EndPointer, int done, void* _service, void *state)
{
	struct UPnPService *Service = (struct UPnPService*)_service;
	struct InvokeStruct *_InvokeData = (struct InvokeStruct*)state;
	int ArgLeft = 1;
	struct ILibXMLNode *xml;
	struct ILibXMLNode *__xml;
	char *tempBuffer;
	int tempBufferLength;
	char* RetAvailableEncodings = NULL;
	
	if(done==0){return;}
	if(_InvokeData->CallbackPtr==NULL)
	{
		UPnPRelease(Service->Parent);
		FREE(_InvokeData);
		return;
	}
	else
	{
		if(header==NULL)
		{
			/* Connection Failed */
			((void (*)(struct UPnPService*,int,void*,char*))_InvokeData->CallbackPtr)(Service,IsInterrupt==0?-1:IsInterrupt,_InvokeData->User,INVALID_DATA);
			UPnPRelease(Service->Parent);
			FREE(_InvokeData);
			return;
		}
		else if(header->StatusCode!=200)
		{
			/* SOAP Fault */
			((void (*)(struct UPnPService*,int,void*,char*))_InvokeData->CallbackPtr)(Service,UPnPGetErrorCode(buffer,EndPointer-(*p_BeginPointer)),_InvokeData->User,INVALID_DATA);
			UPnPRelease(Service->Parent);
			FREE(_InvokeData);
			return;
		}
	}
	
	__xml = xml = ILibParseXML(buffer,0,EndPointer-(*p_BeginPointer));
	ILibProcessXMLNodeList(xml);
	while(xml!=NULL)
	{
		if(xml->NameLength==29 && memcmp(xml->Name,"GetAvailableEncodingsResponse",29)==0)
		{
			xml = xml->Next;
			while(xml!=NULL)
			{
				if(xml->NameLength==21 && memcmp(xml->Name,"RetAvailableEncodings",21) == 0)
				{
					tempBufferLength = ILibReadInnerXML(xml,&tempBuffer);
					--ArgLeft;
					if(tempBufferLength!=0)
					{
						tempBuffer[tempBufferLength] = '\0';
						RetAvailableEncodings = tempBuffer;
					}
				}
				xml = xml->Peer;
			}
		}
		if(xml!=NULL) {xml = xml->Next;}
	}
	ILibDestructXMLNodeList(__xml);
	
	if(ArgLeft!=0)
	{
		((void (*)(struct UPnPService*,int,void*,char*))_InvokeData->CallbackPtr)(Service,-2,_InvokeData->User,INVALID_DATA);
	}
	else
	{
		((void (*)(struct UPnPService*,int,void*,char*))_InvokeData->CallbackPtr)(Service,0,_InvokeData->User,RetAvailableEncodings);
	}
	UPnPRelease(Service->Parent);
	FREE(_InvokeData);
}
void UPnPInvoke_DigitalSecurityCameraStillImage_GetAvailableEncodings(struct UPnPService *service,void (*CallbackPtr)(struct UPnPService*,int,void*,char*), void* user)
{
	int headerLength;
	char *headerBuffer;
	char *SoapBodyTemplate;
	char* buffer;
	int bufferLength;
	char* IP;
	unsigned short Port;
	char* Path;
	struct sockaddr_in addr;
	struct InvokeStruct *invoke_data = (struct InvokeStruct*)MALLOC(sizeof(struct InvokeStruct));
	
	if(service==NULL)
	{
		FREE(invoke_data);
		return;
	}
	buffer = (char*)MALLOC((int)strlen(service->ServiceType)+274);
	SoapBodyTemplate = "%sGetAvailableEncodings xmlns:u=\"%s\"></u:GetAvailableEncodings%s";
	bufferLength = sprintf(buffer,SoapBodyTemplate,UPNPCP_SOAP_BodyHead,service->ServiceType,UPNPCP_SOAP_BodyTail);
	
	UPnPAddRef(service->Parent);
	ILibParseUri(service->ControlURL,&IP,&Port,&Path);
	
	headerBuffer = (char*)MALLOC(176 + (int)strlen(Path) + (int)strlen(IP) + (int)strlen(service->ServiceType));
	headerLength = sprintf(headerBuffer,UPNPCP_SOAP_Header,Path,IP,Port,service->ServiceType,"GetAvailableEncodings",bufferLength);
	
	memset((char *)&addr, 0, sizeof(addr));
	addr.sin_family = AF_INET;
	addr.sin_addr.s_addr = inet_addr(IP);
	addr.sin_port = htons(Port);
	
	invoke_data->CallbackPtr = CallbackPtr;
	invoke_data->User = user;
	ILibAddRequest_DirectEx(((struct UPnPCP*)service->Parent->CP)->HTTP, headerBuffer,headerLength,buffer,bufferLength,&addr, &UPnPInvoke_DigitalSecurityCameraStillImage_GetAvailableEncodings_Sink,service, invoke_data);	
	
	FREE(IP);
	FREE(Path);
}
void UPnPInvoke_DigitalSecurityCameraStillImage_GetImagePresentationURL_Sink(void *reader, struct packetheader *header, int IsInterrupt,char* buffer, int *p_BeginPointer, int EndPointer, int done, void* _service, void *state)
{
	struct UPnPService *Service = (struct UPnPService*)_service;
	struct InvokeStruct *_InvokeData = (struct InvokeStruct*)state;
	int ArgLeft = 1;
	struct ILibXMLNode *xml;
	struct ILibXMLNode *__xml;
	char *tempBuffer;
	int tempBufferLength;
	char* RetImagePresentationURL = NULL;
	
	if(done==0){return;}
	if(_InvokeData->CallbackPtr==NULL)
	{
		UPnPRelease(Service->Parent);
		FREE(_InvokeData);
		return;
	}
	else
	{
		if(header==NULL)
		{
			/* Connection Failed */
			((void (*)(struct UPnPService*,int,void*,char*))_InvokeData->CallbackPtr)(Service,IsInterrupt==0?-1:IsInterrupt,_InvokeData->User,INVALID_DATA);
			UPnPRelease(Service->Parent);
			FREE(_InvokeData);
			return;
		}
		else if(header->StatusCode!=200)
		{
			/* SOAP Fault */
			((void (*)(struct UPnPService*,int,void*,char*))_InvokeData->CallbackPtr)(Service,UPnPGetErrorCode(buffer,EndPointer-(*p_BeginPointer)),_InvokeData->User,INVALID_DATA);
			UPnPRelease(Service->Parent);
			FREE(_InvokeData);
			return;
		}
	}
	
	__xml = xml = ILibParseXML(buffer,0,EndPointer-(*p_BeginPointer));
	ILibProcessXMLNodeList(xml);
	while(xml!=NULL)
	{
		if(xml->NameLength==31 && memcmp(xml->Name,"GetImagePresentationURLResponse",31)==0)
		{
			xml = xml->Next;
			while(xml!=NULL)
			{
				if(xml->NameLength==23 && memcmp(xml->Name,"RetImagePresentationURL",23) == 0)
				{
					tempBufferLength = ILibReadInnerXML(xml,&tempBuffer);
					--ArgLeft;
					if(tempBufferLength!=0)
					{
						tempBuffer[tempBufferLength] = '\0';
						RetImagePresentationURL = tempBuffer;
					}
				}
				xml = xml->Peer;
			}
		}
		if(xml!=NULL) {xml = xml->Next;}
	}
	ILibDestructXMLNodeList(__xml);
	
	if(ArgLeft!=0)
	{
		((void (*)(struct UPnPService*,int,void*,char*))_InvokeData->CallbackPtr)(Service,-2,_InvokeData->User,INVALID_DATA);
	}
	else
	{
		((void (*)(struct UPnPService*,int,void*,char*))_InvokeData->CallbackPtr)(Service,0,_InvokeData->User,RetImagePresentationURL);
	}
	UPnPRelease(Service->Parent);
	FREE(_InvokeData);
}
void UPnPInvoke_DigitalSecurityCameraStillImage_GetImagePresentationURL(struct UPnPService *service,void (*CallbackPtr)(struct UPnPService*,int,void*,char*), void* user, char* ReqEncoding, char* ReqCompression, char* ReqResolution)
{
	int headerLength;
	char *headerBuffer;
	char *SoapBodyTemplate;
	char* buffer;
	int bufferLength;
	char* IP;
	unsigned short Port;
	char* Path;
	struct sockaddr_in addr;
	struct InvokeStruct *invoke_data = (struct InvokeStruct*)MALLOC(sizeof(struct InvokeStruct));
	
	if(service==NULL)
	{
		FREE(invoke_data);
		return;
	}
	buffer = (char*)MALLOC((int)strlen(service->ServiceType)+(int)strlen(ReqEncoding)+(int)strlen(ReqCompression)+(int)strlen(ReqResolution)+369);
	SoapBodyTemplate = "%sGetImagePresentationURL xmlns:u=\"%s\"><ReqEncoding>%s</ReqEncoding><ReqCompression>%s</ReqCompression><ReqResolution>%s</ReqResolution></u:GetImagePresentationURL%s";
	bufferLength = sprintf(buffer,SoapBodyTemplate,UPNPCP_SOAP_BodyHead,service->ServiceType, ReqEncoding, ReqCompression, ReqResolution,UPNPCP_SOAP_BodyTail);
	
	UPnPAddRef(service->Parent);
	ILibParseUri(service->ControlURL,&IP,&Port,&Path);
	
	headerBuffer = (char*)MALLOC(178 + (int)strlen(Path) + (int)strlen(IP) + (int)strlen(service->ServiceType));
	headerLength = sprintf(headerBuffer,UPNPCP_SOAP_Header,Path,IP,Port,service->ServiceType,"GetImagePresentationURL",bufferLength);
	
	memset((char *)&addr, 0, sizeof(addr));
	addr.sin_family = AF_INET;
	addr.sin_addr.s_addr = inet_addr(IP);
	addr.sin_port = htons(Port);
	
	invoke_data->CallbackPtr = CallbackPtr;
	invoke_data->User = user;
	ILibAddRequest_DirectEx(((struct UPnPCP*)service->Parent->CP)->HTTP, headerBuffer,headerLength,buffer,bufferLength,&addr, &UPnPInvoke_DigitalSecurityCameraStillImage_GetImagePresentationURL_Sink,service, invoke_data);	
	
	FREE(IP);
	FREE(Path);
}
void UPnPInvoke_DigitalSecurityCameraStillImage_GetAvailableResolutions_Sink(void *reader, struct packetheader *header, int IsInterrupt,char* buffer, int *p_BeginPointer, int EndPointer, int done, void* _service, void *state)
{
	struct UPnPService *Service = (struct UPnPService*)_service;
	struct InvokeStruct *_InvokeData = (struct InvokeStruct*)state;
	int ArgLeft = 1;
	struct ILibXMLNode *xml;
	struct ILibXMLNode *__xml;
	char *tempBuffer;
	int tempBufferLength;
	char* RetAvailableResolutions = NULL;
	
	if(done==0){return;}
	if(_InvokeData->CallbackPtr==NULL)
	{
		UPnPRelease(Service->Parent);
		FREE(_InvokeData);
		return;
	}
	else
	{
		if(header==NULL)
		{
			/* Connection Failed */
			((void (*)(struct UPnPService*,int,void*,char*))_InvokeData->CallbackPtr)(Service,IsInterrupt==0?-1:IsInterrupt,_InvokeData->User,INVALID_DATA);
			UPnPRelease(Service->Parent);
			FREE(_InvokeData);
			return;
		}
		else if(header->StatusCode!=200)
		{
			/* SOAP Fault */
			((void (*)(struct UPnPService*,int,void*,char*))_InvokeData->CallbackPtr)(Service,UPnPGetErrorCode(buffer,EndPointer-(*p_BeginPointer)),_InvokeData->User,INVALID_DATA);
			UPnPRelease(Service->Parent);
			FREE(_InvokeData);
			return;
		}
	}
	
	__xml = xml = ILibParseXML(buffer,0,EndPointer-(*p_BeginPointer));
	ILibProcessXMLNodeList(xml);
	while(xml!=NULL)
	{
		if(xml->NameLength==31 && memcmp(xml->Name,"GetAvailableResolutionsResponse",31)==0)
		{
			xml = xml->Next;
			while(xml!=NULL)
			{
				if(xml->NameLength==23 && memcmp(xml->Name,"RetAvailableResolutions",23) == 0)
				{
					tempBufferLength = ILibReadInnerXML(xml,&tempBuffer);
					--ArgLeft;
					if(tempBufferLength!=0)
					{
						tempBuffer[tempBufferLength] = '\0';
						RetAvailableResolutions = tempBuffer;
					}
				}
				xml = xml->Peer;
			}
		}
		if(xml!=NULL) {xml = xml->Next;}
	}
	ILibDestructXMLNodeList(__xml);
	
	if(ArgLeft!=0)
	{
		((void (*)(struct UPnPService*,int,void*,char*))_InvokeData->CallbackPtr)(Service,-2,_InvokeData->User,INVALID_DATA);
	}
	else
	{
		((void (*)(struct UPnPService*,int,void*,char*))_InvokeData->CallbackPtr)(Service,0,_InvokeData->User,RetAvailableResolutions);
	}
	UPnPRelease(Service->Parent);
	FREE(_InvokeData);
}
void UPnPInvoke_DigitalSecurityCameraStillImage_GetAvailableResolutions(struct UPnPService *service,void (*CallbackPtr)(struct UPnPService*,int,void*,char*), void* user)
{
	int headerLength;
	char *headerBuffer;
	char *SoapBodyTemplate;
	char* buffer;
	int bufferLength;
	char* IP;
	unsigned short Port;
	char* Path;
	struct sockaddr_in addr;
	struct InvokeStruct *invoke_data = (struct InvokeStruct*)MALLOC(sizeof(struct InvokeStruct));
	
	if(service==NULL)
	{
		FREE(invoke_data);
		return;
	}
	buffer = (char*)MALLOC((int)strlen(service->ServiceType)+278);
	SoapBodyTemplate = "%sGetAvailableResolutions xmlns:u=\"%s\"></u:GetAvailableResolutions%s";
	bufferLength = sprintf(buffer,SoapBodyTemplate,UPNPCP_SOAP_BodyHead,service->ServiceType,UPNPCP_SOAP_BodyTail);
	
	UPnPAddRef(service->Parent);
	ILibParseUri(service->ControlURL,&IP,&Port,&Path);
	
	headerBuffer = (char*)MALLOC(178 + (int)strlen(Path) + (int)strlen(IP) + (int)strlen(service->ServiceType));
	headerLength = sprintf(headerBuffer,UPNPCP_SOAP_Header,Path,IP,Port,service->ServiceType,"GetAvailableResolutions",bufferLength);
	
	memset((char *)&addr, 0, sizeof(addr));
	addr.sin_family = AF_INET;
	addr.sin_addr.s_addr = inet_addr(IP);
	addr.sin_port = htons(Port);
	
	invoke_data->CallbackPtr = CallbackPtr;
	invoke_data->User = user;
	ILibAddRequest_DirectEx(((struct UPnPCP*)service->Parent->CP)->HTTP, headerBuffer,headerLength,buffer,bufferLength,&addr, &UPnPInvoke_DigitalSecurityCameraStillImage_GetAvailableResolutions_Sink,service, invoke_data);	
	
	FREE(IP);
	FREE(Path);
}
void UPnPInvoke_DigitalSecurityCameraStillImage_GetImageURL_Sink(void *reader, struct packetheader *header, int IsInterrupt,char* buffer, int *p_BeginPointer, int EndPointer, int done, void* _service, void *state)
{
	struct UPnPService *Service = (struct UPnPService*)_service;
	struct InvokeStruct *_InvokeData = (struct InvokeStruct*)state;
	int ArgLeft = 1;
	struct ILibXMLNode *xml;
	struct ILibXMLNode *__xml;
	char *tempBuffer;
	int tempBufferLength;
	char* RetImageURL = NULL;
	
	if(done==0){return;}
	if(_InvokeData->CallbackPtr==NULL)
	{
		UPnPRelease(Service->Parent);
		FREE(_InvokeData);
		return;
	}
	else
	{
		if(header==NULL)
		{
			/* Connection Failed */
			((void (*)(struct UPnPService*,int,void*,char*))_InvokeData->CallbackPtr)(Service,IsInterrupt==0?-1:IsInterrupt,_InvokeData->User,INVALID_DATA);
			UPnPRelease(Service->Parent);
			FREE(_InvokeData);
			return;
		}
		else if(header->StatusCode!=200)
		{
			/* SOAP Fault */
			((void (*)(struct UPnPService*,int,void*,char*))_InvokeData->CallbackPtr)(Service,UPnPGetErrorCode(buffer,EndPointer-(*p_BeginPointer)),_InvokeData->User,INVALID_DATA);
			UPnPRelease(Service->Parent);
			FREE(_InvokeData);
			return;
		}
	}
	
	__xml = xml = ILibParseXML(buffer,0,EndPointer-(*p_BeginPointer));
	ILibProcessXMLNodeList(xml);
	while(xml!=NULL)
	{
		if(xml->NameLength==19 && memcmp(xml->Name,"GetImageURLResponse",19)==0)
		{
			xml = xml->Next;
			while(xml!=NULL)
			{
				if(xml->NameLength==11 && memcmp(xml->Name,"RetImageURL",11) == 0)
				{
					tempBufferLength = ILibReadInnerXML(xml,&tempBuffer);
					--ArgLeft;
					if(tempBufferLength!=0)
					{
						tempBuffer[tempBufferLength] = '\0';
						RetImageURL = tempBuffer;
					}
				}
				xml = xml->Peer;
			}
		}
		if(xml!=NULL) {xml = xml->Next;}
	}
	ILibDestructXMLNodeList(__xml);
	
	if(ArgLeft!=0)
	{
		((void (*)(struct UPnPService*,int,void*,char*))_InvokeData->CallbackPtr)(Service,-2,_InvokeData->User,INVALID_DATA);
	}
	else
	{
		((void (*)(struct UPnPService*,int,void*,char*))_InvokeData->CallbackPtr)(Service,0,_InvokeData->User,RetImageURL);
	}
	UPnPRelease(Service->Parent);
	FREE(_InvokeData);
}
void UPnPInvoke_DigitalSecurityCameraStillImage_GetImageURL(struct UPnPService *service,void (*CallbackPtr)(struct UPnPService*,int,void*,char*), void* user, char* ReqEncoding, char* ReqCompression, char* ReqResolution)
{
	int headerLength;
	char *headerBuffer;
	char *SoapBodyTemplate;
	char* buffer;
	int bufferLength;
	char* IP;
	unsigned short Port;
	char* Path;
	struct sockaddr_in addr;
	struct InvokeStruct *invoke_data = (struct InvokeStruct*)MALLOC(sizeof(struct InvokeStruct));
	
	if(service==NULL)
	{
		FREE(invoke_data);
		return;
	}
	buffer = (char*)MALLOC((int)strlen(service->ServiceType)+(int)strlen(ReqEncoding)+(int)strlen(ReqCompression)+(int)strlen(ReqResolution)+345);
	SoapBodyTemplate = "%sGetImageURL xmlns:u=\"%s\"><ReqEncoding>%s</ReqEncoding><ReqCompression>%s</ReqCompression><ReqResolution>%s</ReqResolution></u:GetImageURL%s";
	bufferLength = sprintf(buffer,SoapBodyTemplate,UPNPCP_SOAP_BodyHead,service->ServiceType, ReqEncoding, ReqCompression, ReqResolution,UPNPCP_SOAP_BodyTail);
	
	UPnPAddRef(service->Parent);
	ILibParseUri(service->ControlURL,&IP,&Port,&Path);
	
	headerBuffer = (char*)MALLOC(166 + (int)strlen(Path) + (int)strlen(IP) + (int)strlen(service->ServiceType));
	headerLength = sprintf(headerBuffer,UPNPCP_SOAP_Header,Path,IP,Port,service->ServiceType,"GetImageURL",bufferLength);
	
	memset((char *)&addr, 0, sizeof(addr));
	addr.sin_family = AF_INET;
	addr.sin_addr.s_addr = inet_addr(IP);
	addr.sin_port = htons(Port);
	
	invoke_data->CallbackPtr = CallbackPtr;
	invoke_data->User = user;
	ILibAddRequest_DirectEx(((struct UPnPCP*)service->Parent->CP)->HTTP, headerBuffer,headerLength,buffer,bufferLength,&addr, &UPnPInvoke_DigitalSecurityCameraStillImage_GetImageURL_Sink,service, invoke_data);	
	
	FREE(IP);
	FREE(Path);
}
void UPnPInvoke_DigitalSecurityCameraStillImage_GetDefaultImageURL_Sink(void *reader, struct packetheader *header, int IsInterrupt,char* buffer, int *p_BeginPointer, int EndPointer, int done, void* _service, void *state)
{
	struct UPnPService *Service = (struct UPnPService*)_service;
	struct InvokeStruct *_InvokeData = (struct InvokeStruct*)state;
	int ArgLeft = 1;
	struct ILibXMLNode *xml;
	struct ILibXMLNode *__xml;
	char *tempBuffer;
	int tempBufferLength;
	char* RetImageURL = NULL;
	
	if(done==0){return;}
	if(_InvokeData->CallbackPtr==NULL)
	{
		UPnPRelease(Service->Parent);
		FREE(_InvokeData);
		return;
	}
	else
	{
		if(header==NULL)
		{
			/* Connection Failed */
			((void (*)(struct UPnPService*,int,void*,char*))_InvokeData->CallbackPtr)(Service,IsInterrupt==0?-1:IsInterrupt,_InvokeData->User,INVALID_DATA);
			UPnPRelease(Service->Parent);
			FREE(_InvokeData);
			return;
		}
		else if(header->StatusCode!=200)
		{
			/* SOAP Fault */
			((void (*)(struct UPnPService*,int,void*,char*))_InvokeData->CallbackPtr)(Service,UPnPGetErrorCode(buffer,EndPointer-(*p_BeginPointer)),_InvokeData->User,INVALID_DATA);
			UPnPRelease(Service->Parent);
			FREE(_InvokeData);
			return;
		}
	}
	
	__xml = xml = ILibParseXML(buffer,0,EndPointer-(*p_BeginPointer));
	ILibProcessXMLNodeList(xml);
	while(xml!=NULL)
	{
		if(xml->NameLength==26 && memcmp(xml->Name,"GetDefaultImageURLResponse",26)==0)
		{
			xml = xml->Next;
			while(xml!=NULL)
			{
				if(xml->NameLength==11 && memcmp(xml->Name,"RetImageURL",11) == 0)
				{
					tempBufferLength = ILibReadInnerXML(xml,&tempBuffer);
					--ArgLeft;
					if(tempBufferLength!=0)
					{
						tempBuffer[tempBufferLength] = '\0';
						RetImageURL = tempBuffer;
					}
				}
				xml = xml->Peer;
			}
		}
		if(xml!=NULL) {xml = xml->Next;}
	}
	ILibDestructXMLNodeList(__xml);
	
	if(ArgLeft!=0)
	{
		((void (*)(struct UPnPService*,int,void*,char*))_InvokeData->CallbackPtr)(Service,-2,_InvokeData->User,INVALID_DATA);
	}
	else
	{
		((void (*)(struct UPnPService*,int,void*,char*))_InvokeData->CallbackPtr)(Service,0,_InvokeData->User,RetImageURL);
	}
	UPnPRelease(Service->Parent);
	FREE(_InvokeData);
}
void UPnPInvoke_DigitalSecurityCameraStillImage_GetDefaultImageURL(struct UPnPService *service,void (*CallbackPtr)(struct UPnPService*,int,void*,char*), void* user)
{
	int headerLength;
	char *headerBuffer;
	char *SoapBodyTemplate;
	char* buffer;
	int bufferLength;
	char* IP;
	unsigned short Port;
	char* Path;
	struct sockaddr_in addr;
	struct InvokeStruct *invoke_data = (struct InvokeStruct*)MALLOC(sizeof(struct InvokeStruct));
	
	if(service==NULL)
	{
		FREE(invoke_data);
		return;
	}
	buffer = (char*)MALLOC((int)strlen(service->ServiceType)+268);
	SoapBodyTemplate = "%sGetDefaultImageURL xmlns:u=\"%s\"></u:GetDefaultImageURL%s";
	bufferLength = sprintf(buffer,SoapBodyTemplate,UPNPCP_SOAP_BodyHead,service->ServiceType,UPNPCP_SOAP_BodyTail);
	
	UPnPAddRef(service->Parent);
	ILibParseUri(service->ControlURL,&IP,&Port,&Path);
	
	headerBuffer = (char*)MALLOC(173 + (int)strlen(Path) + (int)strlen(IP) + (int)strlen(service->ServiceType));
	headerLength = sprintf(headerBuffer,UPNPCP_SOAP_Header,Path,IP,Port,service->ServiceType,"GetDefaultImageURL",bufferLength);
	
	memset((char *)&addr, 0, sizeof(addr));
	addr.sin_family = AF_INET;
	addr.sin_addr.s_addr = inet_addr(IP);
	addr.sin_port = htons(Port);
	
	invoke_data->CallbackPtr = CallbackPtr;
	invoke_data->User = user;
	ILibAddRequest_DirectEx(((struct UPnPCP*)service->Parent->CP)->HTTP, headerBuffer,headerLength,buffer,bufferLength,&addr, &UPnPInvoke_DigitalSecurityCameraStillImage_GetDefaultImageURL_Sink,service, invoke_data);	
	
	FREE(IP);
	FREE(Path);
}
void UPnPInvoke_DigitalSecurityCameraStillImage_SetDefaultEncoding_Sink(void *reader, struct packetheader *header, int IsInterrupt,char* buffer, int *p_BeginPointer, int EndPointer, int done, void* _service, void *state)
{
	struct UPnPService *Service = (struct UPnPService*)_service;
	struct InvokeStruct *_InvokeData = (struct InvokeStruct*)state;
	if(done==0){return;}
	if(_InvokeData->CallbackPtr==NULL)
	{
		UPnPRelease(Service->Parent);
		FREE(_InvokeData);
		return;
	}
	else
	{
		if(header==NULL)
		{
			/* Connection Failed */
			((void (*)(struct UPnPService*,int,void*))_InvokeData->CallbackPtr)(Service,-1,_InvokeData->User);
			UPnPRelease(Service->Parent);
			FREE(_InvokeData);
			return;
		}
		else if(header->StatusCode!=200)
		{
			/* SOAP Fault */
			((void (*)(struct UPnPService*,int,void*))_InvokeData->CallbackPtr)(Service,UPnPGetErrorCode(buffer,EndPointer-(*p_BeginPointer)),_InvokeData->User);
			UPnPRelease(Service->Parent);
			FREE(_InvokeData);
			return;
		}
	}
	
	((void (*)(struct UPnPService*,int,void*))_InvokeData->CallbackPtr)(Service,0,_InvokeData->User);
	UPnPRelease(Service->Parent);
	FREE(_InvokeData);
}
void UPnPInvoke_DigitalSecurityCameraStillImage_SetDefaultEncoding(struct UPnPService *service,void (*CallbackPtr)(struct UPnPService*,int,void*), void* user, char* ReqEncoding)
{
	int headerLength;
	char *headerBuffer;
	char *SoapBodyTemplate;
	char* buffer;
	int bufferLength;
	char* IP;
	unsigned short Port;
	char* Path;
	struct sockaddr_in addr;
	struct InvokeStruct *invoke_data = (struct InvokeStruct*)MALLOC(sizeof(struct InvokeStruct));
	
	if(service==NULL)
	{
		FREE(invoke_data);
		return;
	}
	buffer = (char*)MALLOC((int)strlen(service->ServiceType)+(int)strlen(ReqEncoding)+295);
	SoapBodyTemplate = "%sSetDefaultEncoding xmlns:u=\"%s\"><ReqEncoding>%s</ReqEncoding></u:SetDefaultEncoding%s";
	bufferLength = sprintf(buffer,SoapBodyTemplate,UPNPCP_SOAP_BodyHead,service->ServiceType, ReqEncoding,UPNPCP_SOAP_BodyTail);
	
	UPnPAddRef(service->Parent);
	ILibParseUri(service->ControlURL,&IP,&Port,&Path);
	
	headerBuffer = (char*)MALLOC(173 + (int)strlen(Path) + (int)strlen(IP) + (int)strlen(service->ServiceType));
	headerLength = sprintf(headerBuffer,UPNPCP_SOAP_Header,Path,IP,Port,service->ServiceType,"SetDefaultEncoding",bufferLength);
	
	memset((char *)&addr, 0, sizeof(addr));
	addr.sin_family = AF_INET;
	addr.sin_addr.s_addr = inet_addr(IP);
	addr.sin_port = htons(Port);
	
	invoke_data->CallbackPtr = CallbackPtr;
	invoke_data->User = user;
	ILibAddRequest_DirectEx(((struct UPnPCP*)service->Parent->CP)->HTTP, headerBuffer,headerLength,buffer,bufferLength,&addr, &UPnPInvoke_DigitalSecurityCameraStillImage_SetDefaultEncoding_Sink,service, invoke_data);	
	
	FREE(IP);
	FREE(Path);
}
void UPnPInvoke_DigitalSecurityCameraStillImage_GetAvailableCompressionLevels_Sink(void *reader, struct packetheader *header, int IsInterrupt,char* buffer, int *p_BeginPointer, int EndPointer, int done, void* _service, void *state)
{
	struct UPnPService *Service = (struct UPnPService*)_service;
	struct InvokeStruct *_InvokeData = (struct InvokeStruct*)state;
	int ArgLeft = 1;
	struct ILibXMLNode *xml;
	struct ILibXMLNode *__xml;
	char *tempBuffer;
	int tempBufferLength;
	char* RetAvailableCompressionLevels = NULL;
	
	if(done==0){return;}
	if(_InvokeData->CallbackPtr==NULL)
	{
		UPnPRelease(Service->Parent);
		FREE(_InvokeData);
		return;
	}
	else
	{
		if(header==NULL)
		{
			/* Connection Failed */
			((void (*)(struct UPnPService*,int,void*,char*))_InvokeData->CallbackPtr)(Service,IsInterrupt==0?-1:IsInterrupt,_InvokeData->User,INVALID_DATA);
			UPnPRelease(Service->Parent);
			FREE(_InvokeData);
			return;
		}
		else if(header->StatusCode!=200)
		{
			/* SOAP Fault */
			((void (*)(struct UPnPService*,int,void*,char*))_InvokeData->CallbackPtr)(Service,UPnPGetErrorCode(buffer,EndPointer-(*p_BeginPointer)),_InvokeData->User,INVALID_DATA);
			UPnPRelease(Service->Parent);
			FREE(_InvokeData);
			return;
		}
	}
	
	__xml = xml = ILibParseXML(buffer,0,EndPointer-(*p_BeginPointer));
	ILibProcessXMLNodeList(xml);
	while(xml!=NULL)
	{
		if(xml->NameLength==37 && memcmp(xml->Name,"GetAvailableCompressionLevelsResponse",37)==0)
		{
			xml = xml->Next;
			while(xml!=NULL)
			{
				if(xml->NameLength==29 && memcmp(xml->Name,"RetAvailableCompressionLevels",29) == 0)
				{
					tempBufferLength = ILibReadInnerXML(xml,&tempBuffer);
					--ArgLeft;
					if(tempBufferLength!=0)
					{
						tempBuffer[tempBufferLength] = '\0';
						RetAvailableCompressionLevels = tempBuffer;
					}
				}
				xml = xml->Peer;
			}
		}
		if(xml!=NULL) {xml = xml->Next;}
	}
	ILibDestructXMLNodeList(__xml);
	
	if(ArgLeft!=0)
	{
		((void (*)(struct UPnPService*,int,void*,char*))_InvokeData->CallbackPtr)(Service,-2,_InvokeData->User,INVALID_DATA);
	}
	else
	{
		((void (*)(struct UPnPService*,int,void*,char*))_InvokeData->CallbackPtr)(Service,0,_InvokeData->User,RetAvailableCompressionLevels);
	}
	UPnPRelease(Service->Parent);
	FREE(_InvokeData);
}
void UPnPInvoke_DigitalSecurityCameraStillImage_GetAvailableCompressionLevels(struct UPnPService *service,void (*CallbackPtr)(struct UPnPService*,int,void*,char*), void* user)
{
	int headerLength;
	char *headerBuffer;
	char *SoapBodyTemplate;
	char* buffer;
	int bufferLength;
	char* IP;
	unsigned short Port;
	char* Path;
	struct sockaddr_in addr;
	struct InvokeStruct *invoke_data = (struct InvokeStruct*)MALLOC(sizeof(struct InvokeStruct));
	
	if(service==NULL)
	{
		FREE(invoke_data);
		return;
	}
	buffer = (char*)MALLOC((int)strlen(service->ServiceType)+290);
	SoapBodyTemplate = "%sGetAvailableCompressionLevels xmlns:u=\"%s\"></u:GetAvailableCompressionLevels%s";
	bufferLength = sprintf(buffer,SoapBodyTemplate,UPNPCP_SOAP_BodyHead,service->ServiceType,UPNPCP_SOAP_BodyTail);
	
	UPnPAddRef(service->Parent);
	ILibParseUri(service->ControlURL,&IP,&Port,&Path);
	
	headerBuffer = (char*)MALLOC(184 + (int)strlen(Path) + (int)strlen(IP) + (int)strlen(service->ServiceType));
	headerLength = sprintf(headerBuffer,UPNPCP_SOAP_Header,Path,IP,Port,service->ServiceType,"GetAvailableCompressionLevels",bufferLength);
	
	memset((char *)&addr, 0, sizeof(addr));
	addr.sin_family = AF_INET;
	addr.sin_addr.s_addr = inet_addr(IP);
	addr.sin_port = htons(Port);
	
	invoke_data->CallbackPtr = CallbackPtr;
	invoke_data->User = user;
	ILibAddRequest_DirectEx(((struct UPnPCP*)service->Parent->CP)->HTTP, headerBuffer,headerLength,buffer,bufferLength,&addr, &UPnPInvoke_DigitalSecurityCameraStillImage_GetAvailableCompressionLevels_Sink,service, invoke_data);	
	
	FREE(IP);
	FREE(Path);
}
void UPnPInvoke_DigitalSecurityCameraStillImage_GetDefaultEncoding_Sink(void *reader, struct packetheader *header, int IsInterrupt,char* buffer, int *p_BeginPointer, int EndPointer, int done, void* _service, void *state)
{
	struct UPnPService *Service = (struct UPnPService*)_service;
	struct InvokeStruct *_InvokeData = (struct InvokeStruct*)state;
	int ArgLeft = 1;
	struct ILibXMLNode *xml;
	struct ILibXMLNode *__xml;
	char *tempBuffer;
	int tempBufferLength;
	char* RetEncoding = NULL;
	
	if(done==0){return;}
	if(_InvokeData->CallbackPtr==NULL)
	{
		UPnPRelease(Service->Parent);
		FREE(_InvokeData);
		return;
	}
	else
	{
		if(header==NULL)
		{
			/* Connection Failed */
			((void (*)(struct UPnPService*,int,void*,char*))_InvokeData->CallbackPtr)(Service,IsInterrupt==0?-1:IsInterrupt,_InvokeData->User,INVALID_DATA);
			UPnPRelease(Service->Parent);
			FREE(_InvokeData);
			return;
		}
		else if(header->StatusCode!=200)
		{
			/* SOAP Fault */
			((void (*)(struct UPnPService*,int,void*,char*))_InvokeData->CallbackPtr)(Service,UPnPGetErrorCode(buffer,EndPointer-(*p_BeginPointer)),_InvokeData->User,INVALID_DATA);
			UPnPRelease(Service->Parent);
			FREE(_InvokeData);
			return;
		}
	}
	
	__xml = xml = ILibParseXML(buffer,0,EndPointer-(*p_BeginPointer));
	ILibProcessXMLNodeList(xml);
	while(xml!=NULL)
	{
		if(xml->NameLength==26 && memcmp(xml->Name,"GetDefaultEncodingResponse",26)==0)
		{
			xml = xml->Next;
			while(xml!=NULL)
			{
				if(xml->NameLength==11 && memcmp(xml->Name,"RetEncoding",11) == 0)
				{
					tempBufferLength = ILibReadInnerXML(xml,&tempBuffer);
					--ArgLeft;
					if(tempBufferLength!=0)
					{
						tempBuffer[tempBufferLength] = '\0';
						RetEncoding = tempBuffer;
					}
				}
				xml = xml->Peer;
			}
		}
		if(xml!=NULL) {xml = xml->Next;}
	}
	ILibDestructXMLNodeList(__xml);
	
	if(ArgLeft!=0)
	{
		((void (*)(struct UPnPService*,int,void*,char*))_InvokeData->CallbackPtr)(Service,-2,_InvokeData->User,INVALID_DATA);
	}
	else
	{
		((void (*)(struct UPnPService*,int,void*,char*))_InvokeData->CallbackPtr)(Service,0,_InvokeData->User,RetEncoding);
	}
	UPnPRelease(Service->Parent);
	FREE(_InvokeData);
}
void UPnPInvoke_DigitalSecurityCameraStillImage_GetDefaultEncoding(struct UPnPService *service,void (*CallbackPtr)(struct UPnPService*,int,void*,char*), void* user)
{
	int headerLength;
	char *headerBuffer;
	char *SoapBodyTemplate;
	char* buffer;
	int bufferLength;
	char* IP;
	unsigned short Port;
	char* Path;
	struct sockaddr_in addr;
	struct InvokeStruct *invoke_data = (struct InvokeStruct*)MALLOC(sizeof(struct InvokeStruct));
	
	if(service==NULL)
	{
		FREE(invoke_data);
		return;
	}
	buffer = (char*)MALLOC((int)strlen(service->ServiceType)+268);
	SoapBodyTemplate = "%sGetDefaultEncoding xmlns:u=\"%s\"></u:GetDefaultEncoding%s";
	bufferLength = sprintf(buffer,SoapBodyTemplate,UPNPCP_SOAP_BodyHead,service->ServiceType,UPNPCP_SOAP_BodyTail);
	
	UPnPAddRef(service->Parent);
	ILibParseUri(service->ControlURL,&IP,&Port,&Path);
	
	headerBuffer = (char*)MALLOC(173 + (int)strlen(Path) + (int)strlen(IP) + (int)strlen(service->ServiceType));
	headerLength = sprintf(headerBuffer,UPNPCP_SOAP_Header,Path,IP,Port,service->ServiceType,"GetDefaultEncoding",bufferLength);
	
	memset((char *)&addr, 0, sizeof(addr));
	addr.sin_family = AF_INET;
	addr.sin_addr.s_addr = inet_addr(IP);
	addr.sin_port = htons(Port);
	
	invoke_data->CallbackPtr = CallbackPtr;
	invoke_data->User = user;
	ILibAddRequest_DirectEx(((struct UPnPCP*)service->Parent->CP)->HTTP, headerBuffer,headerLength,buffer,bufferLength,&addr, &UPnPInvoke_DigitalSecurityCameraStillImage_GetDefaultEncoding_Sink,service, invoke_data);	
	
	FREE(IP);
	FREE(Path);
}
void UPnPInvoke_DigitalSecurityCameraStillImage_SetDefaultResolution_Sink(void *reader, struct packetheader *header, int IsInterrupt,char* buffer, int *p_BeginPointer, int EndPointer, int done, void* _service, void *state)
{
	struct UPnPService *Service = (struct UPnPService*)_service;
	struct InvokeStruct *_InvokeData = (struct InvokeStruct*)state;
	if(done==0){return;}
	if(_InvokeData->CallbackPtr==NULL)
	{
		UPnPRelease(Service->Parent);
		FREE(_InvokeData);
		return;
	}
	else
	{
		if(header==NULL)
		{
			/* Connection Failed */
			((void (*)(struct UPnPService*,int,void*))_InvokeData->CallbackPtr)(Service,-1,_InvokeData->User);
			UPnPRelease(Service->Parent);
			FREE(_InvokeData);
			return;
		}
		else if(header->StatusCode!=200)
		{
			/* SOAP Fault */
			((void (*)(struct UPnPService*,int,void*))_InvokeData->CallbackPtr)(Service,UPnPGetErrorCode(buffer,EndPointer-(*p_BeginPointer)),_InvokeData->User);
			UPnPRelease(Service->Parent);
			FREE(_InvokeData);
			return;
		}
	}
	
	((void (*)(struct UPnPService*,int,void*))_InvokeData->CallbackPtr)(Service,0,_InvokeData->User);
	UPnPRelease(Service->Parent);
	FREE(_InvokeData);
}
void UPnPInvoke_DigitalSecurityCameraStillImage_SetDefaultResolution(struct UPnPService *service,void (*CallbackPtr)(struct UPnPService*,int,void*), void* user, char* ReqResolution)
{
	int headerLength;
	char *headerBuffer;
	char *SoapBodyTemplate;
	char* buffer;
	int bufferLength;
	char* IP;
	unsigned short Port;
	char* Path;
	struct sockaddr_in addr;
	struct InvokeStruct *invoke_data = (struct InvokeStruct*)MALLOC(sizeof(struct InvokeStruct));
	
	if(service==NULL)
	{
		FREE(invoke_data);
		return;
	}
	buffer = (char*)MALLOC((int)strlen(service->ServiceType)+(int)strlen(ReqResolution)+303);
	SoapBodyTemplate = "%sSetDefaultResolution xmlns:u=\"%s\"><ReqResolution>%s</ReqResolution></u:SetDefaultResolution%s";
	bufferLength = sprintf(buffer,SoapBodyTemplate,UPNPCP_SOAP_BodyHead,service->ServiceType, ReqResolution,UPNPCP_SOAP_BodyTail);
	
	UPnPAddRef(service->Parent);
	ILibParseUri(service->ControlURL,&IP,&Port,&Path);
	
	headerBuffer = (char*)MALLOC(175 + (int)strlen(Path) + (int)strlen(IP) + (int)strlen(service->ServiceType));
	headerLength = sprintf(headerBuffer,UPNPCP_SOAP_Header,Path,IP,Port,service->ServiceType,"SetDefaultResolution",bufferLength);
	
	memset((char *)&addr, 0, sizeof(addr));
	addr.sin_family = AF_INET;
	addr.sin_addr.s_addr = inet_addr(IP);
	addr.sin_port = htons(Port);
	
	invoke_data->CallbackPtr = CallbackPtr;
	invoke_data->User = user;
	ILibAddRequest_DirectEx(((struct UPnPCP*)service->Parent->CP)->HTTP, headerBuffer,headerLength,buffer,bufferLength,&addr, &UPnPInvoke_DigitalSecurityCameraStillImage_SetDefaultResolution_Sink,service, invoke_data);	
	
	FREE(IP);
	FREE(Path);
}
void UPnPInvoke_DigitalSecurityCameraStillImage_GetDefaultCompressionLevel_Sink(void *reader, struct packetheader *header, int IsInterrupt,char* buffer, int *p_BeginPointer, int EndPointer, int done, void* _service, void *state)
{
	struct UPnPService *Service = (struct UPnPService*)_service;
	struct InvokeStruct *_InvokeData = (struct InvokeStruct*)state;
	int ArgLeft = 1;
	struct ILibXMLNode *xml;
	struct ILibXMLNode *__xml;
	char *tempBuffer;
	int tempBufferLength;
	char* RetCompressionLevel = NULL;
	
	if(done==0){return;}
	if(_InvokeData->CallbackPtr==NULL)
	{
		UPnPRelease(Service->Parent);
		FREE(_InvokeData);
		return;
	}
	else
	{
		if(header==NULL)
		{
			/* Connection Failed */
			((void (*)(struct UPnPService*,int,void*,char*))_InvokeData->CallbackPtr)(Service,IsInterrupt==0?-1:IsInterrupt,_InvokeData->User,INVALID_DATA);
			UPnPRelease(Service->Parent);
			FREE(_InvokeData);
			return;
		}
		else if(header->StatusCode!=200)
		{
			/* SOAP Fault */
			((void (*)(struct UPnPService*,int,void*,char*))_InvokeData->CallbackPtr)(Service,UPnPGetErrorCode(buffer,EndPointer-(*p_BeginPointer)),_InvokeData->User,INVALID_DATA);
			UPnPRelease(Service->Parent);
			FREE(_InvokeData);
			return;
		}
	}
	
	__xml = xml = ILibParseXML(buffer,0,EndPointer-(*p_BeginPointer));
	ILibProcessXMLNodeList(xml);
	while(xml!=NULL)
	{
		if(xml->NameLength==34 && memcmp(xml->Name,"GetDefaultCompressionLevelResponse",34)==0)
		{
			xml = xml->Next;
			while(xml!=NULL)
			{
				if(xml->NameLength==19 && memcmp(xml->Name,"RetCompressionLevel",19) == 0)
				{
					tempBufferLength = ILibReadInnerXML(xml,&tempBuffer);
					--ArgLeft;
					if(tempBufferLength!=0)
					{
						tempBuffer[tempBufferLength] = '\0';
						RetCompressionLevel = tempBuffer;
					}
				}
				xml = xml->Peer;
			}
		}
		if(xml!=NULL) {xml = xml->Next;}
	}
	ILibDestructXMLNodeList(__xml);
	
	if(ArgLeft!=0)
	{
		((void (*)(struct UPnPService*,int,void*,char*))_InvokeData->CallbackPtr)(Service,-2,_InvokeData->User,INVALID_DATA);
	}
	else
	{
		((void (*)(struct UPnPService*,int,void*,char*))_InvokeData->CallbackPtr)(Service,0,_InvokeData->User,RetCompressionLevel);
	}
	UPnPRelease(Service->Parent);
	FREE(_InvokeData);
}
void UPnPInvoke_DigitalSecurityCameraStillImage_GetDefaultCompressionLevel(struct UPnPService *service,void (*CallbackPtr)(struct UPnPService*,int,void*,char*), void* user)
{
	int headerLength;
	char *headerBuffer;
	char *SoapBodyTemplate;
	char* buffer;
	int bufferLength;
	char* IP;
	unsigned short Port;
	char* Path;
	struct sockaddr_in addr;
	struct InvokeStruct *invoke_data = (struct InvokeStruct*)MALLOC(sizeof(struct InvokeStruct));
	
	if(service==NULL)
	{
		FREE(invoke_data);
		return;
	}
	buffer = (char*)MALLOC((int)strlen(service->ServiceType)+284);
	SoapBodyTemplate = "%sGetDefaultCompressionLevel xmlns:u=\"%s\"></u:GetDefaultCompressionLevel%s";
	bufferLength = sprintf(buffer,SoapBodyTemplate,UPNPCP_SOAP_BodyHead,service->ServiceType,UPNPCP_SOAP_BodyTail);
	
	UPnPAddRef(service->Parent);
	ILibParseUri(service->ControlURL,&IP,&Port,&Path);
	
	headerBuffer = (char*)MALLOC(181 + (int)strlen(Path) + (int)strlen(IP) + (int)strlen(service->ServiceType));
	headerLength = sprintf(headerBuffer,UPNPCP_SOAP_Header,Path,IP,Port,service->ServiceType,"GetDefaultCompressionLevel",bufferLength);
	
	memset((char *)&addr, 0, sizeof(addr));
	addr.sin_family = AF_INET;
	addr.sin_addr.s_addr = inet_addr(IP);
	addr.sin_port = htons(Port);
	
	invoke_data->CallbackPtr = CallbackPtr;
	invoke_data->User = user;
	ILibAddRequest_DirectEx(((struct UPnPCP*)service->Parent->CP)->HTTP, headerBuffer,headerLength,buffer,bufferLength,&addr, &UPnPInvoke_DigitalSecurityCameraStillImage_GetDefaultCompressionLevel_Sink,service, invoke_data);	
	
	FREE(IP);
	FREE(Path);
}
void UPnPInvoke_DigitalSecurityCameraStillImage_GetDefaultImagePresentationURL_Sink(void *reader, struct packetheader *header, int IsInterrupt,char* buffer, int *p_BeginPointer, int EndPointer, int done, void* _service, void *state)
{
	struct UPnPService *Service = (struct UPnPService*)_service;
	struct InvokeStruct *_InvokeData = (struct InvokeStruct*)state;
	int ArgLeft = 1;
	struct ILibXMLNode *xml;
	struct ILibXMLNode *__xml;
	char *tempBuffer;
	int tempBufferLength;
	char* RetImagePresentationURL = NULL;
	
	if(done==0){return;}
	if(_InvokeData->CallbackPtr==NULL)
	{
		UPnPRelease(Service->Parent);
		FREE(_InvokeData);
		return;
	}
	else
	{
		if(header==NULL)
		{
			/* Connection Failed */
			((void (*)(struct UPnPService*,int,void*,char*))_InvokeData->CallbackPtr)(Service,IsInterrupt==0?-1:IsInterrupt,_InvokeData->User,INVALID_DATA);
			UPnPRelease(Service->Parent);
			FREE(_InvokeData);
			return;
		}
		else if(header->StatusCode!=200)
		{
			/* SOAP Fault */
			((void (*)(struct UPnPService*,int,void*,char*))_InvokeData->CallbackPtr)(Service,UPnPGetErrorCode(buffer,EndPointer-(*p_BeginPointer)),_InvokeData->User,INVALID_DATA);
			UPnPRelease(Service->Parent);
			FREE(_InvokeData);
			return;
		}
	}
	
	__xml = xml = ILibParseXML(buffer,0,EndPointer-(*p_BeginPointer));
	ILibProcessXMLNodeList(xml);
	while(xml!=NULL)
	{
		if(xml->NameLength==38 && memcmp(xml->Name,"GetDefaultImagePresentationURLResponse",38)==0)
		{
			xml = xml->Next;
			while(xml!=NULL)
			{
				if(xml->NameLength==23 && memcmp(xml->Name,"RetImagePresentationURL",23) == 0)
				{
					tempBufferLength = ILibReadInnerXML(xml,&tempBuffer);
					--ArgLeft;
					if(tempBufferLength!=0)
					{
						tempBuffer[tempBufferLength] = '\0';
						RetImagePresentationURL = tempBuffer;
					}
				}
				xml = xml->Peer;
			}
		}
		if(xml!=NULL) {xml = xml->Next;}
	}
	ILibDestructXMLNodeList(__xml);
	
	if(ArgLeft!=0)
	{
		((void (*)(struct UPnPService*,int,void*,char*))_InvokeData->CallbackPtr)(Service,-2,_InvokeData->User,INVALID_DATA);
	}
	else
	{
		((void (*)(struct UPnPService*,int,void*,char*))_InvokeData->CallbackPtr)(Service,0,_InvokeData->User,RetImagePresentationURL);
	}
	UPnPRelease(Service->Parent);
	FREE(_InvokeData);
}
void UPnPInvoke_DigitalSecurityCameraStillImage_GetDefaultImagePresentationURL(struct UPnPService *service,void (*CallbackPtr)(struct UPnPService*,int,void*,char*), void* user)
{
	int headerLength;
	char *headerBuffer;
	char *SoapBodyTemplate;
	char* buffer;
	int bufferLength;
	char* IP;
	unsigned short Port;
	char* Path;
	struct sockaddr_in addr;
	struct InvokeStruct *invoke_data = (struct InvokeStruct*)MALLOC(sizeof(struct InvokeStruct));
	
	if(service==NULL)
	{
		FREE(invoke_data);
		return;
	}
	buffer = (char*)MALLOC((int)strlen(service->ServiceType)+292);
	SoapBodyTemplate = "%sGetDefaultImagePresentationURL xmlns:u=\"%s\"></u:GetDefaultImagePresentationURL%s";
	bufferLength = sprintf(buffer,SoapBodyTemplate,UPNPCP_SOAP_BodyHead,service->ServiceType,UPNPCP_SOAP_BodyTail);
	
	UPnPAddRef(service->Parent);
	ILibParseUri(service->ControlURL,&IP,&Port,&Path);
	
	headerBuffer = (char*)MALLOC(185 + (int)strlen(Path) + (int)strlen(IP) + (int)strlen(service->ServiceType));
	headerLength = sprintf(headerBuffer,UPNPCP_SOAP_Header,Path,IP,Port,service->ServiceType,"GetDefaultImagePresentationURL",bufferLength);
	
	memset((char *)&addr, 0, sizeof(addr));
	addr.sin_family = AF_INET;
	addr.sin_addr.s_addr = inet_addr(IP);
	addr.sin_port = htons(Port);
	
	invoke_data->CallbackPtr = CallbackPtr;
	invoke_data->User = user;
	ILibAddRequest_DirectEx(((struct UPnPCP*)service->Parent->CP)->HTTP, headerBuffer,headerLength,buffer,bufferLength,&addr, &UPnPInvoke_DigitalSecurityCameraStillImage_GetDefaultImagePresentationURL_Sink,service, invoke_data);	
	
	FREE(IP);
	FREE(Path);
}
void UPnPInvoke_DigitalSecurityCameraStillImage_GetDefaultResolution_Sink(void *reader, struct packetheader *header, int IsInterrupt,char* buffer, int *p_BeginPointer, int EndPointer, int done, void* _service, void *state)
{
	struct UPnPService *Service = (struct UPnPService*)_service;
	struct InvokeStruct *_InvokeData = (struct InvokeStruct*)state;
	int ArgLeft = 1;
	struct ILibXMLNode *xml;
	struct ILibXMLNode *__xml;
	char *tempBuffer;
	int tempBufferLength;
	char* RetResolution = NULL;
	
	if(done==0){return;}
	if(_InvokeData->CallbackPtr==NULL)
	{
		UPnPRelease(Service->Parent);
		FREE(_InvokeData);
		return;
	}
	else
	{
		if(header==NULL)
		{
			/* Connection Failed */
			((void (*)(struct UPnPService*,int,void*,char*))_InvokeData->CallbackPtr)(Service,IsInterrupt==0?-1:IsInterrupt,_InvokeData->User,INVALID_DATA);
			UPnPRelease(Service->Parent);
			FREE(_InvokeData);
			return;
		}
		else if(header->StatusCode!=200)
		{
			/* SOAP Fault */
			((void (*)(struct UPnPService*,int,void*,char*))_InvokeData->CallbackPtr)(Service,UPnPGetErrorCode(buffer,EndPointer-(*p_BeginPointer)),_InvokeData->User,INVALID_DATA);
			UPnPRelease(Service->Parent);
			FREE(_InvokeData);
			return;
		}
	}
	
	__xml = xml = ILibParseXML(buffer,0,EndPointer-(*p_BeginPointer));
	ILibProcessXMLNodeList(xml);
	while(xml!=NULL)
	{
		if(xml->NameLength==28 && memcmp(xml->Name,"GetDefaultResolutionResponse",28)==0)
		{
			xml = xml->Next;
			while(xml!=NULL)
			{
				if(xml->NameLength==13 && memcmp(xml->Name,"RetResolution",13) == 0)
				{
					tempBufferLength = ILibReadInnerXML(xml,&tempBuffer);
					--ArgLeft;
					if(tempBufferLength!=0)
					{
						tempBuffer[tempBufferLength] = '\0';
						RetResolution = tempBuffer;
					}
				}
				xml = xml->Peer;
			}
		}
		if(xml!=NULL) {xml = xml->Next;}
	}
	ILibDestructXMLNodeList(__xml);
	
	if(ArgLeft!=0)
	{
		((void (*)(struct UPnPService*,int,void*,char*))_InvokeData->CallbackPtr)(Service,-2,_InvokeData->User,INVALID_DATA);
	}
	else
	{
		((void (*)(struct UPnPService*,int,void*,char*))_InvokeData->CallbackPtr)(Service,0,_InvokeData->User,RetResolution);
	}
	UPnPRelease(Service->Parent);
	FREE(_InvokeData);
}
void UPnPInvoke_DigitalSecurityCameraStillImage_GetDefaultResolution(struct UPnPService *service,void (*CallbackPtr)(struct UPnPService*,int,void*,char*), void* user)
{
	int headerLength;
	char *headerBuffer;
	char *SoapBodyTemplate;
	char* buffer;
	int bufferLength;
	char* IP;
	unsigned short Port;
	char* Path;
	struct sockaddr_in addr;
	struct InvokeStruct *invoke_data = (struct InvokeStruct*)MALLOC(sizeof(struct InvokeStruct));
	
	if(service==NULL)
	{
		FREE(invoke_data);
		return;
	}
	buffer = (char*)MALLOC((int)strlen(service->ServiceType)+272);
	SoapBodyTemplate = "%sGetDefaultResolution xmlns:u=\"%s\"></u:GetDefaultResolution%s";
	bufferLength = sprintf(buffer,SoapBodyTemplate,UPNPCP_SOAP_BodyHead,service->ServiceType,UPNPCP_SOAP_BodyTail);
	
	UPnPAddRef(service->Parent);
	ILibParseUri(service->ControlURL,&IP,&Port,&Path);
	
	headerBuffer = (char*)MALLOC(175 + (int)strlen(Path) + (int)strlen(IP) + (int)strlen(service->ServiceType));
	headerLength = sprintf(headerBuffer,UPNPCP_SOAP_Header,Path,IP,Port,service->ServiceType,"GetDefaultResolution",bufferLength);
	
	memset((char *)&addr, 0, sizeof(addr));
	addr.sin_family = AF_INET;
	addr.sin_addr.s_addr = inet_addr(IP);
	addr.sin_port = htons(Port);
	
	invoke_data->CallbackPtr = CallbackPtr;
	invoke_data->User = user;
	ILibAddRequest_DirectEx(((struct UPnPCP*)service->Parent->CP)->HTTP, headerBuffer,headerLength,buffer,bufferLength,&addr, &UPnPInvoke_DigitalSecurityCameraStillImage_GetDefaultResolution_Sink,service, invoke_data);	
	
	FREE(IP);
	FREE(Path);
}
void UPnPInvoke_PanTilt_GetTiltPosition_Sink(void *reader, struct packetheader *header, int IsInterrupt,char* buffer, int *p_BeginPointer, int EndPointer, int done, void* _service, void *state)
{
	struct UPnPService *Service = (struct UPnPService*)_service;
	struct InvokeStruct *_InvokeData = (struct InvokeStruct*)state;
	int ArgLeft = 1;
	struct ILibXMLNode *xml;
	struct ILibXMLNode *__xml;
	char *tempBuffer;
	int tempBufferLength;
	long TempLong;
	short RetTilt = 0;
	
	if(done==0){return;}
	if(_InvokeData->CallbackPtr==NULL)
	{
		UPnPRelease(Service->Parent);
		FREE(_InvokeData);
		return;
	}
	else
	{
		if(header==NULL)
		{
			/* Connection Failed */
			((void (*)(struct UPnPService*,int,void*,short))_InvokeData->CallbackPtr)(Service,IsInterrupt==0?-1:IsInterrupt,_InvokeData->User,INVALID_DATA);
			UPnPRelease(Service->Parent);
			FREE(_InvokeData);
			return;
		}
		else if(header->StatusCode!=200)
		{
			/* SOAP Fault */
			((void (*)(struct UPnPService*,int,void*,short))_InvokeData->CallbackPtr)(Service,UPnPGetErrorCode(buffer,EndPointer-(*p_BeginPointer)),_InvokeData->User,INVALID_DATA);
			UPnPRelease(Service->Parent);
			FREE(_InvokeData);
			return;
		}
	}
	
	__xml = xml = ILibParseXML(buffer,0,EndPointer-(*p_BeginPointer));
	ILibProcessXMLNodeList(xml);
	while(xml!=NULL)
	{
		if(xml->NameLength==23 && memcmp(xml->Name,"GetTiltPositionResponse",23)==0)
		{
			xml = xml->Next;
			while(xml!=NULL)
			{
				if(xml->NameLength==7 && memcmp(xml->Name,"RetTilt",7) == 0)
				{
					tempBufferLength = ILibReadInnerXML(xml,&tempBuffer);
					--ArgLeft;
					if(ILibGetLong(tempBuffer,tempBufferLength,&TempLong)==0)
					{
						RetTilt = (short) TempLong;
					}
				}
				xml = xml->Peer;
			}
		}
		if(xml!=NULL) {xml = xml->Next;}
	}
	ILibDestructXMLNodeList(__xml);
	
	if(ArgLeft!=0)
	{
		((void (*)(struct UPnPService*,int,void*,short))_InvokeData->CallbackPtr)(Service,-2,_InvokeData->User,INVALID_DATA);
	}
	else
	{
		((void (*)(struct UPnPService*,int,void*,short))_InvokeData->CallbackPtr)(Service,0,_InvokeData->User,RetTilt);
	}
	UPnPRelease(Service->Parent);
	FREE(_InvokeData);
}
void UPnPInvoke_PanTilt_GetTiltPosition(struct UPnPService *service,void (*CallbackPtr)(struct UPnPService*,int,void*,short), void* user)
{
	int headerLength;
	char *headerBuffer;
	char *SoapBodyTemplate;
	char* buffer;
	int bufferLength;
	char* IP;
	unsigned short Port;
	char* Path;
	struct sockaddr_in addr;
	struct InvokeStruct *invoke_data = (struct InvokeStruct*)MALLOC(sizeof(struct InvokeStruct));
	
	if(service==NULL)
	{
		FREE(invoke_data);
		return;
	}
	buffer = (char*)MALLOC((int)strlen(service->ServiceType)+262);
	SoapBodyTemplate = "%sGetTiltPosition xmlns:u=\"%s\"></u:GetTiltPosition%s";
	bufferLength = sprintf(buffer,SoapBodyTemplate,UPNPCP_SOAP_BodyHead,service->ServiceType,UPNPCP_SOAP_BodyTail);
	
	UPnPAddRef(service->Parent);
	ILibParseUri(service->ControlURL,&IP,&Port,&Path);
	
	headerBuffer = (char*)MALLOC(170 + (int)strlen(Path) + (int)strlen(IP) + (int)strlen(service->ServiceType));
	headerLength = sprintf(headerBuffer,UPNPCP_SOAP_Header,Path,IP,Port,service->ServiceType,"GetTiltPosition",bufferLength);
	
	memset((char *)&addr, 0, sizeof(addr));
	addr.sin_family = AF_INET;
	addr.sin_addr.s_addr = inet_addr(IP);
	addr.sin_port = htons(Port);
	
	invoke_data->CallbackPtr = CallbackPtr;
	invoke_data->User = user;
	ILibAddRequest_DirectEx(((struct UPnPCP*)service->Parent->CP)->HTTP, headerBuffer,headerLength,buffer,bufferLength,&addr, &UPnPInvoke_PanTilt_GetTiltPosition_Sink,service, invoke_data);	
	
	FREE(IP);
	FREE(Path);
}
void UPnPInvoke_PanTilt_GetPanPosition_Sink(void *reader, struct packetheader *header, int IsInterrupt,char* buffer, int *p_BeginPointer, int EndPointer, int done, void* _service, void *state)
{
	struct UPnPService *Service = (struct UPnPService*)_service;
	struct InvokeStruct *_InvokeData = (struct InvokeStruct*)state;
	int ArgLeft = 1;
	struct ILibXMLNode *xml;
	struct ILibXMLNode *__xml;
	char *tempBuffer;
	int tempBufferLength;
	long TempLong;
	short RetPan = 0;
	
	if(done==0){return;}
	if(_InvokeData->CallbackPtr==NULL)
	{
		UPnPRelease(Service->Parent);
		FREE(_InvokeData);
		return;
	}
	else
	{
		if(header==NULL)
		{
			/* Connection Failed */
			((void (*)(struct UPnPService*,int,void*,short))_InvokeData->CallbackPtr)(Service,IsInterrupt==0?-1:IsInterrupt,_InvokeData->User,INVALID_DATA);
			UPnPRelease(Service->Parent);
			FREE(_InvokeData);
			return;
		}
		else if(header->StatusCode!=200)
		{
			/* SOAP Fault */
			((void (*)(struct UPnPService*,int,void*,short))_InvokeData->CallbackPtr)(Service,UPnPGetErrorCode(buffer,EndPointer-(*p_BeginPointer)),_InvokeData->User,INVALID_DATA);
			UPnPRelease(Service->Parent);
			FREE(_InvokeData);
			return;
		}
	}
	
	__xml = xml = ILibParseXML(buffer,0,EndPointer-(*p_BeginPointer));
	ILibProcessXMLNodeList(xml);
	while(xml!=NULL)
	{
		if(xml->NameLength==22 && memcmp(xml->Name,"GetPanPositionResponse",22)==0)
		{
			xml = xml->Next;
			while(xml!=NULL)
			{
				if(xml->NameLength==6 && memcmp(xml->Name,"RetPan",6) == 0)
				{
					tempBufferLength = ILibReadInnerXML(xml,&tempBuffer);
					--ArgLeft;
					if(ILibGetLong(tempBuffer,tempBufferLength,&TempLong)==0)
					{
						RetPan = (short) TempLong;
					}
				}
				xml = xml->Peer;
			}
		}
		if(xml!=NULL) {xml = xml->Next;}
	}
	ILibDestructXMLNodeList(__xml);
	
	if(ArgLeft!=0)
	{
		((void (*)(struct UPnPService*,int,void*,short))_InvokeData->CallbackPtr)(Service,-2,_InvokeData->User,INVALID_DATA);
	}
	else
	{
		((void (*)(struct UPnPService*,int,void*,short))_InvokeData->CallbackPtr)(Service,0,_InvokeData->User,RetPan);
	}
	UPnPRelease(Service->Parent);
	FREE(_InvokeData);
}
void UPnPInvoke_PanTilt_GetPanPosition(struct UPnPService *service,void (*CallbackPtr)(struct UPnPService*,int,void*,short), void* user)
{
	int headerLength;
	char *headerBuffer;
	char *SoapBodyTemplate;
	char* buffer;
	int bufferLength;
	char* IP;
	unsigned short Port;
	char* Path;
	struct sockaddr_in addr;
	struct InvokeStruct *invoke_data = (struct InvokeStruct*)MALLOC(sizeof(struct InvokeStruct));
	
	if(service==NULL)
	{
		FREE(invoke_data);
		return;
	}
	buffer = (char*)MALLOC((int)strlen(service->ServiceType)+260);
	SoapBodyTemplate = "%sGetPanPosition xmlns:u=\"%s\"></u:GetPanPosition%s";
	bufferLength = sprintf(buffer,SoapBodyTemplate,UPNPCP_SOAP_BodyHead,service->ServiceType,UPNPCP_SOAP_BodyTail);
	
	UPnPAddRef(service->Parent);
	ILibParseUri(service->ControlURL,&IP,&Port,&Path);
	
	headerBuffer = (char*)MALLOC(169 + (int)strlen(Path) + (int)strlen(IP) + (int)strlen(service->ServiceType));
	headerLength = sprintf(headerBuffer,UPNPCP_SOAP_Header,Path,IP,Port,service->ServiceType,"GetPanPosition",bufferLength);
	
	memset((char *)&addr, 0, sizeof(addr));
	addr.sin_family = AF_INET;
	addr.sin_addr.s_addr = inet_addr(IP);
	addr.sin_port = htons(Port);
	
	invoke_data->CallbackPtr = CallbackPtr;
	invoke_data->User = user;
	ILibAddRequest_DirectEx(((struct UPnPCP*)service->Parent->CP)->HTTP, headerBuffer,headerLength,buffer,bufferLength,&addr, &UPnPInvoke_PanTilt_GetPanPosition_Sink,service, invoke_data);	
	
	FREE(IP);
	FREE(Path);
}
void UPnPInvoke_PanTilt_SetRelTiltPosition_Sink(void *reader, struct packetheader *header, int IsInterrupt,char* buffer, int *p_BeginPointer, int EndPointer, int done, void* _service, void *state)
{
	struct UPnPService *Service = (struct UPnPService*)_service;
	struct InvokeStruct *_InvokeData = (struct InvokeStruct*)state;
	if(done==0){return;}
	if(_InvokeData->CallbackPtr==NULL)
	{
		UPnPRelease(Service->Parent);
		FREE(_InvokeData);
		return;
	}
	else
	{
		if(header==NULL)
		{
			/* Connection Failed */
			((void (*)(struct UPnPService*,int,void*))_InvokeData->CallbackPtr)(Service,-1,_InvokeData->User);
			UPnPRelease(Service->Parent);
			FREE(_InvokeData);
			return;
		}
		else if(header->StatusCode!=200)
		{
			/* SOAP Fault */
			((void (*)(struct UPnPService*,int,void*))_InvokeData->CallbackPtr)(Service,UPnPGetErrorCode(buffer,EndPointer-(*p_BeginPointer)),_InvokeData->User);
			UPnPRelease(Service->Parent);
			FREE(_InvokeData);
			return;
		}
	}
	
	((void (*)(struct UPnPService*,int,void*))_InvokeData->CallbackPtr)(Service,0,_InvokeData->User);
	UPnPRelease(Service->Parent);
	FREE(_InvokeData);
}
void UPnPInvoke_PanTilt_SetRelTiltPosition(struct UPnPService *service,void (*CallbackPtr)(struct UPnPService*,int,void*), void* user, short NewTilt)
{
	int headerLength;
	char *headerBuffer;
	char *SoapBodyTemplate;
	char* buffer;
	int bufferLength;
	char* IP;
	unsigned short Port;
	char* Path;
	struct sockaddr_in addr;
	struct InvokeStruct *invoke_data = (struct InvokeStruct*)MALLOC(sizeof(struct InvokeStruct));
	
	if(service==NULL)
	{
		FREE(invoke_data);
		return;
	}
	buffer = (char*)MALLOC((int)strlen(service->ServiceType)+293);
	SoapBodyTemplate = "%sSetRelTiltPosition xmlns:u=\"%s\"><NewTilt>%d</NewTilt></u:SetRelTiltPosition%s";
	bufferLength = sprintf(buffer,SoapBodyTemplate,UPNPCP_SOAP_BodyHead,service->ServiceType, NewTilt,UPNPCP_SOAP_BodyTail);
	
	UPnPAddRef(service->Parent);
	ILibParseUri(service->ControlURL,&IP,&Port,&Path);
	
	headerBuffer = (char*)MALLOC(173 + (int)strlen(Path) + (int)strlen(IP) + (int)strlen(service->ServiceType));
	headerLength = sprintf(headerBuffer,UPNPCP_SOAP_Header,Path,IP,Port,service->ServiceType,"SetRelTiltPosition",bufferLength);
	
	memset((char *)&addr, 0, sizeof(addr));
	addr.sin_family = AF_INET;
	addr.sin_addr.s_addr = inet_addr(IP);
	addr.sin_port = htons(Port);
	
	invoke_data->CallbackPtr = CallbackPtr;
	invoke_data->User = user;
	ILibAddRequest_DirectEx(((struct UPnPCP*)service->Parent->CP)->HTTP, headerBuffer,headerLength,buffer,bufferLength,&addr, &UPnPInvoke_PanTilt_SetRelTiltPosition_Sink,service, invoke_data);	
	
	FREE(IP);
	FREE(Path);
}
void UPnPInvoke_PanTilt_SetAbsTiltPosition_Sink(void *reader, struct packetheader *header, int IsInterrupt,char* buffer, int *p_BeginPointer, int EndPointer, int done, void* _service, void *state)
{
	struct UPnPService *Service = (struct UPnPService*)_service;
	struct InvokeStruct *_InvokeData = (struct InvokeStruct*)state;
	if(done==0){return;}
	if(_InvokeData->CallbackPtr==NULL)
	{
		UPnPRelease(Service->Parent);
		FREE(_InvokeData);
		return;
	}
	else
	{
		if(header==NULL)
		{
			/* Connection Failed */
			((void (*)(struct UPnPService*,int,void*))_InvokeData->CallbackPtr)(Service,-1,_InvokeData->User);
			UPnPRelease(Service->Parent);
			FREE(_InvokeData);
			return;
		}
		else if(header->StatusCode!=200)
		{
			/* SOAP Fault */
			((void (*)(struct UPnPService*,int,void*))_InvokeData->CallbackPtr)(Service,UPnPGetErrorCode(buffer,EndPointer-(*p_BeginPointer)),_InvokeData->User);
			UPnPRelease(Service->Parent);
			FREE(_InvokeData);
			return;
		}
	}
	
	((void (*)(struct UPnPService*,int,void*))_InvokeData->CallbackPtr)(Service,0,_InvokeData->User);
	UPnPRelease(Service->Parent);
	FREE(_InvokeData);
}
void UPnPInvoke_PanTilt_SetAbsTiltPosition(struct UPnPService *service,void (*CallbackPtr)(struct UPnPService*,int,void*), void* user, short NewTilt)
{
	int headerLength;
	char *headerBuffer;
	char *SoapBodyTemplate;
	char* buffer;
	int bufferLength;
	char* IP;
	unsigned short Port;
	char* Path;
	struct sockaddr_in addr;
	struct InvokeStruct *invoke_data = (struct InvokeStruct*)MALLOC(sizeof(struct InvokeStruct));
	
	if(service==NULL)
	{
		FREE(invoke_data);
		return;
	}
	buffer = (char*)MALLOC((int)strlen(service->ServiceType)+293);
	SoapBodyTemplate = "%sSetAbsTiltPosition xmlns:u=\"%s\"><NewTilt>%d</NewTilt></u:SetAbsTiltPosition%s";
	bufferLength = sprintf(buffer,SoapBodyTemplate,UPNPCP_SOAP_BodyHead,service->ServiceType, NewTilt,UPNPCP_SOAP_BodyTail);
	
	UPnPAddRef(service->Parent);
	ILibParseUri(service->ControlURL,&IP,&Port,&Path);
	
	headerBuffer = (char*)MALLOC(173 + (int)strlen(Path) + (int)strlen(IP) + (int)strlen(service->ServiceType));
	headerLength = sprintf(headerBuffer,UPNPCP_SOAP_Header,Path,IP,Port,service->ServiceType,"SetAbsTiltPosition",bufferLength);
	
	memset((char *)&addr, 0, sizeof(addr));
	addr.sin_family = AF_INET;
	addr.sin_addr.s_addr = inet_addr(IP);
	addr.sin_port = htons(Port);
	
	invoke_data->CallbackPtr = CallbackPtr;
	invoke_data->User = user;
	ILibAddRequest_DirectEx(((struct UPnPCP*)service->Parent->CP)->HTTP, headerBuffer,headerLength,buffer,bufferLength,&addr, &UPnPInvoke_PanTilt_SetAbsTiltPosition_Sink,service, invoke_data);	
	
	FREE(IP);
	FREE(Path);
}
void UPnPInvoke_PanTilt_SetRelPanPosition_Sink(void *reader, struct packetheader *header, int IsInterrupt,char* buffer, int *p_BeginPointer, int EndPointer, int done, void* _service, void *state)
{
	struct UPnPService *Service = (struct UPnPService*)_service;
	struct InvokeStruct *_InvokeData = (struct InvokeStruct*)state;
	if(done==0){return;}
	if(_InvokeData->CallbackPtr==NULL)
	{
		UPnPRelease(Service->Parent);
		FREE(_InvokeData);
		return;
	}
	else
	{
		if(header==NULL)
		{
			/* Connection Failed */
			((void (*)(struct UPnPService*,int,void*))_InvokeData->CallbackPtr)(Service,-1,_InvokeData->User);
			UPnPRelease(Service->Parent);
			FREE(_InvokeData);
			return;
		}
		else if(header->StatusCode!=200)
		{
			/* SOAP Fault */
			((void (*)(struct UPnPService*,int,void*))_InvokeData->CallbackPtr)(Service,UPnPGetErrorCode(buffer,EndPointer-(*p_BeginPointer)),_InvokeData->User);
			UPnPRelease(Service->Parent);
			FREE(_InvokeData);
			return;
		}
	}
	
	((void (*)(struct UPnPService*,int,void*))_InvokeData->CallbackPtr)(Service,0,_InvokeData->User);
	UPnPRelease(Service->Parent);
	FREE(_InvokeData);
}
void UPnPInvoke_PanTilt_SetRelPanPosition(struct UPnPService *service,void (*CallbackPtr)(struct UPnPService*,int,void*), void* user, short NewPan)
{
	int headerLength;
	char *headerBuffer;
	char *SoapBodyTemplate;
	char* buffer;
	int bufferLength;
	char* IP;
	unsigned short Port;
	char* Path;
	struct sockaddr_in addr;
	struct InvokeStruct *invoke_data = (struct InvokeStruct*)MALLOC(sizeof(struct InvokeStruct));
	
	if(service==NULL)
	{
		FREE(invoke_data);
		return;
	}
	buffer = (char*)MALLOC((int)strlen(service->ServiceType)+289);
	SoapBodyTemplate = "%sSetRelPanPosition xmlns:u=\"%s\"><NewPan>%d</NewPan></u:SetRelPanPosition%s";
	bufferLength = sprintf(buffer,SoapBodyTemplate,UPNPCP_SOAP_BodyHead,service->ServiceType, NewPan,UPNPCP_SOAP_BodyTail);
	
	UPnPAddRef(service->Parent);
	ILibParseUri(service->ControlURL,&IP,&Port,&Path);
	
	headerBuffer = (char*)MALLOC(172 + (int)strlen(Path) + (int)strlen(IP) + (int)strlen(service->ServiceType));
	headerLength = sprintf(headerBuffer,UPNPCP_SOAP_Header,Path,IP,Port,service->ServiceType,"SetRelPanPosition",bufferLength);
	
	memset((char *)&addr, 0, sizeof(addr));
	addr.sin_family = AF_INET;
	addr.sin_addr.s_addr = inet_addr(IP);
	addr.sin_port = htons(Port);
	
	invoke_data->CallbackPtr = CallbackPtr;
	invoke_data->User = user;
	ILibAddRequest_DirectEx(((struct UPnPCP*)service->Parent->CP)->HTTP, headerBuffer,headerLength,buffer,bufferLength,&addr, &UPnPInvoke_PanTilt_SetRelPanPosition_Sink,service, invoke_data);	
	
	FREE(IP);
	FREE(Path);
}
void UPnPInvoke_PanTilt_SetAbsPanPosition_Sink(void *reader, struct packetheader *header, int IsInterrupt,char* buffer, int *p_BeginPointer, int EndPointer, int done, void* _service, void *state)
{
	struct UPnPService *Service = (struct UPnPService*)_service;
	struct InvokeStruct *_InvokeData = (struct InvokeStruct*)state;
	if(done==0){return;}
	if(_InvokeData->CallbackPtr==NULL)
	{
		UPnPRelease(Service->Parent);
		FREE(_InvokeData);
		return;
	}
	else
	{
		if(header==NULL)
		{
			/* Connection Failed */
			((void (*)(struct UPnPService*,int,void*))_InvokeData->CallbackPtr)(Service,-1,_InvokeData->User);
			UPnPRelease(Service->Parent);
			FREE(_InvokeData);
			return;
		}
		else if(header->StatusCode!=200)
		{
			/* SOAP Fault */
			((void (*)(struct UPnPService*,int,void*))_InvokeData->CallbackPtr)(Service,UPnPGetErrorCode(buffer,EndPointer-(*p_BeginPointer)),_InvokeData->User);
			UPnPRelease(Service->Parent);
			FREE(_InvokeData);
			return;
		}
	}
	
	((void (*)(struct UPnPService*,int,void*))_InvokeData->CallbackPtr)(Service,0,_InvokeData->User);
	UPnPRelease(Service->Parent);
	FREE(_InvokeData);
}
void UPnPInvoke_PanTilt_SetAbsPanPosition(struct UPnPService *service,void (*CallbackPtr)(struct UPnPService*,int,void*), void* user, short NewPan)
{
	int headerLength;
	char *headerBuffer;
	char *SoapBodyTemplate;
	char* buffer;
	int bufferLength;
	char* IP;
	unsigned short Port;
	char* Path;
	struct sockaddr_in addr;
	struct InvokeStruct *invoke_data = (struct InvokeStruct*)MALLOC(sizeof(struct InvokeStruct));
	
	if(service==NULL)
	{
		FREE(invoke_data);
		return;
	}
	buffer = (char*)MALLOC((int)strlen(service->ServiceType)+289);
	SoapBodyTemplate = "%sSetAbsPanPosition xmlns:u=\"%s\"><NewPan>%d</NewPan></u:SetAbsPanPosition%s";
	bufferLength = sprintf(buffer,SoapBodyTemplate,UPNPCP_SOAP_BodyHead,service->ServiceType, NewPan,UPNPCP_SOAP_BodyTail);
	
	UPnPAddRef(service->Parent);
	ILibParseUri(service->ControlURL,&IP,&Port,&Path);
	
	headerBuffer = (char*)MALLOC(172 + (int)strlen(Path) + (int)strlen(IP) + (int)strlen(service->ServiceType));
	headerLength = sprintf(headerBuffer,UPNPCP_SOAP_Header,Path,IP,Port,service->ServiceType,"SetAbsPanPosition",bufferLength);
	
	memset((char *)&addr, 0, sizeof(addr));
	addr.sin_family = AF_INET;
	addr.sin_addr.s_addr = inet_addr(IP);
	addr.sin_port = htons(Port);
	
	invoke_data->CallbackPtr = CallbackPtr;
	invoke_data->User = user;
	ILibAddRequest_DirectEx(((struct UPnPCP*)service->Parent->CP)->HTTP, headerBuffer,headerLength,buffer,bufferLength,&addr, &UPnPInvoke_PanTilt_SetAbsPanPosition_Sink,service, invoke_data);	
	
	FREE(IP);
	FREE(Path);
}
void UPnPInvoke_DigitalSecurityCameraMotionImage_GetDefaultVideoPresentationURL_Sink(void *reader, struct packetheader *header, int IsInterrupt,char* buffer, int *p_BeginPointer, int EndPointer, int done, void* _service, void *state)
{
	struct UPnPService *Service = (struct UPnPService*)_service;
	struct InvokeStruct *_InvokeData = (struct InvokeStruct*)state;
	int ArgLeft = 1;
	struct ILibXMLNode *xml;
	struct ILibXMLNode *__xml;
	char *tempBuffer;
	int tempBufferLength;
	char* RetVideoPresentationURL = NULL;
	
	if(done==0){return;}
	if(_InvokeData->CallbackPtr==NULL)
	{
		UPnPRelease(Service->Parent);
		FREE(_InvokeData);
		return;
	}
	else
	{
		if(header==NULL)
		{
			/* Connection Failed */
			((void (*)(struct UPnPService*,int,void*,char*))_InvokeData->CallbackPtr)(Service,IsInterrupt==0?-1:IsInterrupt,_InvokeData->User,INVALID_DATA);
			UPnPRelease(Service->Parent);
			FREE(_InvokeData);
			return;
		}
		else if(header->StatusCode!=200)
		{
			/* SOAP Fault */
			((void (*)(struct UPnPService*,int,void*,char*))_InvokeData->CallbackPtr)(Service,UPnPGetErrorCode(buffer,EndPointer-(*p_BeginPointer)),_InvokeData->User,INVALID_DATA);
			UPnPRelease(Service->Parent);
			FREE(_InvokeData);
			return;
		}
	}
	
	__xml = xml = ILibParseXML(buffer,0,EndPointer-(*p_BeginPointer));
	ILibProcessXMLNodeList(xml);
	while(xml!=NULL)
	{
		if(xml->NameLength==38 && memcmp(xml->Name,"GetDefaultVideoPresentationURLResponse",38)==0)
		{
			xml = xml->Next;
			while(xml!=NULL)
			{
				if(xml->NameLength==23 && memcmp(xml->Name,"RetVideoPresentationURL",23) == 0)
				{
					tempBufferLength = ILibReadInnerXML(xml,&tempBuffer);
					--ArgLeft;
					if(tempBufferLength!=0)
					{
						tempBuffer[tempBufferLength] = '\0';
						RetVideoPresentationURL = tempBuffer;
					}
				}
				xml = xml->Peer;
			}
		}
		if(xml!=NULL) {xml = xml->Next;}
	}
	ILibDestructXMLNodeList(__xml);
	
	if(ArgLeft!=0)
	{
		((void (*)(struct UPnPService*,int,void*,char*))_InvokeData->CallbackPtr)(Service,-2,_InvokeData->User,INVALID_DATA);
	}
	else
	{
		((void (*)(struct UPnPService*,int,void*,char*))_InvokeData->CallbackPtr)(Service,0,_InvokeData->User,RetVideoPresentationURL);
	}
	UPnPRelease(Service->Parent);
	FREE(_InvokeData);
}
void UPnPInvoke_DigitalSecurityCameraMotionImage_GetDefaultVideoPresentationURL(struct UPnPService *service,void (*CallbackPtr)(struct UPnPService*,int,void*,char*), void* user)
{
	int headerLength;
	char *headerBuffer;
	char *SoapBodyTemplate;
	char* buffer;
	int bufferLength;
	char* IP;
	unsigned short Port;
	char* Path;
	struct sockaddr_in addr;
	struct InvokeStruct *invoke_data = (struct InvokeStruct*)MALLOC(sizeof(struct InvokeStruct));
	
	if(service==NULL)
	{
		FREE(invoke_data);
		return;
	}
	buffer = (char*)MALLOC((int)strlen(service->ServiceType)+292);
	SoapBodyTemplate = "%sGetDefaultVideoPresentationURL xmlns:u=\"%s\"></u:GetDefaultVideoPresentationURL%s";
	bufferLength = sprintf(buffer,SoapBodyTemplate,UPNPCP_SOAP_BodyHead,service->ServiceType,UPNPCP_SOAP_BodyTail);
	
	UPnPAddRef(service->Parent);
	ILibParseUri(service->ControlURL,&IP,&Port,&Path);
	
	headerBuffer = (char*)MALLOC(185 + (int)strlen(Path) + (int)strlen(IP) + (int)strlen(service->ServiceType));
	headerLength = sprintf(headerBuffer,UPNPCP_SOAP_Header,Path,IP,Port,service->ServiceType,"GetDefaultVideoPresentationURL",bufferLength);
	
	memset((char *)&addr, 0, sizeof(addr));
	addr.sin_family = AF_INET;
	addr.sin_addr.s_addr = inet_addr(IP);
	addr.sin_port = htons(Port);
	
	invoke_data->CallbackPtr = CallbackPtr;
	invoke_data->User = user;
	ILibAddRequest_DirectEx(((struct UPnPCP*)service->Parent->CP)->HTTP, headerBuffer,headerLength,buffer,bufferLength,&addr, &UPnPInvoke_DigitalSecurityCameraMotionImage_GetDefaultVideoPresentationURL_Sink,service, invoke_data);	
	
	FREE(IP);
	FREE(Path);
}
void UPnPInvoke_DigitalSecurityCameraMotionImage_SetDefaultCompressionLevel_Sink(void *reader, struct packetheader *header, int IsInterrupt,char* buffer, int *p_BeginPointer, int EndPointer, int done, void* _service, void *state)
{
	struct UPnPService *Service = (struct UPnPService*)_service;
	struct InvokeStruct *_InvokeData = (struct InvokeStruct*)state;
	if(done==0){return;}
	if(_InvokeData->CallbackPtr==NULL)
	{
		UPnPRelease(Service->Parent);
		FREE(_InvokeData);
		return;
	}
	else
	{
		if(header==NULL)
		{
			/* Connection Failed */
			((void (*)(struct UPnPService*,int,void*))_InvokeData->CallbackPtr)(Service,-1,_InvokeData->User);
			UPnPRelease(Service->Parent);
			FREE(_InvokeData);
			return;
		}
		else if(header->StatusCode!=200)
		{
			/* SOAP Fault */
			((void (*)(struct UPnPService*,int,void*))_InvokeData->CallbackPtr)(Service,UPnPGetErrorCode(buffer,EndPointer-(*p_BeginPointer)),_InvokeData->User);
			UPnPRelease(Service->Parent);
			FREE(_InvokeData);
			return;
		}
	}
	
	((void (*)(struct UPnPService*,int,void*))_InvokeData->CallbackPtr)(Service,0,_InvokeData->User);
	UPnPRelease(Service->Parent);
	FREE(_InvokeData);
}
void UPnPInvoke_DigitalSecurityCameraMotionImage_SetDefaultCompressionLevel(struct UPnPService *service,void (*CallbackPtr)(struct UPnPService*,int,void*), void* user, char* ReqCompressionLevel)
{
	int headerLength;
	char *headerBuffer;
	char *SoapBodyTemplate;
	char* buffer;
	int bufferLength;
	char* IP;
	unsigned short Port;
	char* Path;
	struct sockaddr_in addr;
	struct InvokeStruct *invoke_data = (struct InvokeStruct*)MALLOC(sizeof(struct InvokeStruct));
	
	if(service==NULL)
	{
		FREE(invoke_data);
		return;
	}
	buffer = (char*)MALLOC((int)strlen(service->ServiceType)+(int)strlen(ReqCompressionLevel)+327);
	SoapBodyTemplate = "%sSetDefaultCompressionLevel xmlns:u=\"%s\"><ReqCompressionLevel>%s</ReqCompressionLevel></u:SetDefaultCompressionLevel%s";
	bufferLength = sprintf(buffer,SoapBodyTemplate,UPNPCP_SOAP_BodyHead,service->ServiceType, ReqCompressionLevel,UPNPCP_SOAP_BodyTail);
	
	UPnPAddRef(service->Parent);
	ILibParseUri(service->ControlURL,&IP,&Port,&Path);
	
	headerBuffer = (char*)MALLOC(181 + (int)strlen(Path) + (int)strlen(IP) + (int)strlen(service->ServiceType));
	headerLength = sprintf(headerBuffer,UPNPCP_SOAP_Header,Path,IP,Port,service->ServiceType,"SetDefaultCompressionLevel",bufferLength);
	
	memset((char *)&addr, 0, sizeof(addr));
	addr.sin_family = AF_INET;
	addr.sin_addr.s_addr = inet_addr(IP);
	addr.sin_port = htons(Port);
	
	invoke_data->CallbackPtr = CallbackPtr;
	invoke_data->User = user;
	ILibAddRequest_DirectEx(((struct UPnPCP*)service->Parent->CP)->HTTP, headerBuffer,headerLength,buffer,bufferLength,&addr, &UPnPInvoke_DigitalSecurityCameraMotionImage_SetDefaultCompressionLevel_Sink,service, invoke_data);	
	
	FREE(IP);
	FREE(Path);
}
void UPnPInvoke_DigitalSecurityCameraMotionImage_GetAvailableEncodings_Sink(void *reader, struct packetheader *header, int IsInterrupt,char* buffer, int *p_BeginPointer, int EndPointer, int done, void* _service, void *state)
{
	struct UPnPService *Service = (struct UPnPService*)_service;
	struct InvokeStruct *_InvokeData = (struct InvokeStruct*)state;
	int ArgLeft = 1;
	struct ILibXMLNode *xml;
	struct ILibXMLNode *__xml;
	char *tempBuffer;
	int tempBufferLength;
	char* RetAvailableEncodings = NULL;
	
	if(done==0){return;}
	if(_InvokeData->CallbackPtr==NULL)
	{
		UPnPRelease(Service->Parent);
		FREE(_InvokeData);
		return;
	}
	else
	{
		if(header==NULL)
		{
			/* Connection Failed */
			((void (*)(struct UPnPService*,int,void*,char*))_InvokeData->CallbackPtr)(Service,IsInterrupt==0?-1:IsInterrupt,_InvokeData->User,INVALID_DATA);
			UPnPRelease(Service->Parent);
			FREE(_InvokeData);
			return;
		}
		else if(header->StatusCode!=200)
		{
			/* SOAP Fault */
			((void (*)(struct UPnPService*,int,void*,char*))_InvokeData->CallbackPtr)(Service,UPnPGetErrorCode(buffer,EndPointer-(*p_BeginPointer)),_InvokeData->User,INVALID_DATA);
			UPnPRelease(Service->Parent);
			FREE(_InvokeData);
			return;
		}
	}
	
	__xml = xml = ILibParseXML(buffer,0,EndPointer-(*p_BeginPointer));
	ILibProcessXMLNodeList(xml);
	while(xml!=NULL)
	{
		if(xml->NameLength==29 && memcmp(xml->Name,"GetAvailableEncodingsResponse",29)==0)
		{
			xml = xml->Next;
			while(xml!=NULL)
			{
				if(xml->NameLength==21 && memcmp(xml->Name,"RetAvailableEncodings",21) == 0)
				{
					tempBufferLength = ILibReadInnerXML(xml,&tempBuffer);
					--ArgLeft;
					if(tempBufferLength!=0)
					{
						tempBuffer[tempBufferLength] = '\0';
						RetAvailableEncodings = tempBuffer;
					}
				}
				xml = xml->Peer;
			}
		}
		if(xml!=NULL) {xml = xml->Next;}
	}
	ILibDestructXMLNodeList(__xml);
	
	if(ArgLeft!=0)
	{
		((void (*)(struct UPnPService*,int,void*,char*))_InvokeData->CallbackPtr)(Service,-2,_InvokeData->User,INVALID_DATA);
	}
	else
	{
		((void (*)(struct UPnPService*,int,void*,char*))_InvokeData->CallbackPtr)(Service,0,_InvokeData->User,RetAvailableEncodings);
	}
	UPnPRelease(Service->Parent);
	FREE(_InvokeData);
}
void UPnPInvoke_DigitalSecurityCameraMotionImage_GetAvailableEncodings(struct UPnPService *service,void (*CallbackPtr)(struct UPnPService*,int,void*,char*), void* user)
{
	int headerLength;
	char *headerBuffer;
	char *SoapBodyTemplate;
	char* buffer;
	int bufferLength;
	char* IP;
	unsigned short Port;
	char* Path;
	struct sockaddr_in addr;
	struct InvokeStruct *invoke_data = (struct InvokeStruct*)MALLOC(sizeof(struct InvokeStruct));
	
	if(service==NULL)
	{
		FREE(invoke_data);
		return;
	}
	buffer = (char*)MALLOC((int)strlen(service->ServiceType)+274);
	SoapBodyTemplate = "%sGetAvailableEncodings xmlns:u=\"%s\"></u:GetAvailableEncodings%s";
	bufferLength = sprintf(buffer,SoapBodyTemplate,UPNPCP_SOAP_BodyHead,service->ServiceType,UPNPCP_SOAP_BodyTail);
	
	UPnPAddRef(service->Parent);
	ILibParseUri(service->ControlURL,&IP,&Port,&Path);
	
	headerBuffer = (char*)MALLOC(176 + (int)strlen(Path) + (int)strlen(IP) + (int)strlen(service->ServiceType));
	headerLength = sprintf(headerBuffer,UPNPCP_SOAP_Header,Path,IP,Port,service->ServiceType,"GetAvailableEncodings",bufferLength);
	
	memset((char *)&addr, 0, sizeof(addr));
	addr.sin_family = AF_INET;
	addr.sin_addr.s_addr = inet_addr(IP);
	addr.sin_port = htons(Port);
	
	invoke_data->CallbackPtr = CallbackPtr;
	invoke_data->User = user;
	ILibAddRequest_DirectEx(((struct UPnPCP*)service->Parent->CP)->HTTP, headerBuffer,headerLength,buffer,bufferLength,&addr, &UPnPInvoke_DigitalSecurityCameraMotionImage_GetAvailableEncodings_Sink,service, invoke_data);	
	
	FREE(IP);
	FREE(Path);
}
void UPnPInvoke_DigitalSecurityCameraMotionImage_GetAvailableResolutions_Sink(void *reader, struct packetheader *header, int IsInterrupt,char* buffer, int *p_BeginPointer, int EndPointer, int done, void* _service, void *state)
{
	struct UPnPService *Service = (struct UPnPService*)_service;
	struct InvokeStruct *_InvokeData = (struct InvokeStruct*)state;
	int ArgLeft = 1;
	struct ILibXMLNode *xml;
	struct ILibXMLNode *__xml;
	char *tempBuffer;
	int tempBufferLength;
	char* RetAvailableResolutions = NULL;
	
	if(done==0){return;}
	if(_InvokeData->CallbackPtr==NULL)
	{
		UPnPRelease(Service->Parent);
		FREE(_InvokeData);
		return;
	}
	else
	{
		if(header==NULL)
		{
			/* Connection Failed */
			((void (*)(struct UPnPService*,int,void*,char*))_InvokeData->CallbackPtr)(Service,IsInterrupt==0?-1:IsInterrupt,_InvokeData->User,INVALID_DATA);
			UPnPRelease(Service->Parent);
			FREE(_InvokeData);
			return;
		}
		else if(header->StatusCode!=200)
		{
			/* SOAP Fault */
			((void (*)(struct UPnPService*,int,void*,char*))_InvokeData->CallbackPtr)(Service,UPnPGetErrorCode(buffer,EndPointer-(*p_BeginPointer)),_InvokeData->User,INVALID_DATA);
			UPnPRelease(Service->Parent);
			FREE(_InvokeData);
			return;
		}
	}
	
	__xml = xml = ILibParseXML(buffer,0,EndPointer-(*p_BeginPointer));
	ILibProcessXMLNodeList(xml);
	while(xml!=NULL)
	{
		if(xml->NameLength==31 && memcmp(xml->Name,"GetAvailableResolutionsResponse",31)==0)
		{
			xml = xml->Next;
			while(xml!=NULL)
			{
				if(xml->NameLength==23 && memcmp(xml->Name,"RetAvailableResolutions",23) == 0)
				{
					tempBufferLength = ILibReadInnerXML(xml,&tempBuffer);
					--ArgLeft;
					if(tempBufferLength!=0)
					{
						tempBuffer[tempBufferLength] = '\0';
						RetAvailableResolutions = tempBuffer;
					}
				}
				xml = xml->Peer;
			}
		}
		if(xml!=NULL) {xml = xml->Next;}
	}
	ILibDestructXMLNodeList(__xml);
	
	if(ArgLeft!=0)
	{
		((void (*)(struct UPnPService*,int,void*,char*))_InvokeData->CallbackPtr)(Service,-2,_InvokeData->User,INVALID_DATA);
	}
	else
	{
		((void (*)(struct UPnPService*,int,void*,char*))_InvokeData->CallbackPtr)(Service,0,_InvokeData->User,RetAvailableResolutions);
	}
	UPnPRelease(Service->Parent);
	FREE(_InvokeData);
}
void UPnPInvoke_DigitalSecurityCameraMotionImage_GetAvailableResolutions(struct UPnPService *service,void (*CallbackPtr)(struct UPnPService*,int,void*,char*), void* user)
{
	int headerLength;
	char *headerBuffer;
	char *SoapBodyTemplate;
	char* buffer;
	int bufferLength;
	char* IP;
	unsigned short Port;
	char* Path;
	struct sockaddr_in addr;
	struct InvokeStruct *invoke_data = (struct InvokeStruct*)MALLOC(sizeof(struct InvokeStruct));
	
	if(service==NULL)
	{
		FREE(invoke_data);
		return;
	}
	buffer = (char*)MALLOC((int)strlen(service->ServiceType)+278);
	SoapBodyTemplate = "%sGetAvailableResolutions xmlns:u=\"%s\"></u:GetAvailableResolutions%s";
	bufferLength = sprintf(buffer,SoapBodyTemplate,UPNPCP_SOAP_BodyHead,service->ServiceType,UPNPCP_SOAP_BodyTail);
	
	UPnPAddRef(service->Parent);
	ILibParseUri(service->ControlURL,&IP,&Port,&Path);
	
	headerBuffer = (char*)MALLOC(178 + (int)strlen(Path) + (int)strlen(IP) + (int)strlen(service->ServiceType));
	headerLength = sprintf(headerBuffer,UPNPCP_SOAP_Header,Path,IP,Port,service->ServiceType,"GetAvailableResolutions",bufferLength);
	
	memset((char *)&addr, 0, sizeof(addr));
	addr.sin_family = AF_INET;
	addr.sin_addr.s_addr = inet_addr(IP);
	addr.sin_port = htons(Port);
	
	invoke_data->CallbackPtr = CallbackPtr;
	invoke_data->User = user;
	ILibAddRequest_DirectEx(((struct UPnPCP*)service->Parent->CP)->HTTP, headerBuffer,headerLength,buffer,bufferLength,&addr, &UPnPInvoke_DigitalSecurityCameraMotionImage_GetAvailableResolutions_Sink,service, invoke_data);	
	
	FREE(IP);
	FREE(Path);
}
void UPnPInvoke_DigitalSecurityCameraMotionImage_GetVideoURL_Sink(void *reader, struct packetheader *header, int IsInterrupt,char* buffer, int *p_BeginPointer, int EndPointer, int done, void* _service, void *state)
{
	struct UPnPService *Service = (struct UPnPService*)_service;
	struct InvokeStruct *_InvokeData = (struct InvokeStruct*)state;
	int ArgLeft = 1;
	struct ILibXMLNode *xml;
	struct ILibXMLNode *__xml;
	char *tempBuffer;
	int tempBufferLength;
	char* RetVideoURL = NULL;
	
	if(done==0){return;}
	if(_InvokeData->CallbackPtr==NULL)
	{
		UPnPRelease(Service->Parent);
		FREE(_InvokeData);
		return;
	}
	else
	{
		if(header==NULL)
		{
			/* Connection Failed */
			((void (*)(struct UPnPService*,int,void*,char*))_InvokeData->CallbackPtr)(Service,IsInterrupt==0?-1:IsInterrupt,_InvokeData->User,INVALID_DATA);
			UPnPRelease(Service->Parent);
			FREE(_InvokeData);
			return;
		}
		else if(header->StatusCode!=200)
		{
			/* SOAP Fault */
			((void (*)(struct UPnPService*,int,void*,char*))_InvokeData->CallbackPtr)(Service,UPnPGetErrorCode(buffer,EndPointer-(*p_BeginPointer)),_InvokeData->User,INVALID_DATA);
			UPnPRelease(Service->Parent);
			FREE(_InvokeData);
			return;
		}
	}
	
	__xml = xml = ILibParseXML(buffer,0,EndPointer-(*p_BeginPointer));
	ILibProcessXMLNodeList(xml);
	while(xml!=NULL)
	{
		if(xml->NameLength==19 && memcmp(xml->Name,"GetVideoURLResponse",19)==0)
		{
			xml = xml->Next;
			while(xml!=NULL)
			{
				if(xml->NameLength==11 && memcmp(xml->Name,"RetVideoURL",11) == 0)
				{
					tempBufferLength = ILibReadInnerXML(xml,&tempBuffer);
					--ArgLeft;
					if(tempBufferLength!=0)
					{
						tempBuffer[tempBufferLength] = '\0';
						RetVideoURL = tempBuffer;
					}
				}
				xml = xml->Peer;
			}
		}
		if(xml!=NULL) {xml = xml->Next;}
	}
	ILibDestructXMLNodeList(__xml);
	
	if(ArgLeft!=0)
	{
		((void (*)(struct UPnPService*,int,void*,char*))_InvokeData->CallbackPtr)(Service,-2,_InvokeData->User,INVALID_DATA);
	}
	else
	{
		((void (*)(struct UPnPService*,int,void*,char*))_InvokeData->CallbackPtr)(Service,0,_InvokeData->User,RetVideoURL);
	}
	UPnPRelease(Service->Parent);
	FREE(_InvokeData);
}
void UPnPInvoke_DigitalSecurityCameraMotionImage_GetVideoURL(struct UPnPService *service,void (*CallbackPtr)(struct UPnPService*,int,void*,char*), void* user, char* ReqEncoding, char* ReqCompression, char* ReqResolution)
{
	int headerLength;
	char *headerBuffer;
	char *SoapBodyTemplate;
	char* buffer;
	int bufferLength;
	char* IP;
	unsigned short Port;
	char* Path;
	struct sockaddr_in addr;
	struct InvokeStruct *invoke_data = (struct InvokeStruct*)MALLOC(sizeof(struct InvokeStruct));
	
	if(service==NULL)
	{
		FREE(invoke_data);
		return;
	}
	buffer = (char*)MALLOC((int)strlen(service->ServiceType)+(int)strlen(ReqEncoding)+(int)strlen(ReqCompression)+(int)strlen(ReqResolution)+345);
	SoapBodyTemplate = "%sGetVideoURL xmlns:u=\"%s\"><ReqEncoding>%s</ReqEncoding><ReqCompression>%s</ReqCompression><ReqResolution>%s</ReqResolution></u:GetVideoURL%s";
	bufferLength = sprintf(buffer,SoapBodyTemplate,UPNPCP_SOAP_BodyHead,service->ServiceType, ReqEncoding, ReqCompression, ReqResolution,UPNPCP_SOAP_BodyTail);
	
	UPnPAddRef(service->Parent);
	ILibParseUri(service->ControlURL,&IP,&Port,&Path);
	
	headerBuffer = (char*)MALLOC(166 + (int)strlen(Path) + (int)strlen(IP) + (int)strlen(service->ServiceType));
	headerLength = sprintf(headerBuffer,UPNPCP_SOAP_Header,Path,IP,Port,service->ServiceType,"GetVideoURL",bufferLength);
	
	memset((char *)&addr, 0, sizeof(addr));
	addr.sin_family = AF_INET;
	addr.sin_addr.s_addr = inet_addr(IP);
	addr.sin_port = htons(Port);
	
	invoke_data->CallbackPtr = CallbackPtr;
	invoke_data->User = user;
	ILibAddRequest_DirectEx(((struct UPnPCP*)service->Parent->CP)->HTTP, headerBuffer,headerLength,buffer,bufferLength,&addr, &UPnPInvoke_DigitalSecurityCameraMotionImage_GetVideoURL_Sink,service, invoke_data);	
	
	FREE(IP);
	FREE(Path);
}
void UPnPInvoke_DigitalSecurityCameraMotionImage_SetDefaultEncoding_Sink(void *reader, struct packetheader *header, int IsInterrupt,char* buffer, int *p_BeginPointer, int EndPointer, int done, void* _service, void *state)
{
	struct UPnPService *Service = (struct UPnPService*)_service;
	struct InvokeStruct *_InvokeData = (struct InvokeStruct*)state;
	if(done==0){return;}
	if(_InvokeData->CallbackPtr==NULL)
	{
		UPnPRelease(Service->Parent);
		FREE(_InvokeData);
		return;
	}
	else
	{
		if(header==NULL)
		{
			/* Connection Failed */
			((void (*)(struct UPnPService*,int,void*))_InvokeData->CallbackPtr)(Service,-1,_InvokeData->User);
			UPnPRelease(Service->Parent);
			FREE(_InvokeData);
			return;
		}
		else if(header->StatusCode!=200)
		{
			/* SOAP Fault */
			((void (*)(struct UPnPService*,int,void*))_InvokeData->CallbackPtr)(Service,UPnPGetErrorCode(buffer,EndPointer-(*p_BeginPointer)),_InvokeData->User);
			UPnPRelease(Service->Parent);
			FREE(_InvokeData);
			return;
		}
	}
	
	((void (*)(struct UPnPService*,int,void*))_InvokeData->CallbackPtr)(Service,0,_InvokeData->User);
	UPnPRelease(Service->Parent);
	FREE(_InvokeData);
}
void UPnPInvoke_DigitalSecurityCameraMotionImage_SetDefaultEncoding(struct UPnPService *service,void (*CallbackPtr)(struct UPnPService*,int,void*), void* user, char* ReqEncoding)
{
	int headerLength;
	char *headerBuffer;
	char *SoapBodyTemplate;
	char* buffer;
	int bufferLength;
	char* IP;
	unsigned short Port;
	char* Path;
	struct sockaddr_in addr;
	struct InvokeStruct *invoke_data = (struct InvokeStruct*)MALLOC(sizeof(struct InvokeStruct));
	
	if(service==NULL)
	{
		FREE(invoke_data);
		return;
	}
	buffer = (char*)MALLOC((int)strlen(service->ServiceType)+(int)strlen(ReqEncoding)+295);
	SoapBodyTemplate = "%sSetDefaultEncoding xmlns:u=\"%s\"><ReqEncoding>%s</ReqEncoding></u:SetDefaultEncoding%s";
	bufferLength = sprintf(buffer,SoapBodyTemplate,UPNPCP_SOAP_BodyHead,service->ServiceType, ReqEncoding,UPNPCP_SOAP_BodyTail);
	
	UPnPAddRef(service->Parent);
	ILibParseUri(service->ControlURL,&IP,&Port,&Path);
	
	headerBuffer = (char*)MALLOC(173 + (int)strlen(Path) + (int)strlen(IP) + (int)strlen(service->ServiceType));
	headerLength = sprintf(headerBuffer,UPNPCP_SOAP_Header,Path,IP,Port,service->ServiceType,"SetDefaultEncoding",bufferLength);
	
	memset((char *)&addr, 0, sizeof(addr));
	addr.sin_family = AF_INET;
	addr.sin_addr.s_addr = inet_addr(IP);
	addr.sin_port = htons(Port);
	
	invoke_data->CallbackPtr = CallbackPtr;
	invoke_data->User = user;
	ILibAddRequest_DirectEx(((struct UPnPCP*)service->Parent->CP)->HTTP, headerBuffer,headerLength,buffer,bufferLength,&addr, &UPnPInvoke_DigitalSecurityCameraMotionImage_SetDefaultEncoding_Sink,service, invoke_data);	
	
	FREE(IP);
	FREE(Path);
}
void UPnPInvoke_DigitalSecurityCameraMotionImage_GetAvailableCompressionLevels_Sink(void *reader, struct packetheader *header, int IsInterrupt,char* buffer, int *p_BeginPointer, int EndPointer, int done, void* _service, void *state)
{
	struct UPnPService *Service = (struct UPnPService*)_service;
	struct InvokeStruct *_InvokeData = (struct InvokeStruct*)state;
	int ArgLeft = 1;
	struct ILibXMLNode *xml;
	struct ILibXMLNode *__xml;
	char *tempBuffer;
	int tempBufferLength;
	char* RetAvailableCompressionLevels = NULL;
	
	if(done==0){return;}
	if(_InvokeData->CallbackPtr==NULL)
	{
		UPnPRelease(Service->Parent);
		FREE(_InvokeData);
		return;
	}
	else
	{
		if(header==NULL)
		{
			/* Connection Failed */
			((void (*)(struct UPnPService*,int,void*,char*))_InvokeData->CallbackPtr)(Service,IsInterrupt==0?-1:IsInterrupt,_InvokeData->User,INVALID_DATA);
			UPnPRelease(Service->Parent);
			FREE(_InvokeData);
			return;
		}
		else if(header->StatusCode!=200)
		{
			/* SOAP Fault */
			((void (*)(struct UPnPService*,int,void*,char*))_InvokeData->CallbackPtr)(Service,UPnPGetErrorCode(buffer,EndPointer-(*p_BeginPointer)),_InvokeData->User,INVALID_DATA);
			UPnPRelease(Service->Parent);
			FREE(_InvokeData);
			return;
		}
	}
	
	__xml = xml = ILibParseXML(buffer,0,EndPointer-(*p_BeginPointer));
	ILibProcessXMLNodeList(xml);
	while(xml!=NULL)
	{
		if(xml->NameLength==37 && memcmp(xml->Name,"GetAvailableCompressionLevelsResponse",37)==0)
		{
			xml = xml->Next;
			while(xml!=NULL)
			{
				if(xml->NameLength==29 && memcmp(xml->Name,"RetAvailableCompressionLevels",29) == 0)
				{
					tempBufferLength = ILibReadInnerXML(xml,&tempBuffer);
					--ArgLeft;
					if(tempBufferLength!=0)
					{
						tempBuffer[tempBufferLength] = '\0';
						RetAvailableCompressionLevels = tempBuffer;
					}
				}
				xml = xml->Peer;
			}
		}
		if(xml!=NULL) {xml = xml->Next;}
	}
	ILibDestructXMLNodeList(__xml);
	
	if(ArgLeft!=0)
	{
		((void (*)(struct UPnPService*,int,void*,char*))_InvokeData->CallbackPtr)(Service,-2,_InvokeData->User,INVALID_DATA);
	}
	else
	{
		((void (*)(struct UPnPService*,int,void*,char*))_InvokeData->CallbackPtr)(Service,0,_InvokeData->User,RetAvailableCompressionLevels);
	}
	UPnPRelease(Service->Parent);
	FREE(_InvokeData);
}
void UPnPInvoke_DigitalSecurityCameraMotionImage_GetAvailableCompressionLevels(struct UPnPService *service,void (*CallbackPtr)(struct UPnPService*,int,void*,char*), void* user)
{
	int headerLength;
	char *headerBuffer;
	char *SoapBodyTemplate;
	char* buffer;
	int bufferLength;
	char* IP;
	unsigned short Port;
	char* Path;
	struct sockaddr_in addr;
	struct InvokeStruct *invoke_data = (struct InvokeStruct*)MALLOC(sizeof(struct InvokeStruct));
	
	if(service==NULL)
	{
		FREE(invoke_data);
		return;
	}
	buffer = (char*)MALLOC((int)strlen(service->ServiceType)+290);
	SoapBodyTemplate = "%sGetAvailableCompressionLevels xmlns:u=\"%s\"></u:GetAvailableCompressionLevels%s";
	bufferLength = sprintf(buffer,SoapBodyTemplate,UPNPCP_SOAP_BodyHead,service->ServiceType,UPNPCP_SOAP_BodyTail);
	
	UPnPAddRef(service->Parent);
	ILibParseUri(service->ControlURL,&IP,&Port,&Path);
	
	headerBuffer = (char*)MALLOC(184 + (int)strlen(Path) + (int)strlen(IP) + (int)strlen(service->ServiceType));
	headerLength = sprintf(headerBuffer,UPNPCP_SOAP_Header,Path,IP,Port,service->ServiceType,"GetAvailableCompressionLevels",bufferLength);
	
	memset((char *)&addr, 0, sizeof(addr));
	addr.sin_family = AF_INET;
	addr.sin_addr.s_addr = inet_addr(IP);
	addr.sin_port = htons(Port);
	
	invoke_data->CallbackPtr = CallbackPtr;
	invoke_data->User = user;
	ILibAddRequest_DirectEx(((struct UPnPCP*)service->Parent->CP)->HTTP, headerBuffer,headerLength,buffer,bufferLength,&addr, &UPnPInvoke_DigitalSecurityCameraMotionImage_GetAvailableCompressionLevels_Sink,service, invoke_data);	
	
	FREE(IP);
	FREE(Path);
}
void UPnPInvoke_DigitalSecurityCameraMotionImage_GetDefaultVideoURL_Sink(void *reader, struct packetheader *header, int IsInterrupt,char* buffer, int *p_BeginPointer, int EndPointer, int done, void* _service, void *state)
{
	struct UPnPService *Service = (struct UPnPService*)_service;
	struct InvokeStruct *_InvokeData = (struct InvokeStruct*)state;
	int ArgLeft = 1;
	struct ILibXMLNode *xml;
	struct ILibXMLNode *__xml;
	char *tempBuffer;
	int tempBufferLength;
	char* RetVideoURL = NULL;
	
	if(done==0){return;}
	if(_InvokeData->CallbackPtr==NULL)
	{
		UPnPRelease(Service->Parent);
		FREE(_InvokeData);
		return;
	}
	else
	{
		if(header==NULL)
		{
			/* Connection Failed */
			((void (*)(struct UPnPService*,int,void*,char*))_InvokeData->CallbackPtr)(Service,IsInterrupt==0?-1:IsInterrupt,_InvokeData->User,INVALID_DATA);
			UPnPRelease(Service->Parent);
			FREE(_InvokeData);
			return;
		}
		else if(header->StatusCode!=200)
		{
			/* SOAP Fault */
			((void (*)(struct UPnPService*,int,void*,char*))_InvokeData->CallbackPtr)(Service,UPnPGetErrorCode(buffer,EndPointer-(*p_BeginPointer)),_InvokeData->User,INVALID_DATA);
			UPnPRelease(Service->Parent);
			FREE(_InvokeData);
			return;
		}
	}
	
	__xml = xml = ILibParseXML(buffer,0,EndPointer-(*p_BeginPointer));
	ILibProcessXMLNodeList(xml);
	while(xml!=NULL)
	{
		if(xml->NameLength==26 && memcmp(xml->Name,"GetDefaultVideoURLResponse",26)==0)
		{
			xml = xml->Next;
			while(xml!=NULL)
			{
				if(xml->NameLength==11 && memcmp(xml->Name,"RetVideoURL",11) == 0)
				{
					tempBufferLength = ILibReadInnerXML(xml,&tempBuffer);
					--ArgLeft;
					if(tempBufferLength!=0)
					{
						tempBuffer[tempBufferLength] = '\0';
						RetVideoURL = tempBuffer;
					}
				}
				xml = xml->Peer;
			}
		}
		if(xml!=NULL) {xml = xml->Next;}
	}
	ILibDestructXMLNodeList(__xml);
	
	if(ArgLeft!=0)
	{
		((void (*)(struct UPnPService*,int,void*,char*))_InvokeData->CallbackPtr)(Service,-2,_InvokeData->User,INVALID_DATA);
	}
	else
	{
		((void (*)(struct UPnPService*,int,void*,char*))_InvokeData->CallbackPtr)(Service,0,_InvokeData->User,RetVideoURL);
	}
	UPnPRelease(Service->Parent);
	FREE(_InvokeData);
}
void UPnPInvoke_DigitalSecurityCameraMotionImage_GetDefaultVideoURL(struct UPnPService *service,void (*CallbackPtr)(struct UPnPService*,int,void*,char*), void* user)
{
	int headerLength;
	char *headerBuffer;
	char *SoapBodyTemplate;
	char* buffer;
	int bufferLength;
	char* IP;
	unsigned short Port;
	char* Path;
	struct sockaddr_in addr;
	struct InvokeStruct *invoke_data = (struct InvokeStruct*)MALLOC(sizeof(struct InvokeStruct));
	
	if(service==NULL)
	{
		FREE(invoke_data);
		return;
	}
	buffer = (char*)MALLOC((int)strlen(service->ServiceType)+268);
	SoapBodyTemplate = "%sGetDefaultVideoURL xmlns:u=\"%s\"></u:GetDefaultVideoURL%s";
	bufferLength = sprintf(buffer,SoapBodyTemplate,UPNPCP_SOAP_BodyHead,service->ServiceType,UPNPCP_SOAP_BodyTail);
	
	UPnPAddRef(service->Parent);
	ILibParseUri(service->ControlURL,&IP,&Port,&Path);
	
	headerBuffer = (char*)MALLOC(173 + (int)strlen(Path) + (int)strlen(IP) + (int)strlen(service->ServiceType));
	headerLength = sprintf(headerBuffer,UPNPCP_SOAP_Header,Path,IP,Port,service->ServiceType,"GetDefaultVideoURL",bufferLength);
	
	memset((char *)&addr, 0, sizeof(addr));
	addr.sin_family = AF_INET;
	addr.sin_addr.s_addr = inet_addr(IP);
	addr.sin_port = htons(Port);
	
	invoke_data->CallbackPtr = CallbackPtr;
	invoke_data->User = user;
	ILibAddRequest_DirectEx(((struct UPnPCP*)service->Parent->CP)->HTTP, headerBuffer,headerLength,buffer,bufferLength,&addr, &UPnPInvoke_DigitalSecurityCameraMotionImage_GetDefaultVideoURL_Sink,service, invoke_data);	
	
	FREE(IP);
	FREE(Path);
}
void UPnPInvoke_DigitalSecurityCameraMotionImage_GetDefaultEncoding_Sink(void *reader, struct packetheader *header, int IsInterrupt,char* buffer, int *p_BeginPointer, int EndPointer, int done, void* _service, void *state)
{
	struct UPnPService *Service = (struct UPnPService*)_service;
	struct InvokeStruct *_InvokeData = (struct InvokeStruct*)state;
	int ArgLeft = 1;
	struct ILibXMLNode *xml;
	struct ILibXMLNode *__xml;
	char *tempBuffer;
	int tempBufferLength;
	char* RetEncoding = NULL;
	
	if(done==0){return;}
	if(_InvokeData->CallbackPtr==NULL)
	{
		UPnPRelease(Service->Parent);
		FREE(_InvokeData);
		return;
	}
	else
	{
		if(header==NULL)
		{
			/* Connection Failed */
			((void (*)(struct UPnPService*,int,void*,char*))_InvokeData->CallbackPtr)(Service,IsInterrupt==0?-1:IsInterrupt,_InvokeData->User,INVALID_DATA);
			UPnPRelease(Service->Parent);
			FREE(_InvokeData);
			return;
		}
		else if(header->StatusCode!=200)
		{
			/* SOAP Fault */
			((void (*)(struct UPnPService*,int,void*,char*))_InvokeData->CallbackPtr)(Service,UPnPGetErrorCode(buffer,EndPointer-(*p_BeginPointer)),_InvokeData->User,INVALID_DATA);
			UPnPRelease(Service->Parent);
			FREE(_InvokeData);
			return;
		}
	}
	
	__xml = xml = ILibParseXML(buffer,0,EndPointer-(*p_BeginPointer));
	ILibProcessXMLNodeList(xml);
	while(xml!=NULL)
	{
		if(xml->NameLength==26 && memcmp(xml->Name,"GetDefaultEncodingResponse",26)==0)
		{
			xml = xml->Next;
			while(xml!=NULL)
			{
				if(xml->NameLength==11 && memcmp(xml->Name,"RetEncoding",11) == 0)
				{
					tempBufferLength = ILibReadInnerXML(xml,&tempBuffer);
					--ArgLeft;
					if(tempBufferLength!=0)
					{
						tempBuffer[tempBufferLength] = '\0';
						RetEncoding = tempBuffer;
					}
				}
				xml = xml->Peer;
			}
		}
		if(xml!=NULL) {xml = xml->Next;}
	}
	ILibDestructXMLNodeList(__xml);
	
	if(ArgLeft!=0)
	{
		((void (*)(struct UPnPService*,int,void*,char*))_InvokeData->CallbackPtr)(Service,-2,_InvokeData->User,INVALID_DATA);
	}
	else
	{
		((void (*)(struct UPnPService*,int,void*,char*))_InvokeData->CallbackPtr)(Service,0,_InvokeData->User,RetEncoding);
	}
	UPnPRelease(Service->Parent);
	FREE(_InvokeData);
}
void UPnPInvoke_DigitalSecurityCameraMotionImage_GetDefaultEncoding(struct UPnPService *service,void (*CallbackPtr)(struct UPnPService*,int,void*,char*), void* user)
{
	int headerLength;
	char *headerBuffer;
	char *SoapBodyTemplate;
	char* buffer;
	int bufferLength;
	char* IP;
	unsigned short Port;
	char* Path;
	struct sockaddr_in addr;
	struct InvokeStruct *invoke_data = (struct InvokeStruct*)MALLOC(sizeof(struct InvokeStruct));
	
	if(service==NULL)
	{
		FREE(invoke_data);
		return;
	}
	buffer = (char*)MALLOC((int)strlen(service->ServiceType)+268);
	SoapBodyTemplate = "%sGetDefaultEncoding xmlns:u=\"%s\"></u:GetDefaultEncoding%s";
	bufferLength = sprintf(buffer,SoapBodyTemplate,UPNPCP_SOAP_BodyHead,service->ServiceType,UPNPCP_SOAP_BodyTail);
	
	UPnPAddRef(service->Parent);
	ILibParseUri(service->ControlURL,&IP,&Port,&Path);
	
	headerBuffer = (char*)MALLOC(173 + (int)strlen(Path) + (int)strlen(IP) + (int)strlen(service->ServiceType));
	headerLength = sprintf(headerBuffer,UPNPCP_SOAP_Header,Path,IP,Port,service->ServiceType,"GetDefaultEncoding",bufferLength);
	
	memset((char *)&addr, 0, sizeof(addr));
	addr.sin_family = AF_INET;
	addr.sin_addr.s_addr = inet_addr(IP);
	addr.sin_port = htons(Port);
	
	invoke_data->CallbackPtr = CallbackPtr;
	invoke_data->User = user;
	ILibAddRequest_DirectEx(((struct UPnPCP*)service->Parent->CP)->HTTP, headerBuffer,headerLength,buffer,bufferLength,&addr, &UPnPInvoke_DigitalSecurityCameraMotionImage_GetDefaultEncoding_Sink,service, invoke_data);	
	
	FREE(IP);
	FREE(Path);
}
void UPnPInvoke_DigitalSecurityCameraMotionImage_SetDefaultResolution_Sink(void *reader, struct packetheader *header, int IsInterrupt,char* buffer, int *p_BeginPointer, int EndPointer, int done, void* _service, void *state)
{
	struct UPnPService *Service = (struct UPnPService*)_service;
	struct InvokeStruct *_InvokeData = (struct InvokeStruct*)state;
	if(done==0){return;}
	if(_InvokeData->CallbackPtr==NULL)
	{
		UPnPRelease(Service->Parent);
		FREE(_InvokeData);
		return;
	}
	else
	{
		if(header==NULL)
		{
			/* Connection Failed */
			((void (*)(struct UPnPService*,int,void*))_InvokeData->CallbackPtr)(Service,-1,_InvokeData->User);
			UPnPRelease(Service->Parent);
			FREE(_InvokeData);
			return;
		}
		else if(header->StatusCode!=200)
		{
			/* SOAP Fault */
			((void (*)(struct UPnPService*,int,void*))_InvokeData->CallbackPtr)(Service,UPnPGetErrorCode(buffer,EndPointer-(*p_BeginPointer)),_InvokeData->User);
			UPnPRelease(Service->Parent);
			FREE(_InvokeData);
			return;
		}
	}
	
	((void (*)(struct UPnPService*,int,void*))_InvokeData->CallbackPtr)(Service,0,_InvokeData->User);
	UPnPRelease(Service->Parent);
	FREE(_InvokeData);
}
void UPnPInvoke_DigitalSecurityCameraMotionImage_SetDefaultResolution(struct UPnPService *service,void (*CallbackPtr)(struct UPnPService*,int,void*), void* user, char* ReqResolution)
{
	int headerLength;
	char *headerBuffer;
	char *SoapBodyTemplate;
	char* buffer;
	int bufferLength;
	char* IP;
	unsigned short Port;
	char* Path;
	struct sockaddr_in addr;
	struct InvokeStruct *invoke_data = (struct InvokeStruct*)MALLOC(sizeof(struct InvokeStruct));
	
	if(service==NULL)
	{
		FREE(invoke_data);
		return;
	}
	buffer = (char*)MALLOC((int)strlen(service->ServiceType)+(int)strlen(ReqResolution)+303);
	SoapBodyTemplate = "%sSetDefaultResolution xmlns:u=\"%s\"><ReqResolution>%s</ReqResolution></u:SetDefaultResolution%s";
	bufferLength = sprintf(buffer,SoapBodyTemplate,UPNPCP_SOAP_BodyHead,service->ServiceType, ReqResolution,UPNPCP_SOAP_BodyTail);
	
	UPnPAddRef(service->Parent);
	ILibParseUri(service->ControlURL,&IP,&Port,&Path);
	
	headerBuffer = (char*)MALLOC(175 + (int)strlen(Path) + (int)strlen(IP) + (int)strlen(service->ServiceType));
	headerLength = sprintf(headerBuffer,UPNPCP_SOAP_Header,Path,IP,Port,service->ServiceType,"SetDefaultResolution",bufferLength);
	
	memset((char *)&addr, 0, sizeof(addr));
	addr.sin_family = AF_INET;
	addr.sin_addr.s_addr = inet_addr(IP);
	addr.sin_port = htons(Port);
	
	invoke_data->CallbackPtr = CallbackPtr;
	invoke_data->User = user;
	ILibAddRequest_DirectEx(((struct UPnPCP*)service->Parent->CP)->HTTP, headerBuffer,headerLength,buffer,bufferLength,&addr, &UPnPInvoke_DigitalSecurityCameraMotionImage_SetDefaultResolution_Sink,service, invoke_data);	
	
	FREE(IP);
	FREE(Path);
}
void UPnPInvoke_DigitalSecurityCameraMotionImage_GetDefaultCompressionLevel_Sink(void *reader, struct packetheader *header, int IsInterrupt,char* buffer, int *p_BeginPointer, int EndPointer, int done, void* _service, void *state)
{
	struct UPnPService *Service = (struct UPnPService*)_service;
	struct InvokeStruct *_InvokeData = (struct InvokeStruct*)state;
	int ArgLeft = 1;
	struct ILibXMLNode *xml;
	struct ILibXMLNode *__xml;
	char *tempBuffer;
	int tempBufferLength;
	char* RetCompressionLevel = NULL;
	
	if(done==0){return;}
	if(_InvokeData->CallbackPtr==NULL)
	{
		UPnPRelease(Service->Parent);
		FREE(_InvokeData);
		return;
	}
	else
	{
		if(header==NULL)
		{
			/* Connection Failed */
			((void (*)(struct UPnPService*,int,void*,char*))_InvokeData->CallbackPtr)(Service,IsInterrupt==0?-1:IsInterrupt,_InvokeData->User,INVALID_DATA);
			UPnPRelease(Service->Parent);
			FREE(_InvokeData);
			return;
		}
		else if(header->StatusCode!=200)
		{
			/* SOAP Fault */
			((void (*)(struct UPnPService*,int,void*,char*))_InvokeData->CallbackPtr)(Service,UPnPGetErrorCode(buffer,EndPointer-(*p_BeginPointer)),_InvokeData->User,INVALID_DATA);
			UPnPRelease(Service->Parent);
			FREE(_InvokeData);
			return;
		}
	}
	
	__xml = xml = ILibParseXML(buffer,0,EndPointer-(*p_BeginPointer));
	ILibProcessXMLNodeList(xml);
	while(xml!=NULL)
	{
		if(xml->NameLength==34 && memcmp(xml->Name,"GetDefaultCompressionLevelResponse",34)==0)
		{
			xml = xml->Next;
			while(xml!=NULL)
			{
				if(xml->NameLength==19 && memcmp(xml->Name,"RetCompressionLevel",19) == 0)
				{
					tempBufferLength = ILibReadInnerXML(xml,&tempBuffer);
					--ArgLeft;
					if(tempBufferLength!=0)
					{
						tempBuffer[tempBufferLength] = '\0';
						RetCompressionLevel = tempBuffer;
					}
				}
				xml = xml->Peer;
			}
		}
		if(xml!=NULL) {xml = xml->Next;}
	}
	ILibDestructXMLNodeList(__xml);
	
	if(ArgLeft!=0)
	{
		((void (*)(struct UPnPService*,int,void*,char*))_InvokeData->CallbackPtr)(Service,-2,_InvokeData->User,INVALID_DATA);
	}
	else
	{
		((void (*)(struct UPnPService*,int,void*,char*))_InvokeData->CallbackPtr)(Service,0,_InvokeData->User,RetCompressionLevel);
	}
	UPnPRelease(Service->Parent);
	FREE(_InvokeData);
}
void UPnPInvoke_DigitalSecurityCameraMotionImage_GetDefaultCompressionLevel(struct UPnPService *service,void (*CallbackPtr)(struct UPnPService*,int,void*,char*), void* user)
{
	int headerLength;
	char *headerBuffer;
	char *SoapBodyTemplate;
	char* buffer;
	int bufferLength;
	char* IP;
	unsigned short Port;
	char* Path;
	struct sockaddr_in addr;
	struct InvokeStruct *invoke_data = (struct InvokeStruct*)MALLOC(sizeof(struct InvokeStruct));
	
	if(service==NULL)
	{
		FREE(invoke_data);
		return;
	}
	buffer = (char*)MALLOC((int)strlen(service->ServiceType)+284);
	SoapBodyTemplate = "%sGetDefaultCompressionLevel xmlns:u=\"%s\"></u:GetDefaultCompressionLevel%s";
	bufferLength = sprintf(buffer,SoapBodyTemplate,UPNPCP_SOAP_BodyHead,service->ServiceType,UPNPCP_SOAP_BodyTail);
	
	UPnPAddRef(service->Parent);
	ILibParseUri(service->ControlURL,&IP,&Port,&Path);
	
	headerBuffer = (char*)MALLOC(181 + (int)strlen(Path) + (int)strlen(IP) + (int)strlen(service->ServiceType));
	headerLength = sprintf(headerBuffer,UPNPCP_SOAP_Header,Path,IP,Port,service->ServiceType,"GetDefaultCompressionLevel",bufferLength);
	
	memset((char *)&addr, 0, sizeof(addr));
	addr.sin_family = AF_INET;
	addr.sin_addr.s_addr = inet_addr(IP);
	addr.sin_port = htons(Port);
	
	invoke_data->CallbackPtr = CallbackPtr;
	invoke_data->User = user;
	ILibAddRequest_DirectEx(((struct UPnPCP*)service->Parent->CP)->HTTP, headerBuffer,headerLength,buffer,bufferLength,&addr, &UPnPInvoke_DigitalSecurityCameraMotionImage_GetDefaultCompressionLevel_Sink,service, invoke_data);	
	
	FREE(IP);
	FREE(Path);
}
void UPnPInvoke_DigitalSecurityCameraMotionImage_GetVideoPresentationURL_Sink(void *reader, struct packetheader *header, int IsInterrupt,char* buffer, int *p_BeginPointer, int EndPointer, int done, void* _service, void *state)
{
	struct UPnPService *Service = (struct UPnPService*)_service;
	struct InvokeStruct *_InvokeData = (struct InvokeStruct*)state;
	int ArgLeft = 1;
	struct ILibXMLNode *xml;
	struct ILibXMLNode *__xml;
	char *tempBuffer;
	int tempBufferLength;
	char* RetVideoPresentationURL = NULL;
	
	if(done==0){return;}
	if(_InvokeData->CallbackPtr==NULL)
	{
		UPnPRelease(Service->Parent);
		FREE(_InvokeData);
		return;
	}
	else
	{
		if(header==NULL)
		{
			/* Connection Failed */
			((void (*)(struct UPnPService*,int,void*,char*))_InvokeData->CallbackPtr)(Service,IsInterrupt==0?-1:IsInterrupt,_InvokeData->User,INVALID_DATA);
			UPnPRelease(Service->Parent);
			FREE(_InvokeData);
			return;
		}
		else if(header->StatusCode!=200)
		{
			/* SOAP Fault */
			((void (*)(struct UPnPService*,int,void*,char*))_InvokeData->CallbackPtr)(Service,UPnPGetErrorCode(buffer,EndPointer-(*p_BeginPointer)),_InvokeData->User,INVALID_DATA);
			UPnPRelease(Service->Parent);
			FREE(_InvokeData);
			return;
		}
	}
	
	__xml = xml = ILibParseXML(buffer,0,EndPointer-(*p_BeginPointer));
	ILibProcessXMLNodeList(xml);
	while(xml!=NULL)
	{
		if(xml->NameLength==31 && memcmp(xml->Name,"GetVideoPresentationURLResponse",31)==0)
		{
			xml = xml->Next;
			while(xml!=NULL)
			{
				if(xml->NameLength==23 && memcmp(xml->Name,"RetVideoPresentationURL",23) == 0)
				{
					tempBufferLength = ILibReadInnerXML(xml,&tempBuffer);
					--ArgLeft;
					if(tempBufferLength!=0)
					{
						tempBuffer[tempBufferLength] = '\0';
						RetVideoPresentationURL = tempBuffer;
					}
				}
				xml = xml->Peer;
			}
		}
		if(xml!=NULL) {xml = xml->Next;}
	}
	ILibDestructXMLNodeList(__xml);
	
	if(ArgLeft!=0)
	{
		((void (*)(struct UPnPService*,int,void*,char*))_InvokeData->CallbackPtr)(Service,-2,_InvokeData->User,INVALID_DATA);
	}
	else
	{
		((void (*)(struct UPnPService*,int,void*,char*))_InvokeData->CallbackPtr)(Service,0,_InvokeData->User,RetVideoPresentationURL);
	}
	UPnPRelease(Service->Parent);
	FREE(_InvokeData);
}
void UPnPInvoke_DigitalSecurityCameraMotionImage_GetVideoPresentationURL(struct UPnPService *service,void (*CallbackPtr)(struct UPnPService*,int,void*,char*), void* user, char* ReqEncoding, char* ReqCompression, char* ReqResolution)
{
	int headerLength;
	char *headerBuffer;
	char *SoapBodyTemplate;
	char* buffer;
	int bufferLength;
	char* IP;
	unsigned short Port;
	char* Path;
	struct sockaddr_in addr;
	struct InvokeStruct *invoke_data = (struct InvokeStruct*)MALLOC(sizeof(struct InvokeStruct));
	
	if(service==NULL)
	{
		FREE(invoke_data);
		return;
	}
	buffer = (char*)MALLOC((int)strlen(service->ServiceType)+(int)strlen(ReqEncoding)+(int)strlen(ReqCompression)+(int)strlen(ReqResolution)+369);
	SoapBodyTemplate = "%sGetVideoPresentationURL xmlns:u=\"%s\"><ReqEncoding>%s</ReqEncoding><ReqCompression>%s</ReqCompression><ReqResolution>%s</ReqResolution></u:GetVideoPresentationURL%s";
	bufferLength = sprintf(buffer,SoapBodyTemplate,UPNPCP_SOAP_BodyHead,service->ServiceType, ReqEncoding, ReqCompression, ReqResolution,UPNPCP_SOAP_BodyTail);
	
	UPnPAddRef(service->Parent);
	ILibParseUri(service->ControlURL,&IP,&Port,&Path);
	
	headerBuffer = (char*)MALLOC(178 + (int)strlen(Path) + (int)strlen(IP) + (int)strlen(service->ServiceType));
	headerLength = sprintf(headerBuffer,UPNPCP_SOAP_Header,Path,IP,Port,service->ServiceType,"GetVideoPresentationURL",bufferLength);
	
	memset((char *)&addr, 0, sizeof(addr));
	addr.sin_family = AF_INET;
	addr.sin_addr.s_addr = inet_addr(IP);
	addr.sin_port = htons(Port);
	
	invoke_data->CallbackPtr = CallbackPtr;
	invoke_data->User = user;
	ILibAddRequest_DirectEx(((struct UPnPCP*)service->Parent->CP)->HTTP, headerBuffer,headerLength,buffer,bufferLength,&addr, &UPnPInvoke_DigitalSecurityCameraMotionImage_GetVideoPresentationURL_Sink,service, invoke_data);	
	
	FREE(IP);
	FREE(Path);
}
void UPnPInvoke_DigitalSecurityCameraMotionImage_GetDefaultResolution_Sink(void *reader, struct packetheader *header, int IsInterrupt,char* buffer, int *p_BeginPointer, int EndPointer, int done, void* _service, void *state)
{
	struct UPnPService *Service = (struct UPnPService*)_service;
	struct InvokeStruct *_InvokeData = (struct InvokeStruct*)state;
	int ArgLeft = 1;
	struct ILibXMLNode *xml;
	struct ILibXMLNode *__xml;
	char *tempBuffer;
	int tempBufferLength;
	char* RetResolution = NULL;
	
	if(done==0){return;}
	if(_InvokeData->CallbackPtr==NULL)
	{
		UPnPRelease(Service->Parent);
		FREE(_InvokeData);
		return;
	}
	else
	{
		if(header==NULL)
		{
			/* Connection Failed */
			((void (*)(struct UPnPService*,int,void*,char*))_InvokeData->CallbackPtr)(Service,IsInterrupt==0?-1:IsInterrupt,_InvokeData->User,INVALID_DATA);
			UPnPRelease(Service->Parent);
			FREE(_InvokeData);
			return;
		}
		else if(header->StatusCode!=200)
		{
			/* SOAP Fault */
			((void (*)(struct UPnPService*,int,void*,char*))_InvokeData->CallbackPtr)(Service,UPnPGetErrorCode(buffer,EndPointer-(*p_BeginPointer)),_InvokeData->User,INVALID_DATA);
			UPnPRelease(Service->Parent);
			FREE(_InvokeData);
			return;
		}
	}
	
	__xml = xml = ILibParseXML(buffer,0,EndPointer-(*p_BeginPointer));
	ILibProcessXMLNodeList(xml);
	while(xml!=NULL)
	{
		if(xml->NameLength==28 && memcmp(xml->Name,"GetDefaultResolutionResponse",28)==0)
		{
			xml = xml->Next;
			while(xml!=NULL)
			{
				if(xml->NameLength==13 && memcmp(xml->Name,"RetResolution",13) == 0)
				{
					tempBufferLength = ILibReadInnerXML(xml,&tempBuffer);
					--ArgLeft;
					if(tempBufferLength!=0)
					{
						tempBuffer[tempBufferLength] = '\0';
						RetResolution = tempBuffer;
					}
				}
				xml = xml->Peer;
			}
		}
		if(xml!=NULL) {xml = xml->Next;}
	}
	ILibDestructXMLNodeList(__xml);
	
	if(ArgLeft!=0)
	{
		((void (*)(struct UPnPService*,int,void*,char*))_InvokeData->CallbackPtr)(Service,-2,_InvokeData->User,INVALID_DATA);
	}
	else
	{
		((void (*)(struct UPnPService*,int,void*,char*))_InvokeData->CallbackPtr)(Service,0,_InvokeData->User,RetResolution);
	}
	UPnPRelease(Service->Parent);
	FREE(_InvokeData);
}
void UPnPInvoke_DigitalSecurityCameraMotionImage_GetDefaultResolution(struct UPnPService *service,void (*CallbackPtr)(struct UPnPService*,int,void*,char*), void* user)
{
	int headerLength;
	char *headerBuffer;
	char *SoapBodyTemplate;
	char* buffer;
	int bufferLength;
	char* IP;
	unsigned short Port;
	char* Path;
	struct sockaddr_in addr;
	struct InvokeStruct *invoke_data = (struct InvokeStruct*)MALLOC(sizeof(struct InvokeStruct));
	
	if(service==NULL)
	{
		FREE(invoke_data);
		return;
	}
	buffer = (char*)MALLOC((int)strlen(service->ServiceType)+272);
	SoapBodyTemplate = "%sGetDefaultResolution xmlns:u=\"%s\"></u:GetDefaultResolution%s";
	bufferLength = sprintf(buffer,SoapBodyTemplate,UPNPCP_SOAP_BodyHead,service->ServiceType,UPNPCP_SOAP_BodyTail);
	
	UPnPAddRef(service->Parent);
	ILibParseUri(service->ControlURL,&IP,&Port,&Path);
	
	headerBuffer = (char*)MALLOC(175 + (int)strlen(Path) + (int)strlen(IP) + (int)strlen(service->ServiceType));
	headerLength = sprintf(headerBuffer,UPNPCP_SOAP_Header,Path,IP,Port,service->ServiceType,"GetDefaultResolution",bufferLength);
	
	memset((char *)&addr, 0, sizeof(addr));
	addr.sin_family = AF_INET;
	addr.sin_addr.s_addr = inet_addr(IP);
	addr.sin_port = htons(Port);
	
	invoke_data->CallbackPtr = CallbackPtr;
	invoke_data->User = user;
	ILibAddRequest_DirectEx(((struct UPnPCP*)service->Parent->CP)->HTTP, headerBuffer,headerLength,buffer,bufferLength,&addr, &UPnPInvoke_DigitalSecurityCameraMotionImage_GetDefaultResolution_Sink,service, invoke_data);	
	
	FREE(IP);
	FREE(Path);
}
void UPnPSubscribeForUPnPEvents(struct UPnPService *service, void(*callbackPtr)(struct UPnPService* service,int OK))
{
	char* callback;
	char *IP;
	char *Path;
	unsigned short Port;
	struct packetheader *p;
	char* TempString;
	struct sockaddr_in destaddr;
	
	ILibParseUri(service->SubscriptionURL,&IP,&Port,&Path);
	p = ILibCreateEmptyPacket();
	
	ILibSetDirective(p,"SUBSCRIBE",9,Path,(int)strlen(Path));
	
	TempString = (char*)MALLOC((int)strlen(IP)+7);
	sprintf(TempString,"%s:%d",IP,Port);
	
	ILibAddHeaderLine(p,"HOST",4,TempString,(int)strlen(TempString));
	FREE(TempString);
	
	ILibAddHeaderLine(p,"NT",2,"upnp:event",10);
	ILibAddHeaderLine(p,"TIMEOUT",7,"Second-180",10);
	ILibAddHeaderLine(p,"User-Agent",10,"WINDOWS, UPnP/1.0, Intel MicroStack/1.0.1200",44);
	
	callback = (char*)MALLOC(10+(int)strlen(service->Parent->InterfaceToHost)+6+(int)strlen(Path));
	sprintf(callback,"<http://%s:%d%s>",service->Parent->InterfaceToHost,ILibGetMiniWebServerPortNumber(((struct UPnPCP*)service->Parent->CP)->WebServer),Path);
	
	ILibAddHeaderLine(p,"CALLBACK",8,callback,(int)strlen(callback));
	FREE(callback);
	
	memset((char *)&destaddr, 0,sizeof(destaddr));
	destaddr.sin_family = AF_INET;
	destaddr.sin_addr.s_addr = inet_addr(IP);
	destaddr.sin_port = htons(Port);
	
	UPnPAddRef(service->Parent);
	ILibAddRequest(((struct UPnPCP*)service->Parent->CP)->HTTP, p,&destaddr, &UPnPOnSubscribeSink,(void*)service,service->Parent->CP);
	
	FREE(IP);
	FREE(Path);
}
