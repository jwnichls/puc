// PlayVideo.cpp : Defines the entry point for the console application.
//

#include "Frontend.h"
#include "WinampDevice.h"
#include <windows.h>
#include <stdio.h>
#include <jni.h>

// Playback commands

HWND hwndWinamp;

JNIEXPORT void JNICALL Java_edu_cmu_hcii_puc_devices_WinampDevice_prevTrack
  (JNIEnv *env, jobject obj)
{
	hwndWinamp = FindWindow("Winamp v1.x", NULL);
	SendMessage(hwndWinamp, WM_COMMAND, WINAMP_BUTTON1, 0);
}

JNIEXPORT void JNICALL Java_edu_cmu_hcii_puc_devices_WinampDevice_play
  (JNIEnv *env, jobject obj)
{
	hwndWinamp = FindWindow("Winamp v1.x", NULL);
	SendMessage(hwndWinamp, WM_COMMAND, WINAMP_BUTTON2, 0);
}


JNIEXPORT void JNICALL Java_edu_cmu_hcii_puc_devices_WinampDevice_pause
  (JNIEnv *env, jobject obj)
{
	hwndWinamp = FindWindow("Winamp v1.x", NULL);
	SendMessage(hwndWinamp, WM_COMMAND, WINAMP_BUTTON3, 0);
}

JNIEXPORT void JNICALL Java_edu_cmu_hcii_puc_devices_WinampDevice_stopPlay
  (JNIEnv *env, jobject obj)
{
	hwndWinamp = FindWindow("Winamp v1.x", NULL);
	SendMessage(hwndWinamp, WM_COMMAND, WINAMP_BUTTON4, 0);
}

JNIEXPORT void JNICALL Java_edu_cmu_hcii_puc_devices_WinampDevice_nextTrack
  (JNIEnv *env, jobject obj)
{
	hwndWinamp = FindWindow("Winamp v1.x", NULL);
	SendMessage(hwndWinamp, WM_COMMAND, WINAMP_BUTTON5, 0);
}

JNIEXPORT void JNICALL Java_edu_cmu_hcii_puc_devices_WinampDevice_stopAfterCurrent
  (JNIEnv *env, jobject obj)
{
	hwndWinamp = FindWindow("Winamp v1.x", NULL);
	SendMessage(hwndWinamp, WM_COMMAND, WINAMP_BUTTON4_CTRL, 0);
}

JNIEXPORT void JNICALL Java_edu_cmu_hcii_puc_devices_WinampDevice_fadeStop
  (JNIEnv *env, jobject obj)
{
	hwndWinamp = FindWindow("Winamp v1.x", NULL);
	SendMessage(hwndWinamp, WM_COMMAND, WINAMP_BUTTON4_SHIFT, 0);
}

// Other commands

JNIEXPORT void JNICALL Java_edu_cmu_hcii_puc_devices_WinampDevice_volumeUp
  (JNIEnv *env, jobject obj)
{
	hwndWinamp = FindWindow("Winamp v1.x", NULL);
	SendMessage(hwndWinamp, WM_COMMAND, WINAMP_VOLUMEUP, 0);
}

JNIEXPORT void JNICALL Java_edu_cmu_hcii_puc_devices_WinampDevice_volumeDown
  (JNIEnv *env, jobject obj)
{
	hwndWinamp = FindWindow("Winamp v1.x", NULL);
	SendMessage(hwndWinamp, WM_COMMAND, WINAMP_VOLUMEDOWN, 0);
}

JNIEXPORT void JNICALL Java_edu_cmu_hcii_puc_devices_WinampDevice_setVolume
  (JNIEnv *env, jobject obj, jint newVol)
{
	hwndWinamp = FindWindow("Winamp v1.x", NULL);
	SendMessage(hwndWinamp, WM_WA_IPC, (int) newVol, IPC_SETVOLUME);
}

JNIEXPORT void JNICALL Java_edu_cmu_hcii_puc_devices_WinampDevice_toggleShuffle
  (JNIEnv *env, jobject obj)
{
	hwndWinamp = FindWindow("Winamp v1.x", NULL);
	SendMessage(hwndWinamp, WM_COMMAND, 40023, 0); // 40023 = toggle shuffle
}

JNIEXPORT void JNICALL Java_edu_cmu_hcii_puc_devices_WinampDevice_toggleLoop
  (JNIEnv *env, jobject obj)
{
	hwndWinamp = FindWindow("Winamp v1.x", NULL);
	SendMessage(hwndWinamp, WM_COMMAND, 40022, 0); // 40022 = toggle loop
}

JNIEXPORT void JNICALL Java_edu_cmu_hcii_puc_devices_WinampDevice_seek
  (JNIEnv *env, jobject obj, jint millis)
{
	hwndWinamp = FindWindow("Winamp v1.x", NULL);
	SendMessage(hwndWinamp, WM_WA_IPC, (int) millis, IPC_JUMPTOTIME);
}

