/*
 * $Id: DeviceModuleFinder.java,v 1.5 2003/08/04 22:23:34 klitwack Exp $
 */

package com.maya.puc.common;

import com.maya.puc.proxy.PUCProxy;

import java.io.FilenameFilter;
import java.io.File;
import java.net.URL;
import java.net.JarURLConnection;
import java.net.URLClassLoader;
import java.net.MalformedURLException;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.Vector;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

public class DeviceModuleFinder {
    public static final String DEVICE_CLASS_ATTR = "Device-Class";
    public static final String DEVICE_FACTORY_ATTR = "Device-Factory";

    /**
     * Locate all the device module .jar files in the specified directory,
     * and create Device objects for them.  A device module is simply a
     * .jar file containing a class that implements Device, along with
     * whatever related classes and/or resources that the class needs.
     * The .jar file's manifest must contain a <tt>Device-Class: (class)</tt>
     * entry, and the <tt>Class-Path:</tt> entry is used, if present.
     *
     * @param directory The directory to search for device modules.
     * Use "." to specify the current directory.
     *
     * @return A List containing Device objects.
     */

    public static List getDevices(String directory) {
        FilenameFilter ff = new DeviceFilter();
        File f = new File(directory);
        String path = f.getAbsolutePath();
        path = path.replace('\\', '/');
        path = path.substring(0, path.lastIndexOf('/') + 1);
        String[] modules = f.list(ff);
        Vector devModules = new Vector();
        for (int i = 0; i < modules.length; i++) {
            try {
                String module = modules[i];
		Device2 dev = loadDevice( path, module );
		if ( dev != null )
		    devModules.add(dev);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return devModules;
    }

    public static Device2 loadDevice( File pFile )
	throws java.lang.Exception {

	return loadDevice( pFile.getParent() + File.separator, pFile.getName() );
    }

    public static Device2 loadDevice( String path, String module )
	throws java.lang.Exception  {

	URL pJarURL = createModuleURL(module, path);
	JarURLConnection uc = (JarURLConnection)pJarURL.openConnection();
	Attributes attr = uc.getMainAttributes();
	String mainClass = attr.getValue(DEVICE_CLASS_ATTR);
	if (mainClass != null) {
	    String classPath = attr.getValue(Attributes.Name.CLASS_PATH);
	    URL[] urlArray = getClassPathURLs(pJarURL, classPath, path);
	    URLClassLoader cl = new URLClassLoader(urlArray);
	    Class c = cl.loadClass(mainClass);

	    Object o = c.newInstance();

	    if ( o instanceof Device )
		return new DeviceWrapper( (Device)o );
	    else
		return (Device2)o;
	}

	return null;
    }


    /**
     * Locate all the device factory .jar files in the specified directory,
     * and create DeviceFactory objects for them.  A device factory is simply a
     * .jar file containing a class that implements DeviceFactory, along with
     * whatever related classes and/or resources that the class needs.
     * The .jar file's manifest must contain a <tt>Device-Factory: (class)</tt>
     * entry, and the <tt>Class-Path:</tt> entry is used, if present.
     *
     * @param directory The directory to search for device factories.
     * Use "." to specify the current directory.
     *
     * @return A List containing DeviceFactory objects.
     */

    public static List getDeviceFactories(String directory)
    {
      FilenameFilter ff = new DeviceFilter();
      File f = new File(directory);
      String path = f.getAbsolutePath();
      path = path.replace('\\', '/');
      path = path.substring(0, path.lastIndexOf('/') + 1);
      String[] modules = f.list(ff);
      Vector factoryModules = new Vector();
      for (int i = 0; i < modules.length; i++)
      {
        try
        {
          String module = modules[i];
          DeviceFactory factory = loadDeviceFactory(path, module);
          if (factory != null)
            factoryModules.add(factory);
        }
        catch (Exception e)
        {
          e.printStackTrace();
        }
      }
      return factoryModules;
    }

    public static DeviceFactory loadDeviceFactory(String path, String module)
        throws java.lang.Exception
    {
      URL pJarURL = createModuleURL(module, path);
      JarURLConnection uc = (JarURLConnection) pJarURL.openConnection();
      Attributes attr = uc.getMainAttributes();
      String mainClass = attr.getValue(DEVICE_FACTORY_ATTR);
      if (mainClass != null)
      {
        String classPath = attr.getValue(Attributes.Name.CLASS_PATH);
        URL[] urlArray = getClassPathURLs(pJarURL, classPath, path);
        URLClassLoader cl = new URLClassLoader(urlArray);
        Class c = cl.loadClass(mainClass);

        Object o = c.newInstance();

        if (o instanceof DeviceFactory)
          return (DeviceFactory) o;
      }

      return null;
    }


    private static URL[] getClassPathURLs(URL u, String classPath, String path) throws MalformedURLException {
        Vector urls = new Vector();
        urls.add(u);
        int x = 0;
        if (classPath != null) {
            StringTokenizer st = new StringTokenizer(classPath, " ");
            while (st.hasMoreElements()) {
                URL newU = createModuleURL((String)st.nextElement(), path);
                urls.add(newU);
            }
        }
        URL[] urlArray = new URL[urls.size()];
        for (int j = 0; j < urlArray.length; j++) {
            urlArray[j] = (URL)urls.get(j);
        }
        return urlArray;
    }

    private static URL createModuleURL(String module, String path) throws MalformedURLException {
        String urlString = "file:" + path + module + "!/";
        URL u = new URL("jar", "", urlString);
        return u;
    }

    public static class DeviceFilter implements FilenameFilter {
        public boolean accept(File dir, String name) {
            return name.endsWith(".jar");
        }
    }
}
