package sensei;

import robocode.AdvancedRobot;
import robocode.Rules;
import robocode.ScannedRobotEvent;

public class EnemyState {

    public double x = 0.0D;
    public double y = 0.0D;
    public double distance = 0.0D;
    double velocity = 0.0D;
    double energy = 100.0D;

    double headingRadian = 0.0D;
    double absoluteBearing = 0.0D;
    private double lastEnemyHeading = 0;

    int currStep = 0;

    private static int encode(double dh, double v) {
        if (Math.abs(dh) > Rules.MAX_TURN_RATE_RADIANS) {
            return (char) -1;
        }
        // -10 < toDegrees(dh) < 10
        // -8 < v < 8
        int dhCode = (int) Math.rint(Math.toDegrees(dh)) + 10;
        int vCode = (int) Math.rint(v + 8);
        return (char) (17 * dhCode + vCode);
    }

    void decode(int symbol) {
        headingRadian += Math.toRadians((symbol / 17) - 10);
        velocity = symbol % 17 - 8;
    }

    void update(AdvancedRobot advRobot, ScannedRobotEvent e) {
        headingRadian = e.getHeadingRadians();
        double bearingRadian = e.getBearingRadians();
        absoluteBearing = bearingRadian + advRobot.getHeadingRadians();

        distance = e.getDistance();
        x = advRobot.getX() + Math.sin(absoluteBearing) * distance;
        y = advRobot.getY() + Math.cos(absoluteBearing) * distance;

        velocity = e.getVelocity();
        energy = e.getEnergy();

        currStep = encode(headingRadian - lastEnemyHeading, velocity);
        lastEnemyHeading = headingRadian;
    }
}