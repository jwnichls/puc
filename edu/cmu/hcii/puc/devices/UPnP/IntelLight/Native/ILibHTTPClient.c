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
* $Workfile: ILibHTTPClient.c
* $Revision: #1.0.1200.32486
* $Author:   Intel Corporation, Intel Device Builder
* $Date:     Friday, June 06, 2003
*
*/
#ifndef MICROSTACK_NO_STDAFX
#include "stdafx.h"
#endif
#include <math.h>
#include <winerror.h>
#include <stdlib.h>
#include <stdio.h>
#include <stddef.h>
#include <string.h>
#include <malloc.h>
#include <winsock.h>
#include <wininet.h>
#include <windows.h>
#include <winioctl.h>
#include <winbase.h>

#include "ILibHTTPClient.h"
#include "ILibParsers.h"
#define strncasecmp(x,y,z) _strnicmp(x,y,z)
#define gettimeofday(x,y) (x)->tv_sec = GetTickCount()/1000
#define sem_t HANDLE
#define sem_init(x,y,z) *x=CreateSemaphore(NULL,z,FD_SETSIZE,NULL)
#define sem_destroy(x) (CloseHandle(*x)==0?1:0)
#define sem_wait(x) WaitForSingleObject(*x,INFINITE)
#define sem_trywait(x) ((WaitForSingleObject(*x,0)==WAIT_OBJECT_0)?0:1)
#define sem_post(x) ReleaseSemaphore(*x,1,NULL)
#define DEBUGSTATEMENT(x)
#define LVL3DEBUG(x)

struct RequestQueueNode
{
	char* Request;
	char* Request2;
	int RequestLength;
	int Request2Length;
	struct sockaddr_in Destination;
	void* user;
	void* user2;
	void (*FunctionCallback) (void *sender, struct packetheader *header, int IsInterrupt, char* buffer, int *p_BeginPointer, int EndPointer, int done, void* user, void* user2);
	struct RequestQueueNode* Next;
	struct RequestQueueNode* Previous;
};

struct ILibHCHTTPReaderObject
{
	struct packetheader* PacketHeader;
	char Header[2048];
	char* Body;
	int BodySize;
	int HeaderIndex;
	int LocalIPAddress;
	
	int Body_BeginPointer;
	int Body_EndPointer;
	int Body_MallocSize;
	int Body_Read;
	
	SOCKET ClientSocket;
	int FinRead;
	int FinConnect;
	struct HTTPClientModule *Parent;
	void* user;
	void* user2;
	void (*FunctionCallback) (struct ILibHCHTTPReaderObject *ReaderObject, struct packetheader *header, int IsInterrupt, char* buffer, int *BeginPointer, int BufferSize, int done, void* user, void* user2);
	char* send_data;
	char* send_data2;
	int send_dataLength;
	int send_data2Length;
};

struct HTTPClientModule
{
	void (*PreSelect)(void* object,fd_set *readset, fd_set *writeset, fd_set *errorset, int* blocktime);
	void (*PostSelect)(void* object,int slct, fd_set *readset, fd_set *writeset, fd_set *errorset);
	void (*Destroy)(void* object);
	void *Chain;
	sem_t QueueLock;
	sem_t Monitor;
	int RequestQueueCount;
	struct RequestQueueNode* First;
	int NumSlots;
	struct ILibHCHTTPReaderObject *Readers;
	int Terminate;
	void *Reserved;
	
	void *SocketTimer;
	
	LVL3DEBUG(int ADD_COUNTER;)
	LVL3DEBUG(int FAILED_COUNTER;)
	LVL3DEBUG(int GRACEFUL_COUNTER;)
	LVL3DEBUG(int FORCE_COUNTER;)
	LVL3DEBUG(int START_COUNTER;)
	LVL3DEBUG(int CLOSE_COUNTER;)
	LVL3DEBUG(int SEND_FAIL;)
	LVL3DEBUG(int SEND_FAIL2;)
	LVL3DEBUG(int CONNECT_COUNTER;)
	
};


