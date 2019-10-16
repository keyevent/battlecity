package liuyumin;

import robocode.*;

public class lym extends AdvancedRobot {
    private double previousEnergy = 100;
    private int movementDirection = 1;
    private int gunDirection = 1;
    private int direction = 1;

    private static final double DOUBLE_PI = (Math.PI * 2);
    private static final double HALF_PI = (Math.PI / 2);
    private static final double WALL_AVOID_INTERVAL = 10;
    private static final double WALL_AVOID_FACTORS = 50;
    private static final double WALL_AVOID_DISTANCE = (WALL_AVOID_INTERVAL * WALL_AVOID_FACTORS);

    public void run() {
        while (true) {
            setTurnRightRadiansOptimal(adjustHeadingForWalls(0));
            setAhead(100);
            execute();
        }
    }


    public double calculateBearingToXYRadians(double sourceX, double sourceY,
                                              double sourceHeading, double targetX, double targetY) {
        return normalizeRelativeAngleRadians(
                Math.atan2((targetX - sourceX), (targetY - sourceY)) -
                        sourceHeading);
    }
    public double normalizeAbsoluteAngleRadians(double angle) {
        if (angle < 0) {
            return (DOUBLE_PI + (angle % DOUBLE_PI));
        } else {
            return (angle % DOUBLE_PI);
        }
    }
    public static double normalizeRelativeAngleRadians(double angle) {
        double trimmedAngle = (angle % DOUBLE_PI);
        if (trimmedAngle > Math.PI) {
            return -(Math.PI - (trimmedAngle % Math.PI));
        } else if (trimmedAngle < -Math.PI) {
            return (Math.PI + (trimmedAngle % Math.PI));
        } else {
            return trimmedAngle;
        }
    }

    private double adjustHeadingForWalls(double heading) {
        double fieldHeight = getBattleFieldHeight();
        double fieldWidth = getBattleFieldWidth();
        double centerX = (fieldWidth / 2);
        double centerY = (fieldHeight / 2);
        double currentHeading = getRelativeHeadingRadians();
        double x = getX();
        double y = getY();
        boolean nearWall = false;
        double desiredX;
        double desiredY;

        if ((y < WALL_AVOID_DISTANCE)
                || ((fieldHeight - y) < WALL_AVOID_DISTANCE)) {
            desiredY = centerY;
            nearWall = true;
        } else {
            desiredY = y;
        }
        if ((x < WALL_AVOID_DISTANCE)
                || ((fieldWidth - x) < WALL_AVOID_DISTANCE)) {
            desiredX = centerX;
            nearWall = true;
        } else {
            desiredX = x;
        }

        if (nearWall) {
            double desiredBearing = calculateBearingToXYRadians(x, y,
                    currentHeading, desiredX, desiredY);
            double distanceToWall = Math.min(Math.min(x, (fieldWidth - x)),
                    Math.min(y, (fieldHeight - y)));
            int wallFactor = (int) Math.min(
                    (distanceToWall / WALL_AVOID_INTERVAL), WALL_AVOID_FACTORS);
            return ((((WALL_AVOID_FACTORS - wallFactor) * desiredBearing) + (wallFactor * heading)) / WALL_AVOID_FACTORS);
        } else {
            return heading;
        }
    }

    public double getRelativeHeadingRadians() {
        double relativeHeading = getHeadingRadians();
        if (direction < 1) {
            relativeHeading = normalizeAbsoluteAngleRadians(relativeHeading
                    + Math.PI);
        }
        return relativeHeading;
    }

    public void reverseDirection() {
        double distance = (getDistanceRemaining() * direction);
        direction *= -1;
        setAhead(distance);
    }

    public void setAhead(double distance) {
        double relativeDistance = (distance * direction);
        super.setAhead(relativeDistance);
        if (distance < 0) {
            direction *= -1;
        }
    }

    public void setBack(double distance) {
        double relativeDistance = (distance * direction);
        super.setBack(relativeDistance);
        if (distance > 0) {
            direction *= -1;
        }
    }

    public void setTurnLeftRadiansOptimal(double angle) {
        double turn = normalizeRelativeAngleRadians(angle);
        if (Math.abs(turn) > HALF_PI) {
            reverseDirection();
            if (turn < 0) {
                turn = (HALF_PI + (turn % HALF_PI));
            } else if (turn > 0) {
                turn = -(HALF_PI - (turn % HALF_PI));
            }
        }
        setTurnLeftRadians(turn);
    }

    public void setTurnRightRadiansOptimal(double angle) {
        double turn = normalizeRelativeAngleRadians(angle);
        if (Math.abs(turn) > HALF_PI) {
            reverseDirection();
            if (turn < 0) {
                turn = (HALF_PI + (turn % HALF_PI));
            } else if (turn > 0) {
                turn = -(HALF_PI - (turn % HALF_PI));
            }
        }
        setTurnRightRadians(turn);
    }


    public void onScannedRobot(ScannedRobotEvent e) {
        fire(2);
        setTurnRight(e.getBearing() + 90 - 30 * movementDirection);

        double changeInEnergy = previousEnergy - e.getEnergy();
        if (changeInEnergy > 0 && changeInEnergy <= 3) {
            movementDirection = -movementDirection;
            setAhead((e.getDistance() / 4 + 25) * movementDirection);
        }

        gunDirection = -gunDirection;
        setTurnGunRight(100000 * gunDirection);

        previousEnergy = e.getEnergy();
    }

}
