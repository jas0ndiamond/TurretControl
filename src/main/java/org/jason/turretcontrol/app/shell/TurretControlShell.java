package org.jason.turretcontrol.app.shell;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.codehaus.jettison.json.JSONObject;
import org.jason.turretcontrol.TurretControl;
import org.jason.turretcontrol.config.ConfigLoader;
import org.jason.turretcontrol.demo.DemoRunner;
import org.jason.turretcontrol.exception.JamOccurredException;
import org.jason.turretcontrol.exception.NoAmmoException;
import org.jason.turretcontrol.exception.SafetyEngagedException;

public class TurretControlShell 
{
	private final static String CONFIG_FILE = "./config/config.json";
	
	
	public static void main(String[] args) throws Exception
	{
		String confFile = CONFIG_FILE;
		
		if(args.length > 0)
		{
			confFile = args[0];
		}
		
		//config file, and config
		JSONObject config = ConfigLoader.getConfigJSONObject(confFile);
			
		TurretControl tc = new TurretControl(config.getJSONObject("turret").toString());
		
		BufferedReader stdIn = null;
		
		
		//check demo config
		DemoRunner demo = null;
		
		try
		{
			stdIn = new BufferedReader(new InputStreamReader(System.in));
			
			String[] cmdArgs;
			
			System.out.print(">");
			String command = null;
			while( (command = stdIn.readLine()) != null )
			{
				command.replaceAll("\n", "");
				/*
				get("/fire", (req, res) -> 
				get("/moveX", (req, res) -> 
				get("/moveY", (req, res) -> 
				get("/moveZ", (req, res) -> 
				get("/panX", (req, res) -> 
				get("/moveY", (req, res) -> 
				get("/getAmmo", (req, res) -> 
				get("/resetAmmo", (req, res) -> 
				get("/getMagSize", (req, res) -> 
				get("/setMagSize", (req, res) -> 
				get("/getSafety", (req, res) -> 
				get("/safetyOn", (req, res) -> 
				get("/safetyOff", (req, res) -> 
				get("/status", (req, res) -> 
				get("/shutdown", (req, res) -> 
				get("/reset", (req, res) -> 
				*/
				System.out.println("Got command " + command);
				
				if(command.equals("exit") || command.equals("quit") || command.equals("q") || command.equals("shutdown"))
				{
					if(tc != null)
					{
						tc.shutdown();
					}
					
					System.out.println("Take care...");
					break;
				}
				else if(command.equals("help"))
				{
					System.out.println
					(
						"fire\n" + 
						"moveX val direction\n" +
						"moveY val direction\n" +
						"moveZ val direction\n" +
						"panX steps direction\n" + 
						"panY steps direction\n" +
						"panXhome\n" + 
						"panYhome\n" +
						"panHome\n" +
						"getAmmo\n" +
						"reload magName\n" + 
						"resetAmmo\n" +
						"getMagSize\n" +
						"setMagSize val\n" +
						"getSafety\n" +
						"safetyOn\n" + 
						"safetyOff\n" +
						"getSafety\n" + 
						"status\n" +
						"shutdown\n" +
						"reset\n"
					);
				}
				else if(command.equals("fire"))
				{
					try
					{
						tc.fire();
					}
					catch(NoAmmoException e)
					{
						e.printStackTrace();
					}
					catch(JamOccurredException e)
					{
						e.printStackTrace();
					}
					catch(SafetyEngagedException e)
					{
						e.printStackTrace();
					}
					System.out.println("Cycle completed");
				}
				else if(command.equals("getAmmo"))
				{
					System.out.println(tc.getAmmoCount());
				}
				else if(command.equals("getMagSize"))
				{
					System.out.println(tc.getMagSize());
				}
				else if(command.equals("getSafety"))
				{
					System.out.println(tc.getSafety());
				}
				else if(command.equals("status"))
				{
					//
				}
				else if(command.equals("safetyOn"))
				{
					tc.setSafety(true);				
					System.out.println("Safety engaged: " + tc.getSafety());
				}
				else if(command.equals("safetyOff"))
				{
					tc.setSafety(false);
					System.out.println("Safety engaged: " + tc.getSafety());
				}
				else if(command.equalsIgnoreCase("killmotors"))
				{
					tc.killMotors();
				}
				else if(command.startsWith("panX"))
				{
					cmdArgs = command.split(" ");
					if(cmdArgs.length == 3)
					{
						int steps = Integer.parseInt(cmdArgs[1]);
						int direction = Integer.parseInt(cmdArgs[2]);
						
						tc.panX(steps, direction);
					}
					else
					{
						System.out.println("Malformed command");
					}
				}
				else if(command.startsWith("panY"))
				{
					cmdArgs = command.split(" ");
					if(cmdArgs.length == 3)
					{
						int steps = Integer.parseInt(cmdArgs[1]);
						int direction = Integer.parseInt(cmdArgs[2]);
						
						tc.panY(steps, direction);
					}
					else
					{
						System.out.println("Malformed command");
					}
				}
				else if(command.startsWith("reload"))
				{
					 String magName = command.split("\\s+")[1];
					 
					 tc.reload(magName); 
				}
				else if(command.equals("resetAmmo"))
				{
					//reload current magazine
				}
				else if(command.equals("panXhome"))
				{
					
				}
				else if(command.equals("panYhome"))
				{
					
				}
				else if(command.equals("panHome"))
				{
					
				}
				else if(command.equals("startDemo"))
				{
					demo = new DemoRunner(tc);
					demo.start();
				}
				else if(command.equals("stopDemo"))
				{
					//stop thread launched by startDemo, return turret to home
					if(demo != null)
					{
						demo.setRunning(false);
					}	
				}
				else
				{
					System.out.println("Unrecognized command " + command);
				}
				
				System.out.print("\n>");
			}
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			if(stdIn != null )
			{
				stdIn.close();
			}
		}
	}
	
