/////////////////////////////////////////////////////////////////////////////
//
// wmppucplugin.cpp : Implementation of CWmppucplugin
// Copyright (c) Microsoft Corporation. All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

#include "stdafx.h"
#include "wmppucplugin.h"
#include "CPropertyDialog.h"

/////////////////////////////////////////////////////////////////////////////
// CWmppucplugin::CWmppucplugin
// Constructor

CWmppucplugin::CWmppucplugin()
{
    lstrcpyn(m_szPluginText, _T("wmppucplugin Plugin"), sizeof(m_szPluginText) / sizeof(m_szPluginText[0]));
	m_szDebugStr = _T("Normal Operation");
    m_dwAdviseCookie = 0;
	m_nListenPort = 5250;

	m_sListenSocket = INVALID_SOCKET;
	m_sSocket = INVALID_SOCKET;

	m_bFullStateRequested = false;
	m_bRunThread = false;

	version[0] = '\0';
	status[0] = '\0';
	currentTitle[0] = '\0';
	currentDuration = 0;
	currentPosition = 0;
	playStatus = 0;
	volume = 0;
	mute = false;
	loop = false;
	shuffle = false;

	shuffleString = SysAllocString(L"shuffle");
	loopString = SysAllocString(L"loop");
}

/////////////////////////////////////////////////////////////////////////////
// CWmppucplugin::~CWmppucplugin
// Destructor

CWmppucplugin::~CWmppucplugin()
{
	SysFreeString( shuffleString );
	SysFreeString( loopString );
}

/////////////////////////////////////////////////////////////////////////////
// CWmppucplugin:::FinalConstruct
// Called when an plugin is first loaded. Use this function to do one-time
// intializations that could fail instead of doing this in the constructor,
// which cannot return an error.

HRESULT CWmppucplugin::FinalConstruct()
{
    return S_OK;
}

/////////////////////////////////////////////////////////////////////////////
// CWmppucplugin:::FinalRelease
// Called when a plugin is unloaded. Use this function to free any
// resources allocated in FinalConstruct.

void CWmppucplugin::FinalRelease()
{
    ReleaseCore();
}

/////////////////////////////////////////////////////////////////////////////
// CWmppucplugin::SetCore
// Set WMP core interface

HRESULT CWmppucplugin::SetCore(IWMPCore *pCore)
{
    HRESULT hr = S_OK;

    // release any existing WMP core interfaces
    ReleaseCore();

    // If we get passed a NULL core, this  means
    // that the plugin is being shutdown.

    if (pCore == NULL)
    {
		// stop the thread
		m_bRunThread = false;

		// close any open sockets
		if ( m_sListenSocket != INVALID_SOCKET )
		{
			closesocket( m_sListenSocket );
			// shutdown( m_sListenSocket );
		}

		if ( m_sSocket != INVALID_SOCKET )
		{
			closesocket( m_sSocket );
			// shutdown( m_sSocket );
		}

        return S_OK;
    }

    m_spCore = pCore;

    // connect up the event interface
    CComPtr<IConnectionPointContainer>  spConnectionContainer;

    hr = m_spCore->QueryInterface( &spConnectionContainer );

    if (SUCCEEDED(hr))
    {
        hr = spConnectionContainer->FindConnectionPoint( __uuidof(IWMPEvents), &m_spConnectionPoint );
    }

    if (SUCCEEDED(hr))
    {
        hr = m_spConnectionPoint->Advise( GetUnknown(), &m_dwAdviseCookie );

        if ((FAILED(hr)) || (0 == m_dwAdviseCookie))
        {
            m_spConnectionPoint = NULL;
        }
    }

	// start the thread
	if (SUCCEEDED(hr))
	{
		m_bRunThread = true;
		CoMarshalInterThreadInterfaceInStream( __uuidof(IWMPCore), (LPUNKNOWN)m_spCore, &m_pPUCThreadStream );
		m_hListenThread = Win32ThreadTraits::CreateThread( NULL, 0, &PucThreadProc, this, 0, NULL );

		if (m_hListenThread != NULL)
		{
			CoMarshalInterThreadInterfaceInStream( __uuidof(IWMPCore), (LPUNKNOWN)m_spCore, &m_pStatePollingStream );
			m_hPollThread = Win32ThreadTraits::CreateThread( NULL, 0, &StatePollingThreadProc, this, 0, NULL );

			if ( m_hPollThread != NULL )
			{
				// do something
			}
		}
	}

    return hr;
}

