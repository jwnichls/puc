/////////////////////////////////////////////////////////////////////////////
//
// CPropertyDialog.h : Declaration of the CPropertyDialog
//
// Copyright (c) Microsoft Corporation. All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

#include "atlwin.h"

class CPropertyDialog : public CDialogImpl<CPropertyDialog>
{
public:
    enum { IDD = IDD_PROPERTYDIALOG };

	BEGIN_MSG_MAP(CPropertyDialog)
        MESSAGE_HANDLER( WM_INITDIALOG, OnInitDialog )
        COMMAND_ID_HANDLER( IDOK, OnOK )
        COMMAND_ID_HANDLER( IDCANCEL, OnCancel )
	END_MSG_MAP()

    CPropertyDialog(CWmppucplugin *pPlugin)
    {
        m_pPlugin = pPlugin;
    }

    LRESULT OnInitDialog( UINT uMsg, WPARAM wParam, LPARAM lParam, BOOL& fHandled )
	{
        CenterWindow();
        // initialize text
        SetDlgItemInt(IDC_PORTEDIT, m_pPlugin->m_nListenPort );
		SetDlgItemText(IDC_DEBUGEDIT, m_pPlugin->m_szDebugStr );
		return 1;
	}

    LRESULT OnOK(WORD wNotifyCode, WORD wID, HWND hwndCtl, BOOL& fHandled)
	{
        // save text
        int newPort = GetDlgItemInt( IDC_PORTEDIT );

		if ( newPort != 0 )
		{
			m_pPlugin->m_nListenPort = newPort;
	        EndDialog( IDOK );
		}
		else
			MessageBox( "Improper Port Number", "Error" );

		return 0;
	}

    LRESULT OnCancel(WORD wNotifyCode, WORD wID, HWND hwndCtl, BOOL& fHandled)
	{
        EndDialog( IDCANCEL );
		return 0;
	}

private:
    CWmppucplugin  *m_pPlugin;  // pointer to plugin object
};

