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
import org.jason.turretcontrol.motors.MotorMotionResult;
import org.jason.turretcontrol.sensors.TurretSystem;

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
						System.out.println("Demo stopping...");
						demo.setRunning(false);
						
						while(demo.isAlive())
						{
							try 
							{ 
								System.out.println("Waiting for demo cycle to complete"); 
								Thread.sleep(2000); 
							} catch (InterruptedException e) { e.printStackTrace(); }
						}
						
						System.out.println("Demo stopped");
					}	

					System.out.println("Shutting down the turret");
					
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
						//"moveX val direction\n" +
						//"moveY val direction\n" +
						//"moveZ val direction\n" +
						"panX steps direction\n" + 
						"panY steps direction\n" +
						"panXTo position\n" + 
						"panYTo position\n" +
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
				else if(command.equals("fire") || command.equals("cycle"))
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
					System.out.println("SafetyOn: " + tc.getSafety());
					System.out.println("Ammo: " + tc.getAmmoCount() + "/" + tc.getMagSize() +":" + tc.getCurrentMagazine());
					
					System.out.println("Supported Magazines:");
					for(String mag : tc.getSupportedMagazines())
					{
						System.out.println("\t" + mag);
					}
					
					System.out.println("CPU Temp: " + TurretSystem.getCPUTemp() + "C");
					System.out.println("GPU Temp: " + TurretSystem.getGPUTemp() + "C");
					
					double[] loadAvg = TurretSystem.getLoadAverage();
					System.out.println("Load Avg: " + loadAvg[0] + " " + loadAvg[1] + " " + loadAvg[2] );
					
					int[] memUtil = TurretSystem.getMemoryUtilization();
					System.out.println("Mem: " + memUtil[0] + " " + memUtil[1] + " " + memUtil[2] );
					
					long[] jvmMemUtil = TurretSystem.getJVMMemoryUtilization();
					System.out.println("JVMMem: " + jvmMemUtil[0] + " " + jvmMemUtil[1] + " " + jvmMemUtil[2] );

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
				else if(command.startsWith("panX "))
				{
					cmdArgs = command.split(" ");
					if(cmdArgs.length == 3)
					{
						int steps = Integer.parseInt(cmdArgs[1]);
						int direction = Integer.parseInt(cmdArgs[2]);
						
						System.out.println("Result: " + tc.panX(steps, direction));
						System.out.println("Turret position: " + tc.getPositionX() + ", " + tc.getPositionY());
					}
					else
					{
						System.out.println("Malformed command");
					}
				}
				else if(command.startsWith("panY "))
				{
					cmdArgs = command.split(" ");
					if(cmdArgs.length == 3)
					{
						int steps = Integer.parseInt(cmdArgs[1]);
						int direction = Integer.parseInt(cmdArgs[2]);
						
						System.out.println("Result: " + tc.panY(steps, direction));
						System.out.println("Turret position: " + tc.getPositionX() + ", " + tc.getPositionY());
					}
					else
					{
						System.out.println("Malformed command");
					}
				}
				else if(command.startsWith("panXTo"))
				{
					cmdArgs = command.split(" ");
					if(cmdArgs.length == 2)
					{
						int position = Integer.parseInt(cmdArgs[1]);
						
						System.out.println("Result: " + tc.panXTo(position));						
						System.out.println("Turret position: " + tc.getPositionX() + ", " + tc.getPositionY());
					}
					else
					{
						System.out.println("Malformed command");
					}
				}
				else if(command.startsWith("panYTo"))
				{
					cmdArgs = command.split(" ");
					if(cmdArgs.length == 2)
					{
						int position = Integer.parseInt(cmdArgs[1]);
						
						System.out.println("Result: " + tc.panYTo(position));						
						System.out.println("Turret position: " + tc.getPositionX() + ", " + tc.getPositionY());
					}
					else
					{
						System.out.println("Malformed command");
					}
				}
				else if(command.startsWith("panTo"))
				{
					cmdArgs = command.split(" ");
					if(cmdArgs.length == 3)
					{
						int x = Integer.parseInt(cmdArgs[1]);
						int y = Integer.parseInt(cmdArgs[2]);
						
						System.out.println("Result: " + tc.panTo(x,y));						
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
					tc.reload(tc.getCurrentMagazine());
				}
				else if(command.equalsIgnoreCase("panXhome"))
				{
					System.out.println("Result: " +tc.panXHome());
				}
				else if(command.equalsIgnoreCase("panYhome"))
				{
					System.out.println("Result: " +tc.panYHome());
				}
				else if(command.equalsIgnoreCase("panHome"))
				{
					System.out.println("Result: " +tc.panHome());
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