/////////////////////////////////////////////////////////////////////////////
// CWmppucplugin::ReleaseCore
// Release WMP core interfaces

void CWmppucplugin::ReleaseCore()
{
    if (m_spConnectionPoint)
    {
        if (0 != m_dwAdviseCookie)
        {
            m_spConnectionPoint->Unadvise(m_dwAdviseCookie);
            m_dwAdviseCookie = 0;
        }
        m_spConnectionPoint = NULL;
    }

    if (m_spCore)
    {
        m_spCore = NULL;
    }
}

/////////////////////////////////////////////////////////////////////////////
// CWmppucplugin::DisplayPropertyPage
// Display property page for plugin

HRESULT CWmppucplugin::DisplayPropertyPage(HWND hwndParent)
{
    CPropertyDialog dialog(this);

    dialog.DoModal(hwndParent);

    return S_OK;
}

/////////////////////////////////////////////////////////////////////////////
// CWmppucplugin::GetProperty
// Get plugin property

HRESULT CWmppucplugin::GetProperty(const WCHAR *pwszName, VARIANT *pvarProperty)
{
    if (NULL == pvarProperty)
    {
        return E_POINTER;
    }

    return E_NOTIMPL;
}

/////////////////////////////////////////////////////////////////////////////
// CWmppucplugin::SetProperty
// Set plugin property

HRESULT CWmppucplugin::SetProperty(const WCHAR *pwszName, const VARIANT *pvarProperty)
{
    return E_NOTIMPL;
}

