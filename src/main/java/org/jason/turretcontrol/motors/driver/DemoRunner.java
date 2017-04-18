package org.jason.turretcontrol.motors.driver;

public class DemoRunner extends Thread
{
	private AdafruitStepperMotor motor_x;
	private AdafruitStepperMotor motor_y;
	
	private boolean isRunning;


	public DemoRunner(AdafruitStepperMotor motor_x, AdafruitStepperMotor motor_y)
	{
		this.motor_x = motor_x;
		this.motor_y = motor_y;
		
		isRunning = false;
	}
	
	public void setRunning(boolean isRunning)
	{
		this.isRunning = isRunning;
	}
	
	
	@Override
	public void run()
	{
		isRunning = true;
		
		//secure in home position
		motor_x.setMode(StepperMode.HALF_STEP);
		motor_y.setMode(StepperMode.HALF_STEP);
		
		motor_x.step(5);
		
		motor_x.step(-5);
		
		motor_y.step(5);
		
		motor_y.step(-5);
		try 
		{
			sleep(1000);
		} catch (InterruptedException e) 
		{
			e.printStackTrace();
		}
		
		while(isRunning)
		{
			//step x motor
			
//			motor_x.step(40);
//			try { sleep(1000); } catch (InterruptedException e)	{ e.printStackTrace(); }
//			
//			motor_x.step(-80);
//			try { sleep(1000); } catch (InterruptedException e)	{ e.printStackTrace(); }
//			
//			motor_x.step(40);
//			try { sleep(1000); } catch (InterruptedException e)	{ e.printStackTrace(); }
			
			//step y motor
			
			motor_y.setMode(StepperMode.HALF_STEP);
			motor_y.step(40);
			try { sleep(1000); } catch (InterruptedException e)	{ e.printStackTrace(); }

			motor_y.setMode(StepperMode.HALF_STEP);
			motor_y.step(-80);
			try { sleep(1000); } catch (InterruptedException e)	{ e.printStackTrace(); }
			
			motor_y.setMode(StepperMode.HALF_STEP);
			motor_y.step(40);
			try { sleep(1000); } catch (InterruptedException e)	{ e.printStackTrace(); }
		}
	}
}