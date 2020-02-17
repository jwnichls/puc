/////////////////////////////////////////////////////////////////////////////
//
// wmppucplugin.h : Declaration of the CWmppucplugin
//
// Copyright (c) Microsoft Corporation. All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

#ifndef __WMPPUCPLUGIN_H_
#define __WMPPUCPLUGIN_H_

#pragma once

#include "resource.h"
#include "wmpplug.h"

// {4440A906-0F70-4A4E-BF12-86BFC8FA9FD5}
DEFINE_GUID(CLSID_Wmppucplugin, 0x4440a906, 0xf70, 0x4a4e, 0xbf, 0x12, 0x86, 0xbf, 0xc8, 0xfa, 0x9f, 0xd5);

class CWmppucplugin;

/////////////////////////////////////////////////////////////////////////////
// Thread Methods
DWORD WINAPI PucThreadProc( LPVOID lParam );
DWORD WINAPI StatePollingThreadProc( LPVOID lParam );
int SendStringState( CWmppucplugin* plugin, char* stateName, char* stateValue, char* buffer );
int SendIntState( CWmppucplugin* plugin, char* stateName, int stateValue, char* buffer );
int SendBoolState( CWmppucplugin* plugin, char* stateName, bool stateValue, char* buffer );
void ProcessPacket( CWmppucplugin* plugin, CComPtr<IWMPCore> core, char* packet, int packetlen );

