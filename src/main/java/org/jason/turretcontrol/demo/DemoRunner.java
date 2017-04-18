package org.jason.turretcontrol.demo;

import org.jason.turretcontrol.TurretControl;
import org.jason.turretcontrol.exception.JamOccurredException;
import org.jason.turretcontrol.exception.NoAmmoException;
import org.jason.turretcontrol.exception.SafetyEngagedException;
import org.jason.turretcontrol.motors.wrapper.MotorControl;

public class DemoRunner extends Thread
{
	private TurretControl tc;
	
	private boolean isRunning;


	public DemoRunner(TurretControl tc)
	{
		this.tc = tc;
		
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

		tc.panX(5, MotorControl.DIRECTION_FORWARD);
		try { sleep(2000); } catch (InterruptedException e)	{ e.printStackTrace(); }
		
		tc.panX(5, MotorControl.DIRECTION_BACKWARD);
		try { sleep(2000); } catch (InterruptedException e)	{ e.printStackTrace(); }

		tc.panY(5, MotorControl.DIRECTION_FORWARD);
		try { sleep(2000); } catch (InterruptedException e)	{ e.printStackTrace(); }

		tc.panY(5, MotorControl.DIRECTION_BACKWARD);
		try { sleep(2000); } catch (InterruptedException e)	{ e.printStackTrace(); }
		
		long lastCycle = System.currentTimeMillis();
		long now = lastCycle;
		while(isRunning)
		{
			now = System.currentTimeMillis();
			
			if(now - lastCycle > (1000 * 60 * 10))
			{
				//return to demo cycle position
				
				//cycle turret
				System.out.println("Cycling turret at " + now);
				try 
				{
					tc.fire();
				} 
				catch (JamOccurredException | SafetyEngagedException | NoAmmoException e) 
				{
					e.printStackTrace();
				}
				finally
				{
					//update time
					lastCycle = now;
				}
				
				//wait for reload
				while(tc.getAmmoCount() == 0)
				{
					System.out.println("Waiting for reload");
					try { sleep(5000); } catch (InterruptedException e)	{ e.printStackTrace(); }
				}
			}
			
			//step x motor
			
			tc.panX(40, MotorControl.DIRECTION_FORWARD);
			try { sleep(1000); } catch (InterruptedException e)	{ e.printStackTrace(); }
			
			tc.panX(80, MotorControl.DIRECTION_BACKWARD);
			try { sleep(1000); } catch (InterruptedException e)	{ e.printStackTrace(); }
			
			tc.panX(40, MotorControl.DIRECTION_FORWARD);
			try { sleep(1000); } catch (InterruptedException e)	{ e.printStackTrace(); }
			
			//step y motor
			tc.panY(40, MotorControl.DIRECTION_FORWARD);
			try { sleep(1000); } catch (InterruptedException e)	{ e.printStackTrace(); }
			
			tc.panY(80, MotorControl.DIRECTION_BACKWARD);
			try { sleep(1000); } catch (InterruptedException e)	{ e.printStackTrace(); }
			
			tc.panY(40, MotorControl.DIRECTION_FORWARD);
			try { sleep(1000); } catch (InterruptedException e)	{ e.printStackTrace(); }			
		}
	}
}