char* ILibGetReceivingInterface(void* ReaderObject)
{
	char* RetVal = (char*)MALLOC(16);
	int addr = ((struct ILibHCHTTPReaderObject*)ReaderObject)->LocalIPAddress;
	sprintf(RetVal,"%d.%d.%d.%d",(addr&0xFF),((addr>>8)&0xFF),((addr>>16)&0xFF),((addr>>24)&0xFF));
	return(RetVal);
}
void ILibDestroyHTTPClientModule(void *ClientModule)
{
	struct HTTPClientModule* module = (struct HTTPClientModule*)ClientModule;
	int i;
	struct RequestQueueNode *rqn,*rqn2;
	
	LVL3DEBUG(printf("\r\n\r\nAdd:%d Failed:%d GC:%d Forced:%d Started:%d UserClose:%d SF:%d SF2:%d",module->ADD_COUNTER,module->FAILED_COUNTER,module->GRACEFUL_COUNTER,module->FORCE_COUNTER,module->START_COUNTER,module->CLOSE_COUNTER,module->SEND_FAIL,module->SEND_FAIL2);)
	LVL3DEBUG(printf("\r\nConnected: %d \r\n",module->CONNECT_COUNTER);)
	for(i=0;i<module->NumSlots;++i)
	{
		LVL3DEBUG(printf("Slot: %d	Socket: %d\r\n",i,module->Readers[i].ClientSocket);)
		
		if(module->Readers[i].Body_MallocSize!=0)
		{
			FREE(module->Readers[i].Body);
		}
		if(module->Readers[i].send_data!=NULL)
		{
			FREE(module->Readers[i].send_data);
		}
		if(module->Readers[i].ClientSocket!=~0)
		{
			closesocket(module->Readers[i].ClientSocket);
		}
		if(module->Readers[i].PacketHeader!=NULL)
		{
			ILibDestructPacket(module->Readers[i].PacketHeader);
		}
		if(module->Readers[i].FunctionCallback!=NULL && (module->Readers[i].FinConnect==0 || (module->Readers[i].FinConnect==1 && module->Readers[i].FinRead==0)))
		{
			module->Readers[i].FunctionCallback(&(module->Readers[i]),NULL,HTTP_SESSION_INTERRUPT_CHAIN,NULL,NULL,0,-1,module->Readers[i].user,module->Readers[i].user2);
		}
	}
	
	rqn = module->First;
	while(rqn!=NULL)
	{
		if(rqn->Request!=NULL) {FREE(rqn->Request);}
		if(rqn->Request2!=NULL) {FREE(rqn->Request2);}
		if(rqn->FunctionCallback!=NULL) {rqn->FunctionCallback(module,NULL,HTTP_INTERRUPT_CHAIN,NULL,NULL,0,-1,rqn->user,rqn->user2);}
		rqn2 = rqn->Next;
		FREE(rqn);
		rqn = rqn2;
	}
	
	sem_destroy(&(module->Monitor));
	sem_destroy(&(module->QueueLock));
	FREE(module->Readers);
}

void ILibStartRequest(void *ClientModule, struct RequestQueueNode *Request)
{
	int i=0;
	unsigned long flags;
	struct HTTPClientModule *module = (struct HTTPClientModule*)ClientModule;
	
	for(i=0;i<module->NumSlots;++i)
	{
		if(module->Readers[i].ClientSocket==~0)
		{
			LVL3DEBUG(++module->START_COUNTER;)
			if(module->Readers[i].PacketHeader!=NULL)
			{
				ILibDestructPacket(module->Readers[i].PacketHeader);
				module->Readers[i].PacketHeader = NULL;
			}
			module->Readers[i].FunctionCallback = (void*)Request->FunctionCallback;
			module->Readers[i].send_data = Request->Request;
			module->Readers[i].send_dataLength = Request->RequestLength;
			module->Readers[i].send_data2 = Request->Request2;
			module->Readers[i].send_data2Length = Request->Request2Length;
			module->Readers[i].FinRead = 0;
			module->Readers[i].FinConnect = 0;
			module->Readers[i].Body = NULL;
			module->Readers[i].BodySize = 0;
			module->Readers[i].HeaderIndex = 0;
			module->Readers[i].Body_Read = 0;
			module->Readers[i].Body_BeginPointer = 0;
			module->Readers[i].Body_EndPointer = 0;
			module->Readers[i].user = Request->user;
			module->Readers[i].user2 = Request->user2;
			ILibGetStreamSocket(htonl(INADDR_ANY),0,&(module->Readers[i].ClientSocket));
			
			/* Platform Dependent [Windows] */
			flags = 1;
			ioctlsocket(module->Readers[i].ClientSocket,FIONBIO,&flags);
			
			connect(module->Readers[i].ClientSocket,(struct sockaddr*)&(Request->Destination),sizeof(Request->Destination));	
			break;
		}
	}
}

