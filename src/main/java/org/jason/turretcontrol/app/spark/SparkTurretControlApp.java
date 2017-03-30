package org.jason.turretcontrol.app.spark;

import static spark.Spark.get;

import java.util.LinkedHashMap;

import org.jason.turretcontrol.TurretControl;
import org.jason.turretcontrol.config.ConfigLoader;
import org.jason.turretcontrol.exception.JamOccurredException;
import org.jason.turretcontrol.exception.NoAmmoException;
import org.jason.turretcontrol.exception.SafetyEngagedException;
import org.jason.turretcontrol.headers.HeaderInfo;
import org.jason.turretcontrol.permissions.Permissions;

public class SparkTurretControlApp {

	private final static String CONFIG_FILE = "./config.json";
		
	public static void main(String[] args) throws Exception
	{
		//config file, and config
		String config = ConfigLoader.getConfig(args[0]);
		
		//port(4444);
		
		TurretControl tc = new TurretControl(config);
		
		//read mag size from config^^^
		tc.setMagSize(5);
		
		//////////////////////////////////////////////////////////////
		get("/fire", (req, res) -> 
		{
			LinkedHashMap<String, Object> result = new LinkedHashMap<String, Object>();
			String[] permissionsRequired = {Permissions.Names.FIRE};
			//collect json from request
			Object apiKey = req.attribute("api_key");
			
			System.out.println(apiKey);
			
			//authenticate with token
			//fire
			//return response

			try
			{
				tc.fire();	
				result.put(HeaderInfo.Names.RESULT, HeaderInfo.Values.CYCLED);
				result.put(HeaderInfo.Names.AMMO_COUNT, tc.getAmmoCount());
			}
			catch(JamOccurredException e)
			{
				//report jam
				result.put(HeaderInfo.Names.RESULT, HeaderInfo.Values.JAMMED);
				//message into result
				result.put(HeaderInfo.Names.MESSAGE, e.getMessage());
			}
			catch(SafetyEngagedException e)
			{
				//report jam
				result.put(HeaderInfo.Names.RESULT, HeaderInfo.Values.FAILED);
				//message into result
				result.put(HeaderInfo.Names.MESSAGE, e.getMessage());
			}
			catch(NoAmmoException e)
			{
				result.put(HeaderInfo.Names.RESULT, HeaderInfo.Values.FAILED);
				//message into result
				result.put(HeaderInfo.Names.MESSAGE, e.getMessage());
			}
			catch(Exception e)
			{
				result.put(HeaderInfo.Names.RESULT, HeaderInfo.Values.FAILED);
				//message into result
				result.put(HeaderInfo.Names.MESSAGE, e.getMessage());
			}
			
			//technically the request succeeded regardless of the cycle result
			res.status(200);
			res.type("application/json");

			return "{}";
		});
		
		get("/moveX", (req, res) -> 
		{
			return "{}";
		});

		get("/moveY", (req, res) -> 
		{
			return "Shutdown initiated";
		});
		
		get("/moveZ", (req, res) -> 
		{
			return "Shutdown initiated";
		});
		
		get("/panX", (req, res) -> 
		{
			LinkedHashMap<String, Object> result = new LinkedHashMap<String, Object>();
			String[] permissionsRequired = {Permissions.Names.PANX};
			
			//check if we have input for pan
			result.put(HeaderInfo.Names.RESULT, HeaderInfo.Values.SUCCESS);
			
			//tc.panX();
			
			res.status(200);
			res.type("application/json");

			return "{}";
		});
		
		get("/moveY", (req, res) -> 
		{
			LinkedHashMap<String, Object> result = new LinkedHashMap<String, Object>();
			String[] permissionsRequired = {Permissions.Names.PANY};
			
			//check if we have input for pan
			result.put(HeaderInfo.Names.RESULT, HeaderInfo.Values.SUCCESS);
			
			//tc.panY();
			
			res.status(200);
			res.type("application/json");

			return "{}";
		});

		get("/getAmmo", (req, res) -> 
		{
			LinkedHashMap<String, Object> result = new LinkedHashMap<String, Object>();
			String[] permissionsRequired = {"getAmmo"};
			
			result.put(HeaderInfo.Names.RESULT, HeaderInfo.Values.SUCCESS);
			result.put(HeaderInfo.Names.AMMO_COUNT, tc.getAmmoCount());

			res.status(200);
			res.type("application/json");

			return "{}";
		});

		get("/resetAmmo", (req, res) -> 
		{
			LinkedHashMap<String, Object> result = new LinkedHashMap<String, Object>();
			String[] permissionsRequired = {"resetAmmo"};
			
			tc.resetAmmoCount();
			
			result.put(HeaderInfo.Names.RESULT, HeaderInfo.Values.SUCCESS);
			result.put(HeaderInfo.Names.AMMO_COUNT, tc.getAmmoCount());

			res.status(200);
			res.type("application/json");

			return "{}";
		});

		get("/getMagSize", (req, res) -> 
		{
			LinkedHashMap<String, Object> result = new LinkedHashMap<String, Object>();
			String[] permissionsRequired = {"getMagSize"};
			
			result.put(HeaderInfo.Names.RESULT, HeaderInfo.Values.SUCCESS);
			result.put(HeaderInfo.Names.MAG_SIZE, tc.getMagSize());
			
			res.status(200);
			res.type("application/json");

			return "{}";
		});

		get("/setMagSize", (req, res) -> 
		{
			LinkedHashMap<String, Object> result = new LinkedHashMap<String, Object>();
			String[] permissionsRequired = {"setMagSize"};
			
			//get magsize from args
			
			result.put(HeaderInfo.Names.RESULT, HeaderInfo.Values.SUCCESS);
			result.put(HeaderInfo.Names.MAG_SIZE, tc.getMagSize());
			
			res.status(200);
			res.type("application/json");

			return "{}";		});

		get("/getSafety", (req, res) -> 
		{
			LinkedHashMap<String, Object> result = new LinkedHashMap<String, Object>();
			String[] permissionsRequired = {"getSafety"};
			
			result.put(HeaderInfo.Names.RESULT, HeaderInfo.Values.SUCCESS);
			
			if(tc.getSafety())
			{
				result.put(HeaderInfo.Names.SAFETY, HeaderInfo.Values.SAFETY_ON);
			}
			else
			{
				result.put(HeaderInfo.Names.SAFETY, HeaderInfo.Values.SAFETY_OFF);
			}
			
			res.status(200);
			res.type("application/json");

			return "{}";		});

		get("/safetyOn", (req, res) -> 
		{
			LinkedHashMap<String, Object> result = new LinkedHashMap<String, Object>();
			String[] permissionsRequired = {"enageSafety"};
			
			tc.setSafety(true);
			
			if(tc.getSafety())
			{
				result.put(HeaderInfo.Names.RESULT, HeaderInfo.Values.SUCCESS);
			}
			else
			{
				result.put(HeaderInfo.Names.RESULT, HeaderInfo.Values.FAILED);
				//message into result
				result.put(HeaderInfo.Names.MESSAGE, "Safety off after engage");
			}
			
			res.status(200);
			res.type("application/json");

			return "{}";		});
		
		get("/safetyOff", (req, res) -> 
		{
			LinkedHashMap<String, Object> result = new LinkedHashMap<String, Object>();
			String[] permissionsRequired = {"disenageSafety"};
			
			tc.setSafety(false);
			
			if(tc.getSafety())
			{
				result.put(HeaderInfo.Names.RESULT, HeaderInfo.Values.FAILED);
				//message into result
				result.put(HeaderInfo.Names.MESSAGE, "Safety on after disengage");
			}
			else
			{
				result.put(HeaderInfo.Names.RESULT, HeaderInfo.Values.SUCCESS);
			}
			
			res.status(200);
			res.type("application/json");

			return "{}";		});
		
		get("/status", (req, res) -> 
		{
			LinkedHashMap<String, Object> result = new LinkedHashMap<String, Object>();
			String[] permissionsRequired = {"status"};
			
			if(tc.getSafety())
			{
				result.put(HeaderInfo.Names.SAFETY, HeaderInfo.Values.SAFETY_ON);
			}
			else
			{
				result.put(HeaderInfo.Names.SAFETY, HeaderInfo.Values.SAFETY_OFF);
			}
			
			result.put(HeaderInfo.Names.AMMO_COUNT, tc.getAmmoCount());
			
			res.status(200);
			res.type("application/json");

			return "{}";		});
		
		get("/shutdown", (req, res) -> 
		{
			LinkedHashMap<String, Object> result = new LinkedHashMap<String, Object>();
			String[] permissionsRequired = {"shutdown"};
			
			result.put(HeaderInfo.Names.RESULT, HeaderInfo.Values.SUCCESS);
			tc.shutdown();
			
			res.status(200);
			res.type("application/json");

			return "{}";		});
		
		get("/reset", (req, res) -> 
		{
			LinkedHashMap<String, Object> result = new LinkedHashMap<String, Object>();
			String[] permissionsRequired = {"reset"};
			
			result.put(HeaderInfo.Names.RESULT, HeaderInfo.Values.SUCCESS);
			tc.reset();
			
			res.status(200);
			res.type("application/json");

			return "{}";
		});
	}
}