/////////////////////////////////////////////////////////////////////////////
// PucThreadProc
// start a server-side socket and listen for connections
DWORD WINAPI PucThreadProc( LPVOID lParam )
{
	CWmppucplugin* plugin  = (CWmppucplugin*)lParam;
	HRESULT hr;
	int r;
	char packetbuf[100];
	char buf[2];
	int len, pos;

	// initialize COM
	hr = CoInitializeEx( NULL, COINIT_APARTMENTTHREADED );

	CComPtr<IWMPCore> core;
	CComPtr<IWMPControls> controls;
	CComPtr<IWMPSettings> settings;

	// unmarshal core object
	hr = CoGetInterfaceAndReleaseStream( plugin->m_pPUCThreadStream, __uuidof(IWMPCore), (LPVOID*)&core );

	hr = core->get_controls( &controls );

	if (!SUCCEEDED(hr))
	{
		plugin->m_szDebugStr = _T("Getting IWMPControls failed");
		return 0;
	}

	plugin->m_sListenSocket = socket( AF_INET, SOCK_STREAM, IPPROTO_TCP );

	if ( plugin->m_sListenSocket == INVALID_SOCKET )
	{
		WSADATA data;
		int error = WSAGetLastError();

		switch( error )
		{
		case WSANOTINITIALISED:
			error = WSAStartup( MAKEWORD( 2, 0 ), &data );
			if ( error != 0 ) // not successful
				return 0;

			plugin->m_sListenSocket = socket( AF_INET, SOCK_STREAM, IPPROTO_TCP );

			if ( plugin->m_sListenSocket == INVALID_SOCKET )
			{
				error = WSAGetLastError();
			}
			else
				goto OKAY;

			break;

		case WSAENETDOWN:
			break;

		case WSAEAFNOSUPPORT:
			break;

		case WSAEINPROGRESS:
			break;

		case WSAEMFILE:
			break;

		case WSAENOBUFS:
			break;

		case WSAEPROTONOSUPPORT:
			break;

		case WSAEPROTOTYPE:
			break;

		case WSAESOCKTNOSUPPORT:
			break;
		}

		plugin->m_szDebugStr = _T("ListenSocket not initialized");
		return 0;
	}

OKAY:	sockaddr_in inaddr;

	inaddr.sin_family = AF_INET;
	inaddr.sin_port = htons( plugin->m_nListenPort );
	inaddr.sin_addr.S_un.S_addr = htonl(INADDR_ANY);

	r = bind( plugin->m_sListenSocket, (SOCKADDR*)&inaddr, sizeof(inaddr) );

	if ( r == 0 )
	{
		r = listen( plugin->m_sListenSocket, SOMAXCONN );
	}
	else
	{
		plugin->m_szDebugStr = _T("bind() failed");
		return 0;
	}

	while( plugin->m_bRunThread && 
		   plugin->m_sListenSocket != INVALID_SOCKET &&
		   plugin->m_sSocket == INVALID_SOCKET )
	{
		if ( r == 0 )
		{
			plugin->m_sSocket = accept( plugin->m_sListenSocket, NULL, NULL );
		}
		else
		{
			plugin->m_szDebugStr = _T("listen() failed");
			return 0;
		}

		if ( r == 0 && plugin->m_sSocket != INVALID_SOCKET )
		{
			pos = 0;
			while( plugin->m_bRunThread && plugin->m_sSocket != INVALID_SOCKET )
			{
				len = recv( plugin->m_sSocket, buf, 1, 0 );

				if ( len == SOCKET_ERROR || len == 0 )
				{
					closesocket( plugin->m_sSocket );
					plugin->m_sSocket = INVALID_SOCKET;
				}
				else {
					packetbuf[pos] = buf[0];
					if ( buf[0] == '\n' )
					{
						// reached the end of a packet
						ProcessPacket( plugin, core, packetbuf, pos );
						pos = 0;
					}
					else
						pos++;
					/*
					for( i = 0; i < len; i++ )
					{
						switch( buf[i] )
						{
						case 'p':
							controls->play();
							break;

						case 's':
							controls->stop();
							break;

						case '0':
							// test of current position variable
							controls->put_currentPosition( 15.0 );
							break;

						case 'v':
							hr = core->get_settings( &settings );
							if ( SUCCEEDED(hr) )
							{
								long vol;
								hr = settings->get_volume( &vol );

								if ( SUCCEEDED(hr) )
								{
									sprintf( buf, "volume: %ld\r\n", vol );
									send( plugin->m_sSocket, buf, (size_t)(strlen(buf)+1), 0 );
								}
							}
							break;

						case '>':
							hr = core->get_settings( &settings );
							if ( SUCCEEDED(hr) )
							{
								long vol;
								hr = settings->get_volume( &vol );

								if ( SUCCEEDED(hr) )
								{
									if ( vol != 100 )
										vol += 10;

									if ( vol > 100 )
										vol = 100;

									settings->put_volume( vol );
								}
							}
							break;

						case '<':
							hr = core->get_settings( &settings );
							if ( SUCCEEDED(hr) )
							{
								long vol;
								hr = settings->get_volume( &vol );

								if ( SUCCEEDED(hr) )
								{
									if ( vol != 0 )
										vol -= 10;

									if ( vol < 0 )
										vol = 0;

									settings->put_volume( vol );
								}
							}
							break;

						case 'm':
							//mute
							hr = core->get_settings( &settings );
							if ( SUCCEEDED(hr) )
							{
								VARIANT_BOOL muted;
								hr = settings->get_mute( &muted );
								if ( SUCCEEDED(hr) )
								{
									settings->put_mute( !muted );
								}
							}
							break;

						case ',':
							//previous track
							controls->previous();
							break;

						case '.':
							//next track
							controls->next();
							break;

						case 'i':
							//get version info
							BSTR version;
							hr = core->get_versionInfo( &version );
							if ( SUCCEEDED(hr) )
							{
								sprintf( buf, "%ws\r\n", version );
								send( plugin->m_sSocket, buf, (size_t)(strlen(buf)+1), 0 );
							}
							break;

						case 't':
							BSTR status;
							hr = core->get_status( &status );
							if ( SUCCEEDED(hr) )
							{
								sprintf( buf, "%ws\r\n", status );
								send( plugin->m_sSocket, buf, (size_t)(strlen(buf)+1), 0 );
							}

						case 'f':
							plugin->m_bFullStateRequested = true;
							break;
						}
					}
					*/
				}
			}
		}
		else
		{
			plugin->m_szDebugStr = _T("accept() failed");
			return 0;
		}
	}

	return 0;
}

