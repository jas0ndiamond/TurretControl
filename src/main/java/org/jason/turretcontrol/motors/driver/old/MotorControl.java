package org.jason.turretcontrol.motors.driver.old;

import java.io.IOException;
import java.util.HashMap;

import org.jason.turretcontrol.motors.driver.old.AdafruitMotorHAT;
import org.jason.turretcontrol.motors.driver.old.AdafruitMotorHAT.AdafruitStepperMotor;
import org.jason.turretcontrol.motors.driver.old.AdafruitMotorHAT.ServoCommand;
import org.jason.turretcontrol.motors.driver.old.AdafruitMotorHAT.Style;

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
		
	private final static HashMap<Integer, Integer> ports = new HashMap<Integer, Integer>() 
	{
		private static final long serialVersionUID = 4739823974683355020L;

		{
			put(MOTOR_Y_ID, AdafruitMotorHAT.AdafruitStepperMotor.PORT_M1_M2);
			put(MOTOR_X_ID, AdafruitMotorHAT.AdafruitStepperMotor.PORT_M3_M4);
		}
	};
	
	private final static HashMap<Integer, Style> stepStyles = new HashMap<Integer, Style>()
	{
		private static final long serialVersionUID = 7754490000321920041L;
		{
			put(STEP_SINGLE, Style.SINGLE);
			put(STEP_DOUBLE, Style.DOUBLE);
			put(STEP_INTERLEAVE, Style.INTERLEAVE);
			put(STEP_MICROSTEP, Style.MICROSTEP);
		}
	};
	
	private final static HashMap<Integer, ServoCommand> directions = new HashMap<Integer, ServoCommand>() 
	{
		private static final long serialVersionUID = 7754490000321920041L;
		{
			put(DIRECTION_FORWARD, ServoCommand.FORWARD);
			put(DIRECTION_BACKWARD, ServoCommand.BACKWARD);
		}
	};
	
	private HashMap<String, AdafruitStepperMotor> motors;
	private AdafruitMotorHAT hat;

	public MotorControl() throws Exception
	{
		this(DEFAULT_HAT_ADDR, DEFAULT_FREQ);
	}
	
	public MotorControl(int hatAddress, int hatFreq) throws Exception
	{
		hat = new AdafruitMotorHAT(hatAddress, hatFreq);
		motors = new HashMap<String, AdafruitStepperMotor>();
	}
	
	public void loadPreviousState()
	{
		//json describing previous motor state
	}
	
	public void addMotor(String name, int port, int speed, int min, int max)
	{
		motors.put(name, hat.getStepper( ports.get(port) ) );
	}
	
	public synchronized void stepMotor(String name, int steps, int direction, int stepStyle) throws IOException
	{
		if(motors.containsKey(name) && directions.containsKey(direction) && stepStyles.containsKey(stepStyle) )
		{
			motors.get(name).step(steps, directions.get(direction), stepStyles.get(stepStyle));
			
			//write state
		}
		else
		{
			System.out.println("Could not move motor");
		}
	}
	
	public void stepMotorSingle(String name, int steps, int direction) throws IOException
	{
		stepMotor(name, steps, direction, STEP_SINGLE);
	}
	
	public void stepMotorDouble(String name, int steps, int direction) throws IOException
	{
		stepMotor(name, steps, direction, STEP_DOUBLE);
	}
	
	public void stepMotorInterleave(String name, int steps, int direction) throws IOException
	{
		stepMotor(name, steps, direction, STEP_INTERLEAVE);		
	}
	
	public void stepMotorMicrostep(String name, int steps, int direction) throws IOException
	{
		stepMotor(name, steps, direction, STEP_MICROSTEP);
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
