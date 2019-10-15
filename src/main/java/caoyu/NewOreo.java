package caoyu;

import robocode.*;

import java.awt.*;

import static robocode.util.Utils.normalRelativeAngleDegrees;

public class NewOreo extends AdvancedRobot {
    int robotHeading = 1;
    int turnHeading = 1;
    int move = 10;
    int backFromWallMove = 40;

    int diameter = 5;//用于设置最大转向速度
    int tooCloseThreshold = 30;

    @Override
    public void run() {
        // Set colors
        setBodyColor(Color.black);
        setGunColor(Color.white);
        setRadarColor(Color.black);
        setScanColor(Color.white);
        setBulletColor(Color.white);


        //set separated
        //setAdjustGunForRobotTurn(false);
        //setAdjustRadarForGunTurn(false);
        //setAdjustRadarForRobotTurn(false);


        //to close to wall condition
        Condition tooCloseToWallCondition = new Condition("tooCloseToWallCondition", 10) {
            @Override
            public boolean test() {
                return getX() < tooCloseThreshold || getX() > getBattleFieldWidth() - tooCloseThreshold || getY() < tooCloseThreshold || getY() > getBattleFieldHeight() - tooCloseThreshold;
            }
        };
        addCustomEvent(tooCloseToWallCondition);


        setTurnGunRight(Double.POSITIVE_INFINITY);
        // Loop forever
        while (true) {
            move();
            turnGunRight(10); // Scans automatically
        }
    }

    @Override
    public void onHitRobot(HitRobotEvent event) {
        if (event.isMyFault()) {
            setMaxTurnRate(Rules.MAX_TURN_RATE);
            turnRight(90);
            robotHeading *= -1;
            turnHeading *= -1;
            move();
            super.onHitRobot(event);
        }
    }

    @Override
    public void onScannedRobot(ScannedRobotEvent event) {// Calculate exact location of the robot
        double absoluteBearing = getHeading() + event.getBearing();
        double bearingFromGun = normalRelativeAngleDegrees(absoluteBearing - getGunHeading());

        if (Math.abs(bearingFromGun) <= 4) {
            turnGunRight(bearingFromGun);
            if (event.getDistance() < 250) {
                setFire(400 / event.getDistance());
            }
        } else {
            turnGunRight(bearingFromGun);
        }
        move();
    }

    void move() {
        setMaxTurnRate(Rules.MAX_TURN_RATE / diameter);
        setAhead(robotHeading * move);
        setTurnRight(turnHeading * move);
    }

    @Override
    public void onCustomEvent(CustomEvent event) {
        if ("tooCloseToWallCondition" == event.getCondition().getName()) {
            double dFromCenterX = getBattleFieldWidth() / 2 - getX();
            double dFromCenterY = getBattleFieldHeight() / 2 - getY();
            double dist = Math.sqrt(dFromCenterX * dFromCenterX + dFromCenterY * dFromCenterY);
            double sin = dFromCenterX / dist;
            double cos = dFromCenterY / dist;
            double asin = Math.asin(sin);
            double angle;
            if (cos >= 0) {
                angle = (asin + 2 * Math.PI) % (2 * Math.PI);
            } else {
                angle = (-asin + Math.PI) % (2 * Math.PI);
            }

            setMaxTurnRate(Rules.MAX_TURN_RATE);
            setTurnRight((angle / Math.PI) * 180 - getHeading());
            setTurnGunRight(10);
            setAhead(backFromWallMove);
            execute();
            turnHeading = 1;
            robotHeading = 1;
        }
    }
}
