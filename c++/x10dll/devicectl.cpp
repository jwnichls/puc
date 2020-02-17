#include <jni.h>
#include <time.h>
#include <iostream.h>


#include "edu_cmu_hcii_puc_devices_X10Lamp.h"
#include "StdAfx.h"
#include "x10.h"


char codes[]  = {	0x06, 0x0E, 0x02, 0x0A,

					0x01, 0x09, 0x05, 0x0D,

					0x07, 0x0F, 0x03, 0x0B,

					0x00, 0x08, 0x04, 0x0C   };


 
// global handle

HANDLE hCom;

// Exception-throwing function

void throwException (JNIEnv *env, char *message)
{
    jclass deviceExceptionClass;
	//cout << "test";
    deviceExceptionClass = env->FindClass ("DeviceException");
    if (deviceExceptionClass == 0) { /* Unable to find the exception class, give up. */
      return;
    }
    env->ThrowNew(deviceExceptionClass, message);
}

JNIEXPORT void JNICALL Java_edu_cmu_hcii_puc_devices_X10Lamp_native_1open_1serial_1port
  (JNIEnv *env, jobject obj, jstring port_num, jint baudrate)

{

	DCB dcb;

	DWORD dwError;

	BOOL fSuccess;

	//cout << "port: " << env->GetStringUTFChars( port_num, false ) << " baud: " << baudrate << endl;


	char port_str[10];

	sprintf( port_str, "\\\\.\\COM%s", (char *) env->GetStringUTFChars( port_num, false ) );



	 hCom = CreateFile(	port_str,

						GENERIC_READ | GENERIC_WRITE,

						0,				// comm devices must be opened w/exclusive-access 

						NULL,			// no security attributes 

						OPEN_EXISTING,	// comm devices must use OPEN_EXISTING 

						0,				// not overlapped I/O 

						NULL			// hTemplate must be NULL for comm devices 

					 );


	
	if ( hCom == INVALID_HANDLE_VALUE) 

	{

		dwError = GetLastError();

		throwException (env, "Problem opening the communications port.");

		

	}



	// Omit the call to SetupComm to use the default queue sizes.

	// Get the current configuration.



	fSuccess = GetCommState(hCom, &dcb);



	if (!fSuccess) 

	{

		throwException (env, "Problem getting the state of the communications port.");

		Java_edu_cmu_hcii_puc_devices_X10Lamp_native_1close_1serial_1port (env, obj);

	

	}



	// Fill in the DCB: baud=9600, 8 data bits, no parity, 1 stop bit. 



	dcb.BaudRate = baudrate;

	dcb.ByteSize = 8;

	dcb.Parity   = NOPARITY;

	dcb.StopBits = ONESTOPBIT;

	dcb.fBinary  = 1;



	dcb.fOutxCtsFlow      = 0;          // CTS output flow control off at first

                                        // (turn it on after sending an 

                                                                                // "are you there message" below)

    dcb.fOutxDsrFlow      = 0;          // DSR output flow control 

    dcb.fDtrControl       = 2;          // DTR flow control type 

    dcb.fDsrSensitivity   = 0;          // DSR sensitivity 

    dcb.fTXContinueOnXoff = 0;          // XOFF continues Tx 

    dcb.fOutX             = 0;          // XON/XOFF out flow control 

    dcb.fInX              = 0;          // XON/XOFF in flow control 

    dcb.fErrorChar        = 0;          // enable error replacement 

    dcb.fNull             = 0;          // enable null stripping 

    dcb.fRtsControl       = 1;          // RTS flow control 

    dcb.fAbortOnError     = 0;          // abort reads/writes on error 

    dcb.ByteSize          = 8;          // number of bits/byte, 4-8 



	fSuccess = SetCommState(hCom, &dcb);



	if (!fSuccess) 

	{

		Java_edu_cmu_hcii_puc_devices_X10Lamp_native_1close_1serial_1port (env, obj);

		throwException (env, "Problem setting the state of the communications port.");

		

	}





}

JNIEXPORT void JNICALL Java_edu_cmu_hcii_puc_devices_X10Lamp_native_1close_1serial_1port
  (JNIEnv *env, jobject obj)
{
		CloseHandle (hCom);

}

int write_to_serial_port (JNIEnv *env, char* bytes, int len ) 
{

	BOOL fSuccess;

	int written = 0;

	fSuccess = WriteFile( hCom, bytes, len, (unsigned long*)&written, NULL );

	if ( !fSuccess ) {

		throwException (env, "Encountered error writing to serial port.");

		return 0;

	}

//	else { printf( "write_to_serial_port: Bytes written = %d\n", written ); }

	return written;

}

int recv_from_serial_port (JNIEnv *env, char* bytes, int req_len ) 
{

	BOOL fSuccess;

	int  read = 0;

	fSuccess = ReadFile( hCom, bytes, req_len, (unsigned long*)&read, NULL );

	if ( !fSuccess ) {

		throwException (env, "Encountered error reading from serial port.");

		return 0;

	}

	return read;	

}

