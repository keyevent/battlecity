package demo.training;

import robocode.*;
import java.awt.geom.Point2D;
import robocode.util.Utils;
import java.awt.*;

public class IcyGrain extends AdvancedRobot{
    private Battle battle;
    private static double SAFE_DISTANCE = 100;
    private static double POWER_DISTANCE = 100;
    public static double BULLET_POWER = 2;
    public void run(){
        battle = new Battle(this);
        setAdjustGunForRobotTurn(true);
        setAdjustRadarForGunTurn(true);
        turnRadarRightRadians(Double.POSITIVE_INFINITY);
    }
    public void onScannedRobot(ScannedRobotEvent event){
        setTurnRadarRightRadians(Utils.normalRelativeAngle
                ((event.getBearingRadians() + getHeadingRadians() -getRadarHeadingRadians()))*2);
        battle.update(event);
        battle.createWave();
        movement();
        setFire(event.getDistance() < POWER_DISTANCE ? 3 : BULLET_POWER);
        setTurnGunRightRadians(battle.getBestMatchFireAngle());
    }

    private void movement() {
        if (getDistanceRemaining() < 1) {
            double nx = Math.random() * (getBattleFieldWidth() - 2 * SAFE_DISTANCE) + SAFE_DISTANCE;
            double ny = Math.random() * (getBattleFieldHeight() - 2 * SAFE_DISTANCE) + SAFE_DISTANCE;

            double headArg = 90 - Math.atan2(ny - getY(), nx - getX());
            headArg = Utils.normalAbsoluteAngle(headArg);
            double distance = Point2D.distance(getX(), getY(), nx, ny);
            if (headArg - getHeadingRadians() > Math.PI / 2) {
                setTurnRightRadians(headArg - getHeadingRadians() + Math.PI);
                setAhead(-distance);
            }
            else {
                setTurnRightRadians(headArg - getHeadingRadians());
                setAhead(distance);
            }
        }
    }
}

