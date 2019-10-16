package xushizhi.movement;

import robocode.AdvancedRobot;
import robocode.ScannedRobotEvent;
import robocode.util.Utils;

public class Orbiting implements MovementStrategy {

    private int ahead;  // Forward
    private double movement;  // Forward movement

    public Orbiting() {
        this.ahead = 1;
    }

    @Override
    public void orbiting(final AdvancedRobot advancedRobot, final ScannedRobotEvent eSR) {
        if (Math.random() <= 0.1) {
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

        movement = this.ahead * 60;
        advancedRobot.setAhead(movement);  // Move forward
    }

    @Override
    public double getMovement() {
        return this.movement;
    }

    @Override
    public void setMovement(double newMovement) {
        this.movement = newMovement;
    }
}
