package demo.training.zhuyurui;

import robocode.AdvancedRobot;
import robocode.ScannedRobotEvent;

import java.awt.*;

public class Excelsior extends AdvancedRobot {

    private double previousEnergy = 100;
    private int movementDirection = 1;
    private int gunDirection = 1;
    private int direction = 1;

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
            turnRightRadians(adjustHeading(0));
            setAhead(100);
            //setTurnRadarLeft(Double.POSITIVE_INFINITY);

            execute();
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
            double dHeading = getDestinationHeading(x, y,
                    getRelativeHeading(), desiredX, desiredY);
            //get the min distance
            double distanceToWall = Math.min(Math.min(x, (mapWidth - x)),
                    Math.min(y, (mapHeight - y)));
            int wallFactor = (int) Math.min((distanceToWall / WALL_AVOID_INTERVAL), WALL_AVOID_FACTORS);

            return ((((WALL_AVOID_FACTORS - wallFactor) * dHeading) + (wallFactor * heading)) / WALL_AVOID_FACTORS);
        } else {
            return heading;
        }
    }

    private double getDestinationHeading(double sourceX, double sourceY, double sourceHeading, double targetX, double targetY) {
        return normalizeRelAngle(
                Math.atan2((targetX - sourceX), (targetY - sourceY)) - sourceHeading);
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

    /**
     * onScannedRobot: What to do when you see another robot
     */
    @Override
    public void onScannedRobot(ScannedRobotEvent e) {
        // Stay at right angles to the opponent
        setTurnRight(e.getBearing() + 90 - 30 * movementDirection);
        fire(2);

        // If the bot has small energy drop,
        // assume it fired
        double changeInEnergy = previousEnergy - e.getEnergy();
        if (changeInEnergy > 0 && changeInEnergy <= 3) {
            // Dodge!
            movementDirection = -movementDirection;
            setAhead((e.getDistance() / 4 + 25) * movementDirection);
        }
        // When a bot is spotted, sweep the gun and radar
        gunDirection = -gunDirection;
        setTurnGunRight(99999 * gunDirection);


        // Track the energy level
        previousEnergy = e.getEnergy();
    }

}
