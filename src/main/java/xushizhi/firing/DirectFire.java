package xushizhi.firing;

import robocode.AdvancedRobot;
import robocode.ScannedRobotEvent;
import robocode.util.Utils;

public class DirectFire implements FireControl {

    private double minStrength, maxStrength;
    private double minDist, maxDist;

    public DirectFire(final double minStrength, final double maxStrength, final double maxDist, final double minDist) {
        this.minStrength = minStrength;
        this.maxStrength = maxStrength;
        this.minDist = minDist;
        this.maxDist = maxDist;
    }

    @Override
    // Turn the gun towards the target
    public void takeAim(final AdvancedRobot advRobot, final ScannedRobotEvent eSR) {
        // getHeadingRadians() -> Returns the heading of the robot, in radians (0 <= getHeading() < 2 * PI)
        // getBearingRadians() -> Returns the bearing to the robot you scanned, relative to your robot's heading, in radians (-PI <= getBearingRadians() < PI)
        final double absoluteRobotBearing = advRobot.getHeadingRadians() + eSR.getBearingRadians();
        // getGunHeadingRadians() -> Returns the direction that the robot's gun is facing, in radians
        final double absoluteGunBearing = absoluteRobotBearing - advRobot.getGunHeadingRadians();
        final double relativeGunBearing = Utils.normalRelativeAngle(absoluteGunBearing);

        advRobot.setTurnGunRightRadians(relativeGunBearing);  // Rotate the barrel

        fire(advRobot, eSR, relativeGunBearing);
    }

    @Override
    public void fire(final AdvancedRobot advRobot, final ScannedRobotEvent eSR, final double relativeGunBearing) {
        // Close Dist -> Heavy Shot, Long Dist -> Weak Shot
        if (relativeGunBearing < Math.PI / 15) {  // Fire when the barrel only have to rotate slightly
            final double closer_heavier = Math.min(1.0, (this.maxDist - eSR.getDistance()) / (this.maxDist - this.minDist));
            final double firepower = this.minStrength + (1.0 - Math.max(0.0, closer_heavier)) * (this.maxStrength - this.minStrength);
            advRobot.setFire(firepower);
        } else if (relativeGunBearing < Math.PI / 6) {  // Wish for lucky shot
            advRobot.setFire(minStrength);
        }
    }
}