void ILibForceClose(void *Reader)
{
	struct ILibHCHTTPReaderObject *r = (struct ILibHCHTTPReaderObject*)Reader;
	
	if(r->ClientSocket!=~0)
	{
		LVL3DEBUG(++r->Parent->FORCE_COUNTER;)
		closesocket(r->ClientSocket);
		r->ClientSocket = ~0;
	}
}
void ILibProcessSocket(struct ILibHCHTTPReaderObject *Reader)
{
	int bytesReceived;
	int i;
	struct packetheader_field_node *node;
	char* CharStar;
	
	if(Reader->BodySize==0)
	{
		/* Still Reading Headers */
		bytesReceived = recv(Reader->ClientSocket,Reader->Header+Reader->HeaderIndex,2048-Reader->HeaderIndex,0);
		if(bytesReceived==0)
		{
			LVL3DEBUG(++Reader->Parent->GRACEFUL_COUNTER;)
			if(Reader->PacketHeader!=NULL) {ILibDestructPacket(Reader->PacketHeader);}
			if(Reader->Body_MallocSize!=0) {FREE(Reader->Body);}
			Reader->Body = NULL;
			Reader->Body_MallocSize = 0;
			Reader->PacketHeader = NULL;
			closesocket(Reader->ClientSocket);
			Reader->ClientSocket = ~0;
			ILibLifeTime_Remove(Reader->Parent->SocketTimer,Reader);
			if(Reader->FinRead==0)
			{
				// Prematurely Closed: Error
				Reader->FunctionCallback(Reader,NULL,HTTP_SESSION_INTERRUPT_PEERRESET,NULL,NULL,0,-1,Reader->user,Reader->user2);
			}
			return;
		}
		else
		{
			if(Reader->FinRead!=0)
			{
				return;
			}
		}
		Reader->HeaderIndex += bytesReceived;
		if(Reader->HeaderIndex>4)
		{
			/* Must have read at least 4 bytes to perform check */
			for(i=0;i<(Reader->HeaderIndex - 3);i++)
			{
				if (Reader->Header[i] == '\r' && Reader->Header[i+1] == '\n' && Reader->Header[i+2] == '\r' && Reader->Header[i+3] == '\n')
				{
					/* Finished Header */
					Reader->PacketHeader = ILibParsePacketHeader(Reader->Header,0,i+4);
					if(Reader->PacketHeader==NULL)
					{
						//Invalid Packet
						Reader->FunctionCallback(Reader,Reader->PacketHeader,0,NULL,NULL,0,-1,Reader->user,Reader->user2);
						Reader->BodySize = 0;
						if(Reader->Body!=NULL)
						{
							FREE(Reader->Body);
							Reader->Body = NULL;
							Reader->Body_MallocSize = 0;
						}
						Reader->FinRead=1;
						ILibLifeTime_Add(Reader->Parent->SocketTimer,Reader,1,&ILibForceClose,NULL);
						break;
					}
					Reader->PacketHeader->ReceivingAddress = Reader->LocalIPAddress;
					Reader->BodySize = -1;
					Reader->Body_Read = 0;
					node = Reader->PacketHeader->FirstField;
					while(node!=NULL)
					{
						if(strncasecmp(node->Field,"CONTENT-LENGTH",14)==0)
						{
							CharStar = (char*)MALLOC(1+node->FieldDataLength);
							memcpy(CharStar,node->FieldData,node->FieldDataLength);
							CharStar[node->FieldDataLength] = '\0';
							Reader->BodySize = atoi(CharStar);
							FREE(CharStar);
							break;
						}
						node = node->NextField;
					}
					if(Reader->BodySize!=-1)
					{
						if(Reader->BodySize!=0)
						{
							Reader->Body = (char*)MALLOC(Reader->BodySize);
							Reader->Body_MallocSize = Reader->BodySize;
						}
						else
						{
							Reader->Body = NULL;
							Reader->Body_MallocSize = 0;
						}
					}
					else
					{
						Reader->Body = (char*)MALLOC(4096);
						Reader->Body_MallocSize = 4096;
					}
					
					if(Reader->HeaderIndex>i+4 && Reader->BodySize!=0)
					{
						/* Part of the body is in here */
						memcpy(Reader->Body,Reader->Header+i+4,Reader->HeaderIndex-(&Reader->Header[i+4]-Reader->Header));
						Reader->Body_BeginPointer = 0;
						Reader->Body_EndPointer = Reader->HeaderIndex-(int)(&Reader->Header[i+4]-Reader->Header);
						Reader->Body_Read = Reader->Body_EndPointer;
						
						if(Reader->BodySize!=-1 && Reader->Body_Read>=Reader->BodySize)
						{
							DEBUGSTATEMENT(printf("Close\r\n"));
							Reader->FunctionCallback(Reader,Reader->PacketHeader,0,Reader->Body,&Reader->Body_BeginPointer,Reader->Body_EndPointer - Reader->Body_BeginPointer,-1,Reader->user,Reader->user2);
							
							while(Reader->Body_BeginPointer!=Reader->Body_EndPointer && Reader->Body_BeginPointer!=0)
							{
								memcpy(Reader->Body,Reader->Body+Reader->Body_BeginPointer,Reader->Body_EndPointer-Reader->Body_BeginPointer);
								Reader->Body_EndPointer = Reader->Body_EndPointer-Reader->Body_BeginPointer;
								Reader->Body_BeginPointer = 0;
								Reader->FunctionCallback(Reader,Reader->PacketHeader,0,Reader->Body,&Reader->Body_BeginPointer,Reader->Body_EndPointer,-1,Reader->user,Reader->user2);
							}
							
							Reader->BodySize = 0;
							if(Reader->Body!=NULL)
							{
								FREE(Reader->Body);
								Reader->Body = NULL;
								Reader->Body_MallocSize = 0;
							}
							Reader->FinRead=1;
							ILibLifeTime_Add(Reader->Parent->SocketTimer,Reader,1,&ILibForceClose,NULL);
						}
						else
						{
							Reader->FunctionCallback(Reader,Reader->PacketHeader,0,Reader->Body,&Reader->Body_BeginPointer,Reader->Body_EndPointer - Reader->Body_BeginPointer,0,Reader->user,Reader->user2);
							while(Reader->Body_BeginPointer!=Reader->Body_EndPointer && Reader->Body_BeginPointer!=0)
							{
								memcpy(Reader->Body,Reader->Body+Reader->Body_BeginPointer,Reader->Body_EndPointer-Reader->Body_BeginPointer);
								Reader->Body_EndPointer = Reader->Body_EndPointer-Reader->Body_BeginPointer;
								Reader->Body_BeginPointer = 0;
								Reader->FunctionCallback(Reader,Reader->PacketHeader,0,Reader->Body,&Reader->Body_BeginPointer,Reader->Body_EndPointer,0,Reader->user,Reader->user2);
							}
						}
					}
					else
					{
						/* There is no body, but the packet is here */
						Reader->Body_BeginPointer = 0;
						Reader->Body_EndPointer = 0;
						
						if(Reader->BodySize==0)
						{
							Reader->FunctionCallback(Reader,Reader->PacketHeader,0,NULL,&Reader->Body_BeginPointer,0,-1,Reader->user,Reader->user2);
							Reader->BodySize = 0;
							Reader->FinRead=1;
							ILibLifeTime_Add(Reader->Parent->SocketTimer,Reader,1,&ILibForceClose,NULL);
						}
						else
						{
							Reader->FunctionCallback(Reader,Reader->PacketHeader,0,NULL,&Reader->Body_BeginPointer,0,0,Reader->user,Reader->user2);
						}
					}
					break;
				}
			}
		}
	}
	else
	{
		/* Reading Body Only */
		if(Reader->Body_BeginPointer == Reader->Body_EndPointer)
		{
			Reader->Body_BeginPointer = 0;
			Reader->Body_EndPointer = 0;
		}
		else
		{
			if(Reader->Body_BeginPointer!=0)
			{
				Reader->Body_EndPointer = Reader->Body_BeginPointer;
			}
		}
		
		
		if(Reader->Body_EndPointer == Reader->Body_MallocSize)
		{
			Reader->Body_MallocSize += 4096;
			Reader->Body = (char*)realloc(Reader->Body,Reader->Body_MallocSize);
		}
		
		bytesReceived = recv(Reader->ClientSocket,Reader->Body+Reader->Body_EndPointer,Reader->Body_MallocSize-Reader->Body_EndPointer,0);
		if(bytesReceived==0)
		{
			LVL3DEBUG(++Reader->Parent->GRACEFUL_COUNTER;)
		}
		Reader->Body_EndPointer += bytesReceived;
		Reader->Body_Read += bytesReceived;
		
		Reader->FunctionCallback(Reader, Reader->PacketHeader, 0,Reader->Body+Reader->Body_BeginPointer, &Reader->Body_BeginPointer, Reader->Body_EndPointer - Reader->Body_BeginPointer, 0, Reader->user, Reader->user2);
		while(Reader->Body_BeginPointer!=Reader->Body_EndPointer && Reader->Body_BeginPointer!=0)
		{
			memcpy(Reader->Body,Reader->Body+Reader->Body_BeginPointer,Reader->Body_EndPointer-Reader->Body_BeginPointer);
			Reader->Body_EndPointer = Reader->Body_EndPointer-Reader->Body_BeginPointer;
			Reader->Body_BeginPointer = 0;
			Reader->FunctionCallback(Reader,Reader->PacketHeader,0,Reader->Body,&Reader->Body_BeginPointer,Reader->Body_EndPointer,0,Reader->user,Reader->user2);
		}
		
		if((Reader->BodySize!=-1 && Reader->Body_Read>=Reader->BodySize)||(bytesReceived==0))
		{
			if(Reader->Body_BeginPointer == Reader->Body_EndPointer)
			{
				Reader->Body_BeginPointer = 0;
				Reader->Body_EndPointer = 0;
			}
			Reader->FunctionCallback(Reader, Reader->PacketHeader, 0,Reader->Body, &Reader->Body_BeginPointer, Reader->Body_EndPointer, -1,Reader->user,Reader->user2);
			if(Reader->Body!=NULL)
			{
				FREE(Reader->Body);
				Reader->Body = NULL;
				Reader->Body_MallocSize = 0;
			}
			Reader->BodySize = 0;
			Reader->FinRead=1;
			ILibLifeTime_Add(Reader->Parent->SocketTimer,Reader,1,&ILibForceClose,NULL);
		}
		
		if(Reader->Body_BeginPointer==Reader->Body_EndPointer)
		{
			Reader->Body_BeginPointer = 0;
			Reader->Body_EndPointer = 0;
		}
	}
}

