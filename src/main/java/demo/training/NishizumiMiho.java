package demo.training;

import robocode.*;
import robocode.util.Utils;

import java.awt.*;

// Bullet power should be 0.1~3.0
// A drop between 0.1 and 3 usually means that enemy fired a bullet
// If velocity < 2, hitting the wall causes no damage.
// The gun turns at 20 degrees per tick
// Radar turns 45 degrees per tick.
// Unless you have a good reason, you should almost always use the setXXX() version when writing AdvancedRobots.
// Two conflict set will end in a recombination behavior.
public class NishizumiMiho extends AdvancedRobot {
    private double prevEnergy = 100;
    private double mvDistance = 1.0;
    private double radarMv = 45;

    @Override
    public void run() {
        setBodyColor(new Color(57, 197, 187));
        setGunColor(new Color(57, 197, 187));
        setRadarColor(new Color(57, 197, 187));
        setBulletColor(new Color(57, 197, 187));
        setScanColor(new Color(57, 197, 187));

        // This is the main loop, all onXXX call actually happens here.
        while (true) {
            turnRadarLeft(radarMv);
        }
    }

    // TODO: Stuck in corner
    private void moveWithWallDetection(double move) {
        double heading = getHeadingRadians();
        double x = getX() + move * Math.sin(heading);
        double y = getY() + move * Math.cos(heading);
        double width = getBattleFieldWidth();
        double height = getBattleFieldHeight();
        if (x >= 30 || x <= width - 30 || y >= 30 || y <= height - 30) {
            setAhead(move);
        }
    }

    @Override
    public void onScannedRobot(ScannedRobotEvent event) {
        if (event.getDistance() >= 50) {
            tryEvade(event);
        }
        tryFire(event);
    }

    private void tryEvade(ScannedRobotEvent event) {
        double changeInEnergy = prevEnergy - event.getEnergy();
        if (changeInEnergy >= 0.1 && changeInEnergy <= 3) {
            double randomDistance = Math.random() * 0.6 + 0.7;
            mvDistance = mvDistance > 0 ? -randomDistance : randomDistance;
            setTurnRightRadians(event.getBearingRadians() + Math.PI / 2);
            moveWithWallDetection((event.getDistance() / 4 + 25) * mvDistance);
        }
        prevEnergy = event.getEnergy();
    }

    private void tryFire(ScannedRobotEvent event) {
        double enemyDist = event.getDistance();
        radarMv = -radarMv;
        double bearingRadians = event.getBearingRadians();
        // Method tryEvade and tryFire call together will end in turn right double bearing.
        setTurnLeftRadians(Math.PI / 2 - bearingRadians);
        // Method normalRelativeAngle normalizes an angle to a relative angle(-180~180)
        setTurnGunRightRadians(Utils.normalRelativeAngle(getHeadingRadians() + bearingRadians - getGunHeadingRadians()));
        setFire(400 / enemyDist);
    }

    @Override
    public void onHitWall(HitWallEvent event) {
        super.onHitWall(event);
        if (getVelocity() > 2) {
            throw new IllegalAccessError();
        }
    }
}
