#import Adafruit_MotorHAT, Adafruit_DCMotor, Adafruit_Stepper
from Adafruit_MotorHAT import Adafruit_MotorHAT, Adafruit_StepperMotor

import sys

def main(sys):
	
	#x:1. y:2
	
	#StepperCommand s 1 200 f 30 3 3
	#[motor steps-per-rev direction steps style speed]

	#StepperCommand k 1
	#[ motor ]
        
	if(len(sys.argv) == 3 and sys.argv[1] == "k"):
		#kill motor
		motor = int(sys.argv[2])
		
		if(motor == 1):
			mh = Adafruit_MotorHAT()
			mh.getMotor(1).run(Adafruit_MotorHAT.RELEASE)
			mh.getMotor(2).run(Adafruit_MotorHAT.RELEASE)
		elif(motor == 2):
			mh = Adafruit_MotorHAT()
			mh.getMotor(4).run(Adafruit_MotorHAT.RELEASE)
			mh.getMotor(3).run(Adafruit_MotorHAT.RELEASE)
		else:
			print "Cannot execute kill - Unrecognized motor\n"
		
	elif(len(sys.argv) == 8 and sys.argv[1] == "s"):
		
		motor_num = int(sys.argv[2])

		
		if( (motor_num != 1 and motor_num != 2) ):
			print "Cannot execute step -- Unrecognized motor\n"
			return

		steps_per_rev = int(sys.argv[3])
		
		direction = sys.argv[4]
		step_direction = None
			
		if( direction == "f" ):
			step_direction = Adafruit_MotorHAT.FORWARD
		elif( direction == "b" ):
			step_direction = Adafruit_MotorHAT.BACKWARD
		else:
			print "Cannot execute step -- Unrecognized direction\n"
			return
		
		steps = int(sys.argv[5])
		style = int(sys.argv[6])
		speed = int(sys.argv[7])
		
		mh = Adafruit_MotorHAT()
		motor = mh.getStepper(steps_per_rev, motor_num)  # 200 steps/rev, motor port #2
		motor.setSpeed(speed)             # RPM
		
		motor.step(steps, step_direction, style)
		
	else:
		print("Unrecognized command\n")

main(sys)