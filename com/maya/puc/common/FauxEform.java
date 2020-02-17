package com.maya.puc.common;

/*
 * FauxEform.java - represents a simple approximation of an Eform
 * Copyright (C) 2001 Joseph Hughes (joe@retrovirus.com)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

/**
 * A naive implementation of a serializable Eform.  An Eform
 * (as described by MAYA Design) is a data structure containing
 * attribute-value pairs.  This is a "Faux" Eform because it's a rather
 * naive implementation, compared to MAYA's.  This is a quick-n-dirty
 * implementation geared toward user-editable configuration files.  Note
 * that there is no guarantee of the ordering of attributes--if you load
 * and immediately save a FauxEform, the attributes are likely to be
 * in different places in the file.  They will, however, properly
 * retain their values.
 *
 * @author Joseph Hughes
 * @version $Id: FauxEform.java,v 1.3 2002/03/29 00:28:50 maya Exp $
 */

import java.io.*;
import java.util.Enumeration;
import java.util.Hashtable;

public class FauxEform {
    private Hashtable table = null;

    public FauxEform() {
        table = new Hashtable();
    }

    public void load(String filename) {
        String line;
        try {
            FileReader fr = new FileReader(filename);
            BufferedReader in = new BufferedReader(fr);

            while ((line = in.readLine()) != null) {

                // weed out whitespace and comments (which begin with '#')
                line = line.trim();
                if ((line.length() > 0) && !(line.startsWith("#"))) {
                    int comment = line.indexOf('#');
                    if (comment > -1)
                        line = line.substring(0, comment);

                    // use '=' to separate attribute and value
                    int brk = line.indexOf('=');
                    if (brk > -1) {
                        String attr = line.substring(0, brk).trim();
                        String val = line.substring(brk + 1).trim();
                        setDecodedAttr(attr, val);
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Error: " + e);
        }
    }

    public void save(String filename) {

        try {
            FileOutputStream fout = new FileOutputStream(filename);

            // now convert the FileOutputStream into a PrintStream

            PrintWriter out = new PrintWriter(fout);

            Object attr;
            Object val;
            Enumeration enum = table.keys();

            while (enum.hasMoreElements()) {
                attr = enum.nextElement();
                val = table.get(attr);
                if (val instanceof String)
                    out.println(attr + " = \"" + val + "\"");
                else
                    out.println(attr + " = " + val);
            }

            out.close();
        } catch (IOException e) {
            System.err.println("Error opening file: " + e);
        }
    }

    public Object getAttr(Object attr) {
        return table.get(attr);
    }

    public boolean getBooleanAttr(Object attr, boolean defaultValue) {
        Object val = table.get(attr);
        if ((val == null) || !(val instanceof Boolean)) {
            return defaultValue;
        }

        return ((Boolean) val).booleanValue();
    }

    public String getStringAttr(Object attr, String defaultValue) {
        Object val = table.get(attr);
        if ((val == null) || !(val instanceof String)) {
            return defaultValue;
        }

        return new String((String) val);
    }

    public double getDoubleAttr(Object attr, double defaultValue) {
        Object val = table.get(attr);
        if ((val == null) || !(val instanceof Double)) {
            return defaultValue;
        }

        return ((Double) val).doubleValue();
    }

    public int getIntAttr(Object attr, int defaultValue) {
        Object val = table.get(attr);
        if ((val == null) || !(val instanceof Integer)) {
            return defaultValue;
        }

        return ((Integer) val).intValue();
    }

    public void setAttr(Object attr, Object val) {

        table.put(attr, val);
    }

    public void setAttr(Object attr, int val) {
        setAttr(attr, new Integer(val));
    }

    public void setAttr(Object attr, boolean val) {
        setAttr(attr, new Boolean(val));
    }

    public void setAttr(Object attr, double val) {
        setAttr(attr, new Double(val));
    }

    private void setDecodedAttr(String attr, String val) {
        try {
            if (val.startsWith("\"")) {
                val = val.substring(1, val.length() - 1);
                setAttr((Object) attr, (Object) val);
            } else if (val.equalsIgnoreCase("true") || (val.equalsIgnoreCase("false"))) {
                setAttr((Object) attr, (Object) Boolean.valueOf(val));
            } else if (val.indexOf('.') != -1) {
                setAttr((Object) attr, (Object) Double.valueOf(val));
            } else {
                setAttr((Object) attr, (Object) Integer.valueOf(val));
            }
        } catch (Exception e) {
            System.err.println("Error parsing line: " + attr + " = " + val);
        }
    }

    public void removeAttr(Object attr) {
        table.remove(attr);
    }

    public String toString() {
        String out = "";
        Object attr;
        Enumeration enum = table.keys();

        while (enum.hasMoreElements()) {
            attr = enum.nextElement();
            out += "(" + attr + "," + table.get(attr) + ") ";
        }
        return out;
    }
}
