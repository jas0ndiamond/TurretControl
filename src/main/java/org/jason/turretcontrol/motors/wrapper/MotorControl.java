package org.jason.turretcontrol.motors.wrapper;

import java.io.IOException;
import java.util.HashMap;

import org.jason.turretcontrol.motors.MotorMotionResult;
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
	
	public void addMotor(String name, int port, int speed, int style, int min, int max, int stepsPerRev)
	{
		motors.put(name, new StepperMotor(name, port, style, speed, min, max, stepsPerRev) );
		
		logger.info("Added motor " + name + ": " + motors.get(name).toString());
	}
	
	public MotorMotionResult panHome()
	{
		MotorMotionResult result = new MotorMotionResult();
		
		result.setX(panXHome().getX());
		result.setY(panYHome().getY());
				
		return result;
	}
	
	public MotorMotionResult panXHome() 
	{
		return panXTo(0);
	}
	
	public MotorMotionResult panYHome() 
	{
		return panYTo(0);
	}
	
	public MotorMotionResult panXTo(int x) 
	{
		MotorMotionResult result;
		
		//determine x position and how to step to x
		int stepCount = Math.abs(xPos - x);
		if(xPos > x)
		{
			result = stepMotorX(stepCount, DIRECTION_BACKWARD);
		}
		else if(xPos < x)
		{
			result = stepMotorX(stepCount, DIRECTION_FORWARD);
		}
		else
		{
			//move zero steps
			result = new MotorMotionResult();
		}
		
		return result;
	}
	
	public MotorMotionResult panYTo(int y)
	{
		MotorMotionResult result;
		
		if(yPos > y)
		{
			result = stepMotorY(Math.abs(yPos - y), DIRECTION_BACKWARD);
		}
		else if(yPos < y)
		{
			result = stepMotorY(Math.abs(yPos - y), DIRECTION_FORWARD);
		}
		else
		{
			//move zero steps
			result = new MotorMotionResult();
		}
		

		return result;
	}
	
	public MotorMotionResult panTo(int x, int y) 
	{
		MotorMotionResult result = new MotorMotionResult();
		
		result.setX(panXTo(x).getX());
		result.setY(panYTo(y).getY());
				
		return result;
	}
		
	public MotorMotionResult stepMotorX(int steps, int direction)
	{
		MotorMotionResult result = new MotorMotionResult();
		
		if(steps > 0 && directions.containsKey(direction))
		{
			int oldXPos = xPos;
			
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
		
			result.setX(steps, oldXPos, xPos);
			stepMotor(MOTOR_X_NAME, steps, direction);
			logger.debug("Motor " + MOTOR_X_NAME + " move result: " + result.toString());
		}
		else
		{
			logger.warn("Cannot execute panX - invalid step count or direction");
		}
		
		return result;
	}
	
	public MotorMotionResult stepMotorY(int steps, int direction) 
	{
		MotorMotionResult result = new MotorMotionResult();
		
		if(steps > 0 && directions.containsKey(direction))
		{
			int oldYPos = yPos;
			
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
		
			result.setY(steps, oldYPos, yPos);
			stepMotor(MOTOR_Y_NAME, steps, direction);
			logger.debug("Motor " + MOTOR_Y_NAME + " move result: " + result.toString());
		}
		else
		{
			logger.warn("Cannot execute stepMotorY - invalid step count or direction");
		}
		
		return result;
	}
	
	private void stepMotor(String name, int steps, int direction)
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
				catch(IOException e)
				{
					logger.error("IOException during pan operation", e);
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
	
	public void killMotors()
	{
		//killing anywhere other than home will likely cause swinging
		panHome();
		
		//most cases, x and y are held. kill y while holding x to prevent possible swinging.
		//kill x when y has had time to stabilize
		
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
	
	public void killXMotor()
	{
		try 
		{
			Runtime.getRuntime().exec(stepperCommandSyscall + " k " + MOTOR_X_ID);
		} 
		catch (IOException e) 
		{
			logger.error("IOException killing motorX", e);
		}
	}
	
	public void killYMotor()
	{
		try 
		{
			Runtime.getRuntime().exec(stepperCommandSyscall + " k " + MOTOR_Y_ID);
		} 
		catch (IOException e) 
		{
			logger.error("IOException killing motorY", e);
		}
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
	
	private class MotorPosition
	{
		private int x;
		private int y;
		
		public int getX() {
			return x;
		}
		public void setX(int x) {
			this.x = x;
		}
		public int getY() {
			return y;
		}
		public void setY(int y) {
			this.y = y;
		}
		public MotorPosition(int x, int y) {
			this.x = x;
			this.y = y;
		}

		
	}
}
