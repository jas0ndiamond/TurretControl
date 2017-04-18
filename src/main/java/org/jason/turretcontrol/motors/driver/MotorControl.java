package org.jason.turretcontrol.motors.driver;

import java.io.IOException;
import java.util.HashMap;

import org.jason.turretcontrol.motors.driver.AdafruitStepperMotor;

public class MotorControl {

	private static int DEFAULT_HAT_ADDR = 0x60;
	private static int DEFAULT_FREQ = 1600;
	private static int DEFAULT_MOTOR_SPEED = 30;
	
	public final static int DIRECTION_FORWARD = 0;
	public final static int DIRECTION_BACKWARD = 1;
	
	public final static int STEP_SINGLE = 0;
	public final static int STEP_DOUBLE = 1;
	public final static int STEP_INTERLEAVE = 2;
	public final static int STEP_MICROSTEP = 3;
	
	public final static String MOTOR_X_NAME = "motor_x";
	public final static String MOTOR_Y_NAME = "motor_y";
	
	public final static int MOTOR_X_ID = 0;
	public final static int MOTOR_Y_ID = 1;
		
	private final static HashMap<Integer, String> ports = new HashMap<Integer, String>() 
	{
		private static final long serialVersionUID = 4739823974683355020L;

		{
			put(MOTOR_Y_ID, AdafruitMotorHat.STEPPER_MOTOR_1);
			put(MOTOR_X_ID, AdafruitMotorHat.STEPPER_MOTOR_2);
		}
	};
	
	private final static HashMap<Integer, StepperMode> stepStyles = new HashMap<Integer, StepperMode>()
	{
		private static final long serialVersionUID = 7754490000321920041L;
		{
			put(STEP_SINGLE, StepperMode.SINGLE_PHASE);
			put(STEP_DOUBLE, StepperMode.DOUBLE_PHASE);
			put(STEP_INTERLEAVE, StepperMode.HALF_STEP);
			put(STEP_MICROSTEP, StepperMode.MULTI_STEP);
		}
	};
//	
//	private final static HashMap<Integer, ServoCommand> directions = new HashMap<Integer, ServoCommand>() 
//	{
//		private static final long serialVersionUID = 7754490000321920041L;
//		{
//			put(DIRECTION_FORWARD, ServoCommand.FORWARD);
//			put(DIRECTION_BACKWARD, ServoCommand.BACKWARD);
//		}
//	};
	
	private HashMap<String, AdafruitStepperMotor> motors;
	private AdafruitMotorHat hat;

	public MotorControl() throws Exception
	{
		this(DEFAULT_HAT_ADDR, DEFAULT_FREQ);
	}
	
	public MotorControl(int hatAddress, int hatFreq) throws Exception
	{
		hat = new AdafruitMotorHat(hatAddress);
		motors = new HashMap<String, AdafruitStepperMotor>();
	}
	
	public void loadPreviousState()
	{
		//json describing previous motor state
	}
	
	public void addMotor(String name, int port, int speed, int style, int min, int max)
	{
		motors.put(name, hat.getStepperMotor( ports.get(port) ) );
		
		motors.get(name).setStepsPerRevolution(200);
		motors.get(name).setStepInterval(speed);
		motors.get(name).setMode( stepStyles.get(style) );
		
		System.out.println("Added motor " + name + " at " + ports.get(port));
	}
	
	public synchronized void stepMotor(String name, int steps, int direction) throws IOException
	{
		if(motors.containsKey(name) && Math.abs(direction) == 1  )
		{
			System.out.println("Moving motor " + name + " " + steps + " steps in direction " + direction);
			
			motors.get(name).step(steps * direction);
			
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
	
//	public void stepMotorSingle(String name, int steps, int direction) throws IOException
//	{
//		stepMotor(name, steps, direction, STEP_SINGLE);
//	}
//	
//	public void stepMotorDouble(String name, int steps, int direction) throws IOException
//	{
//		stepMotor(name, steps, direction, STEP_DOUBLE);
//	}
//	
//	public void stepMotorInterleave(String name, int steps, int direction) throws IOException
//	{
//		stepMotor(name, steps, direction, STEP_INTERLEAVE);		
//	}
//	
//	public void stepMotorMicrostep(String name, int steps, int direction) throws IOException
//	{
//		stepMotor(name, steps, direction, STEP_MICROSTEP);
//	}
	
	public void killMotors()
	{
		killXMotor();
		killYMotor();
	}
	
	public void killXMotor()
	{
		motors.get(MOTOR_X_NAME).stop();
	}
	
	public void killYMotor()
	{
		motors.get(MOTOR_Y_NAME).stop();
	}
	
	public void stepHome()
	{
		
	}
	
	public void shutdown()
	{
		//for each motor, write state to file
	}
	
	private class StepperMotor
	{
		public StepperMotor()
		{
			
		}
	}
}
