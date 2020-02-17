/*
 * $Id: TextResource.java,v 1.2 2002/03/29 00:28:50 maya Exp $
 */

package com.maya.puc.common;

import java.io.*;

public class TextResource {

    /**
     * Reads a text file in the same directory as the source class into
     * a string.  This works even if the class and text file are in the
     * same directory inside a .jar file.
     *
     * @param source A class in the same directory as the text file.
     * @param resourceName The name of the text file to retrieve.
     *
     * @return A String containing the contents of the text file, or an
     * empty string if there was an error.
     */

    public static String readToString(Class source, String resourceName) {
        InputStream stream = source.getResourceAsStream(resourceName);
        if (stream == null) {
            System.err.println("Unable to retrieve spec: " + source.getResource(resourceName));
            return "";
        }
        InputStreamReader isr = new InputStreamReader(stream);
        BufferedReader reader = new BufferedReader(isr);
        StringWriter sw = new StringWriter();
        BufferedWriter writer = new BufferedWriter(sw);
        String line;
        try {
            while((line = reader.readLine()) != null) {
                writer.write(line);
                writer.newLine();
            }
            writer.flush();
            writer.close();
        } catch (IOException e) {
            System.err.println("Error retrieving spec in CSDevice.getSpec():");
            e.printStackTrace();
        }
        return sw.toString();
    }
}
