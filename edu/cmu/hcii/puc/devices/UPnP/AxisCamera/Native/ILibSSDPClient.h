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
* $Workfile: ILibSSDPClient.h
* $Revision: #1.0.1200.32486
* $Author:   Intel Corporation, Intel Device Builder
* $Date:     Thursday, June 12, 2003
*
*/
#ifndef __ILibSSDPClient__
#define __ILibSSDPClient__

#define gettimeofday(x,y) (x)->tv_sec = GetTickCount()/1000


void* ILibCreateSSDPClientModule(void *chain, char* DeviceURN, int DeviceURNLength, void (*FunctionCallback)(void *sender, char* UDN, int Alive, char* LocationURL, int Timeout, void *user),void *user);
void ILibSSDP_IPAddressListChanged(void *SSDPToken);

#endif
