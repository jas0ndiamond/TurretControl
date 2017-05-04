package org.jason.turretcontrol;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.codehaus.jettison.json.JSONObject;
import org.jason.turretcontrol.exception.JamOccurredException;
import org.jason.turretcontrol.exception.NoAmmoException;
import org.jason.turretcontrol.exception.SafetyEngagedException;
import org.jason.turretcontrol.firecontrol.CS18GPIOFireControl;
import org.jason.turretcontrol.firecontrol.FireControl;
import org.jason.turretcontrol.firecontrol.G36GPIOFireControl;
import org.jason.turretcontrol.firecontrol.MockFireControl;
import org.jason.turretcontrol.motors.wrapper.MotorControl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TurretControl 
{
	private AtomicInteger ammoCount;
	private int magSize;

	private FireControl fc;
	
	private HashMap<String, Integer> supportedMagazines;

	public final static int DEFAULT_AMMO_COUNT = 0;
	public final static int DEFAULT_MAG_SIZE = 0;

	private AtomicBoolean chamberFilled;
	private JSONObject config;
	
	private boolean enableTempSensor;
	private int tempSensorBus;
	private int tempSensorAddr;
	
	private MotorControl motorControl;
	
	private final static Logger logger = LoggerFactory.getLogger(TurretControl.class); 
	
	public TurretControl(String configString) throws Exception
	{
		supportedMagazines = new HashMap<>();
		magSize = 0;
		ammoCount = new AtomicInteger(DEFAULT_AMMO_COUNT);
		chamberFilled = new AtomicBoolean(false);
		
        this.config = new JSONObject(configString);
		
		//determine fire control module from config
		
        String turretControlConfig = this.config.toString();
        
        logger.debug("Found turretcontrol config " + turretControlConfig);
        
        String fireControlName = this.config.getJSONObject("firecontrol").getString("name");
        

        logger.debug("Found firecontrol " + fireControlName);
        

        //////////////////////
        //fire control
        if(fireControlName.equals("MockFireControl"))
        {
        	fc = new MockFireControl(this.config.getString("firecontrol"));
        }
        else if(fireControlName.equals("G36GPIOFireControl"))
        {
        	fc = new G36GPIOFireControl(this.config.getString("firecontrol"));
        }
        else if(fireControlName.equals("CS18GPIOFireControl"))
        {
        	fc = new CS18GPIOFireControl(this.config.getString("firecontrol"));
        }
        else
        {
        	throw new Exception("Unrecognized fire control: " + fireControlName);
        }
        
        ///////////////////////
        //temperature sensor
        enableTempSensor = false;
        
        /*
        //temp sensor
        if(this.config.getJSONObject("turret").has("tempSensor"))
        {
        	enableTempSensor = true;
        	
        	tempSensorBus = this.config.getJSONObject("turret").getJSONObject("tempSensor").getInt("bus");
        	tempSensorAddr = Integer.parseInt(this.config.getJSONObject("turret").getJSONObject("tempSensor").getString("address").substring(2), 16 );
        	
        	logger.debug("Enabling temp sensor at bus " + tempSensorBus + " and address " + Integer.toHexString(tempSensorAddr));
        	
        	double[] derp = BME280Sensor.getReading(tempSensorBus, 0x77);
        	
        	for(int i =0; i < derp.length; i++)
        	{
        		logger.debug(derp[i]);
        	}
        }
        */
        
        ///////////////////////
        //motor control
        //if(!fireControlName.equals("MockFireControl") && this.config.has("panning"))
        if(this.config.has("panning"))
        {
        	JSONObject panningConfig = this.config.getJSONObject("panning"); 
        	        	
//        	String hatAddr = panningConfig.getString("hat_address");
//        	int hatFreq = panningConfig.getInt("hat_freq");
        	
        	JSONObject motors = panningConfig.getJSONObject("motors");
        	
        	logger.debug("Found motors config: " + motors.toString());
        	
        	motorControl = new MotorControl();
        	        	
    		JSONObject motorConfig; 
    		
        	Iterator keyIterator = motors.keys();
        	while(keyIterator.hasNext())
        	{
        		String motorName = (String)(keyIterator.next() );
        		
        		motorConfig = motors.getJSONObject(motorName);
        		
        		logger.debug("Found motor " + motorName + " with config " + motorConfig.toString());
        		
				int port = motorConfig.getInt("port");
				int speed = motorConfig.getInt("speed");
				int min = motorConfig.getInt("min");
				int max = motorConfig.getInt("max");
				int style = motorConfig.getInt("style");
				int stepsPerRev = motorConfig.getInt("steps_per_revolution");
				motorControl.addMotor(motorName, port, speed, style, min, max, stepsPerRev);
        	}        	
        }
        
        /////////////////////////
        //magazine management
        if(this.config.has("magazines"))
        {
        	JSONObject magazineConfig = config.getJSONObject("magazines");	
        	Iterator<?> keyIterator = magazineConfig.keys();
        	String magName;
        	int capacity;
        	while(keyIterator.hasNext())
        	{
        		magName = (String)(keyIterator.next() );
        		capacity = magazineConfig.getJSONObject(magName).getInt("capacity");
        		
        		logger.debug("Adding supported magazine " + magName + " with capacity " + capacity );
        		
        		supportedMagazines.put(magName, capacity);
        	}
        }
	}
	
	public synchronized int getAmmoCount()
	{
		return ammoCount.get();
	}
	
	public synchronized void setAmmoCount(int count)
	{
		if(count >= 0)
		{
			ammoCount.set(count);
		}
	}
	
	public synchronized void resetAmmoCount()
	{
		setAmmoCount(magSize);
	}
	
	public synchronized int getMagSize()
	{
		return magSize;
	}
	
	public synchronized void setMagSize(int size)
	{
		if(size > 0)
		{
			magSize = size;
		}
	}
	
	public synchronized int getPositionX()
	{
		return motorControl.getXPos();
	}
	
	public synchronized int getPositionY()
	{
		return motorControl.getYPos();
	}
	
	public void killMotors() 
	{
		if(motorControl != null)
		{
			try
			{
				motorControl.killMotors();
			}
			catch(IOException e)
			{
				logger.error("Exception executing killmotors", e);
			}
		}
	}
	
	public synchronized void clearJam()
	{
		chamberFilled.set(false);
		fc.setIsJammed(false);
	}
	
	public synchronized void fire() throws JamOccurredException, SafetyEngagedException, NoAmmoException
	{		
		if
		(
			ammoCount.get() > 0
		)
		{
			//avoid double feeds
			if(!chamberFilled.get())
			{
				ammoCount.decrementAndGet();
			}
			chamberFilled.set(true);
			//get round from mag
		
			try
			{
				//cycle firecontrol
				//fire control confirms round exit and throws jamexception otherwise
				
				fc.cycle();
				
				chamberFilled.set(false);
			}
			catch(Exception e)
			{
				throw e;
			}
			finally
			{
				//update service life of non-fc parts
				//retrieve telemetry from fc sensors
			}
		}
		else
		{
			throw new NoAmmoException("No ammo- reload");
		}
	}
	
	public synchronized void panX(int steps, int direction)
	{
		try
		{
			motorControl.stepMotorX(steps, direction);
		}
		catch(IOException e)
		{
			logger.error("Exception executing panX", e);
		}
	}
	
	public synchronized void panY(int steps, int direction)
	{
		try
		{
			motorControl.stepMotorY(steps, direction);
		}
		catch(IOException e)
		{
			logger.error("Exception executing panY", e);
		}	
	}
	
	public synchronized void panXTo(int pos)
	{
		try 
		{
			motorControl.panXTo(pos);
		} 
		catch (IOException e) 
		{
			logger.error("Exception executing panXTo", e);
		}
	}
	
	public synchronized void panYTo(int pos)
	{
		try 
		{
			motorControl.panYTo(pos);
		} 
		catch (IOException e) 
		{
			logger.error("Exception executing panYTo", e);
		}
	}
	
	public synchronized void panTo(int xPos, int yPos)
	{
		try 
		{
			motorControl.panTo(xPos, yPos);
		} 
		catch (IOException e) 
		{
			logger.error("Exception executing panTo", e);
		}
	}
	
	public synchronized void panXHome()
	{
		try 
		{
			motorControl.panXHome();
		} 
		catch (IOException e) 
		{
			logger.error("Exception executing panXHome", e);
		}
	}
	
	public synchronized void panYHome()
	{
		try 
		{
			motorControl.panYHome();
		} 
		catch (IOException e) 
		{
			logger.error("Exception executing panYHome", e);
		}
	}
	
	public synchronized void panHome()
	{
		try 
		{
			motorControl.panHome();
		} 
		catch (IOException e) 
		{
			logger.error("Exception executing panHome", e);
		}
	}
	
	public boolean isChamberFilled()
	{
		return chamberFilled.get();
	}
	
	public synchronized boolean getSafety()
	{
		return fc.getSafety();
	}
	
	public synchronized void setSafety(boolean state)
	{
		fc.setSafety(state);
	}
	
	public void shutdown()
	{
		fc.shutdown();
		
		panHome();
		
		killMotors();
	}
	
	public void reset() throws InterruptedException
	{
		fc.reset();
	}
	
	public String getState()
	{
		String retval = null;
		
		if(enableTempSensor)
		{
			
		}
		
		return retval ;
	}

	public synchronized void reload(String magName) 
	{
		Integer magCap = supportedMagazines.get(magName);
		
		if(magCap != null)
		{
			setAmmoCount(magCap);
		}
		else
		{
			logger.warn("Attempted reload of unsupported magazine: " + magName);
		}
	}

	public String[] getSupportedMagazines() 
	{
		return supportedMagazines.keySet().toArray(new String[supportedMagazines.size()]);
	}
}