void ILibCloseRequest(void *ReaderObject)
{
	if(((struct ILibHCHTTPReaderObject*)ReaderObject)->ClientSocket!=~0)
	{
		LVL3DEBUG(++(((struct ILibHCHTTPReaderObject*)ReaderObject)->Parent->CLOSE_COUNTER);)
		if(((struct ILibHCHTTPReaderObject*)ReaderObject)->Body!=NULL)
		{
			FREE(((struct ILibHCHTTPReaderObject*)ReaderObject)->Body);
			((struct ILibHCHTTPReaderObject*)ReaderObject)->Body = NULL;
		}
		if(((struct ILibHCHTTPReaderObject*)ReaderObject)->PacketHeader!=NULL)
		{
			ILibDestructPacket(((struct ILibHCHTTPReaderObject*)ReaderObject)->PacketHeader);
			((struct ILibHCHTTPReaderObject*)ReaderObject)->PacketHeader = NULL;
		}
		closesocket(((struct ILibHCHTTPReaderObject*)ReaderObject)->ClientSocket);
		((struct ILibHCHTTPReaderObject*)ReaderObject)->ClientSocket = ~0;
		((struct ILibHCHTTPReaderObject*)ReaderObject)->BodySize = 0;
		((struct ILibHCHTTPReaderObject*)ReaderObject)->user = NULL;
		((struct ILibHCHTTPReaderObject*)ReaderObject)->user2 = NULL;
	}
}

