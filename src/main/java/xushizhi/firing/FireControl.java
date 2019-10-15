package xushizhi.firing;

import robocode.AdvancedRobot;
import robocode.ScannedRobotEvent;

public interface FireControl {
    void takeAim(final AdvancedRobot advancedRobot, final ScannedRobotEvent eScannedRobot);

    void fire(final AdvancedRobot advancedRobot, final ScannedRobotEvent eScannedRobot, final double relativeGunBearing);
}
