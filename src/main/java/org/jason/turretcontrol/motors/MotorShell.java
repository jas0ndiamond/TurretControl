package org.jason.turretcontrol.motors;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class MotorShell {

	public static void main(String[] args) throws Exception
	{
		AdafruitMotorHAT mh;
		AdafruitMotorHAT.AdafruitStepperMotor motor_x, motor_y;

		mh = new AdafruitMotorHAT(0x60, 1600); // Default addr 0x60

		motor_y = mh.getStepper(AdafruitMotorHAT.AdafruitStepperMotor.PORT_M1_M2);
		motor_y.setSpeed(30d); // 30 RPM

		motor_x = mh.getStepper(AdafruitMotorHAT.AdafruitStepperMotor.PORT_M3_M4);
		motor_x.setSpeed(30d); // 30 RPM

		boolean runShell = true;

		BufferedReader stdIn = null;


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
					System.out.println("Take care...");
					runShell = false;
				}
				else
				{
					if(command.matches("^[xy]\\s+[fb]\\s+\\d+\\s*"))
					{
						String[] fields = command.split("\\s");

						int steps = Integer.parseInt(fields[2]);

						if(fields[0].equals("x"))
						{

							if(fields[1].equals("f"))
							{
								motor_x.step(steps, AdafruitMotorHAT.ServoCommand.FORWARD, AdafruitMotorHAT.Style.MICROSTEP);
							}
							else if(fields[1].equals("b"))
							{									
								motor_x.step(steps, AdafruitMotorHAT.ServoCommand.BACKWARD, AdafruitMotorHAT.Style.MICROSTEP);
							}
						}
						else if(fields[0].equals("y"))
						{
							if(fields[1].equals("f"))
							{
								motor_y.step(steps, AdafruitMotorHAT.ServoCommand.FORWARD, AdafruitMotorHAT.Style.MICROSTEP);
							}
							else if(fields[1].equals("b"))
							{									
								motor_y.step(steps, AdafruitMotorHAT.ServoCommand.BACKWARD, AdafruitMotorHAT.Style.MICROSTEP);
							}
						}
					}
					else
					{
						System.out.println("Malformed move command");
					}
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
