package org.jason.turretcontrol.firecontrol;

import org.codehaus.jettison.json.JSONObject;
import org.jason.turretcontrol.exception.JamOccurredException;
import org.jason.turretcontrol.exception.SafetyEngagedException;
import org.jason.turretcontrol.firecontrol.cycle.CycleResult;
import org.jason.turretcontrol.gpio.GpioConfigurationFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import com.pi4j.wiringpi.GpioUtil;

public class G36GPIOFireControl extends FireControl {

	private GpioController gpio;
	private int triggerPin;
	private GpioPinDigitalOutput trigger;
	private int pirPin;
	private GpioPinDigitalInput barrelExitPir;
	
	private int maxCycleLength;
	public final static int DEFAULT_MAX_CYCLE_LEN = 1000;
	
	
	private int triggerDuration;
	public final static int DEFAULT_TRIGGER_DURATION =500;
	
	private int distanceToPir;	//mm
	
	private int lastRoundVelocity;
	private int minVelocity;
	
	private JSONObject turretConfig;
	
	private final static Logger logger = LoggerFactory.getLogger(G36GPIOFireControl.class); 

	
	public G36GPIOFireControl(String config)
	{
		super(config);
		/*
		 * Your python code uses the BCM index mode, whose port mappings are listed in the table. 
		 * In this case, the BCM port 4 maps to GPIO_7 in Pi4j instead of the GPIO_4 you use in your java code.
		 */
		
		
		
		//harvest pins from config in json format, exception for fails
		triggerPin = 7;
		pirPin = 22;
		
		triggerDuration = 500;
		
		startup();
		
	}
	
	public synchronized void burstCycle() throws JamOccurredException, SafetyEngagedException
	{
		//fire,sleep, fire,sleep fire
	}
	
	@Override
	public synchronized CycleResult cycle() throws JamOccurredException, SafetyEngagedException
	{
		CycleResult cycleResult = new CycleResult();
		
		//safety is primary cycle preventer
		if(!safety.get())
		{
			//secondary cycle preventer
			if(!isJammed.get())
			{
				boolean exited = false;
				//cycle the firing mech and confim the round exits the barrel
				//launch pir listener in separate threads
				
				isJammed.set(true);
				//trigger the relay to complete the cycle circuit
				
				//activate pir, launch thread that waits for motion of projectile
				//maybe look into setting gpio listener
				
				trigger.low();
				
				try
				{
					Thread.sleep(triggerDuration);
					trigger.high();
				}
				catch(InterruptedException e)
				{
					//not the end of the world if the trigger duration is interrupted
					//will still need to close the relay and it's worthwhile to confirm round exit
					logger.error("Cycle interrupted", e);
				}
				finally
				{
					//force the relay closed
					if(trigger.isLow())
					{
						trigger.high();
					}
				}
				//confirm round exits barrel, throw jam exception if not
				//check pir reading
				exited = true;
				
				if(exited)
				{
					isJammed.set(false);
				}
				else
				{
					isJammed.set(true);
					throw new JamOccurredException("Jam occurred during cycle- clear the jam");
				}
			}
			else
			{
				throw new JamOccurredException("Cycle attempted with jam- clear the jam");
			}
		}
		else
		{
			throw new SafetyEngagedException("Cycle attempted with safety engaged");
		}
		
		return cycleResult;
	}
	
	public void shutdown()
	{

		
		//write any state locally

		//shutdown gpio
		gpio.shutdown();
		
		//unprovision pins
		gpio.unprovisionPin(trigger);
		gpio.unprovisionPin(barrelExitPir);
	}



	@Override
	public void startup() {
		
       	GpioUtil.enableNonPrivilegedAccess();
		
		gpio = GpioFactory.getInstance();
		
		//set trigger gpio
		trigger = GpioConfigurationFactory.getGpioOutputPin(gpio, GpioConfigurationFactory.lookupPin(triggerPin), "Trigger", PinState.HIGH);

		//set pir
		barrelExitPir = gpio.provisionDigitalInputPin( GpioConfigurationFactory.lookupPin(pirPin), "BarrelExit");
		barrelExitPir.addListener
		(
			new GpioPinListenerDigital() 
			{
	            @Override
	            public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) 
	            {
	            	//barrel exit detection
	            	//set flags and record system time
	                logger.debug(" --> GPIO PIN STATE CHANGE: " + event.getPin() + " = " + event.getState());
	            }
			}
		);
	}
}
