package org.jason.turretcontrol.firecontrol;

import org.jason.turretcontrol.exception.JamOccurredException;
import org.jason.turretcontrol.exception.NoAmmoException;
import org.jason.turretcontrol.exception.SafetyEngagedException;
import org.jason.turretcontrol.gpio.GpioConfigurationFactory;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import com.pi4j.wiringpi.GpioUtil;

public class CS18GPIOFireControl extends FireControl {

	private GpioController gpio;
	private int triggerPin;
	private GpioPinDigitalOutput trigger;
	
	private int accelPin;
	private GpioPinDigitalOutput accel;
	
	private int pirPin;
	private GpioPinDigitalInput barrelExitPir;
	
	private int maxCycleLength;
	public final static int DEFAULT_MAX_CYCLE_LEN = 1000;
	
	
	private int triggerDuration;
	public final static int DEFAULT_TRIGGER_DURATION =500;
	
	private int distanceToPir;	//mm
	
	private int lastRoundVelocity;
	private int minVelocity;
	private long accelStartDuration;
	private long accelStopDuration;
	
	public CS18GPIOFireControl(String config)
	{
		super(config);
		/*
		 * Your python code uses the BCM index mode, whose port mappings are listed in the table. 
		 * In this case, the BCM port 4 maps to GPIO_7 in Pi4j instead of the GPIO_4 you use in your java code.
		 */
		
		
		//harvest pins from config in json format, exception for fails
		triggerPin = 7;
		accelPin = 11;
		
		pirPin = 12;
		
		triggerDuration = 450;
		accelStartDuration = 750;
		accelStopDuration = 250;
		
		startup();
	}
	
	public synchronized void burstCycle() throws JamOccurredException, SafetyEngagedException, NoAmmoException
	{
		//fire,sleep, fire,sleep fire
	}
	
	@Override
	public synchronized void cycle() throws JamOccurredException, SafetyEngagedException
	{
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
				

				
				try
				{
					//start the flywheels and wait till they get up to speed
					accel.low();
					Thread.sleep(accelStartDuration);
					
					//fire, then unfire the blaster
					trigger.low();
					Thread.sleep(triggerDuration);
					trigger.high();
					
					//stop the flywheels
					Thread.sleep(accelStopDuration);
					accel.low();
				}
				catch(InterruptedException e)
				{
					//not the end of the world if the trigger duration is interrupted
					//will still need to close the relay and it's worthwhile to confirm round exit
					e.printStackTrace();
				}
				finally
				{
					//force the accel relay closed
					if(accel.isLow())
					{
						accel.high();
					}
					
					//force the trigger relay closed
					if(trigger.isLow())
					{
						trigger.high();
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
			}
			else
			{
				throw new JamOccurredException("Cycle attempted with jam- clear the jam");
			}
		}
		else
		{
			//throw new NoAmmoException("Cycle attempted without ammo- reload");
			throw new SafetyEngagedException("Cycle attempted with safety engaged");
		}
	}
	
	public void shutdown()
	{
		//shutdown pin inputs
		gpio.shutdown();
		
		//unprovision pins
		gpio.unprovisionPin(trigger);
		gpio.unprovisionPin(accel);
		gpio.unprovisionPin(barrelExitPir);
	}



	@Override
	public void startup() {
       	GpioUtil.enableNonPrivilegedAccess();
		
		gpio = GpioFactory.getInstance();

		//set trigger gpio
		trigger = GpioConfigurationFactory.getGpioOutputPin(gpio, GpioConfigurationFactory.lookupPin(triggerPin), "Trigger", PinState.HIGH);

		
		//set trigger gpio
		accel = GpioConfigurationFactory.getGpioOutputPin(gpio, GpioConfigurationFactory.lookupPin(accelPin), "Trigger", PinState.HIGH);

		//set pir
		barrelExitPir = GpioConfigurationFactory.getGpioInputPin(gpio, GpioConfigurationFactory.lookupPin(pirPin), "BarrelExit", PinPullResistance.PULL_DOWN);
		barrelExitPir.addListener
		(
			new GpioPinListenerDigital() 
			{
	            @Override
	            public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) 
	            {
	            	//barrel exit detection
	            	//set flags and record system time
	                System.out.println(" --> GPIO PIN STATE CHANGE: " + event.getPin() + " = " + event.getState());
	            }
			}
		);
	}
}
