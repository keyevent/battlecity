package demo.training;

import demo.training.util.TankUtil;
import robocode.*;

import java.awt.*;
import java.awt.geom.Point2D;

import static robocode.util.Utils.normalRelativeAngle;
import static robocode.util.Utils.normalRelativeAngleDegrees;

public class A7V extends AdvancedRobot {
    private TankUtil tankUtil;
    private Double WALL_STICK = 200D;
    private int[] hitCount = new int[2];//[0] for direction 1, [1] for direction -1
    private int direction = 1;//1 for turn right, -1 for turn left
    private double turningFactor = 1.1D;

    @Override
    public void onDeath(DeathEvent event) {
        super.onDeath(event);
    }

    @Override
    public void run() {
        setBodyColor(new Color(220, 20, 60));
        setGunColor(new Color(0, 150, 50));
        setRadarColor(new Color(0, 100, 100));
        setBulletColor(new Color(255, 255, 100));
        setScanColor(new Color(255, 200, 200));
        //
        tankUtil = new TankUtil(getBattleFieldWidth(), getBattleFieldHeight());
        turnRadarLeft(Double.POSITIVE_INFINITY);
        while (true) {
            execute();
        }
    }

    @Override
    public void onHitByBullet(HitByBulletEvent event) {
        if (direction == 1) {
            hitCount[0] += 1;
        } else {
            hitCount[1] += 1;
        }
    }

    @Override
    public void onHitRobot(HitRobotEvent event) {

        double bearing = event.getBearing();
        if (Math.abs(bearing) < 90) {
            out.println("mayday mayday, close combat, fire at will ");
            fire(Rules.MAX_BULLET_POWER);
        }
        super.onHitRobot(event);
    }

    @Override
    public void onHitWall(HitWallEvent event) {
        out.println("hit the wall. " + "x: " + getX() + "y: " + getY() + " getHeadingRadians " + getHeadingRadians());
    }

    @Override
    public void onScannedRobot(ScannedRobotEvent event) {

        Point2D.Double pos = new Point2D.Double(getX(), getY());
        double bearRadian = event.getBearingRadians();
        double headingRadian = getHeadingRadians();
        //
        double bearingRadianFromGun = normalRelativeAngle(bearRadian + headingRadian - getGunHeadingRadians());
        setTurnGunRightRadians(bearingRadianFromGun);
        out.println("bearingRadianFromGun " + bearingRadianFromGun);


        shootHim(event.getDistance(), bearingRadianFromGun);
    }

    private void shootHim(double distance, double bearingRadianFromGun) {
        out.println("distance " + distance + ", bearingRadianFromGun " + bearingRadianFromGun);
        if (distance < 100 && Math.abs(bearingRadianFromGun) < Math.PI / 6) {
            out.println("fire Rules.MAX_BULLET_POWER");
            setFire(Rules.MAX_BULLET_POWER);
            return;
        }
        if (Math.abs(bearingRadianFromGun) < Math.PI / 18) {
            out.println("fire Rules.MIN_BULLET_POWER");
            setFire(Rules.MIN_BULLET_POWER);
            return;
        }

    }

    @Override
    public void onWin(WinEvent event) {
        out.println(this.getName() + " wins");
    }
}
