package org.jason.turretcontrol.sensors;

import org.jason.turretcontrol.gpio.GpioConfigurationFactory;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import com.pi4j.wiringpi.GpioUtil;

public class PIRSensor 
{
	public static void main(String args[])
	{
       	GpioUtil.enableNonPrivilegedAccess();
		
		GpioController gpio = GpioFactory.getInstance();
		
		int pirPin = 3;
		GpioPinDigitalInput barrelExitPir = 
				GpioConfigurationFactory.getGpioInputPin(gpio, GpioConfigurationFactory.lookupPin(pirPin ), "BarrelExit", PinPullResistance.PULL_DOWN);
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
		
		System.out.println("Starting up");
		
		int waitTime = 0, max = 30000;
		while(waitTime < max)
		{
			try 
			{
				Thread.sleep(1000);
				waitTime += 1000;
			} 
			catch (InterruptedException e) 
			{
				e.printStackTrace();
			}
		}
		
		System.out.println("Shutting down");
		
		gpio.shutdown();
	}
}
