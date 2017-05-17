package org.jason.turretcontrol.motors;

import java.util.HashMap;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.jason.turretcontrol.motors.wrapper.MotorControl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MotorMotionResult 
{
	public final static String STEPS_MOVED = "stepsMoved";
	public final static String OLD_POS = "oldPos";
	public final static String NEW_POS = "newPos";
	public final static String TIME = "time";
	public final static String FINAL_POS = "finalPos";
	public final static String FINAL_X_POS = "finalX";
	public final static String FINAL_Y_POS = "finalY";
	
	/*
	 * {motor_name => {stepsmoved => 4, oldpos => 0, newpos => 4, time => 12345326234}}
	 */
	
	private HashMap<String, HashMap<String, Long>> result;
	
	private final static Logger logger = LoggerFactory.getLogger(MotorMotionResult.class); 
	
	public MotorMotionResult()
	{
		result = new HashMap<>();
		result.put(MotorControl.MOTOR_X_NAME, new HashMap<>());
		result.put(MotorControl.MOTOR_Y_NAME, new HashMap<>());
	}
	
	public boolean hasXMoved()
	{
		return result.containsKey(MotorControl.MOTOR_X_NAME) && result.get(MotorControl.MOTOR_X_NAME).containsKey(TIME);
	}
	
	public boolean hasYMoved()
	{
		return result.containsKey(MotorControl.MOTOR_Y_NAME) && result.get(MotorControl.MOTOR_Y_NAME).containsKey(TIME);
	}
	
	public HashMap<String, Long> getX()
	{
		return result.get(MotorControl.MOTOR_X_NAME);
	}
	
	public HashMap<String, Long> getY()
	{
		return result.get(MotorControl.MOTOR_Y_NAME);
	}
	
	public void setX(long steps, long oldPos, long newPos)
	{
		setX(steps, oldPos, newPos, System.currentTimeMillis());
	}
	
	public void setY(long steps, long oldPos, long newPos)
	{
		setY(steps, oldPos, newPos, System.currentTimeMillis());
	}
	
	public void setX(HashMap<String, Long> params)
	{
		if( params.containsKey(STEPS_MOVED) && params.containsKey(OLD_POS) && params.containsKey(NEW_POS))
		{
			if(params.containsKey(TIME))
			{
				setX( params.get(STEPS_MOVED), params.get(OLD_POS), params.get(NEW_POS), params.get(TIME));
			}
			else
			{
				setX( params.get(STEPS_MOVED), params.get(OLD_POS), params.get(NEW_POS));				
			}
		}
		else
		{
			logger.warn("SetX invoked with missing fields");
		}
	}
	
	public void setY(HashMap<String, Long> params)
	{
		if( params.containsKey(STEPS_MOVED) && params.containsKey(OLD_POS) && params.containsKey(NEW_POS))
		{
			if(params.containsKey(TIME))
			{
				setY( params.get(STEPS_MOVED), params.get(OLD_POS), params.get(NEW_POS), params.get(TIME));
			}
			else
			{
				setY( params.get(STEPS_MOVED), params.get(OLD_POS), params.get(NEW_POS));				
			}
		}
		else
		{
			logger.warn("SetY invoked with missing fields");			
		}
	}
	
	public void setX(long steps, long oldPos, long newPos, long time)
	{
		result.put
		(
			MotorControl.MOTOR_X_NAME, 
			new HashMap<String, Long>()
			{
				private static final long serialVersionUID = 960078521798960689L;
				{
					put(STEPS_MOVED, steps);
					put(OLD_POS, oldPos);
					put(NEW_POS, newPos);
					put(TIME, time);
				}
			}
		);
	}
	
	public void setY(long steps, long oldPos, long newPos, long time)
	{
		result.put
		(
			MotorControl.MOTOR_Y_NAME, 
			new HashMap<String, Long>()
			{
				private static final long serialVersionUID = 960078521798960689L;
				{
					put(STEPS_MOVED, steps);
					put(OLD_POS, oldPos);
					put(NEW_POS, newPos);
					put(TIME, time);
				}
			}
		);
	}
	
	public int getFinalXPos()
	{
		int retval = 0;
		if(getX().containsKey(FINAL_X_POS))
		{
			retval = getX().get(FINAL_X_POS).intValue();
		}
		else
		{
			
		}
		
		return retval;
	}
	
	public int getFinalYPos()
	{
		int retval = 0;
		if(getY().containsKey(FINAL_Y_POS))
		{
			retval = getY().get(FINAL_Y_POS).intValue();
		}
		else
		{
			
		}
		
		return retval;
	}
	
	@Override
	public String toString()
	{
		JSONObject result = new JSONObject();
		
		if(!getX().isEmpty())
		{
			HashMap<String, Long> xResult = getX();
			try 
			{
				result.put
				(
					MotorControl.MOTOR_X_NAME, 
					new JSONObject()
					{
						{
							put(STEPS_MOVED, xResult.get(STEPS_MOVED));
							put(OLD_POS, xResult.get(OLD_POS));
							put(NEW_POS, xResult.get(NEW_POS));
							put(TIME, xResult.get(TIME));
						}
					}
				);
			} 
			catch (JSONException e) 
			{
				logger.error("Exception while processing xResult", e);
			}
		}
		
		if(!getY().isEmpty())
		{
			HashMap<String, Long> yResult = getY();
			try 
			{
				result.put
				(
					MotorControl.MOTOR_Y_NAME, 
					new JSONObject()
					{
						{
							put(STEPS_MOVED, yResult.get(STEPS_MOVED));
							put(OLD_POS, yResult.get(OLD_POS));
							put(NEW_POS, yResult.get(NEW_POS));
							put(TIME, yResult.get(TIME));
						}
					}
				);
			} 
			catch (JSONException e) 
			{
				logger.error("Exception while processing xResult", e);
			}
		}
 
		return result.toString();
	}
}