JNIEXPORT void JNICALL Java_edu_cmu_hcii_puc_devices_WinampDevice_setPlaylistPos
  (JNIEnv *env, jobject obj, jint pos)
{
	hwndWinamp = FindWindow("Winamp v1.x", NULL);

	// Subtract 1 from pos, so passing 32 will play track 32, instead of 33
	// (Winamp's implementation of this command seems to be offset by 1)
	SendMessage(hwndWinamp, WM_WA_IPC, ((int) pos) - 1, IPC_SETPLAYLISTPOS);
}

JNIEXPORT void JNICALL Java_edu_cmu_hcii_puc_devices_WinampDevice_setPanning
  (JNIEnv *env, jobject obj, jint panning)
{
	hwndWinamp = FindWindow("Winamp v1.x", NULL);
	SendMessage(hwndWinamp, WM_WA_IPC, (int) panning, IPC_SETPANNING);
}

// Static information

JNIEXPORT jint JNICALL Java_edu_cmu_hcii_puc_devices_WinampDevice_winampVersion
  (JNIEnv *env, jobject obj)
{
	hwndWinamp = FindWindow("Winamp v1.x", NULL);
	int version = SendMessage(hwndWinamp, WM_WA_IPC, 0, IPC_GETVERSION);
	return (jint) version;
}

JNIEXPORT jint JNICALL Java_edu_cmu_hcii_puc_devices_WinampDevice_playlistLength
  (JNIEnv *env, jobject obj)
{
	hwndWinamp = FindWindow("Winamp v1.x", NULL);
	int length = SendMessage(hwndWinamp,WM_WA_IPC,0,IPC_GETLISTLENGTH);
	return (jint) length;
}

// Non-Static information

JNIEXPORT jint JNICALL Java_edu_cmu_hcii_puc_devices_WinampDevice_playbackStatus
  (JNIEnv *env, jobject obj)
{
	hwndWinamp = FindWindow("Winamp v1.x", NULL);
	int mode = SendMessage(hwndWinamp,WM_WA_IPC,0,IPC_ISPLAYING);
	switch (mode)
	{
	case 1: // Playing
		return edu_cmu_hcii_puc_devices_WinampDevice_MODE_PLAY;
	case 3: // Paused
		return edu_cmu_hcii_puc_devices_WinampDevice_MODE_PAUSE;
	case 0: // Stopped
		// Fall through
	default:
		return edu_cmu_hcii_puc_devices_WinampDevice_MODE_STOP;
	}
}

JNIEXPORT jstring JNICALL Java_edu_cmu_hcii_puc_devices_WinampDevice_currentTitle
  (JNIEnv *env, jobject obj)
{
	hwndWinamp = FindWindow("Winamp v1.x", NULL);

	// This code is taken from the nsdn (Nullsoft Developers Network) website
	char this_title[2048],*p;
	GetWindowText(hwndWinamp,this_title,sizeof(this_title));
	p = this_title+strlen(this_title)-8;
	while (p >= this_title)
	{

	if (!strnicmp(p,"- Winamp",8)) break;
	p--;

	}
	if (p >= this_title) p--;
	while (p >= this_title && *p == ' ') p--;
	*++p=0;
	// End borrowed code

	return env->NewStringUTF(this_title);
}

JNIEXPORT jint JNICALL Java_edu_cmu_hcii_puc_devices_WinampDevice_outputTime
  (JNIEnv *env, jobject obj)
{
	hwndWinamp = FindWindow("Winamp v1.x", NULL);
	int time = SendMessage(hwndWinamp, WM_WA_IPC, 0, IPC_GETOUTPUTTIME);
	return (jint) time;
}

JNIEXPORT jint JNICALL Java_edu_cmu_hcii_puc_devices_WinampDevice_songLength
  (JNIEnv *env, jobject obj)
{
	hwndWinamp = FindWindow("Winamp v1.x", NULL);
	int length = SendMessage(hwndWinamp, WM_WA_IPC, 1, IPC_GETOUTPUTTIME);
	return (jint) length;
}

JNIEXPORT jint JNICALL Java_edu_cmu_hcii_puc_devices_WinampDevice_posInPlaylist
  (JNIEnv *env, jobject obj)
{
	hwndWinamp = FindWindow("Winamp v1.x", NULL);
	int pos = SendMessage(hwndWinamp, WM_WA_IPC, 0, IPC_GETLISTPOS);
	return (jint) pos;
}

JNIEXPORT jboolean JNICALL Java_edu_cmu_hcii_puc_devices_WinampDevice_isShuffling
  (JNIEnv *env, jobject obj)
{
	hwndWinamp = FindWindow("Winamp v1.x", NULL);
	int val = SendMessage(hwndWinamp, WM_WA_IPC, 0, IPC_GET_SHUFFLE);
	if (val == 1) return JNI_TRUE;
	else return JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_edu_cmu_hcii_puc_devices_WinampDevice_isLooping
  (JNIEnv *env, jobject obj)
{
	hwndWinamp = FindWindow("Winamp v1.x", NULL);
	int val = SendMessage(hwndWinamp, WM_WA_IPC, 0, IPC_GET_REPEAT);
	if (val == 1) return JNI_TRUE;
	else return JNI_FALSE;
}

