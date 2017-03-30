package main.java.org.jason.turretcontrol.firecontrol;

import main.java.org.jason.turretcontrol.exception.JamOccurredException;
import main.java.org.jason.turretcontrol.exception.NoAmmoException;
import main.java.org.jason.turretcontrol.exception.SafetyEngagedException;

public class MockFireControl extends FireControl {
	
	public MockFireControl(String config)
	{
		super(config);		
		
		startup();
	}

	
	@Override
	public synchronized void cycle() throws JamOccurredException, SafetyEngagedException
	{
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
	}
	
	public synchronized void jamCycle() throws JamOccurredException, SafetyEngagedException
	{
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
	}
	
	public void shutdown()
	{

	}



	@Override
	public void startup() {

	}
}
