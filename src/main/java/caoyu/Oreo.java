package caoyu;

import robocode.*;
import robocode.util.Utils;

import java.awt.*;

import static robocode.util.Utils.normalRelativeAngleDegrees;

public class Oreo extends AdvancedRobot {
    int robotHeading = 1, turnHeading = 1;
    int move = 10, turnMove = 10;
    int whiteCaneLength = 100;
    int diameter = 5;//行进直径
    int counter = 0, denominator = 300;//自行转向的周期

    Double lastScannedAbusoluteBearing;

    @Override
    public void run() {

        // Set colors
        setBodyColor(Color.black);
        setGunColor(Color.white);
        setRadarColor(Color.black);
        setScanColor(Color.white);
        setBulletColor(Color.white);

        setAdjustRadarForGunTurn(true);
        setAdjustRadarForRobotTurn(true);
        setAdjustGunForRobotTurn(true);

        //white cane out of wall condition
        Condition whiteCaneOutOfBattlefieldCondition = new Condition("whiteCaneOutOfBattlefieldCondition", 10) {
            @Override
            public boolean test() {
                double whiteCaneX =
                        getX() + robotHeading * (whiteCaneLength * Math.sin((getHeading() / 360) * (2 * Math.PI)));
                double whiteCaneY =
                        getY() + robotHeading * (whiteCaneLength * Math.cos((getHeading() / 360) * (2 * Math.PI)));
                return whiteCaneX < 0 || whiteCaneX > getBattleFieldWidth() || whiteCaneY < 0 || whiteCaneY > getBattleFieldHeight();
            }
        };
        addCustomEvent(whiteCaneOutOfBattlefieldCondition);

        setTurnRadarRight(540);
        // Loop forever
        while (true) {
            move();
            execute();
        }
    }

    void move() {
        setMaxTurnRate(Rules.MAX_TURN_RATE / diameter);
        setAhead(robotHeading * move);
        setTurnRight(turnHeading * turnMove);
        if (counter++ % denominator == 0) {
            turnHeading *= -1;
        }
    }

    void track(ScannedRobotEvent event) {
        double bearingRadian = event.getBearingRadians();
        double absoluteBearing = bearingRadian + getHeadingRadians();
        double RadarOffset;
        RadarOffset = Utils.normalRelativeAngle(absoluteBearing - getRadarHeadingRadians());
        setTurnRadarRightRadians(RadarOffset * 1.2);
    }

    @Override
    public void onHitRobot(HitRobotEvent event) {
        if (event.isMyFault()) {
            setMaxTurnRate(Rules.MAX_TURN_RATE);
            robotHeading *= -1;
            turnHeading *= -1;
            move();
            super.onHitRobot(event);
        }
    }

    @Override
    public void onCustomEvent(CustomEvent event) {
        if ("whiteCaneOutOfBattlefieldCondition" == event.getCondition().getName()) {
            setMaxTurnRate(Rules.MAX_TURN_RATE);
            turnHeading *= -1;
            turnRight(10);
            setTurnRadarRight(-5);
        }
    }

    @Override
    public void onScannedRobot(ScannedRobotEvent event) {// Calculate exact location of the robot
        double absoluteBearing = getHeading() + event.getBearing();
        double bearingFromGun = normalRelativeAngleDegrees(absoluteBearing - getGunHeading());

        track(event);
        if (Math.abs(bearingFromGun) <= 3) {
            setTurnGunRight(bearingFromGun);
            if (event.getDistance() < 300) {
                setFire(400 / event.getDistance());
            }
        } else {
            setTurnGunRight(bearingFromGun);
        }
        move();
    }

    @Override
    public void onRobotDeath(RobotDeathEvent event) {
        setTurnRadarRight(540);
    }
}