void ILibDeleteRequests(void *ClientModule, void *user1)
{
	struct HTTPClientModule *module = (struct HTTPClientModule*)ClientModule;
	struct RequestQueueNode *RequestNode = NULL,*TempNode = NULL;
	struct RequestQueueNode *DeleteHead=NULL,*DeleteTail=NULL;
	int i;
	sem_wait(&(module->QueueLock));
	for(i=0;i<module->NumSlots;++i)
	{
		if(module->Readers[i].ClientSocket!=~0 && module->Readers[i].user==user1)
		{
			module->Readers[i].FinRead = 1;
			TempNode = (struct RequestQueueNode*)MALLOC(sizeof(struct RequestQueueNode));
			memset(TempNode,0,sizeof(struct RequestQueueNode));
			TempNode->FunctionCallback = module->Readers[i].FunctionCallback;
			TempNode->user = module->Readers[i].user;
			TempNode->user2 = module->Readers[i].user2;
			if(DeleteHead==NULL)
			{
				DeleteHead = TempNode;
				DeleteTail = TempNode;
			}
			else
			{
				DeleteTail->Next = TempNode;
				DeleteTail = TempNode;
			}
		}
	}
	if(module->First!=NULL)
	{
		TempNode = module->First;
		while(TempNode!=NULL)
		{
			if(TempNode->user==user1)
			{
				//Match, Delete this item
				if(DeleteHead==NULL)
				{
					DeleteHead = TempNode;
				}
				else
				{
					DeleteTail->Next = TempNode;
				}
				
				
				
				if(TempNode->Previous==NULL)
				{
					//First Item
					module->First = TempNode->Next;
					if(module->First!=NULL)
					{
						module->First->Previous = NULL;
						if(module->First->Next!=NULL)
						{
							module->First->Next->Previous = module->First;
						}
					}
				}
				else
				{
					//Not First Item
					TempNode->Previous->Next = TempNode->Next;
					if(TempNode->Next!=NULL)
					{
						TempNode->Next->Previous = TempNode->Previous;
					}
				}
				
				RequestNode = TempNode->Next;
				--module->RequestQueueCount;
				TempNode->Next = NULL;
				TempNode->Previous = DeleteTail;
				DeleteTail = TempNode;
				TempNode = RequestNode;
			}
			else
			{
				TempNode = TempNode->Next;
			}
		}
	}
	sem_post(&(module->QueueLock));
	
	
	TempNode = DeleteHead;
	while(TempNode!=NULL)
	{
		if(TempNode->FunctionCallback!=NULL)
		{
			TempNode->FunctionCallback(ClientModule, NULL,HTTP_DELETEREQUEST_INTERRUPT, NULL, NULL, 0, -1, TempNode->user, TempNode->user2);
		}
		if(TempNode->Request!=NULL)	{FREE(TempNode->Request);}
		if(TempNode->Request2!=NULL) {FREE(TempNode->Request2);}
		DeleteHead = TempNode->Next;	
		FREE(TempNode);
		TempNode = DeleteHead;
	}
}
void ILibAddRequest_DirectEx(void *ClientModule, char *buffer, int bufferlength,char *buffer2, int buffer2length, struct sockaddr_in *Destination, void (*CallbackPtr)(void *reader, struct packetheader *header, int IsInterrupt, char* buffer, int *p_BeginPointer, int EndPointer, int done, void* user, void* user2),void* user, void* user2)
{
	struct HTTPClientModule *module = (struct HTTPClientModule*)ClientModule;
	struct RequestQueueNode *RequestNode = (struct RequestQueueNode*)MALLOC(sizeof(struct RequestQueueNode));
	struct RequestQueueNode *TempNode;
	
	LVL3DEBUG(++module->ADD_COUNTER;)
	
	RequestNode->RequestLength = bufferlength;
	RequestNode->Request = buffer;
	RequestNode->Request2 = buffer2;
	RequestNode->Request2Length = buffer2length;
	RequestNode->user = user;
	RequestNode->user2 = user2;
	RequestNode->FunctionCallback = CallbackPtr;
	RequestNode->Next = NULL;
	
	if(Destination!=NULL)
	{
		memset((char *)&(RequestNode->Destination), 0, sizeof(RequestNode->Destination));
		RequestNode->Destination.sin_family = AF_INET;
		RequestNode->Destination.sin_addr.s_addr = Destination->sin_addr.s_addr;
		RequestNode->Destination.sin_port = Destination->sin_port;
	}
	sem_wait(&(module->QueueLock));
	++module->RequestQueueCount;
	if(module->First==NULL)
	{
		module->First = RequestNode;
		RequestNode->Previous = NULL;
	}
	else
	{
		TempNode = module->First;
		while(TempNode->Next!=NULL)
		{
			TempNode = TempNode->Next;
		}
		TempNode->Next = RequestNode;
		RequestNode->Previous = TempNode;
	}
	sem_post(&(module->QueueLock));
	sem_post(&(module->Monitor));
	ILibForceUnBlockChain(module->Chain);
}
void ILibAddRequest(void *ClientModule, struct packetheader *packet,struct sockaddr_in *Destination, void (*CallbackPtr)(void *reader, struct packetheader *header, int IsInterrupt, char* buffer, int *p_BeginPointer, int EndPointer, int done, void* user, void* user2), void* user, void* user2)
{
	int BufferLength;
	char *Buffer;
	
	BufferLength = ILibGetRawPacket(packet,&Buffer);
	ILibDestructPacket(packet);
	ILibAddRequest_Direct(ClientModule,Buffer,BufferLength,Destination,CallbackPtr,user,user2);
}