void set_x10_time (JNIEnv *env) 
{

	char s1buf;

	long milli_time = time( NULL );

	struct tm *curr_time = localtime( &milli_time ); 



	// download time code



	s1buf = (char)0x9B;

	write_to_serial_port (env, &s1buf, 1);



	// download seconds



	s1buf = (char)curr_time->tm_sec;

	write_to_serial_port (env,  &s1buf, 1);



	// download minutes



	s1buf = (char)curr_time->tm_min;

	write_to_serial_port(env,  &s1buf, 1 );



	// download hours



	s1buf = (char)curr_time->tm_hour;

	write_to_serial_port( env,  &s1buf, 1 );



	// download day of year



	s1buf = (char)( curr_time->tm_yday & 0xFF );

	write_to_serial_port(env,  &s1buf, 1 );



	// download day of week + last bit of year



	s1buf = (char)( ( ( curr_time->tm_yday & 0x00000100 ) >> 1 ) | 

					  ( 0x00000040 >> curr_time->tm_wday ) );

	write_to_serial_port(env,  &s1buf, 1 );



	s1buf = (char)0x67;

	write_to_serial_port(env,  &s1buf, 1 );

}





void clear_x10_macros (JNIEnv *env) 
{

	char s1buf;

	char recv_byte;


	s1buf = (char)0xFB;

	write_to_serial_port(env,  &s1buf, 1 );



	s1buf = 0x00;

	for( int j = 0; j < 42; j++ )                     // send a blank 42-byte macro

		write_to_serial_port(env,  &s1buf, 1 );


	recv_from_serial_port(env,  &recv_byte, 1 );     // receive checksum (should be zero, but we'll ignore)


	write_to_serial_port(env,  &s1buf, 1 );          // send confirmation

}
/*
JNIEXPORT void JNICALL Java_edu_cmu_hcii_puc_devices_X10Lamp_native_1Handle_1X10_1Device
  (JNIEnv *env, jobject obj, jstring house_code, jstring dev_num, jstring command)
{

	char xHouse, xDev, xCommand, recv_byte; //fa_mask;


	char  s1buf;   // a misc 1 byte buffer

	char *buffer;  // misc buffer to use when needed


	char *nullstr = "\0";


	x10_packet packets[2];


	// convert parameters from command line to usable X10 values

	// (using functions in x10.cpp)


	xHouse   = house_code_to_x10 (((char *) env->GetStringUTFChars(house_code, NULL))[0]);

	xDev     = device_code_to_x10 ((char *) env->GetStringUTFChars (dev_num, NULL));

	xCommand = command_to_x10 ((char *) env->GetStringUTFChars (command, NULL));



	// get bytes to send



	generate_bytes( xHouse, xDev, xCommand, packets, packets+1 );



	// negotiate with device



	// first we'll check to be sure the interface is ready for us to send

	// and handle any problems (the interface may be polling us)



	int read, written;  // status vars

	int i;

	int ATTEMPTS = 100; // how many times do we try to get an interface ready response before giving up?

	

	// prime the interface to see if it's in a state we can use

	s1buf = 0x04;

	write_to_serial_port (env, &s1buf, 1 ); 

	s1buf = 0x66;

	write_to_serial_port (env, &s1buf, 1 );

	

	for( int w = 0; w < ATTEMPTS; w++ ) {

		read = recv_from_serial_port (env, &recv_byte, 1 );



		if ( read == 1 ) {



			if ( recv_byte == 0x55 ) break;  // out ATTEMPTS loop



			switch( recv_byte & 0xFF ) {



			default:   // default case

						s1buf = 0x00;

						write_to_serial_port (env, &s1buf, 1 );

						break;



			case 0x5a: // data is waiting on the interface to send to the PC

						s1buf = (unsigned char)0xC3;

						write_to_serial_port (env, &s1buf, 1 );          // send ready to receive

						do {

							recv_from_serial_port (env, &recv_byte, 1);     // get size of data to recv, BYTE 0
						
						} while (recv_byte == 0x5a);

						recv_from_serial_port (env, &fa_mask, 1);     // Function/Address mask BYTE 1

						for (i = 0; i < recv_byte - 1; ++i) {
						
							//buffer = new char[recv_byte - 1];

							recv_from_serial_port (env, &s1buf, 1); // recv data

							if ((fa_mask & (1 << i)) == 0)

								cout << "Function: " << s1buf << "\n";
							else 
								cout << " Address : "  << s1buf << "\n";
						
						}
						recv_from_serial_port (env, &recv_byte, 1);
						buffer = new char[recv_byte];
						recv_from_serial_port (env, buffer, recv_byte);

						break;



			case 0xf3: // default poll signal

						s1buf = (char)0xF3;

						write_to_serial_port (env, &s1buf, 1 );

						break;



			case 0xa5: // set clock request and...

					   // power-fail macro download poll - i.e. the device wants us to give it macros

						// set the time

						set_x10_time (env);

  					    // download an empty list of macros

						clear_x10_macros (env);

						break;

			}

		}

	}



	for( i = 0; i < 2; i++ ) {



		written = write_to_serial_port (env, packets[i].code, 2 );



		read = recv_from_serial_port (env, &recv_byte, 1 );



		if ( recv_byte != packets[i].chksum ) { i -= 1; continue; }



		written = write_to_serial_port (env, nullstr, 1 );



		if ( written != 1 ) { return; }



		read = recv_from_serial_port (env, &recv_byte, 1 );

	}

}*/

