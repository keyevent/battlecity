package demo.training.zhuyurui;

import robocode.AdvancedRobot;
import robocode.ScannedRobotEvent;

import java.awt.*;
import java.awt.geom.Point2D;

public class Excelsior extends AdvancedRobot {

    private int direction = 1;
    enemyStatus enemy = new enemyStatus();
    boolean target=false;

    private static final double DOUBLE_PI = (Math.PI * 2);
    private static final double HALF_PI = (Math.PI / 2);

    private static final double WALL_AVOID_INTERVAL = 10;
    private static final double WALL_AVOID_FACTORS = 20;
    private static final double WALL_AVOID_DISTANCE = WALL_AVOID_FACTORS * WALL_AVOID_INTERVAL;


    @Override
    public void run() {
        
        setBodyColor(Color.black);
        setGunColor(Color.black);
        setRadarColor(Color.yellow);
        setBulletColor(Color.cyan);
        setScanColor(Color.cyan);



        while (true) {
            if(!target){
                setTurnRadarRightRadians(Double.POSITIVE_INFINITY);
                execute();
            }else{
                doScan();
                doMove();
                doFire();
                execute();
            }

        }

    }

    private void doMove(){
        turnRightRadians(adjustHeading(0));
        setAhead(100);
    }

    private void doScan(){
        double radar;
        radar=normalizeRelAngle(enemy.direction=getHeadingRadians());
        setTurnRadarRightRadians(radar*2);
    }

    private void doFire(){
        double bPower=enemy.calcFirePower(enemy.energy);
        double bVelocity=20-3*bPower;

        double bHeading=enemy.headingRadian-enemy.preVelocity+0.001;

        double r=enemy.velocity/bHeading;

        double predictDirection=0D;
        double eDistance=enemy.distance;

        for (int i=0;i<4;i++){
            double bFlyingTime=eDistance/bVelocity;
            double preHeadingRadian=enemy.headingRadian+bHeading*bFlyingTime;
            double predictX=enemy.x-r*Math.cos(preHeadingRadian)+Math.cos(enemy.headingRadian);
            double predictY=enemy.y+r*Math.sin(preHeadingRadian)-Math.sin(enemy.headingRadian);
            predictDirection=getAngle(getX(),getY(),predictX,predictY);

            eDistance= Point2D.distance(getX(),getY(),predictX,predictY);

        }
        double gun=normalizeRelAngle(predictDirection-getGunHeadingRadians());
        setTurnGunRightRadians(gun);
        if(getGunHeat()==0){
            setFire(bPower);
        }
    }

    private double adjustHeading(double heading) {
        double mapHeight = getBattleFieldHeight();
        double mapWidth = getBattleFieldWidth();
        double centerX = (mapWidth / 2);
        double centerY = (mapHeight / 2);
        double x = getX();
        double y = getY();
        boolean nearWall = false;
        double desiredX;
        double desiredY;
        // If we are too close to a wall, calculate a course toward the center of the battlefield.
        if ((y < WALL_AVOID_DISTANCE)
                || ((mapHeight - y) < WALL_AVOID_DISTANCE)) {
            desiredY = centerY;
            nearWall = true;
        } else {
            desiredY = y;
        }
        if ((x < WALL_AVOID_DISTANCE)
                || ((mapWidth - x) < WALL_AVOID_DISTANCE)) {
            desiredX = centerX;
            nearWall = true;
        } else {
            desiredX = x;
        }
        // Determine the safe heading and factor it in with the desired heading if the bot is near a wall
        if (nearWall) {
            double dHeading = normalizeRelAngle(getAngle(x,y,desiredX,desiredY)-getRelativeHeading());

            //get the min distance
            double distanceToWall = Math.min(Math.min(x, (mapWidth - x)),
                    Math.min(y, (mapHeight - y)));
            int wallFactor = (int) Math.min((distanceToWall / WALL_AVOID_INTERVAL), WALL_AVOID_FACTORS);

            return ((((WALL_AVOID_FACTORS - wallFactor) * dHeading) + (wallFactor * heading)) / WALL_AVOID_FACTORS);
        } else {
            return heading;
        }
    }


    private double getAngle(double sourceX, double sourceY, double targetX, double targetY) {
        return Math.atan2((targetX - sourceX), (targetY - sourceY));
    }




    //change into angle within -180 ~ 180
    private static double normalizeRelAngle(double angle) {
        double trimmedAngle = (angle % DOUBLE_PI);
        if (trimmedAngle > Math.PI) {
            return -(Math.PI - (trimmedAngle % Math.PI));
        } else if (trimmedAngle < -Math.PI) {
            return (Math.PI + (trimmedAngle % Math.PI));
        } else {
            return trimmedAngle;
        }
    }


    //change into angle within 0 ~ 360
    private double normalizeAbsAngle(double angle) {
        if (angle < 0) {
            return (DOUBLE_PI + (angle % DOUBLE_PI));
        } else {
            return (angle % DOUBLE_PI);
        }
    }


    private double getRelativeHeading() {
        double relativeHeading = getHeadingRadians();
        if (direction < 1) {
            relativeHeading = normalizeAbsAngle(relativeHeading
                    + Math.PI);
        }
        return relativeHeading;
    }

    private void reverseDirection() {
        double distance = (getDistanceRemaining() * direction);
        direction *= -1;
        setAhead(distance);
    }


    @Override
    public void setAhead(double distance) {
        double relativeDistance = (distance * direction);
        super.setAhead(relativeDistance);
        if (distance < 0) {
            direction *= -1;
        }
    }


    @Override
    public void setBack(double distance) {
        double relativeDistance = (distance * direction);
        super.setBack(relativeDistance);
        if (distance > 0) {
            direction *= -1;
        }
    }



    @Override
    public void turnRightRadians(double angle) {
        double turn = normalizeRelAngle(angle);
        if (Math.abs(turn) > HALF_PI) {
            reverseDirection();
            if (turn < 0) {
                turn = (HALF_PI + (turn % HALF_PI));
            } else if (turn > 0) {
                turn = -(HALF_PI - (turn % HALF_PI));
            }
        }
        super.turnRightRadians(turn);

    }


    @Override
    public void onScannedRobot(ScannedRobotEvent e){
        target=true;
        enemy.updateStatus(e,this);
    }



}

class enemyStatus{
    public double headingRadian=0D;
    public double bearingRadian=0D;
    public double distance=500D;
    public double direction=0D;
    public double x=0D;
    public double y=0D;
    public double velocity=0D;
    public double preVelocity=0D;
    public double preHeadingRadian=0D;
    public double energy=100D;

    public void updateStatus(ScannedRobotEvent e,AdvancedRobot bot){
        preHeadingRadian=headingRadian;
        preVelocity=velocity;
        headingRadian=e.getHeadingRadians();
        bearingRadian=e.getBearingRadians();
        distance=e.getDistance();

        direction=bearingRadian+bot.getHeadingRadians();
        x=bot.getX()+Math.sin(direction)*distance;
        y=bot.getY()+Math.cos(direction)*distance;
    }

    public double calcFirePower(double energy){
        return Math.min(3,energy/4+0.1D);
    }



}