/////////////////////////////////////////////////////////////////////////////
// CWmppucplugin
class ATL_NO_VTABLE CWmppucplugin : 
    public CComObjectRootEx<CComSingleThreadModel>,
    public CComCoClass<CWmppucplugin, &CLSID_Wmppucplugin>,
    public IWMPEvents,
    public IWMPPluginUI
{
public:
    CWmppucplugin();
    ~CWmppucplugin();

DECLARE_REGISTRY_RESOURCEID(IDR_WMPPUCPLUGIN)

DECLARE_PROTECT_FINAL_CONSTRUCT()

BEGIN_COM_MAP(CWmppucplugin)
    COM_INTERFACE_ENTRY(IWMPEvents)
    COM_INTERFACE_ENTRY(IWMPPluginUI)
END_COM_MAP()

    // CComCoClass methods
    HRESULT FinalConstruct();
    void    FinalRelease();

    // IWMPPluginUI methods
    STDMETHODIMP SetCore(IWMPCore *pCore);
    STDMETHODIMP Create(HWND hwndParent, HWND *phwndWindow) { return E_NOTIMPL; }
    STDMETHODIMP Destroy() { return E_NOTIMPL; }
    STDMETHODIMP TranslateAccelerator(LPMSG lpmsg) { return E_NOTIMPL; }
    STDMETHODIMP DisplayPropertyPage(HWND hwndParent);
    STDMETHODIMP GetProperty(const WCHAR *pwszName, VARIANT *pvarProperty);
    STDMETHODIMP SetProperty(const WCHAR *pwszName, const VARIANT *pvarProperty);

    // IWMPEvents methods
    void STDMETHODCALLTYPE OpenStateChange( long NewState );
    void STDMETHODCALLTYPE PlayStateChange( long NewState );
    void STDMETHODCALLTYPE AudioLanguageChange( long LangID );
    void STDMETHODCALLTYPE StatusChange();
    void STDMETHODCALLTYPE ScriptCommand( BSTR scType, BSTR Param );
    void STDMETHODCALLTYPE NewStream();
    void STDMETHODCALLTYPE Disconnect( long Result );
    void STDMETHODCALLTYPE Buffering( VARIANT_BOOL Start );
    void STDMETHODCALLTYPE Error();
    void STDMETHODCALLTYPE Warning( long WarningType, long Param, BSTR Description );
    void STDMETHODCALLTYPE EndOfStream( long Result );
    void STDMETHODCALLTYPE PositionChange( double oldPosition, double newPosition);
    void STDMETHODCALLTYPE MarkerHit( long MarkerNum );
    void STDMETHODCALLTYPE DurationUnitChange( long NewDurationUnit );
    void STDMETHODCALLTYPE CdromMediaChange( long CdromNum );
    void STDMETHODCALLTYPE PlaylistChange( IDispatch * Playlist, WMPPlaylistChangeEventType change );
    void STDMETHODCALLTYPE CurrentPlaylistChange( WMPPlaylistChangeEventType change );
    void STDMETHODCALLTYPE CurrentPlaylistItemAvailable( BSTR bstrItemName );
    void STDMETHODCALLTYPE MediaChange( IDispatch * Item );
    void STDMETHODCALLTYPE CurrentMediaItemAvailable( BSTR bstrItemName );
    void STDMETHODCALLTYPE CurrentItemChange( IDispatch *pdispMedia);
    void STDMETHODCALLTYPE MediaCollectionChange();
    void STDMETHODCALLTYPE MediaCollectionAttributeStringAdded( BSTR bstrAttribName,  BSTR bstrAttribVal );
    void STDMETHODCALLTYPE MediaCollectionAttributeStringRemoved( BSTR bstrAttribName,  BSTR bstrAttribVal );
    void STDMETHODCALLTYPE MediaCollectionAttributeStringChanged( BSTR bstrAttribName, BSTR bstrOldAttribVal, BSTR bstrNewAttribVal);
    void STDMETHODCALLTYPE PlaylistCollectionChange();
    void STDMETHODCALLTYPE PlaylistCollectionPlaylistAdded( BSTR bstrPlaylistName);
    void STDMETHODCALLTYPE PlaylistCollectionPlaylistRemoved( BSTR bstrPlaylistName);
    void STDMETHODCALLTYPE PlaylistCollectionPlaylistSetAsDeleted( BSTR bstrPlaylistName, VARIANT_BOOL varfIsDeleted);
    void STDMETHODCALLTYPE ModeChange( BSTR ModeName, VARIANT_BOOL NewValue);
    void STDMETHODCALLTYPE MediaError( IDispatch * pMediaObject);
    void STDMETHODCALLTYPE OpenPlaylistSwitch( IDispatch *pItem );
    void STDMETHODCALLTYPE DomainChange( BSTR strDomain);
    void STDMETHODCALLTYPE SwitchedToPlayerApplication();
    void STDMETHODCALLTYPE SwitchedToControl();
    void STDMETHODCALLTYPE PlayerDockedStateChange();
    void STDMETHODCALLTYPE PlayerReconnect();
    void STDMETHODCALLTYPE Click( short nButton, short nShiftState, long fX, long fY );
    void STDMETHODCALLTYPE DoubleClick( short nButton, short nShiftState, long fX, long fY );
    void STDMETHODCALLTYPE KeyDown( short nKeyCode, short nShiftState );
    void STDMETHODCALLTYPE KeyPress( short nKeyAscii );
    void STDMETHODCALLTYPE KeyUp( short nKeyCode, short nShiftState );
    void STDMETHODCALLTYPE MouseDown( short nButton, short nShiftState, long fX, long fY );
    void STDMETHODCALLTYPE MouseMove( short nButton, short nShiftState, long fX, long fY );
    void STDMETHODCALLTYPE MouseUp( short nButton, short nShiftState, long fX, long fY );

    TCHAR        m_szPluginText[MAX_PATH];

public:
    void         ReleaseCore();

    CComPtr<IWMPCore>           m_spCore;
    CComPtr<IConnectionPoint>   m_spConnectionPoint;
    DWORD                       m_dwAdviseCookie;

	// thread variables
	bool					    m_bRunThread;
	HANDLE						m_hListenThread;
	HANDLE						m_hPollThread;

	// properties
	int							m_nListenPort;
	SOCKET					    m_sListenSocket;
	SOCKET						m_sSocket;
	LPCTSTR					    m_szDebugStr;
	bool						m_bFullStateRequested;

	// state variable storage
	char						version[25];
	char						status[100];
	char						currentTitle[200];
	char						currentURL[200];
	int							currentDuration;
	int							currentPosition;
	int						    playStatus;
	int							volume;
	bool						mute;
	bool						shuffle;
	bool						loop;
	bool						positionAvailable;
	bool						playAvailable;
	bool						stopAvailable;
	bool						pauseAvailable;
	bool						prevAvailable;
	bool						nextAvailable;

	BSTR						shuffleString;
	BSTR						loopString;


	LPSTREAM					m_pPUCThreadStream;
	LPSTREAM					m_pStatePollingStream;
};

#endif //__WMPPUCPLUGIN_H_
