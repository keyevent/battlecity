package xushizhi.movement;

import robocode.AdvancedRobot;
import robocode.ScannedRobotEvent;

public interface MovementStrategy {

    void orbiting(final AdvancedRobot advancedRobot, final ScannedRobotEvent eSR);

    static double predictVelocity(final double distance) {
        if (distance > 20.0) {
            return 8.0;
        }
        if (distance > 12.0) {
            return distance / 4.0 + 3.0;
        }
        if (distance > 6.0) {
            return distance / 3.0 + 2.0;
        }
        if (distance > 2.0) {
            return distance / 2.0 + 1.0;
        }
        return 2.0;
    }

    static double maxVelocity(final double x, final double y) {
        double maxVelocity = 8.0;
        maxVelocity = Math.min(maxVelocity, predictVelocity(x - 20.0));
        maxVelocity = Math.min(maxVelocity, predictVelocity(800.0 - x - 20.0));  // getBattleFieldWidth()
        maxVelocity = Math.min(maxVelocity, predictVelocity(y - 20.0));
        maxVelocity = Math.min(maxVelocity, predictVelocity(600.0 - y - 20.0));  // getBattleFieldHeight()
        return maxVelocity;
    }

    double getMovement();

    void setMovement(double newMovement);
}
