package org.jason.turretcontrol.firecontrol;

import java.util.concurrent.atomic.AtomicBoolean;

import org.jason.turretcontrol.exception.JamOccurredException;
import org.jason.turretcontrol.exception.SafetyEngagedException;

public abstract class FireControl {

	protected AtomicBoolean safety;
	protected boolean automatic;
	protected AtomicBoolean isJammed;
	
	protected FireControl(String config)
	{
		safety = new AtomicBoolean(true);
		automatic = false;
		isJammed = new AtomicBoolean(false);
		
		//config should override above defaults
	}
	
	public synchronized void setSafety(boolean safe)
	{
		this.safety.set(safe);
	}
	
	public synchronized boolean getSafety()
	{
		return this.safety.get();
	}
	
	public synchronized AtomicBoolean isJammed()
	{
		return isJammed;
	}
	
	public synchronized void setIsJammed(boolean isJammed)
	{
		//if isJammed is wrongly set to true, the next cycle should fail and rejam the firecontrol
		this.isJammed.set(isJammed);
	}
	
	public abstract void cycle() throws JamOccurredException, SafetyEngagedException;
	
	public abstract void shutdown();
	
	public void reset() throws InterruptedException
	{
		shutdown();
		Thread.sleep(3000);
		startup();
	}
	
	public abstract void startup();
}
