package org.jason.turretcontrol.motors.wrapper;

import java.io.IOException;
import java.util.HashMap;

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
	}
	
	public void addMotor(String name, int port, int speed, int style, int stepsPerRev)
	{
		motors.put(name, new StepperMotor(name, port, style, speed, stepsPerRev) );
		
		System.out.println("Added motor " + name + ": " + motors.get(name).toString());
	}
	
	public synchronized void stepMotor(String name, int steps, int direction) throws IOException
	{
		if(motors.containsKey(name) && directions.containsKey(direction) && steps > 0  )
		{
			
			//StepperCommand s 1 200 f 30 3 3
			//[motor steps-per-rev direction steps style speed]
			
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
			
			System.out.println("Syscall: " + syscall);
			
			try
			{
				Runtime.getRuntime().exec(syscall.toString()).waitFor();
			}
			catch(InterruptedException e)
			{
				e.printStackTrace();
			}
			
			System.out.println("Moving motor " + name + " " + steps + " steps in direction " + direction);
			
			//write state
		}
		else
		{
			System.out.println("Could not move motor");
		}
	}
	
	public synchronized void stepMotorX(int steps, int direction) throws IOException
	{
		stepMotor(MOTOR_X_NAME, steps, direction);
	}
	
	public synchronized void stepMotorY(int steps, int direction) throws IOException
	{
		stepMotor(MOTOR_Y_NAME, steps, direction);
	}
	
	public void killMotors() throws IOException
	{
		killXMotor();
		killYMotor();
	}
	
	public void killXMotor() throws IOException
	{
		Runtime.getRuntime().exec(stepperCommandSyscall + " k " + MOTOR_X_ID);
	}
	
	public void killYMotor() throws IOException
	{
		Runtime.getRuntime().exec(stepperCommandSyscall + " k " + MOTOR_Y_ID);
	}
	
	public void stepHome()
	{
		
	}
	
	private class StepperMotor
	{


		private String name;
		private int port;
		private int style;
		private int speed;
		private int stepsPerRev;

		public StepperMotor(String name, int port, int style, int speed, int stepsPerRev)
		{
			this.name = name;
			this.port = port;
			this.style = style;
			this.speed = speed;
			this.stepsPerRev = stepsPerRev;
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
			return new StringBuilder().append("").toString();
		}
	}
}
