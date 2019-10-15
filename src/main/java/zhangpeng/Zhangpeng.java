package zhangpeng;


import robocode.*;

import java.awt.*;


/*
 * lack of better wall avoid
 * */
public class Zhangpeng extends AdvancedRobot {
    //    private double enemyEnergy = 100;
    private int direction = 1;

    public void run() {
        // Set colors
        setBodyColor(new Color(255, 0, 35));
        setGunColor(new Color(222, 12, 0));
        setRadarColor(new Color(222, 0, 5));
        setBulletColor(new Color(255, 0, 35));
        setScanColor(new Color(255, 0, 35));

        setAdjustGunForRobotTurn(true);
        setAdjustRadarForGunTurn(true);
        //
        turnRadarRight(Double.POSITIVE_INFINITY);
    }

    @Override
    public void onHitByBullet(HitByBulletEvent event) {
        super.onHitByBullet(event);
    }

    @Override
    public void onHitRobot(HitRobotEvent event) {
        super.onHitRobot(event);
    }

    @Override
    public void onHitWall(HitWallEvent event) {
        direction = -direction;
    }

    /*
     * if the distance > 150, we go toward the enemy, and shoot
     * if the enemy is close, go around him, and shoot
     *
     * the angle to turn the gun must add an offset, which depends on the speed of enemy diverge our original track
     * */
    @Override
    public void onScannedRobot(ScannedRobotEvent event) {
        double safeDistance = 140;
        double amountToTurnGun;
        double enemyAbsDirection = event.getBearingRadians() + getHeadingRadians();
        double offset = event.getVelocity() * Math.sin(event.getHeadingRadians() - enemyAbsDirection);
        double radarAngle = robocode.util.Utils.normalRelativeAngle(enemyAbsDirection - getRadarHeadingRadians());
        setTurnRadarRightRadians(radarAngle * 2.0);//radar lock

        if (event.getDistance() > safeDistance + 10) {
            amountToTurnGun = robocode.util.Utils.normalRelativeAngle(enemyAbsDirection - getGunHeadingRadians() + offset / 22);
            setTurnGunRightRadians(amountToTurnGun);
            setTurnRightRadians(robocode.util.Utils.normalRelativeAngle(enemyAbsDirection - getHeadingRadians()));

//            setTurnRight(enemyAbsDirection - getHeadingRadians()+wallAvoid);
            setAhead((event.getDistance() - safeDistance) * direction);
            setFire(Rules.MAX_BULLET_POWER);
        } else {
            amountToTurnGun = robocode.util.Utils.normalRelativeAngle(enemyAbsDirection - getGunHeadingRadians() + offset / 15);
            setTurnGunRightRadians(amountToTurnGun);
            setTurnLeftRadians(-90 - event.getBearingRadians());
//            setTurnRightRadians(90 - event.getBearingRadians());
//            double x = getX() + Math.sin(enemyAbsDirection - getHeadingRadians()) * (event.getDistance() - 140);
//            double y = getY() + Math.cos(enemyAbsDirection - getHeadingRadians()) * (event.getDistance() - 140);
//            if (x < 30 || x > getBattleFieldWidth() - 30 || y < 30 || y > getBattleFieldHeight() - 30) {
//                setBack(event.getDistance() - 140);
//            } else {
//                setAhead((event.getDistance() - 140));
//            }
            setAhead((event.getDistance() - safeDistance) * direction);
            setFire(Rules.MAX_BULLET_POWER);
        }
    }
}
