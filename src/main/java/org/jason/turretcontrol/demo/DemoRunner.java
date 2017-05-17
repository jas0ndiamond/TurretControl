package org.jason.turretcontrol.demo;

import java.io.IOException;

import org.jason.turretcontrol.TurretControl;
import org.jason.turretcontrol.exception.JamOccurredException;
import org.jason.turretcontrol.exception.NoAmmoException;
import org.jason.turretcontrol.exception.SafetyEngagedException;
import org.jason.turretcontrol.motors.wrapper.MotorControl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DemoRunner extends Thread
{
	private TurretControl tc;
	
	private boolean runDemo;
	private final static Logger logger = LoggerFactory.getLogger(DemoRunner.class); 


	public DemoRunner(TurretControl tc)
	{
		this.tc = tc;
		
		runDemo = false;
	}
	
	public void setRunning(boolean isRunning)
	{
		this.runDemo = isRunning;
	}
	
	public void holdTurret() 
	{
		tc.panX(5, MotorControl.DIRECTION_FORWARD);
		try { sleep(2000); } catch (InterruptedException e)	{ logger.warn("Sleep interrupted", e); }
		
		tc.panX(5, MotorControl.DIRECTION_BACKWARD);
		try { sleep(2000); } catch (InterruptedException e)	{ logger.warn("Sleep interrupted", e); }

		tc.panY(5, MotorControl.DIRECTION_FORWARD);
		try { sleep(2000); } catch (InterruptedException e)	{ logger.warn("Sleep interrupted", e); }

		tc.panY(5, MotorControl.DIRECTION_BACKWARD);
		try { sleep(2000); } catch (InterruptedException e)	{ logger.warn("Sleep interrupted", e); }
	}
	
	@Override
	public void run()
	{
		long lastCycle = System.currentTimeMillis();
		long now = lastCycle;
		final long cycleInterval = 15 * 60 * 1000; //15 mins -> 3/hour -> 18 cycles -> 4.5 hours
		final long restInterval = 20 * 60 * 1000; //30 mins
		long lastRest = now;
		long restTime;
		final long restLength = 5 * 60 * 1000; //5 mins
	
		//secure in home position
		holdTurret();
		runDemo = true;

		while(runDemo)
		{
			now = System.currentTimeMillis();
			
			if(now - lastRest > restInterval)
			{
				logger.info("Entering rest mode");
				
				//cease motion, should be at grav neutral, so kill motors and wait
				tc.killMotors();
				
				restTime = 0;
				while(runDemo && restTime < restLength)
				{
					try 
					{
						Thread.sleep(2000);
						restTime += 2000;
					} 
					catch (InterruptedException e) 
					{
						logger.warn("Sleep interrupted", e);
					}
				}
				
				lastRest = System.currentTimeMillis();
				
				//motors were killed earlier, secure them at grav neutral
				holdTurret();
			}
			else if(now - lastCycle > cycleInterval)
			{
				//return to demo cycle position
				
				//sleep so offset the turret shaking
				try { sleep(3000); } catch (InterruptedException e)	{ logger.warn("Sleep interrupted", e); }
				
				//cycle turret
				logger.info("Cycling turret at " + now);
				try 
				{
					tc.fire();
				} 
				catch (JamOccurredException | SafetyEngagedException | NoAmmoException e) 
				{
					logger.error("Cycle failure", e);
				}
				
				//wait for reload
				while(runDemo && tc.getAmmoCount() == 0)
				{
					logger.info("Waiting for reload");
					try { sleep(5000); } catch (InterruptedException e)	{ logger.warn("Sleep interrupted", e); }
				}
				
				//update time regardless if cycle succeeded
				lastCycle = System.currentTimeMillis();
			}
			else
			{
				//pan in a clockwise box, then return to grav neutral
				
				//top right
				System.out.println(tc.panX(40, MotorControl.DIRECTION_FORWARD));
				try { sleep(2500); } catch (InterruptedException e)	{ logger.warn("Sleep interrupted", e); }
				
				System.out.println(tc.panY(40, MotorControl.DIRECTION_FORWARD));
				try { sleep(2500); } catch (InterruptedException e)	{ logger.warn("Sleep interrupted", e); }
				
				//bottom right
				System.out.println(tc.panY(80, MotorControl.DIRECTION_BACKWARD));
				try { sleep(2500); } catch (InterruptedException e)	{ logger.warn("Sleep interrupted", e); }
	
				//bottom left
				System.out.println(tc.panX(80, MotorControl.DIRECTION_BACKWARD));
				try { sleep(2500); } catch (InterruptedException e)	{ logger.warn("Sleep interrupted", e); }
				
				//top left
				System.out.println(tc.panY(80, MotorControl.DIRECTION_FORWARD));
				try { sleep(2500); } catch (InterruptedException e)	{ logger.warn("Sleep interrupted", e); }
				
				//top right
				System.out.println(tc.panX(80, MotorControl.DIRECTION_FORWARD));
				try { sleep(2500); } catch (InterruptedException e)	{ logger.warn("Sleep interrupted", e); }
				
				//center
				System.out.println(tc.panHome());
				try { sleep(2500); } catch (InterruptedException e)	{ logger.warn("Sleep interrupted", e); }
			}
		}
	}
}
