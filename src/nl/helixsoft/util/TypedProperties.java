package nl.helixsoft.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;


/**
 * Wrapper for java.util.Properties with the following features
 * 
 * instead of String keys, keys need to implement interface KeyType. 
 * This enables you to use an enum instead of a string for the key.
 * 
 * The KeyType interface has the method getDefault() -> this
 * allows you to set the default right at the declaration of the preference 
 * 
 * you can use the set/getInt, set/getString and set/getFile to
 * automatically convert to the right type
 */
public class TypedProperties<T extends KeyType>
{
	private final Properties properties;
	
	public TypedProperties() 
	{
		properties = new Properties();
	}

	public TypedProperties(T[] defaults) 
	{
		Properties defaultProps = new Properties();
		for (T t : defaults)
		{
			defaultProps.setProperty("" + t, t.getDefault());
		}
		properties = new Properties(defaultProps);
	}

	public void store(OutputStream os, String comments) throws IOException
	{
		properties.store(os, comments);
	}

	public void load(InputStream is) throws IOException
	{
		properties.load(is);
	}

	public void setInt (T p, int value)
	{
		properties.setProperty ("" + p, "" + value);
	}

	public int getInt (T p)
	{
		return Integer.parseInt (properties.getProperty ("" + p));
	}

	public String getString (T p)
	{
		return properties.getProperty ("" + p);
	}
	
	public void setString (T p, String value)
	{
		properties.setProperty ("" + p, value);
	}
	
	public File getFile (T p)
	{
		return new File (properties.getProperty ("" + p));
	}
	
	public void setFile (T p, File value)
	{
		properties.setProperty("" + p, "" + value);
	}
}
