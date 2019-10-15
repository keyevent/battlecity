package caoyu;

import robocode.*;

import java.awt.*;

import static robocode.util.Utils.normalRelativeAngleDegrees;

public class Oreo extends AdvancedRobot {
    int robotHeading = 1;
    int turnHeading = 1;
    int move = 10;
    int turnMove = 10;
    int whiteCaneLength = 70;


    double diameter = 5;

    @Override
    public void run() {
        // Set colors
        setBodyColor(Color.black);
        setGunColor(Color.white);
        setRadarColor(Color.black);
        setScanColor(Color.white);
        setBulletColor(Color.white);

        //white cane out of wall condition
        Condition whiteCaneOutOfBattlefieldCondition = new Condition("whiteCaneOutOfBattlefieldCondition", 11) {
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


        setTurnGunRight(Double.POSITIVE_INFINITY);
        // Loop forever
        while (true) {
            move();
            turnGunRight(Double.POSITIVE_INFINITY); // Scans automatically
        }
    }

    @Override
    public void onHitRobot(HitRobotEvent event) {
        if (event.isMyFault()) {
            setMaxTurnRate(Rules.MAX_TURN_RATE);
            turnRight(45);
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

        if (Math.abs(bearingFromGun) <= 3) {
            turnGunRight(bearingFromGun);
            if (event.getDistance() < 300) {
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
        setTurnRight(turnHeading * turnMove);
    }

    @Override
    public void onCustomEvent(CustomEvent event) {
        if ("whiteCaneOutOfBattlefieldCondition" == event.getCondition().getName()) {
            setMaxTurnRate(Rules.MAX_TURN_RATE);
            turnHeading *= -1;
            turnRight(10);
            turnGunRight(-10);
        }
    }
}
