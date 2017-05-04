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
					//stop thread launched by startDemo, return turret to home
					if(demo != null)
					{
						demo.setRunning(false);
						
						while(demo.isAlive())
						{
							try 
							{ 
								System.out.println("Waiting for demo cycle to complete"); 
								Thread.sleep(2000); 
							} catch (InterruptedException e) { e.printStackTrace(); }
						}
					}	
					
					
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
				else if(command.equalsIgnoreCase("getAmmo"))
				{
					System.out.println(tc.getAmmoCount());
				}
				else if(command.equalsIgnoreCase("getMagSize"))
				{
					System.out.println(tc.getMagSize());
				}
				else if(command.equalsIgnoreCase("getSafety"))
				{
					System.out.println(tc.getSafety());
				}
				else if(command.equals("status"))
				{
					//
				}
				else if(command.equalsIgnoreCase("safetyOn"))
				{
					tc.setSafety(true);				
					System.out.println("Safety engaged: " + tc.getSafety());
				}
				else if(command.equalsIgnoreCase("safetyOff"))
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
						
						System.out.println("Turret position: " + tc.getPositionX() + ", " + tc.getPositionY());
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
						
						System.out.println("Turret position: " + tc.getPositionX() + ", " + tc.getPositionY());
					}
					else
					{
						System.out.println("Malformed command");
					}
				}
				else if(command.startsWith("reload"))
				{					 
					tc.reload(command.split("\\s+")[1]); 
				}
				else if(command.equalsIgnoreCase("resetAmmo"))
				{
					//reload current magazine
				}
				else if(command.equalsIgnoreCase("panXhome"))
				{
					
				}
				else if(command.equalsIgnoreCase("panYhome"))
				{
					
				}
				else if(command.equalsIgnoreCase("panHome"))
				{
					
				}
				else if(command.equalsIgnoreCase("startDemo") || command.equalsIgnoreCase("demostart"))
				{
					demo = new DemoRunner(tc);
					demo.start();
				}
				else if(command.equalsIgnoreCase("stopDemo") || command.equalsIgnoreCase("demostop"))
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
}
