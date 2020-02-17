/*
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: Mar 19, 2002
 * Time: 1:37:47 PM
 * To change template for new class use
 * Code Style | Class Templates options (Tools | IDE Options).
 */
package com.maya.puc.proxy;

import com.maya.puc.common.Device2;
import com.maya.puc.common.PUCServer;

import javax.swing.table.AbstractTableModel;
import javax.swing.event.TableModelEvent;
import java.util.List;
import java.util.ArrayList;

public class DeviceTableModel extends AbstractTableModel {
    private List devices = new ArrayList();
    public static final int NUM_COLUMNS = 6;
    public static final int COL_TYPE = 0;
    public static final String COL_TYPE_LABEL = "Device Type";
    public static final int COL_PORT = 1;
    public static final String COL_PORT_LABEL = "Server Port";
    public static final int COL_ACTIVE = 2;
    public static final String COL_ACTIVE_LABEL = "Active";
    public static final int COL_STATUS = 3;
    public static final String COL_STATUS_LABEL = "Status";
    public static final int COL_CONFIG = 4;
    public static final String COL_CONFIG_LABEL = "Configure";
    public static final int COL_GUI = 5;
    public static final String COL_GUI_LABEL = "Show GUI";

    public static final int[] COL_WIDTHS = {220, 70, 70, 90, 70, 70};

    private PUCServer server;

    public DeviceTableModel(List devices) {
        this.devices = devices;
        server = new PUCServer();
    }

    public void addDevice( Device2 d ) {
	devices.add( d );
	update();
    }

    public void removeDevice(Device2 device)
    {
      setValueAt(new Boolean(false), devices.indexOf(device), COL_ACTIVE);
      devices.remove(device);
      update();
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
        Device2 dev = (Device2)devices.get(rowIndex);
        switch(columnIndex) {
            case COL_GUI:
                return dev.hasGUI();
            case COL_ACTIVE:
                return dev.isManuallyActivatable();
            case COL_CONFIG:
                return true;
            case COL_PORT:
                return !dev.isRunning();
            default:
                return false;
        }
    }

    public int getRowCount() {
        return devices.size();
    }

    public int getColumnCount() {
        return NUM_COLUMNS;
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        final Device2 dev = (Device2)devices.get(rowIndex);
        switch(columnIndex) {
            case COL_TYPE:
                return dev.getName();
            case COL_PORT:
                return new Integer(dev.getPort());
            case COL_CONFIG:
                return new Boolean(false);
            case COL_ACTIVE:
                return new Boolean(dev.isRunning());
            case COL_GUI:
                return new Boolean(dev.isGUIVisible());
            case COL_STATUS:
                String status = dev.getStatus();
                if (status == null)
                    status = (dev.isRunning()?"Active":"Stopped");
                return status;
            default:
                return null;
        }
    }

    public String getColumnName(int column) {
        switch(column) {
            case COL_TYPE:
                return COL_TYPE_LABEL;
            case COL_PORT:
                return COL_PORT_LABEL;
            case COL_GUI:
                return COL_GUI_LABEL;
            case COL_CONFIG:
                return COL_CONFIG_LABEL;
            case COL_STATUS:
                return COL_STATUS_LABEL;
            case COL_ACTIVE:
                return COL_ACTIVE_LABEL;
            default:
                return super.getColumnName(column);
        }
    }

    public Class getColumnClass(int columnIndex) {
        return (getValueAt(0, columnIndex)).getClass();
    }

    public void update() {
        fireTableChanged(new TableModelEvent(this));
    }

    public void setActive(Device2 device, boolean newIsActive)
    {
      if (newIsActive)
      {
        server.addDevice(device);
        server.startListener(device);
      }
      else
      {
        stopDevice(device);
      }
    }

    public void stopDevice( Device2 device ) {

	if (device.isRunning()) {
	    device.stop();
	    server.stopListener(device);
	    server.removeDevice(device);
	}
    }

    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        Device2 dev = (Device2)devices.get(rowIndex);
        switch(columnIndex) {
            case COL_CONFIG:
              dev.configure();
              break;
            case COL_PORT:
                if (aValue instanceof Integer) {
                    Integer newPort = (Integer) aValue;
                    dev.setPort(newPort.intValue());
                }
                break;
            case COL_ACTIVE:
              if (aValue instanceof Boolean)
              {
                boolean val = ((Boolean) aValue).booleanValue();
                if (val)
                {
                  dev.start();
                }
                else
                {
                  dev.stop();
                }
              }
              break;
            case COL_GUI:
                if (aValue instanceof Boolean) {
                    Boolean gui = (Boolean) aValue;
                    dev.setGUIVisibility(gui.booleanValue());
                }
                break;
            default:
        }
    }

}
