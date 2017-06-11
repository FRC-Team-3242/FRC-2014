/*----------------------------------------------------------------------------*/
/* Copyright (c) FIRST 2008. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package com.fhsemit.first.iterative;


import edu.wpi.first.wpilibj.AnalogPotentiometer;
import edu.wpi.first.wpilibj.CANJaguar;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.RobotDrive;
import edu.wpi.first.wpilibj.Talon;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.can.CANTimeoutException;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the IterativeRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the manifest file in the resource
 * directory.
 */
public class RobotTemplate extends IterativeRobot {
    
    RobotDrive driveMotors;
    Joystick xBoxController;
    Talon forkLiftMotor;
    Talon shooterMotor1;
    Talon shooterMotor2;
    Talon pushOffMotor;
    
    DigitalInput armSensor;
    DigitalInput armSensorDown;
    
    AnalogPotentiometer forkPot;
    AnalogPotentiometer shooterPot;
    
    int teleopShootState;
    Timer timer;
    
    /**
     * This function is run when the robot is first started up and should be
     * used for any initialization code.
     */
    public void robotInit() {
        CANJaguar driveM1 = null;
        CANJaguar driveM2 = null;
        CANJaguar driveM3 = null;
        CANJaguar driveM4 = null;
        try {
            driveM1 = new CANJaguar(4);
            driveM2 = new CANJaguar(2);
            driveM3 = new CANJaguar(5);
            driveM4 = new CANJaguar(3);
        } catch (CANTimeoutException ex) {
            ex.printStackTrace();
        }
        
        driveMotors = new RobotDrive(driveM1,driveM2,driveM3,driveM4);//4,2,5,3
        driveMotors.setInvertedMotor(RobotDrive.MotorType.kFrontRight, true);
        driveMotors.setInvertedMotor(RobotDrive.MotorType.kRearRight, true);
        
        xBoxController = new Joystick(1);
        
        forkLiftMotor = new Talon(1);
        shooterMotor1 = new Talon(2);
        shooterMotor2 = new Talon(3);
        pushOffMotor = new Talon(4);
        
        armSensor = new DigitalInput(1);
        armSensorDown = new DigitalInput(2);
        
        forkPot = new AnalogPotentiometer(1);
        shooterPot = new AnalogPotentiometer(2);
        
        System.out.print("test init");
        
        teleopShootState = 0;
        timer = new Timer();
        timer.start();
        
        di = new DigitalInput(3);
    }

    /**
     * This function is called periodically during autonomous
     */
    public void autonomousPeriodic() {

    }

    /**
     * This function is called periodically during operator control
     */
    public void teleopPeriodic() {
        forkLiftMotor.set(xBoxController.getRawAxis(3));//forklift control
        teleopDrive();
        teleopShoot();
        
    }
    
    
    private void teleopDrive(){
        double xAxis = xBoxController.getRawAxis(1);
        double yAxis = xBoxController.getRawAxis(2);
        double strafe = 0;
        
        if(xAxis <= 0.1 && xAxis >= -0.1) xAxis = 0;
        if(yAxis <= 0.1 && yAxis >= -0.1) yAxis = 0;
        
        if(xBoxController.getRawButton(2)) strafe += 0.5;
        if(xBoxController.getRawButton(3)) strafe -= 0.5;
        
        driveMotors.mecanumDrive_Cartesian(strafe, yAxis, xAxis, 0);
        //TODO figure out if the gyro parameter is going to be a problem
    }
    
    private void teleopShoot(){
        switch(teleopShootState){
            case 0://waiting for button
                if(xBoxController.getRawButton(6)) teleopShootState++;
                break;
            case 1://moving forklift
                if(forkPot.get() >= 4.35){
                    forkLiftMotor.set(0);
                    teleopShootState = 3;
                    timer.reset();
                    break;
                }
                forkLiftMotor.set(-1);
                break;
            case 2://starting push off motor
                pushOffMotor.set(1);//TODO configure direction
                if(timer.get() > 0.3){
                    teleopShootState++;
                    timer.reset();
                }//TODO configure timing
                break;
            case 3://starting shooter motors and resetting push off motor
                if(timer.get() > 0.8){//TODO configure timing
                    pushOffMotor.set(0);
                }else{
                    pushOffMotor.set(1);
                }
                
                shooterMotor1.set(-1);
                shooterMotor2.set(-1);
                if(armSensor.get()){
                    pushOffMotor.set(0);//to be safe
                    teleopShootState++;
                    timer.reset();
                }
                break;
            case 4://brake
                shooterMotor1.set(0.11);
                shooterMotor2.set(0.11);
                if(timer.get() > 0.5){
                    timer.reset();
                    teleopShootState++;
                }
                break;
            case 5://reverse
                shooterMotor1.set(0.4);//TODO configure power
                shooterMotor2.set(0.4);
                if(this.armSensorDown.get()){
                    shooterMotor1.set(0);
                    shooterMotor2.set(0);
                    teleopShootState = 0;
                }
                break;
        }
        
    }
    
    
    DigitalInput di;
    /**
     * This function is called periodically during test mode
     */
    public void testPeriodic(){ 
        System.out.println("DI 1 (armSensor): " + armSensor.get());
        System.out.println("DI 2 (armSensorDown): " + armSensorDown.get());
        System.out.println("DI 3 (???): " + this.di.get());
        
    }
}