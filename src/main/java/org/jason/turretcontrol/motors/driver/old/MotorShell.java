package org.jason.turretcontrol.motors.driver.old;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

import org.jason.turretcontrol.motors.driver.old.AdafruitMotorHAT.ServoCommand;
import org.jason.turretcontrol.motors.driver.old.AdafruitMotorHAT.Style;

public class MotorShell {

	public static void main(String[] args) throws Exception
	{
		HashMap<Integer, Style> stepStyles = new HashMap<>();
		stepStyles.put(1, AdafruitMotorHAT.Style.SINGLE);
		stepStyles.put(2, AdafruitMotorHAT.Style.DOUBLE);
		stepStyles.put(3, AdafruitMotorHAT.Style.INTERLEAVE);
		stepStyles.put(4, AdafruitMotorHAT.Style.MICROSTEP);

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

			Style stepStyle;
			ServoCommand direction;

			while( runShell && (command = stdIn.readLine()) != null )
			{
				command.replaceAll("\n", "");

				if(command.equals("exit") || command.equals("quit") || command.equals("q") || command.equals("shutdown"))
				{						
					System.out.println("Take care...");
					runShell = false;
				}
				else if(command.matches("^[xy]\\s+[fb]\\s+\\d+\\s+\\d\\s*"))
				{
					String[] fields = command.split("\\s");

					int steps = Integer.parseInt(fields[2]);
					int stepTypeInput = Integer.parseInt(fields[3]);

					direction = null;
					stepStyle = null;

					if(fields[1].equals("f"))
					{
						direction = ServoCommand.FORWARD;
					}
					else if(fields[1].equals("b"))
					{									
						direction = ServoCommand.BACKWARD;
					}

					stepStyle = stepStyles.get(stepTypeInput);

					if(stepStyle != null)
					{
						System.out.println("Stepping with style: " + stepStyle + " and direction " + direction); 

						if(fields[0].equals("x"))
						{
							motor_x.step(steps, direction, stepStyle);
						}
						else if(fields[0].equals("y"))
						{
							motor_y.step(steps, direction, stepStyle);
						}
					}

					if(direction != null && stepStyle != null)
					{
						motor_y.step(steps, direction, stepStyle);
					}
				}
				else
				{
					System.out.println("Malformed move command");
				}


				System.out.print("\n>");
			}

			motor_x.shutdown();
			motor_y.shutdown();

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
