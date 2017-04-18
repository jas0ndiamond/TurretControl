package org.jason.turretcontrol.motors.wrapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class MotorShell {


	public static void main(String[] args) throws Exception
	{	
		boolean runShell = true;

		BufferedReader stdIn = null;

		MotorControl mc = new MotorControl();
		
		mc.addMotor(MotorControl.MOTOR_X_NAME, MotorControl.MOTOR_X_ID, 4, 3, 200);
		mc.addMotor(MotorControl.MOTOR_Y_NAME, MotorControl.MOTOR_Y_ID, 4, 3, 200);
		
		try
		{
			stdIn = new BufferedReader(new InputStreamReader(System.in));

			System.out.print(">");
			String command = null;

			while( runShell && (command = stdIn.readLine()) != null )
			{
				command.replaceAll("\n", "");

				if(command.equals("exit") || command.equals("quit") || command.equals("q") || command.equals("shutdown"))
				{					
					mc.killMotors();
					
					System.out.println("Take care...");
					runShell = false;
				}
				else if(command.matches("^[xy]\\s+[fb]\\s+\\d+\\s+\\d\\s*"))
				{
					//motor direction steps style
					String[] fields = command.split("\\s+");

					int steps = Integer.parseInt(fields[2]);
					int stepStyle = Integer.parseInt(fields[3]);

					int direction = 0;
					
					if(fields[1].equals("f"))
					{
						direction = 0;
					}
					else if(fields[1].equals("b"))
					{									
						direction = 1;
					}

					if(stepStyle > 0 && stepStyle < 4)
					{
						System.out.println("Stepping with style: " + stepStyle + " and direction " + direction); 
						
						if(fields[0].equals("x"))
						{
							mc.stepMotorX(steps, direction);
							System.out.println("Motor x advanced steps " + steps + " in direction: " + direction);
						}
						else if(fields[0].equals("y"))
						{
							mc.stepMotorY(steps, direction);
							
							System.out.println("Motor y advanced steps " + steps + " in direction: " + direction);
						}
					}
				}
				else if(command.equals("killmotors"))
				{
					mc.killMotors();
				}	
				else if(command.equals("hold"))
				{
					mc.stepMotorX(5, 0);
					mc.stepMotorX(5, 1);
					mc.stepMotorY(5, 0);
					mc.stepMotorY(5, 1);
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
