package org.jason.turretcontrol.motors.driver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

public class MotorShell {


	public static void main(String[] args) throws Exception
	{
		final int motorHATAddress = 0X60;
		
		DemoRunner demoRunner = null;

		HashMap<Integer, StepperMode> stepStyles = new HashMap<>();
		stepStyles.put(1, StepperMode.SINGLE_PHASE);
		stepStyles.put(2, StepperMode.DOUBLE_PHASE);
		stepStyles.put(3, StepperMode.HALF_STEP);
		stepStyles.put(4, StepperMode.MULTI_STEP);

		//create instance of a motor HAT
		AdafruitMotorHat motorHat = new AdafruitMotorHat(motorHATAddress);
		/*
		 * Because the Adafruit motor HAT uses PWMs that pulse independently of
		 * the Raspberry Pi the motors will keep running at its current direction
		 * and power levels if the program abnormally terminates. 
		 * A shutdown hook like the one in this example is useful to stop the 
		 * motors when the program is abnormally interrupted.
		 */		
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() { 
				System.out.println("Turn off all motors");
				motorHat.stopAll();		    	
			}
		});

		AdafruitStepperMotor motor_x = motorHat.getStepperMotor("SM1");
		motor_x.setStepsPerRevolution(200);
		motor_x.setStepInterval(50);

		AdafruitStepperMotor motor_y = motorHat.getStepperMotor("SM2");
		motor_y.setStepsPerRevolution(200);	
		motor_y.setStepInterval(50);
		
		boolean runShell = true;

		BufferedReader stdIn = null;

		try
		{
			stdIn = new BufferedReader(new InputStreamReader(System.in));

			System.out.print(">");
			String command = null;

			int direction = 1;
			StepperMode stepStyle = null;

			while( runShell && (command = stdIn.readLine()) != null )
			{
				command.replaceAll("\n", "");

				if(command.equals("exit") || command.equals("quit") || command.equals("q") || command.equals("shutdown"))
				{					
					if(demoRunner != null)
					{
						demoRunner.setRunning(false);
						
						while(demoRunner.isAlive())
						{
							try { Thread.sleep(5000); } catch (InterruptedException e)	{ e.printStackTrace(); }
							System.out.println("Waiting for demo cycle to end");
						}
					}
					
					motorHat.stopAll();
					
					System.out.println("Take care...");
					runShell = false;
				}
				else if(command.matches("^[xy]\\s+[fb]\\s+\\d+\\s+\\d\\s*"))
				{
					String[] fields = command.split("\\s+");

					int steps = Integer.parseInt(fields[2]);
					int stepTypeInput = Integer.parseInt(fields[3]);

					direction = 1;
					stepStyle = null;
					
					if(fields[1].equals("f"))
					{
						direction = 1;
					}
					else if(fields[1].equals("b"))
					{									
						direction = -1;
					}

					stepStyle = stepStyles.get(stepTypeInput);

					if(stepStyle != null)
					{
						System.out.println("Stepping with style: " + stepStyle + " and direction " + direction); 

						steps *= direction;
						
						if(fields[0].equals("x"))
						{
							motor_x.setMode(stepStyle);
							motor_x.step(steps);
							System.out.println("Motor x advanced steps " + steps + " in direction: " + motor_x.getState());
						}
						else if(fields[0].equals("y"))
						{
							motor_y.setMode(stepStyle);
							motor_y.step(steps);	
							
							System.out.println("Motor y advanced steps " + steps + " in direction: " + motor_y.getState());
						}
					}
				}
				else if(command.equals("demostart"))
				{
					demoRunner = new DemoRunner(motor_x, motor_y);
					demoRunner.start();
					
				}
				else if(command.equals("demostop"))
				{
					if(demoRunner != null)
					{
						demoRunner.setRunning(false);
						demoRunner = null;
					}
				}
				else if(command.equals("killmotors"))
				{
					motorHat.stopAll();
				}	
				else if(command.equals("hold"))
				{
					motor_x.setMode(StepperMode.HALF_STEP);
					motor_y.setMode(StepperMode.HALF_STEP);
					
					motor_x.step(5);
					motor_x.step(-5);
					motor_y.step(5);
					motor_y.step(-5);
				}	
				else
				{
					System.out.println("Malformed move command");
				}

				System.out.print("\n>");

			}
		}
		catch (IOException ioe) 
		{
			ioe.printStackTrace();
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
