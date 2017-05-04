package org.jason.turretcontrol.motors.wrapper;

import java.io.IOException;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MotorControl {
	
	public final static int DIRECTION_FORWARD = 0;
	public final static int DIRECTION_BACKWARD = 1;
	
	public final static int STEP_SINGLE = 0;
	public final static int STEP_DOUBLE = 1;
	public final static int STEP_INTERLEAVE = 2;
	public final static int STEP_MICROSTEP = 3;
	
	public final static String MOTOR_X_NAME = "motor_x";
	public final static String MOTOR_Y_NAME = "motor_y";
	
	public final static int MOTOR_X_ID = 1;
	public final static int MOTOR_Y_ID = 2;
	
	private int xPos;
	private int yPos;
		
	private final static Logger logger = LoggerFactory.getLogger(MotorControl.class); 

	
	private final static String stepperCommandSyscall = "/usr/bin/python ./scripts/motor/Adafruit_MotorHAT/StepperCommand.py";
	
	private HashMap<String, StepperMotor> motors;

	private final static HashMap<Integer, String> directions = new HashMap<Integer, String>() 
	{
		private static final long serialVersionUID = -4943318151848026056L;
		{
			put(DIRECTION_FORWARD, "f");
			put(DIRECTION_BACKWARD, "b");
		}
	};

	public MotorControl() throws Exception
	{
		motors = new HashMap<String, StepperMotor>();
		xPos = 0;
		yPos =0;
	}
	
	public int getXPos()
	{
		return xPos;
	}
	
	public int getYPos()
	{
		return yPos;
	}
	
	public synchronized void panHome() throws IOException
	{
		panXHome();
		panYHome();
	}
	
	public synchronized void panXHome() throws IOException
	{
		panXTo(0);
	}
	
	public synchronized void panYHome() throws IOException
	{
		panYTo(0);
	}
	
	public synchronized void panXTo(int x) throws IOException
	{
		//determine x position and how to step to x
		if(xPos > x)
		{
			stepMotorX(Math.abs(xPos - x), DIRECTION_BACKWARD);
		}
		else if(xPos < x)
		{
			stepMotorX(Math.abs(xPos - x), DIRECTION_FORWARD);
		}
	}
	
	public synchronized void panYTo(int y) throws IOException
	{
		if(yPos > y)
		{
			stepMotorY(Math.abs(yPos - y), DIRECTION_BACKWARD);
		}
		else if(yPos < y)
		{
			stepMotorY(Math.abs(yPos - y), DIRECTION_FORWARD);
		}
	}
	
	public synchronized void panTo(int x, int y) throws IOException
	{
		panXTo(x);
		panYTo(y);
	}
	
	public void addMotor(String name, int port, int speed, int style, int min, int max, int stepsPerRev)
	{
		motors.put(name, new StepperMotor(name, port, style, speed, min, max, stepsPerRev) );
		
		logger.info("Added motor " + name + ": " + motors.get(name).toString());
	}
	
	private synchronized void stepMotor(String name, int steps, int direction) throws IOException
	{
		if(motors.containsKey(name) && directions.containsKey(direction) )
		{
			//StepperCommand s 1 200 f 30 3 3
			//[motor steps-per-rev direction steps style speed]
			
			if( steps > 0 )
			{
				StepperMotor thisMotor = motors.get(name);
				
				StringBuilder syscall = new StringBuilder()
					.append(stepperCommandSyscall)
					.append(" s ")
					.append(thisMotor.getPort())
					.append(" ")
					.append(thisMotor.stepsPerRev)
					.append(" ")
					.append(directions.get(direction))
					.append(" ")
					.append(steps)
					.append(" ")
					.append(thisMotor.getStyle())
					.append(" ")
					.append(thisMotor.getSpeed());
				
				logger.debug("Syscall: " + syscall);
				
				try
				{							
					Runtime.getRuntime().exec(syscall.toString()).waitFor();
				}
				catch(InterruptedException e)
				{
					logger.error("Motor panning interrupted", e);
				}
				
				logger.info("Moving motor " + name + " " + steps + " steps in direction " + direction);
			}
			else
			{
				logger.debug("Ignoring 0 step instruction");
			}
		}
		else
		{
			logger.warn("Could not move motor");
		}
	}
	
	public synchronized void stepMotorX(int steps, int direction) throws IOException
	{
		if(steps > 0 && directions.containsKey(direction))
		{
			StepperMotor thisMotor = motors.get(MOTOR_X_NAME);
			
			int max = thisMotor.getMax();
			int min = thisMotor.getMin();
			
			if(direction == DIRECTION_FORWARD)
			{							
				if(xPos + steps > max)
				{
					steps = max - xPos;
					xPos = max;
				}
				else
				{
					xPos += steps;
				}
			}
			else if(direction == DIRECTION_BACKWARD)
			{
				if(xPos - steps < min)
				{
					steps = Math.abs(min) + xPos; 
					xPos = min;
				}
				else
				{
					xPos -= steps;
				}
			}
		
			stepMotor(MOTOR_X_NAME, steps, direction);
		}
		else
		{
			logger.warn("Cannot execute panX - invalid step count or direction");
		}
	}
	
	public synchronized void stepMotorY(int steps, int direction) throws IOException
	{
		if(steps > 0 && directions.containsKey(direction))
		{
			StepperMotor thisMotor = motors.get(MOTOR_Y_NAME);
			
			int max = thisMotor.getMax();
			int min = thisMotor.getMin();
			
			if(direction == DIRECTION_FORWARD)
			{							
				if(yPos + steps > max)
				{
					steps = max - yPos;
					yPos = max;
				}
				else
				{
					yPos += steps;
				}
			}
			else if(direction == DIRECTION_BACKWARD)
			{
				if(yPos - steps < min)
				{
					steps = Math.abs(min) + yPos; 
					yPos = min;
				}
				else
				{
					yPos -= steps;
				}
			}
		
			stepMotor(MOTOR_Y_NAME, steps, direction);
		}
		else
		{
			logger.warn("Cannot execute panX - invalid step count or direction");
		}
	}
	
	public void killMotors() throws IOException
	{
		//most cases, x and y are held. kill y and hold x to prevent possible swinging.
		//kill x when y has had time to stablize
		
		killYMotor();
		try
		{
			Thread.sleep(1500);
		}
		catch(InterruptedException e)
		{
			logger.error("Motor kill sleep interrupted", e);
		}
		
		killXMotor();

	}
	
	public void killXMotor() throws IOException
	{
		Runtime.getRuntime().exec(stepperCommandSyscall + " k " + MOTOR_X_ID);
	}
	
	public void killYMotor() throws IOException
	{
		Runtime.getRuntime().exec(stepperCommandSyscall + " k " + MOTOR_Y_ID);
	}
	
	private class StepperMotor
	{
		private String name;
		private int port;
		private int style;
		private int speed;
		private int min;
		private int max;
		private int stepsPerRev;

		public StepperMotor(String name, int port, int style, int speed, int min, int max, int stepsPerRev)
		{
			this.name = name;
			this.port = port;
			this.style = style;
			this.speed = speed;
			this.min = min;
			this.max = max;
			this.stepsPerRev = stepsPerRev;
		}
		
		public int getMin()
		{
			return min;
		}
		
		public int getMax()
		{
			return max;
		}
		
		public String getName() {
			return name;
		}

		public int getPort() {
			return port;
		}

		public int getStyle() {
			return style;
		}

		public int getSpeed() {
			return speed;
		}

		public int getStepsPerRev() {
			return stepsPerRev;
		}
		
		@Override
		public String toString()
		{
			return new StringBuilder()
				.append("name: ")
				.append(getName())
				.append(", port: ")
				.append(getPort())
				.append(", style: ")
				.append(getStyle())
				.append(", speed: ")
				.append(getSpeed())
				.append(", min: ")
				.append(getMin())
				.append(", max: ")
				.append(getMax())
				.append(", stepsPerRev: ")
				.append(getStepsPerRev())
				.toString();
		}
	}
}
