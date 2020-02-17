/////////////////////////////////////////////////////////////////////////////
//
// wmppucpluginEvents.cpp : Implementation of CWmppucplugin events
//
// Copyright (c) Microsoft Corporation. All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

#include "stdafx.h"
#include "wmppucplugin.h"

void CWmppucplugin::OpenStateChange( long NewState )
{
    switch (NewState)
    {
    case wmposUndefined:
        break;
	case wmposPlaylistChanging:
        break;
	case wmposPlaylistLocating:
        break;
	case wmposPlaylistConnecting:
        break;
	case wmposPlaylistLoading:
        break;
	case wmposPlaylistOpening:
        break;
	case wmposPlaylistOpenNoMedia:
        break;
	case wmposPlaylistChanged:
        break;
	case wmposMediaChanging:
        break;
	case wmposMediaLocating:
        break;
	case wmposMediaConnecting:
        break;
	case wmposMediaLoading:
        break;
	case wmposMediaOpening:
        break;
	case wmposMediaOpen:
        break;
	case wmposBeginCodecAcquisition:
        break;
	case wmposEndCodecAcquisition:
        break;
	case wmposBeginLicenseAcquisition:
        break;
	case wmposEndLicenseAcquisition:
        break;
	case wmposBeginIndividualization:
        break;
	case wmposEndIndividualization:
        break;
	case wmposMediaWaiting:
        break;
	case wmposOpeningUnknownURL:
        break;
    default:
        break;
    }
}

void CWmppucplugin::PlayStateChange( long NewState )
{
    switch (NewState)
    {
    case wmppsUndefined:
        break;
	case wmppsStopped:
        break;
	case wmppsPaused:
        break;
	case wmppsPlaying:
		break;
	case wmppsScanForward:
        break;
	case wmppsScanReverse:
        break;
	case wmppsBuffering:
        break;
	case wmppsWaiting:
        break;
	case wmppsMediaEnded:
        break;
	case wmppsTransitioning:
        break;
	case wmppsReady:
        break;
	case wmppsReconnecting:
        break;
	case wmppsLast:
        break;
    default:
        break;
    }
}

void CWmppucplugin::AudioLanguageChange( long LangID )
{
}

void CWmppucplugin::StatusChange()
{
}

void CWmppucplugin::ScriptCommand( BSTR scType, BSTR Param )
{
}

void CWmppucplugin::NewStream()
{
}

void CWmppucplugin::Disconnect( long Result )
{
}

void CWmppucplugin::Buffering( VARIANT_BOOL Start )
{
}

void CWmppucplugin::Error()
{
    CComPtr<IWMPError>      spError;
    CComPtr<IWMPErrorItem>  spErrorItem;
    HRESULT                 dwError = S_OK;
    HRESULT                 hr = S_OK;

    if (m_spCore)
    {
        hr = m_spCore->get_error(&spError);

        if (SUCCEEDED(hr))
        {
            hr = spError->get_item(0, &spErrorItem);
        }

        if (SUCCEEDED(hr))
        {
            hr = spErrorItem->get_errorCode( (long *) &dwError );
        }
    }
}

void CWmppucplugin::Warning( long WarningType, long Param, BSTR Description )
{
}

void CWmppucplugin::EndOfStream( long Result )
{
}

void CWmppucplugin::PositionChange( double oldPosition, double newPosition)
{
}

void CWmppucplugin::MarkerHit( long MarkerNum )
{
}

void CWmppucplugin::DurationUnitChange( long NewDurationUnit )
{
}

void CWmppucplugin::CdromMediaChange( long CdromNum )
{
}

void CWmppucplugin::PlaylistChange( IDispatch * Playlist, WMPPlaylistChangeEventType change )
{
    switch (change)
    {
    case wmplcUnknown:
        break;
	case wmplcClear:
        break;
	case wmplcInfoChange:
        break;
	case wmplcMove:
        break;
	case wmplcDelete:
        break;
	case wmplcInsert:
        break;
	case wmplcAppend:
        break;
	case wmplcPrivate:
        break;
	case wmplcNameChange:
        break;
	case wmplcMorph:
        break;
	case wmplcSort:
        break;
	case wmplcLast:
        break;
    default:
        break;
    }
}

void CWmppucplugin::CurrentPlaylistChange( WMPPlaylistChangeEventType change )
{
    switch (change)
    {
    case wmplcUnknown:
        break;
	case wmplcClear:
        break;
	case wmplcInfoChange:
        break;
	case wmplcMove:
        break;
	case wmplcDelete:
        break;
	case wmplcInsert:
        break;
	case wmplcAppend:
        break;
	case wmplcPrivate:
        break;
	case wmplcNameChange:
        break;
	case wmplcMorph:
        break;
	case wmplcSort:
        break;
	case wmplcLast:
        break;
    default:
        break;
    }
}

void CWmppucplugin::CurrentPlaylistItemAvailable( BSTR bstrItemName )
{
}

void CWmppucplugin::MediaChange( IDispatch * Item )
{
}

void CWmppucplugin::CurrentMediaItemAvailable( BSTR bstrItemName )
{
}

void CWmppucplugin::CurrentItemChange( IDispatch *pdispMedia)
{
}

void CWmppucplugin::MediaCollectionChange()
{
}

void CWmppucplugin::MediaCollectionAttributeStringAdded( BSTR bstrAttribName,  BSTR bstrAttribVal )
{
}

void CWmppucplugin::MediaCollectionAttributeStringRemoved( BSTR bstrAttribName,  BSTR bstrAttribVal )
{
}

void CWmppucplugin::MediaCollectionAttributeStringChanged( BSTR bstrAttribName, BSTR bstrOldAttribVal, BSTR bstrNewAttribVal)
{
}

void CWmppucplugin::PlaylistCollectionChange()
{
}

void CWmppucplugin::PlaylistCollectionPlaylistAdded( BSTR bstrPlaylistName)
{
}

void CWmppucplugin::PlaylistCollectionPlaylistRemoved( BSTR bstrPlaylistName)
{
}

void CWmppucplugin::PlaylistCollectionPlaylistSetAsDeleted( BSTR bstrPlaylistName, VARIANT_BOOL varfIsDeleted)
{
}

void CWmppucplugin::ModeChange( BSTR ModeName, VARIANT_BOOL NewValue)
{
}

void CWmppucplugin::MediaError( IDispatch * pMediaObject)
{
}

void CWmppucplugin::OpenPlaylistSwitch( IDispatch *pItem )
{
}

void CWmppucplugin::DomainChange( BSTR strDomain)
{
}

void CWmppucplugin::SwitchedToPlayerApplication()
{
}

void CWmppucplugin::SwitchedToControl()
{
}

void CWmppucplugin::PlayerDockedStateChange()
{
}

void CWmppucplugin::PlayerReconnect()
{
}

void CWmppucplugin::Click( short nButton, short nShiftState, long fX, long fY )
{
}

void CWmppucplugin::DoubleClick( short nButton, short nShiftState, long fX, long fY )
{
}

void CWmppucplugin::KeyDown( short nKeyCode, short nShiftState )
{
}

void CWmppucplugin::KeyPress( short nKeyAscii )
{
}

void CWmppucplugin::KeyUp( short nKeyCode, short nShiftState )
{
}

void CWmppucplugin::MouseDown( short nButton, short nShiftState, long fX, long fY )
{
}

void CWmppucplugin::MouseMove( short nButton, short nShiftState, long fX, long fY )
{
}

void CWmppucplugin::MouseUp( short nButton, short nShiftState, long fX, long fY )
{
}
