package main.java.org.jason.turretcontrol.gpio;

import com.pi4j.io.gpio.Pin;

public class GpioPirSensorListener extends Thread 
{
	private Pin pin;
	private int timeout;

	public GpioPirSensorListener(Pin pin, int timeout)
	{
		this.timeout = timeout;
		this.pin = pin;
	}
	
	public void run()
	{
		
	}
}