void ProcessPacket( CWmppucplugin* plugin, CComPtr<IWMPCore> core, char* packet, int packetlen )
{
	CComPtr<IWMPControls> controls;
	CComPtr<IWMPSettings> settings;
	HRESULT hr;

	// get COM interfaces that we'll need
	hr = core->get_controls( &controls );

	if (!SUCCEEDED(hr))
	{
		plugin->m_szDebugStr = _T("Getting IWMPControls failed");
		return;
	}
	
	hr = core->get_settings( &settings );

	if (!SUCCEEDED(hr))
	{
		plugin->m_szDebugStr = _T("Getting IWMPSettings failed");
		return;
	}

	switch( packet[0] )
	{
	case 'm': // MUTE
		settings->put_mute( packet[1] == 1 );
		break;

	case 'l': // LOOP
		settings->setMode( plugin->loopString, packet[1] == 1 );
		break;

	case 'h': // SHUFFLE
		settings->setMode( plugin->shuffleString, packet[1] == 1 );
		break;

	case 'v': // VOLUME
		settings->put_volume( packet[1] );
		break;

	case 'p': // PLAY
		controls->play();
		break;

	case 's': // STOP
		controls->stop();
		break;

	case 'd': // PAUSE
		controls->pause();
		break;

	case ',': // PREV TRACK
		controls->previous();
		break;

	case '.': // NEXT TRACK
		controls->next();
		break;

	case 'f': // Full State Request
		plugin->m_bFullStateRequested = true;
		break;

	case 'o': // Output Time Change (current position change)
		int i = 0;
		for( ; i < (packetlen-1); i++ )
		{
			if ( packet[ i+1 ] > 57 || packet[ i+1 ] < 48 )
				break;
		}

		if ( i == 0 )
			return;

		packet[ i+1 ] = '\0';
		char* numstr = packet+1;
		int num = atoi( numstr );
	
		if ( num >= plugin->currentDuration )
			num = plugin->currentDuration - 1;

		controls->put_currentPosition( (double)num );
		break;
	}
}


