package demo.training;


import java.awt.Color;

import robocode.*;

import static java.lang.Math.PI;
import static robocode.util.Utils.normalRelativeAngle;

public class xs99159 extends AdvancedRobot {
    private String scanned = null;
    private int bullet;
    private double heightMax;
    private double widthMax;

    public void run() {
        setBodyColor(new Color(72, 13, 7));
        setGunColor(new Color(0, 0, 0));
        setRadarColor(new Color(16, 15, 52));
        setBulletColor(new Color(255, 255, 100));
        setScanColor(new Color(36, 33, 54));

        while (true) {
            setAdjustRadarForGunTurn(false);
            if (scanned == null) {
                setTurnRadarLeftRadians(2 * PI);
            }
            heightMax = getBattleFieldHeight() - getWidth();//600-18
            widthMax = getBattleFieldWidth() - getWidth();//800-18
            move();
            execute();


        }
    }

    private void move() {
        if (willHottingWalls(heightMax, widthMax, getX(), getY(), getHeadingRadians())) {
            setTurnLeftRadians(0.2);
        }

        setAhead(400);
    }


    public void onHitWall(HitWallEvent e) {

        if (getDistanceRemaining() < 0) {
            setAhead(4000);
            waitFor(new TurnCompleteCondition(this));
            setTurnRight(180);
        } else {
            setBack(4000);
            waitFor(new TurnCompleteCondition(this));
            setTurnLeft(180);

        }
    }

    public void onHitByBullet(HitByBulletEvent event) {
        double radians = normalRelativeAngle(event.getBearingRadians() + Math.PI / 2);
        out.println("Hitting by bullet: " + radians);
        setTurnLeftRadians(radians);
        execute();

    }

    public void onScannedRobot(ScannedRobotEvent e) {
        move();
        scanned = e.getName();
        double bearingRadian = e.getBearingRadians();
        double direction = bearingRadian + this.getHeadingRadians();
        double offset = normalRelativeAngle(direction - getRadarHeadingRadians());
        double distance = e.getDistance();
        setTurnRadarRightRadians(offset * 1.5);

        if (distance > 500) {
            bullet = 1;
        } else if (distance < 500 && distance > 100) {
            bullet = 2;
        } else {
            bullet = 3;
        }

        double bearing = (getHeadingRadians() + e.getBearingRadians()) % (2 * PI);
        double eX = getX() + Math.sin(bearing) * distance;
        double eY = getY() + Math.cos(bearing) * distance;
        double heading = e.getHeadingRadians();
        double newEX = newX(eX, heading, e.getVelocity(), distance / (20 - 3 * bullet));
        double newEY = newY(eY, heading, e.getVelocity(), distance / (20 - 3 * bullet));

        double radians = normalRelativeAngle(getGunHeadingRadians() - positionBearing(getX(), getY(), newEX, newEY));
        out.println("Gun need turn " + radians);
        setTurnGunLeftRadians(radians);

        fire(bullet);
        scanned = null;

    }

    private boolean willHottingWalls(double heightMax, double widthMax,
                                     double x, double y, double angle) {
        double distance = 150;
        double Min = 50;
        double newX = x + Math.sin(angle) * distance;
        double newY = y + Math.cos(angle) * distance;
        if (newX < Min || newY < Min || newX > widthMax || newY > heightMax) {
            return true;
        }
        return false;
    }


    private double newX(double x, double heading, double velocity, double time) {
        return x + Math.sin(heading) * velocity * time;
    }

    private double newY(double y, double heading, double velocity, double time) {
        return y + Math.cos(heading) * velocity * time;
    }

    private double positionBearing(double x, double y, double newX, double newY) {
        double deltaX = newX - x;
        double deltaY = newY - y;
        double s = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
        if (deltaX > 0 && deltaY > 0) {
            return Math.asin(deltaX / s);
        }
        if (deltaX > 0 && deltaY < 0) {
            return PI / 2 + Math.asin(-deltaY / s);
        }
        if (deltaX < 0 && deltaY < 0) {
            return PI + Math.asin(-deltaX / s);
        }
        if (deltaX < 0 && deltaY > 0) {
            return 1.5 * PI + Math.acos(-deltaX / s);
        }

        return 0;
    }

}