JNIEXPORT jstring JNICALL Java_edu_cmu_hcii_puc_devices_X10Lamp_native_1Prime_1X10_1Interface
  (JNIEnv *env, jobject obj)
{
		
		int read; //, written;  // status vars
		char s1buf, recv_byte;
//		char  fa_mask;
//		int i;

//		char *buffer;  // misc buffer to use when needed

		// PRIME
		s1buf = 0x04;

		//cout << "about to write to serial port" << endl;

		write_to_serial_port (env, &s1buf, 1 ); 

		s1buf = 0x66;

		write_to_serial_port (env, &s1buf, 1 );
		//READ	

		//cout << "about to read from serial port" << endl;

		read = recv_from_serial_port (env, &recv_byte, 1 );

		//cout << "done reading for the first time" << endl;
			
		for ( int ATTEMPTS = 0; ATTEMPTS < 100 && read == 1; ATTEMPTS++ ) {

			switch( recv_byte & 0xFF ) {

			default:   // default case

						s1buf = 0x00;

						write_to_serial_port (env, &s1buf, 1 );

						break;

			case 0x55:  // the X10 interface is ready
				
						return env->NewStringUTF ("0x55");

			case 0x5a: // data is waiting on the interface to send to the PC
                        
						return env->NewStringUTF ("0x5a");
												
			case 0xf3: // default poll signal

						s1buf = (char)0xF3;

						write_to_serial_port (env, &s1buf, 1 );

						return env->NewStringUTF("");

			case 0xa5: // set clock request and...

					   // power-fail macro download poll - i.e. the device wants us to give it macros

						// set the time

						set_x10_time (env);

  					    // download an empty list of macros

						clear_x10_macros (env);

						return env->NewStringUTF("");

			}

			read = recv_from_serial_port (env, &recv_byte, 1 );
		}
		return env->NewStringUTF ("");
}

JNIEXPORT void JNICALL Java_edu_cmu_hcii_puc_devices_X10Lamp_native_1Execute_1command
  (JNIEnv * env, jobject obj, jstring house_code, jstring dev_num, jstring command)
{

	char xHouse, xDev, xCommand, recv_byte; //fa_mask;
		
	char *nullstr = "\0";
		
	int i ;
	
	int read, written;  // status vars;

	x10_packet packets[2];

	// convert parameters from command line to usable X10 values

	// (using functions in x10.cpp)

	xHouse   = house_code_to_x10 (((char *) env->GetStringUTFChars(house_code, NULL))[0]);

	xDev     = device_code_to_x10 ((char *) env->GetStringUTFChars (dev_num, NULL));

	xCommand = command_to_x10 ((char *) env->GetStringUTFChars (command, NULL));

	// get bytes to send

	generate_bytes( xHouse, xDev, xCommand, packets, packets+1 );

	for( i = 0; i < 2; i++ ) {

		written = write_to_serial_port (env, packets[i].code, 2 );

		read = recv_from_serial_port (env, &recv_byte, 1 );

		if ( recv_byte != packets[i].chksum ) { i -= 1; continue; }

		written = write_to_serial_port (env, nullstr, 1 );

		if ( written != 1 ) { return; }

		read = recv_from_serial_port (env, &recv_byte, 1 );

	}
}

JNIEXPORT jboolean JNICALL Java_edu_cmu_hcii_puc_devices_X10Lamp_native_1read_1x10_1status
  (JNIEnv *env, jobject jobj)
{
	char s1buf, recv_byte, fa_mask;
	
	s1buf = (unsigned char)0xC3;

    do {
		write_to_serial_port (env, &s1buf, 1 ); // send ready to receive
		recv_from_serial_port (env, &recv_byte, 1); // get size of data to recv, BYTE 0
	} while (recv_byte == 0x5a);

	recv_from_serial_port (env, &fa_mask, 1);     // Function/Address mask BYTE 1

	for (int i = 0; i < recv_byte - 1; ++i) {
	
		recv_from_serial_port (env, &s1buf, 1); // recv data

		if ((fa_mask & (1 << i)) == 0) {
				// Address
		}
		else {
				//Function
				if ((s1buf & 0x0F) == 0xd)
					return JNI_TRUE;
  			     
				else // if ((s1buf & 0x0F) == 0xe)
					return JNI_FALSE;				 
		}
	}

	return JNI_FALSE;
}