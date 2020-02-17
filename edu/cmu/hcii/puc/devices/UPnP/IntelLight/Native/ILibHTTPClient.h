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
* $Workfile: ILibHTTPClient.h
* $Revision: #1.0.1200.32486
* $Author:   Intel Corporation, Intel Device Builder
* $Date:     Friday, June 06, 2003
*
*/
#ifndef __ILibHTTPClient__
#define __ILibHTTPClient__

#define HTTP_SESSION_INTERRUPT_CHAIN 1
#define HTTP_SESSION_INTERRUPT_PEERRESET 2
#define HTTP_INTERRUPT_CHAIN 3
#define HTTP_DELETEREQUEST_INTERRUPT 4
#define ILibAddRequest_Direct(ClientModule, buffer, bufferlength,Destination, CallbackPtr, user, user2) ILibAddRequest_DirectEx(ClientModule, buffer, bufferlength,NULL,0,Destination, CallbackPtr, user, user2)

/* Forward Declaration */
struct packetheader;

void* ILibCreateHTTPClientModule(void *Chain, int MaxSockets);
void  ILibDestroyHTTPClientModule(void *ClientModule);

char* ILibGetReceivingInterface(void* ReaderObject);
void  ILibAddRequest(void *ClientModule, struct packetheader *packet,struct sockaddr_in *Destination, void (*CallbackPtr)(void *reader, struct packetheader *header, int IsInterrupt,char* buffer, int *p_BeginPointer, int EndPointer, int done, void* user, void* user2), void* user, void* user2);
void  ILibAddRequest_DirectEx(void *ClientModule, char *buffer, int bufferlength,char *buffer2, int buffer2length,struct sockaddr_in *Destination, void (*CallbackPtr)(void *reader, struct packetheader *header, int IsInterrupt, char* buffer, int *p_BeginPointer, int EndPointer, int done, void* user, void* user2), void* user, void* user2);
void  ILibCloseRequest(void* ReaderObject);
void  ILibDeleteRequests(void *ClientModule, void *user1);

#endif
