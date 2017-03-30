package main.java.org.jason.turretcontrol;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import main.java.org.jason.turretcontrol.exception.JamOccurredException;
import main.java.org.jason.turretcontrol.exception.NoAmmoException;
import main.java.org.jason.turretcontrol.exception.SafetyEngagedException;
import main.java.org.jason.turretcontrol.firecontrol.CS18GPIOFireControl;
import main.java.org.jason.turretcontrol.firecontrol.FireControl;
import main.java.org.jason.turretcontrol.firecontrol.G36GPIOFireControl;
import main.java.org.jason.turretcontrol.firecontrol.MockFireControl;
import main.java.org.jason.turretcontrol.motors.MotorControl;

import org.codehaus.jettison.json.JSONObject;

public class TurretControl 
{
	private AtomicInteger ammoCount;
	private int magSize;

	private FireControl fc;
	
	private HashMap<String, HashMap<String, String>> permissions;

	private final static int DEFAULT_PAN = 10;
	private final static int DEFAULT_MOVEMENT = 10;
	public final static int DEFAULT_AMMO_COUNT = 0;
	public final static int DEFAULT_MAG_SIZE = 0;

	private AtomicBoolean chamberFilled;
	private JSONObject config;
	
	private boolean enableTempSensor;
	private int tempSensorBus;
	private int tempSensorAddr;
	
	private MotorControl motorControl;
	
	public TurretControl(String configString) throws Exception
	{
		magSize = 0;
		ammoCount = new AtomicInteger(DEFAULT_AMMO_COUNT);
		chamberFilled = new AtomicBoolean(false);
		
        this.config = new JSONObject(configString);
		
		//determine fire control module from config
		
        String fireControlConfig = this.config.toString();
        
        System.out.println("Found firecontrol config " + fireControlConfig);
        
        String fireControlName = this.config.getString("type");
        

        System.out.println("Found firecontrol " + fireControlName);
        

        //////////////////////
        //fire control
        if(fireControlName.equals("MockFireControl"))
        {
        	fc = new MockFireControl(fireControlConfig);
        }
        else if(fireControlName.equals("G36GPIOFireControl"))
        {
        	fc = new G36GPIOFireControl(fireControlConfig);
        }
        else if(fireControlName.equals("CS18GPIOFireControl"))
        {
        	fc = new CS18GPIOFireControl(fireControlConfig);
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
        	
        	System.out.println("Enabling temp sensor at bus " + tempSensorBus + " and address " + Integer.toHexString(tempSensorAddr));
        	
        	double[] derp = BME280Sensor.getReading(tempSensorBus, 0x77);
        	
        	for(int i =0; i < derp.length; i++)
        	{
        		System.out.println(derp[i]);
        	}
        }
        */
        
        ///////////////////////
        //motor control
        if(!fireControlName.equals("MockFireControl") && this.config.has("panning"))
        {
        	JSONObject panningConfig = this.config.getJSONObject("panning"); 
        	
        	String hatAddr = panningConfig.getString("hat_address");
        	int hatFreq = panningConfig.getInt("hat_freq");
        	
        	JSONObject motors = panningConfig.getJSONObject("motors");
        	
        	motorControl = new MotorControl(0x60, hatFreq);
        	
    		JSONObject motorConfig; 
    		
        	Iterator keyIterator = motors.keys();
        	while(keyIterator.hasNext())
        	{
        		String motorName = (String)(keyIterator.next() );
        		
        		motorConfig = motors.getJSONObject(motorName);
        		
        		
        		System.out.println("Found motor " + motorName + " with config " + motorConfig.toString());
        		
        		
        		

				int port = motorConfig.getInt("port");
				int speed = motorConfig.getInt("speed");
				int min = motorConfig.getInt("min");
				int max = motorConfig.getInt("max");
				motorControl.addMotor(motorName, port, speed, min, max);
        		
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
	
	public synchronized void panX(int steps, int direction, int style)
	{
		try
		{
			motorControl.stepMotor(MotorControl.MOTOR_X_NAME, steps, direction, style);
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public synchronized void panY(int steps, int direction, int style)
	{
		try
		{
			motorControl.stepMotor(MotorControl.MOTOR_Y_NAME, steps, direction, style);
		}
		catch(IOException e)
		{
			e.printStackTrace();
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
		
		if(motorControl != null)
		{
			motorControl.shutdown();
		}
		
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
}