	/*
	private class DemoRunner extends Thread
	{
		private TurretControl turretControl;
		private boolean running;
		private int yPosOffset;

		private int xCyclePos;
		private int yCyclePos;
		
		private final static int X_CYCLE_POS_UNDEF = -1;
		private final static int Y_CYCLE_POS_UNDEF = -1;
		
		private final static int PAUSE_LEN = 3000;
		private final static int DEFAULT_DEMO_Y_POS_OFFSET = 0;
		
		private long cycleInterval;
		private final static long DEFAULT_CYCLE_INTERVAL = 60L * 1000L * 5L; //5 mins
		
		public DemoRunner(TurretControl t)
		{
			turretControl = t;
			running = false;
			
			yPosOffset= DEFAULT_DEMO_Y_POS_OFFSET;
			
			xCyclePos = -1;
			yCyclePos = -1;
		}
		
		public void setXCyclePos(int val)
		{
			//check x motor min/max
			xCyclePos = val;
		}
		
		public void setYCyclePos(int val)
		{
			//check x motor min/max
			yCyclePos = val;
		}

		public void setYPosOffset(int offset)
		{
			yPosOffset = offset;
		}
		
		@Override
		public void run()
		{
			long lastCycle = -1L;
			long now;
			while(running)
			{
				now = System.currentTimeMillis();
				
				if(xCyclePos != X_CYCLE_POS_UNDEF && yCyclePos != Y_CYCLE_POS_UNDEF && now - lastCycle > cycleInterval)
				{
					//return to demo cycle position
					
					//cycle turret
					
					//update time
					lastCycle = now;
				}
				
				//pan turret to a random spot
				
				//generate random number between x min and x max
					
				//generate random number between y min and y max
				//consider y pos offset. don't want to aim at a person
				
				
				try 
				{
					//give the motors a break
					sleep(PAUSE_LEN);
				} 
				catch (InterruptedException e) 
				{
					e.printStackTrace();
				}
			}
				
		}
		
		public void startDemo()
		{
			running = true;
			start();
		}
		
		public void stopDemo()
		{
			running = false;
		}
		
		public boolean isRunning()
		{
			return running;
		}
	}
	*/

}