void ILibHTTPClientModule_PreSelect(void *ClientModule,fd_set *readset, fd_set *writeset, fd_set *errorset, int *blocktime)
{
	int OK,idx;
	int i=0;
	int NumFree = 0;
	struct RequestQueueNode *data;
	struct HTTPClientModule *module = (struct HTTPClientModule*)ClientModule;
	
	NumFree = 0;
	for(i=0;i<module->NumSlots;++i)
	{
		if(module->Readers[i].ClientSocket==~0) {++NumFree;}
	}
	DEBUGSTATEMENT(printf("NumFree = %d\r\n",NumFree));
	DEBUGSTATEMENT(printf("NumSlots = %d\r\n",module->NumSlots));
	
	for(i=0;i<NumFree;++i)
	{
		if(sem_trywait(&(module->Monitor))==0 || module->RequestQueueCount>0)
		{
			sem_wait(&(module->QueueLock));
			data = module->First;
			OK = 0;
			while(OK==0 && data !=NULL)
			{
				OK = -1;
				for(idx=0;idx<module->NumSlots;++idx)
				{
					if(module->Readers[idx].ClientSocket!=~0 && module->Readers[idx].user==data->user)
					{
						// Try Again with another Request
						OK = 0;
						data = data->Next;
						break;
					}
				}
			}
			
			if(data!=NULL)
			{
				if(data->Previous == NULL)
				{
					//First Item
					module->First = data->Next;
					if(module->First!=NULL)
					{
						module->First->Previous = NULL;
					}
				}
				else
				{
					//Not First Item
					data->Previous->Next = data->Next;
					if(data->Next!=NULL)
					{
						data->Next->Previous = data->Previous;
					}
				}
			}
			sem_post(&(module->QueueLock));
			
			if(data!=NULL)
			{
				ILibStartRequest(module,data);
				FREE(data);
				--module->RequestQueueCount;
			}
		}
		else
		{
			break;
		}
	}
	
	/* Pre Select */
	for(i=0;i<module->NumSlots;++i)
	{
		if(module->Readers[i].ClientSocket!=~0)
		{
			if(module->Readers[i].FinConnect==0)
			{
				/* Not Connected Yet */
				FD_SET(module->Readers[i].ClientSocket,writeset);
				FD_SET(module->Readers[i].ClientSocket,errorset);
			}
			else
			{
				/* Already Connected, just needs reading */
				FD_SET(module->Readers[i].ClientSocket,readset);
				FD_SET(module->Readers[i].ClientSocket,errorset);
			}
		}
	}
}
void ILibHTTPClient_PostSelect(void *ClientModule, int slct, fd_set *readset, fd_set *writeset, fd_set *errorset)
{
	int i=0;
	int tst;
	unsigned long flags;
	struct sockaddr_in receivingAddress;
	int receivingAddressLength = sizeof(struct sockaddr_in);
	struct HTTPClientModule *module = (struct HTTPClientModule*)ClientModule;
	
	for(i=0;i<module->NumSlots;++i)
	{
		if(module->Readers[i].ClientSocket!=~0)
		{
			if(module->Readers[i].FinConnect==0)
			{
				/* Not Connected Yet */
				if(FD_ISSET(module->Readers[i].ClientSocket,writeset)!=0)
				{
					/* Connected */
					getsockname(module->Readers[i].ClientSocket,(struct sockaddr*)&receivingAddress,&receivingAddressLength);
					module->Readers[i].LocalIPAddress = receivingAddress.sin_addr.s_addr;
					module->Readers[i].FinConnect = 1;
					module->Readers[i].BodySize = 0;
					module->Readers[i].Body_Read = 0;
					module->Readers[i].Body_BeginPointer = 0;
					module->Readers[i].Body_EndPointer = 0;
					module->Readers[i].HeaderIndex = 0;
					
					/* Platform Dependent [Windows] */
					flags = 0;
					ioctlsocket(module->Readers[i].ClientSocket,FIONBIO,&flags);
					tst=send(module->Readers[i].ClientSocket,module->Readers[i].send_data,module->Readers[i].send_dataLength,0);
					LVL3DEBUG(if(tst!=module->Readers[i].send_dataLength))
					LVL3DEBUG({)
						LVL3DEBUG(	++module->SEND_FAIL;)
						LVL3DEBUG(})
					LVL3DEBUG(else)
					LVL3DEBUG({)
						LVL3DEBUG(	++module->CONNECT_COUNTER;)
						LVL3DEBUG(})
					FREE(module->Readers[i].send_data);
					module->Readers[i].send_data=NULL;
					if(module->Readers[i].send_data2!=NULL)
					{
						tst=send(module->Readers[i].ClientSocket,module->Readers[i].send_data2,module->Readers[i].send_data2Length,0);
						LVL3DEBUG(if(tst!=module->Readers[i].send_dataLength))
						LVL3DEBUG({)
							LVL3DEBUG(	++module->SEND_FAIL2;)
							LVL3DEBUG(})
						FREE(module->Readers[i].send_data2);
						module->Readers[i].send_data2=NULL;
					}
				}
				if(FD_ISSET(module->Readers[i].ClientSocket,errorset)!=0)
				{
					/* Connection Failed */
					LVL3DEBUG(++(module->FAILED_COUNTER);)
					if(module->Readers[i].send_data!=NULL)
					{
						FREE(module->Readers[i].send_data);
						module->Readers[i].send_data = NULL;
					}
					if(module->Readers[i].send_data2!=NULL)
					{
						FREE(module->Readers[i].send_data2);
						module->Readers[i].send_data2 = NULL;
					}
					closesocket(module->Readers[i].ClientSocket);
					module->Readers[i].Body_BeginPointer = 0;
					module->Readers[i].ClientSocket = ~0;
					module->Readers[i].BodySize = 0;
					module->Readers[i].FunctionCallback(&(module->Readers[i]), NULL,0, NULL, &module->Readers[i].Body_BeginPointer, 0, -1,module->Readers[i].user,module->Readers[i].user2);
				}
			}
			else
			{
				/* Already Connected, just needs reading */
				if(FD_ISSET(module->Readers[i].ClientSocket,readset)!=0)
				{
					/* Data Available */
					ILibProcessSocket(&(module->Readers[i]));
				}
				/* Check if PeerReset */
				else if(FD_ISSET(module->Readers[i].ClientSocket,errorset)!=0)
				{
					/* Socket Closed */
					closesocket(module->Readers[i].ClientSocket);
					module->Readers[i].ClientSocket = ~0;
					module->Readers[i].BodySize = 0;
					if(module->Readers[i].BodySize==-1)
					{
						module->Readers[i].Body_BeginPointer = 0;
						module->Readers[i].FunctionCallback(&(module->Readers[i]), module->Readers[i].PacketHeader, 0,NULL, &module->Readers[i].Body_BeginPointer, 0, -1, module->Readers[i].user,module->Readers[i].user2);
					}
				}
			}
		}
	}
}

