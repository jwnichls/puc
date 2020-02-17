/*

	x10.h



	Functions for data conversion needed to implement

	X10 protocol.

*/



#ifndef __X10_H__

#define __X10_H__





struct x10_packet {

	char code[2];

	char chksum;

};





char house_code_to_x10( char letter );



char device_code_to_x10( char* number );



char command_to_x10( char* command );



void generate_bytes( char house, char dev, char cmd, x10_packet* addr, x10_packet* func );





#endif // __X10_H__