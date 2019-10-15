package sample;

import robocode.*;
import robocode.util.Utils;
import java.awt.*;
import java.awt.geom.*;

public class df122 extends AdvancedRobot {
    private static Rectangle2D fieldRectangle;
    private static Point2D robotLocation = new Point2D.Double();
    private static Point2D oldRobotLocation = new Point2D.Double();
    private static Point2D enemyLocation = new Point2D.Double();
    private static Point2D oldEnemyLocation = new Point2D.Double();
    private static double enemyAbsoluteBearing;
    private static double enemyDistance;
    private static double enemyEnergy;
    private static double enemyFirePower;
    private static double movementDirection = 1;
    private long nextTime;

    public void run() {
        fieldRectangle = new Rectangle2D.Double(0, 0 , getBattleFieldWidth(), getBattleFieldHeight());
        setAdjustGunForRobotTurn(true);
        setAdjustRadarForGunTurn(true);
        setColors(Color.black, Color.red, Color.white);
        turnRadarRightRadians(Double.POSITIVE_INFINITY);
    }

    public void onScannedRobot(ScannedRobotEvent e) {
        oldRobotLocation.setLocation(robotLocation);
        robotLocation.setLocation(getX(), getY());
        enemyAbsoluteBearing = getHeadingRadians() + e.getBearingRadians();
        enemyDistance = e.getDistance();
        oldEnemyLocation.setLocation(enemyLocation);
        toLocation(enemyAbsoluteBearing, enemyDistance, robotLocation, enemyLocation);
        double enemyEnergyLost = enemyEnergy - e.getEnergy();
        enemyEnergy = e.getEnergy();
        if (enemyEnergyLost >= 0.1 && enemyEnergyLost <= 3.0) {
            enemyFirePower = enemyEnergyLost;
        }

        gun(Math.min(enemyEnergy / 4, getEnergy() / 5));

        move();

        setTurnRadarLeftRadians(getRadarTurnRemaining());
    }

    private void gun(double bulletPower) {
        setTurnGunRightRadians(Utils.normalRelativeAngle(enemyAbsoluteBearing - getGunHeadingRadians()));   //shoot to the enemy
        setFireBullet(bulletPower);
    }

    private void move() {
        setMaxVelocity(Math.abs(getTurnRemaining()) < 40 ? 8 : 0);
        if (Math.random() < 0.5)
            movementDirection *= -1;    //random move toward opposite direction
        Point2D destination = new Point2D.Double();
        toLocation(enemyAbsoluteBearing + Math.PI + 0.3 * movementDirection, enemyDistance * 1.1, enemyLocation, destination);
        translateInsideField(destination, 40);

        double angle = Utils.normalRelativeAngle(absoluteBearing(robotLocation, destination) - getHeadingRadians());
        int direction = 1;
        if (Math.abs(angle) > Math.PI / 2) {
            angle += Math.acos(direction = -1);
        }
        setTurnRightRadians(Utils.normalRelativeAngle(angle));
        setAhead(robotLocation.distance(destination) * direction);
    }

    private void translateInsideField(Point2D point, double margin) {
        //wall avoidance
        point.setLocation(Math.max(margin, Math.min(fieldRectangle.getWidth() - margin, point.getX())),
                Math.max(margin, Math.min(fieldRectangle.getHeight() - margin, point.getY())));
    }

    private void toLocation(double angle, double length, Point2D sourceLocation, Point2D targetLocation) {
        //cal destiny
        targetLocation.setLocation(sourceLocation.getX() + Math.sin(angle) * length, sourceLocation.getY() + Math.cos(angle) * length);
    }

    private double absoluteBearing(Point2D source, Point2D target) {
        return Math.atan2(target.getX() - source.getX(), target.getY() - source.getY());
    }
}
