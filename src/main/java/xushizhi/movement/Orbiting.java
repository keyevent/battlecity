package xushizhi.movement;

import robocode.AdvancedRobot;
import robocode.ScannedRobotEvent;
import robocode.util.Utils;

public class Orbiting implements MovementStrategy {

    private double ahead;  // Forward movement

    public Orbiting() {
        this.ahead = 1.0;
    }

    @Override
    public void move(final AdvancedRobot advancedRobot, final ScannedRobotEvent eSR) {
        if (Math.random() <= 0.05) {
            this.ahead *= -1;
        }

        final double bearingRadian = eSR.getBearingRadians();  // (-PI <= getBearingRadians() < PI)
        // Turn the hull left or right, the slighter the better
        final double leftSpin = Utils.normalRelativeAngle(bearingRadian + Math.PI / 2);
        final double rightSpin = Utils.normalRelativeAngle(bearingRadian - Math.PI / 2);
        if (Math.abs(leftSpin) < Math.abs(rightSpin)) {
            advancedRobot.setTurnRightRadians(leftSpin);
        } else {
            advancedRobot.setTurnRightRadians(rightSpin);
        }

        advancedRobot.setAhead(this.ahead * 20);  // Move forward
    }
}