/////////////////////////////////////////////////////////////////////////////
// StatePollingThreadProc
// start a server-side socket and listen for connections
DWORD WINAPI StatePollingThreadProc( LPVOID lParam )
{
	CWmppucplugin* plugin  = (CWmppucplugin*)lParam;

	CComPtr<IWMPCore> core;
	CComPtr<IWMPControls> controls;
	CComPtr<IWMPSettings> settings;

	// Initialize COM
	CoInitializeEx( NULL, COINIT_APARTMENTTHREADED );

	// unmarshal core object
	CoGetInterfaceAndReleaseStream( plugin->m_pStatePollingStream, __uuidof(IWMPCore), (LPVOID*)&core );

	HRESULT hr;
	bool	fullState = false;

	// temporary local variables
	BSTR tempBSTR;
	VARIANT_BOOL tempVBOOL;
	double tempDbl;
	long tempLng;
	WMPPlayState tempPlayState;
	char buf[250];

	// temporary locations to store new variable locations
	char	tempStatus[100];
	char	tempCurrentTitle[200];
	char    tempCurrentURL[200];
	int		tempCurrentDuration;
	int		tempCurrentPosition;
	int		tempPlayStatus;
	int		tempVolume;
	bool	tempMute;
	bool	tempShuffle;
	bool	tempLoop;
	bool	tempPositionAvailable;
	bool    tempPlayAvailable;
	bool	tempStopAvailable;
	bool	tempPauseAvailable;
	bool    tempPrevAvailable;
	bool	tempNextAvailable;

	// BSTRs to use when checking for availability
	BSTR    playLabel	= SysAllocString(L"play");
	BSTR	stopLabel	= SysAllocString(L"stop");
	BSTR	posLabel	= SysAllocString(L"currentPosition");
	BSTR	pauseLabel  = SysAllocString(L"pause");
	BSTR	prevLabel	= SysAllocString(L"previous");
	BSTR	nextLabel	= SysAllocString(L"next");


	// get COM interfaces that we'll need
	hr = core->get_controls( &controls );

	if (!SUCCEEDED(hr))
	{
		plugin->m_szDebugStr = _T("Getting IWMPControls failed");
		return 0;
	}
	
	hr = core->get_settings( &settings );

	if (!SUCCEEDED(hr))
	{
		plugin->m_szDebugStr = _T("Getting IWMPSettings failed");
		return 0;
	}

	// get version state (only need to do this once)
	hr = core->get_versionInfo( &tempBSTR );
	if ( SUCCEEDED(hr) )
	{
		sprintf( plugin->version, "%ws", tempBSTR );
	}

	// poll the values
	while ( plugin->m_bRunThread ) 
	{
		// verify full state request 
		// (we don't want it to change in the middle of loop)
		fullState = plugin->m_bFullStateRequested;


		// get state variables

		// VOLUME state
		if ( fullState )
		{
			SendStringState( plugin, "version", plugin->version, buf );
		}

		// STATUS state
		hr = core->get_status( &tempBSTR );
		if ( SUCCEEDED(hr) )
		{
			sprintf( tempStatus, "%ws", tempBSTR );
			int cmp = strcmp( plugin->status, tempStatus );

			if ( cmp != 0 )
				strcpy( plugin->status, tempStatus );

			if ( fullState || cmp != 0 )
				SendStringState( plugin, "status", plugin->status, buf );
		}

		// CURRENT POSITION state
		hr = controls->get_currentPosition( &tempDbl );
		if ( SUCCEEDED(hr) )
		{
			tempCurrentPosition = (int)tempDbl;
			bool bcmp = tempCurrentPosition != plugin->currentPosition;

			if ( bcmp )
				plugin->currentPosition = tempCurrentPosition;

			if ( fullState || bcmp )
				SendIntState( plugin, "pos", plugin->currentPosition, buf );
		}


		// PLAY STATUS state
		hr = core->get_playState( &tempPlayState );
		if ( SUCCEEDED(hr) )
		{
			switch(tempPlayState)
			{
			case wmppsStopped:
				tempPlayStatus = 1;
				break;

			case wmppsPlaying:
				tempPlayStatus = 2;
				break;

			case wmppsPaused:
				tempPlayStatus = 3;
				break;

			case wmppsReady:
				tempPlayStatus = 1;
				break;

			case wmppsMediaEnded:
				tempPlayStatus = 1;
		        break;

			default:
				tempPlayStatus = plugin->playStatus;
				break;
			}

			bool bcmp = tempPlayStatus != plugin->playStatus;

			if ( bcmp )
				plugin->playStatus = tempPlayStatus;

			if ( fullState || bcmp )
				SendIntState( plugin, "playstatus", plugin->playStatus, buf );
		}


		// PLAY AVAILABLE state
		hr = controls->get_isAvailable( playLabel, &tempVBOOL );
		if ( SUCCEEDED(hr) )
		{
			tempPlayAvailable = (bool)tempVBOOL;
			bool bcmp = tempPlayAvailable != plugin->playAvailable;

			if ( bcmp )
				plugin->playAvailable = tempPlayAvailable;

			if ( fullState || bcmp )
				SendBoolState( plugin, "playAvail", plugin->playAvailable, buf );
		}

		// STOP AVAILABLE state
		hr = controls->get_isAvailable( stopLabel, &tempVBOOL );
		if ( SUCCEEDED(hr) )
		{
			tempStopAvailable = (bool)tempVBOOL;
			bool bcmp = tempStopAvailable != plugin->stopAvailable;

			if ( bcmp )
				plugin->stopAvailable = tempStopAvailable;

			if ( fullState || bcmp )
				SendBoolState( plugin, "stopAvail", plugin->stopAvailable, buf );
		}

		// PAUSE AVAILABLE state
		hr = controls->get_isAvailable( pauseLabel, &tempVBOOL );
		if ( SUCCEEDED(hr) )
		{
			tempPauseAvailable = (bool)tempVBOOL;
			bool bcmp = tempPauseAvailable != plugin->pauseAvailable;

			if ( bcmp )
				plugin->pauseAvailable = tempPauseAvailable;

			if ( fullState || bcmp )
				SendBoolState( plugin, "pauseAvail", plugin->pauseAvailable, buf );
		}

		// PREV AVAILABLE state
		hr = controls->get_isAvailable( prevLabel, &tempVBOOL );
		if ( SUCCEEDED(hr) )
		{
			tempPrevAvailable = (bool)tempVBOOL;
			bool bcmp = tempPrevAvailable != plugin->prevAvailable;

			if ( bcmp )
				plugin->prevAvailable = tempPrevAvailable;

			if ( fullState || bcmp )
				SendBoolState( plugin, "prevAvail", plugin->prevAvailable, buf );
		}

		// NEXT AVAILABLE state
		hr = controls->get_isAvailable( nextLabel, &tempVBOOL );
		if ( SUCCEEDED(hr) )
		{
			tempNextAvailable = (bool)tempVBOOL;
			bool bcmp = tempNextAvailable != plugin->nextAvailable;

			if ( bcmp )
				plugin->nextAvailable = tempNextAvailable;

			if ( fullState || bcmp )
				SendBoolState( plugin, "nextAvail", plugin->nextAvailable, buf );
		}

		// POSITION AVAILABLE state
		hr = controls->get_isAvailable( posLabel, &tempVBOOL );
		if ( SUCCEEDED(hr) )
		{
			tempPositionAvailable = (bool)tempVBOOL;
			bool bcmp = tempPositionAvailable != plugin->positionAvailable;

			if ( bcmp )
				plugin->positionAvailable = tempPositionAvailable;

			if ( fullState || bcmp )
				SendBoolState( plugin, "positionAvail", plugin->positionAvailable, buf );
		}


		// VOLUME state
		hr = settings->get_volume( &tempLng );
		if ( SUCCEEDED(hr) )
		{
			tempVolume = (int)tempLng;
			bool bcmp = tempVolume != plugin->volume;

			if ( bcmp )
				plugin->volume = tempVolume;

			if ( fullState || bcmp )
				SendIntState( plugin, "vol", plugin->volume, buf );
		}

		// MUTE state
		hr = settings->get_mute( &tempVBOOL );
		if ( SUCCEEDED(hr) )
		{
			tempMute = (bool)tempVBOOL;
			bool bcmp = tempMute != plugin->mute;

			if ( bcmp )
				plugin->mute = tempMute;

			if ( fullState || bcmp )
				SendBoolState( plugin, "mute", plugin->mute, buf );
		}

		// SHUFFLE state
		hr = settings->getMode( plugin->shuffleString, &tempVBOOL );
		if ( SUCCEEDED(hr) )
		{
			tempShuffle = (bool)tempVBOOL;
			bool bcmp = tempShuffle != plugin->shuffle;

			if ( bcmp )
				plugin->shuffle = tempShuffle;

			if ( fullState || bcmp )
				SendBoolState( plugin, "shuffle", plugin->shuffle, buf );
		}

		// LOOP state
		hr = settings->getMode( plugin->loopString, &tempVBOOL );
		if ( SUCCEEDED(hr) )
		{
			tempLoop = (bool)tempVBOOL;
			bool bcmp = tempLoop != plugin->loop;

			if ( bcmp )
				plugin->loop = tempLoop;

			if ( fullState || bcmp )
				SendBoolState( plugin, "loop", plugin->loop, buf );
		}

		// CURRENT URL state
		hr = core->get_URL( &tempBSTR );
		if ( SUCCEEDED(hr) )
		{
			sprintf( tempCurrentURL, "%ws", tempBSTR );
			int cmp = strcmp( plugin->currentURL, tempCurrentURL );

			if ( cmp != 0 )
				strcpy( plugin->currentURL, tempCurrentURL );

			if ( fullState || cmp != 0 )
				SendStringState( plugin, "url", plugin->currentURL, buf );
		}

		// get media object for remaining two states (may not exist)
		CComPtr<IWMPMedia> media;
		hr = core->get_currentMedia( &media );
		if ( SUCCEEDED(hr) && media != NULL )
		{
			// CURRENT TITLE state
			hr = media->get_name( &tempBSTR );
			if ( SUCCEEDED(hr) )
			{
				sprintf( tempCurrentTitle, "%ws", tempBSTR );
				int cmp = strcmp( plugin->currentTitle, tempCurrentTitle );

				if ( cmp != 0 )
					strcpy( plugin->currentTitle, tempCurrentTitle );

				if ( fullState || cmp != 0 )
					SendStringState( plugin, "title", plugin->currentTitle, buf );
			}

			
			// CURRENT DURATION state
			if ( plugin->playStatus > 1 )
			{
				hr = media->get_duration( &tempDbl );
				if ( SUCCEEDED(hr) )
				{
					tempCurrentDuration = (int)tempDbl;
					bool bcmp = tempCurrentDuration != plugin->currentDuration;

					if ( bcmp )
						plugin->currentDuration = tempCurrentDuration;

					if ( fullState || bcmp )
						SendIntState( plugin, "len", plugin->currentDuration, buf );
				}
			}
			else if ( fullState ) {
				SendIntState( plugin, "len", plugin->currentDuration, buf );
			}
		}
		else
		{
			strcpy( plugin->currentTitle, "none" );
			SendStringState( plugin, "title", plugin->currentTitle, buf );
			plugin->currentDuration = 0;
			SendIntState( plugin, "len", plugin->currentDuration, buf );
		}

		// clear full state request, if necessary
		if ( fullState )
			plugin->m_bFullStateRequested = false;

		// sleep for a little while
		Sleep( 900 );
	}

	SysFreeString(playLabel);
	SysFreeString(stopLabel);
	SysFreeString(pauseLabel);
	SysFreeString(prevLabel);
	SysFreeString(nextLabel);
	SysFreeString(posLabel);

	return 0;
}

int SendStringState( CWmppucplugin* plugin, char* stateName, char* stateValue, char* buffer )
{
	if ( plugin->m_sSocket != INVALID_SOCKET )
	{
		sprintf( buffer, "%s:%s\r\n", stateName, stateValue );
		return send( plugin->m_sSocket, buffer, (size_t)strlen(buffer), 0 );
	}

	return SOCKET_ERROR;
}

int SendIntState( CWmppucplugin* plugin, char* stateName, int stateValue, char* buffer )
{
	if ( plugin->m_sSocket != INVALID_SOCKET )
	{
		sprintf( buffer, "%s:%d\r\n", stateName, stateValue );
		return send( plugin->m_sSocket, buffer, (size_t)strlen(buffer), 0 );
	}

	return SOCKET_ERROR;
}

int SendBoolState( CWmppucplugin* plugin, char* stateName, bool stateValue, char* buffer )
{
	if ( plugin->m_sSocket != INVALID_SOCKET )
	{
		sprintf( buffer, "%s:%s\r\n", stateName, (stateValue ? "true" : "false") );
		return send( plugin->m_sSocket, buffer, (size_t)strlen(buffer), 0 );
	}

	return SOCKET_ERROR;
}