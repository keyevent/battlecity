package dingziyuan;

import robocode.*;
import robocode.util.Utils;
import dingziyuan.utils.Vector3;

import java.awt.*;


public class DZYbot extends AdvancedRobot {
    enum Direction {
        CLOCKWISE,
        COUNTER_CLOCKWISE
    }

    private Vector3 centerPos;
    private Vector3 enemyPos = new Vector3(0, 0, 0);
    private final int BORDER_WIDTH = 40;

    @Override
    public void onDeath(DeathEvent event) {
        super.onDeath(event);
    }


    @Override
    public void run() {
        Color color = Color.WHITE;
        setBodyColor(color);
        setGunColor(color);
        setRadarColor(color);
        setBulletColor(color);
        setScanColor(color);

        setAdjustGunForRobotTurn(true);
        setAdjustRadarForGunTurn(true);
        setTurnRadarLeft(Double.POSITIVE_INFINITY);
        centerPos = new Vector3(getBattleFieldWidth() / 2, getBattleFieldHeight() / 2, 0);

        while (true) {
            this.setMaxVelocity(8.0);

            if (getX() < BORDER_WIDTH
                    || getX() > getBattleFieldWidth() - BORDER_WIDTH
                    || getY() < BORDER_WIDTH
                    || getY() > getBattleFieldHeight() - BORDER_WIDTH) {
                this.setMaxVelocity(4.0);

                Vector3 currentPos = new Vector3(getX(), getY(), 0);
                Vector3 toDir = centerPos.sub(currentPos).normlize();
                Vector3 currentDir = new Vector3(getHeadingRadians()).normlize();
                double delta = Math.abs(Math.acos(toDir.dot(currentDir) / (toDir.norm() * currentDir.norm()))) * 57.3;
                if (toDir.dot(currentDir) >= 0) {
                    System.out.println("GO");
                    setAhead(100);
                } else {
                    if (currentDir.isOnMyRight(toDir))
                        setTurnRight(delta);
                    else
                        setTurnLeft(delta);
                }
            }
            //tracking enemy bot
            else {
                goCircle(enemyPos, 150, 1, Direction.COUNTER_CLOCKWISE);
            }
            setAhead(100);
            execute();
        }
    }


    @Override
    public void onHitByBullet(HitByBulletEvent event) {
        super.onHitByBullet(event);
    }

    @Override
    public void onHitRobot(HitRobotEvent event) {

        double bearing = event.getBearing();
        if (Math.abs(bearing) < 90) {
            out.println("mayday mayday, close combat, fire at will ");
            fire(Rules.MAX_BULLET_POWER);
        }
        setBack(50);
        gotoPoint(centerPos,1);
        super.onHitRobot(event);
    }

    @Override
    public void onHitWall(HitWallEvent event) {
        System.out.println("Hit WALL!!");
        super.onHitWall(event);
    }

    @Override
    public void onScannedRobot(ScannedRobotEvent event) {
        System.out.println("SCANNED!!");
        Vector3 currentPos = new Vector3(getX(), getY(), 0);
        Vector3 toEnemyDir = new Vector3(getGunHeadingRadians()).normlize();
        enemyPos = currentPos.add(toEnemyDir.mul(event.getDistance()));
        System.out.println("currentPos: " + currentPos);
        System.out.println("toEnemyDir: " + toEnemyDir);
        System.out.println("enemyPos: " + enemyPos);

        double delta = 10 + event.getDistance() * 0.05;
        setTurnRadarRight(2.0 * Utils.normalRelativeAngleDegrees(getHeading() + event.getBearing() - getRadarHeading()));
        setTurnGunRight(1.0 * Utils.normalRelativeAngleDegrees(getHeading() + event.getBearing() - getGunHeading()) + delta);
        setFire(1);
//        shotHim(event.getDistance(), event.getBearing());
    }

    public void gotoPoint(Vector3 to, double speed) {
        System.out.println("gotoPoint:" + to);
        Vector3 currentPos = new Vector3(getX(), getY(), 0);
        Vector3 toDir = to.sub(currentPos).normlize();
        double dis = to.sub(currentPos).norm();
        Vector3 currentDir = new Vector3(getHeadingRadians()).normlize();

//        if (dis < 10)
//            return;

        //calculate the angle between currentDir and toDir
        System.out.println(Math.abs(Math.acos(toDir.dot(currentDir) / (toDir.norm() * currentDir.norm()))));
        double delta = Math.abs(Math.acos(toDir.dot(currentDir) / (toDir.norm() * currentDir.norm()))) * 57.3;
        if (delta <= 5) {
            System.out.println("GO");
            setAhead(dis);
        }
        if (delta >= 170) {
            System.out.println("GO");
            setBack(dis);
        } else {
            if (currentDir.isOnMyRight(toDir))
                setTurnRight(delta);
            else
                setTurnLeft(delta);
        }
    }

    public void goCircle(Vector3 center, double radius, double speed, Direction circleDirection) {
        Vector3 currentPos = new Vector3(getX(), getY(), 0);
        Vector3 center2currentDir = currentPos.sub(center).normlize();
        Vector3 delta = center2currentDir.cross(new Vector3(0, 0, 1)).mul(36);
        System.out.println("delta: " + delta);
        Vector3 targetPoint = center.add(center2currentDir.mul(radius)).add(delta);
        System.out.println("target:" + targetPoint);
        gotoPoint(targetPoint, 1);

        Graphics2D g = getGraphics();
        g.setColor(Color.YELLOW);
        g.drawString("center2currentDir", (int) center.getX(), (int) center.getY());
        g.drawLine((int) center.getX(), (int) center.getY(),
                (int) center.add(center2currentDir.mul(radius)).getX(), (int) center.add(center2currentDir.mul(radius)).getY());

        g.setColor(Color.GREEN);
        g.drawString("delta", (int) center.add(center2currentDir.mul(radius)).getX(), (int) center.add(center2currentDir.mul(radius)).getY());
        g.drawLine((int) center.add(center2currentDir.mul(radius)).getX(), (int) center.add(center2currentDir.mul(radius)).getY(),
                (int) targetPoint.getX(), (int) targetPoint.getY());

        g.setColor(Color.RED);
        g.drawString("target", (int) targetPoint.getX(), (int) targetPoint.getY());
        g.drawOval((int) targetPoint.getX(), (int) targetPoint.getY(), 2, 2);
    }

    private void shotHim(double distance, double bearing) {
        if (distance < 10) {
            out.println("fire Rules.MAX_BULLET_POWER");
            fire(Rules.MAX_BULLET_POWER);
            return;
        }
        if (distance < 100 && Math.abs(bearing) < 30) {
            out.println("fire Rules.MAX_BULLET_POWER");
            fire(Rules.MAX_BULLET_POWER);
            return;
        }
        if (distance < 200) {
            out.println("fire Rules.MIN_BULLET_POWER");
            this.fire(Rules.MIN_BULLET_POWER);
            return;
        }

    }
}
