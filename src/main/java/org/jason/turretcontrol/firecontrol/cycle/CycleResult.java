package org.jason.turretcontrol.firecontrol.cycle;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CycleResult 
{
	private final static Logger logger = LoggerFactory.getLogger(CycleResult.class); 
	
	public final static String SUCCESS = "SUCCESS";
	public final static String FAIL = "FAIL";
	public final static String NONE = "NONE";
	
	private String status;
	private String message;
	private long triggerDuration;
	private long accelDuration;
	private long time;
	
	public CycleResult()
	{
		status = NONE;
		message = NONE;
		triggerDuration = 0;
		accelDuration = 0;
		time = 0;
	}
	
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) 
	{
		if(status.equals(NONE) || status.equals(FAIL) || status.equals(SUCCESS))
		{
			this.status = status;
		}
	}



	public String getMessage() {
		return message;
	}



	public void setMessage(String message) {
		this.message = message;
	}



	public long getTriggerDuration() {
		return triggerDuration;
	}



	public void setTriggerDuration(long triggerDuration) {
		this.triggerDuration = triggerDuration;
	}



	public long getAccelDuration() {
		return accelDuration;
	}



	public void setAccelDuration(long accelDuration) {
		this.accelDuration = accelDuration;
	}



	public long getTime() {
		return time;
	}



	public void setTime(long time) {
		this.time = time;
	}


	@Override
	public String toString()
	{
		String retval = null;
		try 
		{
			retval =  new JSONObject()
				.put("time", getTime())
				.put("triggerDuration", getTriggerDuration())
				.put("accelDuration", getAccelDuration())
				.put("status", getStatus())
				.put("message", getMessage())
				.toString();
		} 
		catch (JSONException e) 
		{
			logger.error("Exception while processing cycle result", e);
		}
		return retval;
	}
}
