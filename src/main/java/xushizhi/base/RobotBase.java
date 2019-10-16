package xushizhi.base;

import robocode.*;
import robocode.util.Utils;
import xushizhi.firing.FireControl;
import xushizhi.movement.MovementStrategy;

import java.awt.*;

public abstract class RobotBase extends AdvancedRobot {

    private FireControl targeting;
    private MovementStrategy movement;
    private static final double BASE_MOVEMENT = 180;

    RobotBase(final FireControl targeting, final MovementStrategy movement) {
        this.targeting = targeting;
        this.movement = movement;
    }

    public void run() {
        setBodyColor(Color.BLUE);
        setGunColor(Color.BLUE);
        setBulletColor(Color.WHITE);
        setRadarColor(Color.RED);
        setScanColor(Color.YELLOW);

        this.setAdjustGunForRobotTurn(true);
        this.setAdjustRadarForGunTurn(true);
        this.setAdjustRadarForRobotTurn(true);
        this.setTurnRadarLeftRadians(Math.PI * 4);

        while (true) {
            // setMaxVelocity() -> Sets the maximum velocity of the robot measured in pixels/turn
            // * if the robot should move slower than Rules.MAX_VELOCITY (8 pixels/turn)
            this.setMaxVelocity(MovementStrategy.maxVelocity(this.getX(), this.getY()));
            this.execute();
        }
    }

    public void onScannedRobot(final ScannedRobotEvent eSR) {
        // getHeadingRadians() -> Returns the heading of the robot, in radians (0 <= getHeading() < 2 * PI)
        // getBearingRadians() -> Returns the bearing to the robot you scanned, relative to your robot's heading, in radians (-PI <= getBearingRadians() < PI)
        final double absoluteRobotBearing = this.getHeadingRadians() + eSR.getBearingRadians();
        // getGunHeadingRadians() -> Returns the direction that the robot's radar is facing, in radians
        final double absoluteRadarBearing = absoluteRobotBearing - this.getRadarHeadingRadians();
        double relativeRadarBearing = Utils.normalRelativeAngle(absoluteRadarBearing);

        relativeRadarBearing += relativeRadarBearing / Math.abs(relativeRadarBearing) * (Math.PI / 30);
        this.setTurnRadarRightRadians(relativeRadarBearing);

        this.targeting.takeAim(this, eSR);
        this.movement.orbiting(this, eSR);
    }

    // Hit wall event
    public void onHitWall(final HitWallEvent eHW) {
        if (Math.abs(movement.getMovement()) > BASE_MOVEMENT) {
            this.movement.setMovement(BASE_MOVEMENT);
        }
    }

    // Restart radar if the robot is dead
    public void onRobotDeath(RobotDeathEvent eRD) {
        setTurnRadarRightRadians(Math.PI * 4);
    }

    // Restart radar if the robot is hit by a bullet
    public void onHitByBullet(HitByBulletEvent e) {
        setTurnRadarRightRadians(Math.PI * 4);
    }
}
