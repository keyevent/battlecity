package xushizhi.base;

import robocode.AdvancedRobot;
import robocode.HitWallEvent;
import robocode.ScannedRobotEvent;
import robocode.util.Utils;
import xushizhi.firing.FireControl;
import xushizhi.movement.MovementStrategy;

public abstract class RobotBase extends AdvancedRobot {

    private FireControl targeting;
    private MovementStrategy movement;

    RobotBase(final FireControl targeting, final MovementStrategy movement) {
        this.targeting = targeting;
        this.movement = movement;
    }

    public void run() {
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

        relativeRadarBearing += relativeRadarBearing / Math.abs(relativeRadarBearing) * (Math.PI / 9);
        this.setTurnRadarRightRadians(relativeRadarBearing);

        this.targeting.takeAim(this, eSR);
        this.movement.move(this, eSR);
    }

    public void onHitWall(final HitWallEvent e) {
        super.onHitWall(e);
    }
}
