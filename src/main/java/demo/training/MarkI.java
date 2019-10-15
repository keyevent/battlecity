package demo.training;

import robocode.*;

import java.awt.*;

public class MarkI extends AdvancedRobot {


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

        turnRadarLeft(Double.POSITIVE_INFINITY);
        while (true) {

//            setAhead(5000);
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
        super.onHitRobot(event);
    }

    @Override
    public void onHitWall(HitWallEvent event) {
        super.onHitWall(event);
    }

    @Override
    public void onScannedRobot(ScannedRobotEvent event) {
        double heading = event.getHeading();
        double bearing = event.getBearing();
//        setTurnRadarRight(event.getBearing());
        setTurnRight(bearing);
        setAhead(100);
        shotHim(event.getDistance(), bearing);
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
        if (distance < 200 && Math.abs(bearing) < 10) {
            out.println("fire Rules.MIN_BULLET_POWER");
            this.fire(Rules.MIN_BULLET_POWER);
            return;
        }

    }
}