void* ILibCreateHTTPClientModule(void *Chain, int MaxSockets)
{
	struct HTTPClientModule *RetVal = (struct HTTPClientModule*)MALLOC(sizeof(struct HTTPClientModule));
	int i=0;
	struct timeval tv;
	
	gettimeofday(&tv,NULL);
	srand((int)tv.tv_sec);
	
	LVL3DEBUG(RetVal->ADD_COUNTER=0;)
	LVL3DEBUG(RetVal->FAILED_COUNTER = 0;)
	LVL3DEBUG(RetVal->GRACEFUL_COUNTER = 0;)
	LVL3DEBUG(RetVal->FORCE_COUNTER = 0;)
	LVL3DEBUG(RetVal->START_COUNTER = 0;)
	LVL3DEBUG(RetVal->CLOSE_COUNTER = 0;)
	LVL3DEBUG(RetVal->SEND_FAIL = 0;)
	LVL3DEBUG(RetVal->SEND_FAIL2 = 0;)
	LVL3DEBUG(RetVal->CONNECT_COUNTER = 0;)
	
	RetVal->RequestQueueCount = 0;
	RetVal->Terminate = 0;
	RetVal->NumSlots = MaxSockets;
	RetVal->First = NULL;
	RetVal->Readers = (struct ILibHCHTTPReaderObject*)MALLOC(MaxSockets*sizeof(struct ILibHCHTTPReaderObject));
	
	memset(RetVal->Readers,0,MaxSockets*sizeof(struct ILibHCHTTPReaderObject));
	for(i=0;i<MaxSockets;++i)
	{
		RetVal->Readers[i].ClientSocket = ~0;
		RetVal->Readers[i].Parent = RetVal;
	}
	
	sem_init(&(RetVal->QueueLock),0,1);
	sem_init(&(RetVal->Monitor),0,0);
	RetVal->PreSelect = &ILibHTTPClientModule_PreSelect;
	RetVal->PostSelect = &ILibHTTPClient_PostSelect;
	RetVal->Destroy = &ILibDestroyHTTPClientModule;
	RetVal->Chain = Chain;
	
	RetVal->SocketTimer = ILibCreateLifeTime(Chain);
	
	ILibAddToChain(Chain,RetVal);
	return((void*)RetVal);
}

