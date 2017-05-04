package org.jason.turretcontrol.firecontrol;

import org.jason.turretcontrol.exception.JamOccurredException;
import org.jason.turretcontrol.exception.SafetyEngagedException;
import org.jason.turretcontrol.firecontrol.cycle.CycleResult;

public class MockFireControl extends FireControl {
	
	public MockFireControl(String config)
	{
		super(config);		
		
		startup();
	}

	
	@Override
	public synchronized CycleResult cycle() throws JamOccurredException, SafetyEngagedException
	{
		CycleResult cycleResult = new CycleResult();
		
		//safety is primary cycle preventer
		if(!safety.get())
		{
			//secondary cycle preventer
			if(!isJammed.get())
			{
				boolean exited = false;
				//cycle the firing mech and confim the round exits the barrel
				//launch pir listener in separate threads
				
				isJammed.set(true);				

				//confirm round exits barrel, throw jam exception if not
				exited = true;
				
				if(exited)
				{
					isJammed.set(false);
				}
				else
				{
					isJammed.set(true);
					throw new JamOccurredException("Jam occurred during cycle- clear the jam");
				}
			}
			else
			{
				throw new JamOccurredException("Cycle attempted with jam- clear the jam");
			}
		}
		else
		{
			//throw new NoAmmoException("Cycle attempted without ammo- reload");
			throw new SafetyEngagedException("Cycle attempted with safety engaged");
		}
		
		return cycleResult;
	}
	
	public synchronized CycleResult jamCycle() throws JamOccurredException, SafetyEngagedException
	{
		CycleResult cycleResult = new CycleResult();
		
		//safety is primary cycle preventer
		if(!safety.get())
		{
			//secondary cycle preventer
			if(!isJammed.get())
			{				
				isJammed.set(true);
			}
			else
			{
				throw new JamOccurredException("Cycle attempted with jam- clear the jam");
			}
		}
		else
		{
			//throw new NoAmmoException("Cycle attempted without ammo- reload");
			throw new SafetyEngagedException("Cycle attempted with safety engaged");
		}
		
		return cycleResult;
	}
	
	public void shutdown()
	{

	}



	@Override
	public void startup() {

	}
}
