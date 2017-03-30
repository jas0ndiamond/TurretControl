package org.jason.turretcontrol.config;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;


public class ConfigLoader 
{
	public static String getConfig(String file) throws IOException
	{
		BufferedReader dataIn = null;
		StringBuilder config = new StringBuilder();
		try
		{
			dataIn = new BufferedReader(new FileReader(file));
			
			String line;
			while( (line = dataIn.readLine()) != null)
			{
				config.append(line);
			}
		}
		finally
		{
			if(dataIn != null)
			{
				dataIn.close();
			}
		}
		
		return config.toString();
	}
	
	public static JSONObject getConfigJSONObject(String file) throws IOException, JSONException
	{
		return new JSONObject(getConfig(file));
	}
}
