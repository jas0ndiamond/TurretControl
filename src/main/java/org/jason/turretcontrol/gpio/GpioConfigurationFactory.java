package org.jason.turretcontrol.gpio;

import java.util.HashMap;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;

public abstract class GpioConfigurationFactory 
{
	private final static HashMap<Integer, Pin> pinLookup = new HashMap<Integer, Pin>() 
	{
		private static final long serialVersionUID = 7651562087368603073L;
		{
			put(0, RaspiPin.GPIO_00);
			put(1, RaspiPin.GPIO_01);
			put(2, RaspiPin.GPIO_02);
			put(3, RaspiPin.GPIO_03);
			put(4, RaspiPin.GPIO_04);
			put(5, RaspiPin.GPIO_05);
			put(6, RaspiPin.GPIO_06);
			put(7, RaspiPin.GPIO_07);
			put(8, RaspiPin.GPIO_08);
			put(9, RaspiPin.GPIO_09);
			put(10, RaspiPin.GPIO_10);
			put(11, RaspiPin.GPIO_11);
			put(12, RaspiPin.GPIO_12);
			put(13, RaspiPin.GPIO_13);
			put(14, RaspiPin.GPIO_14);
			put(15, RaspiPin.GPIO_15);
			put(16, RaspiPin.GPIO_16);
			put(17, RaspiPin.GPIO_17);
			put(18, RaspiPin.GPIO_18);
			put(19, RaspiPin.GPIO_19);
			put(20, RaspiPin.GPIO_20);
			put(21, RaspiPin.GPIO_21);
			put(22, RaspiPin.GPIO_22);
		}
	};
	
	private final static String DEFAULT_PIN_NAME = "DEFAULT_GPIO_PIN";

	public static Pin lookupPin(int pinNum)
	{
		return pinLookup.get(pinNum);
	}
	
	public static GpioPinDigitalInput getGpioInputPin(GpioController gpio, Pin pin, PinPullResistance defaultPPRes)
	{
		return getGpioInputPin(gpio, pin, DEFAULT_PIN_NAME, defaultPPRes);
	}
	
	public static GpioPinDigitalInput getGpioInputPin(GpioController gpio, Pin pin, String name, PinPullResistance defaultPPRes)
	{
		return gpio.provisionDigitalInputPin(pin, name, defaultPPRes);
	}
	
	public static GpioPinDigitalOutput getGpioOutputPin(GpioController gpio, Pin pin, PinState defaultState)
	{
		return getGpioOutputPin(gpio, pin, DEFAULT_PIN_NAME, defaultState);
	}
	
	public static GpioPinDigitalOutput getGpioOutputPin(GpioController gpio, Pin pin, String name, PinState defaultState)
	{

		GpioPinDigitalOutput thisPin = gpio.provisionDigitalOutputPin(pin, name, defaultState);
		
        // configure the pin shutdown behavior; these settings will be 
        // automatically applied to the pin when the application is terminated
		thisPin.setShutdownOptions(true, defaultState);
		
		return thisPin;
	}
	
	public static boolean supportsGPIO()
	{
		boolean retval = false;
		try
		{
			GpioFactory.getInstance().shutdown();
			retval  = true;
		}
		catch(Exception e)
		{
			//don't care
		}
		
		return retval;
	}
}
