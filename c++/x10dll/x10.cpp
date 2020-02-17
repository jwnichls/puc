/*

	x10.cpp

*/



#include "stdafx.h"



#include <stdlib.h>



#include "x10.h"



// conversion arrays (same conversion for A-P as 1-16)



char CODES[]  = {	0x06, 0x0E, 0x02, 0x0A,

					0x01, 0x09, 0x05, 0x0D,

					0x07, 0x0F, 0x03, 0x0B,

					0x00, 0x08, 0x04, 0x0C   };



int NUM_CMDS = 8;



char* CMD_STRS[] = { "ON", "OFF", "DIM", "BRIGHT", "ALLLIGHTSON", 

					 "ALLLIGHTSOFF", "ALLUNITSOFF" ,"STATUSREQUEST" };



char CMD_CODES[] = {  0x2,  0x3 ,  0x4 ,   0x5   ,      0x1,

						  0x6      ,	  0x0	 , 0xF  };





// Functions



char house_code_to_x10( char letter ) {



	letter &= 0x9F;



	int idx = (int)letter - 1;



	return CODES[idx];

}
	




char device_code_to_x10( char* number ) {



	int idx = atoi( number ) - 1;



	return CODES[idx];

}





char command_to_x10( char* command ) {



	int i;

	int len = strlen( command );



	for( i = 0; i < len; i++ )  // uppercase command string

		command[i] &= 0xDF;



	for( i = 0; i < NUM_CMDS; i++ ) {

		if ( strcmp( command, CMD_STRS[i] ) == 0 )

			return CMD_CODES[i];

	}



	return 0xF;  // if nothing was found, return "status request" cmd

}





void generate_bytes( char house, char dev, char cmd, x10_packet* addr, x10_packet* func ) {



	// there are two bytes per packet



	char byteone;

	char bytetwo;



	// first packet is address



	byteone = 0x04;

	bytetwo = ( house << 4 ) | dev;



	addr->code[0] = byteone;

	addr->code[1] = bytetwo;



	addr->chksum = byteone + bytetwo;



	// second packet is function



	byteone = 0x06;

	bytetwo = ( house << 4 ) | cmd;



	func->code[0] = byteone;

	func->code[1] = bytetwo;



	func->chksum = byteone + bytetwo;

}

