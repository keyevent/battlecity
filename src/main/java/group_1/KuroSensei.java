package group_1;

import robocode.*;
import robocode.util.Utils;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KuroSensei extends AdvancedRobot {
    private static Boolean confirmHit = false;
    private EnemyState enemy = new EnemyState();

    // Match Pattern
    private static final int MAX_PATTERN_LENGTH = 30;
    private static Map<String, int[]> matcher = new HashMap<String, int[]>(40000);

    // Enemy History
    private static String enemyHistory;

    // Prediction
    private static double FIRE_POWER = 3;
    private static double FIRE_SPEED = Rules.getBulletSpeed(FIRE_POWER);
    private static List<Point2D.Double> predictions = new ArrayList<Point2D.Double>();

    private static double movement;
    private static final double BASE_MOVEMENT = 180;  // Set default movement to 180
    private static final double BASE_TURN = Math.PI / 1.5;  // Set default turn radians to 120 degrees

    public void run() {
        setAdjustGunForRobotTurn(true);
        setAdjustRadarForGunTurn(true);

        setBodyColor(Color.BLACK);
        setGunColor(Color.BLACK);
        setBulletColor(Color.RED);
        setRadarColor(Color.BLACK);
        setScanColor(Color.PINK);

        enemyHistory = "";
        movement = Double.POSITIVE_INFINITY;

        setTurnRadarRight(400);
        do {
            scan();
            if (getDistanceRemaining() == 0) {
                setAhead(movement = -movement);
                setTurnRightRadians(BASE_TURN);
                confirmHit = false;
            }
        } while (true);
    }

    public void onScannedRobot(ScannedRobotEvent e) {
        enemy.update(this, e);
        // Fire if the gun doesn't change angle
        if (getGunTurnRemaining() == 0 && getEnergy() > 1) {
            smartFire();
        }
        tracking();

        if (enemy.currStep == (char) -1) {
            return;
        }
        record(enemy.currStep);
        enemyHistory = (char) enemy.currStep + enemyHistory;

        predictions.clear();
        // My Pos & Enemy Pos
        Point2D.Double myP = new Point2D.Double(getX(), getY());
        Point2D.Double enemyP = project(myP, enemy.absoluteBearing, e.getDistance());

        String pattern = enemyHistory;
        for (double d = 0; d < myP.distance(enemyP); d += FIRE_SPEED) {
            int nextStep = predict(pattern);
            enemy.decode(nextStep);
            enemyP = project(enemyP, enemy.headingRadian, enemy.velocity);
            predictions.add(enemyP);
            pattern = (char) nextStep + pattern;
        }

        enemy.absoluteBearing = Math.atan2(enemyP.x - myP.x, enemyP.y - myP.y);
        double gunTurn = enemy.absoluteBearing - getGunHeadingRadians();
        setTurnGunRightRadians(Utils.normalRelativeAngle(gunTurn));
    }

    public void onHitWall(HitWallEvent e) {
        if (Math.abs(movement) > BASE_MOVEMENT) {
            movement = BASE_MOVEMENT;
        }
    }

    // Restart radar if the robot is dead
    public void onRobotDeath(RobotDeathEvent e) {
        setTurnRadarRightRadians(Math.PI * 4);
    }

    // Restart radar if the robot is hit by a bullet
    public void onHitByBullet(HitByBulletEvent e) {
        setTurnRadarRightRadians(Math.PI * 4);
    }

    // Turn the radar towards the enemy if got hit
    public void onHitRobot(HitRobotEvent e) {
        if (!confirmHit) {
            final double absoluteBearing = e.getBearingRadians() + getHeadingRadians();
            final double absoluteRadarBearing = absoluteBearing - getRadarHeadingRadians();
            turnRadarRightRadians(Utils.normalRelativeAngle(absoluteRadarBearing));
            confirmHit = true;
        }
    }

    private void smartFire() {
        FIRE_POWER = Math.min(Math.min(getEnergy() / 6d, 1000d / enemy.distance), enemy.energy / 3d);
        FIRE_SPEED = Rules.getBulletSpeed(FIRE_POWER);
        setFire(FIRE_POWER);
    }

    private void tracking() {
        double RadarOffset;
        RadarOffset = Utils.normalRelativeAngle(enemy.absoluteBearing - getRadarHeadingRadians());
        setTurnRadarRightRadians(RadarOffset * 1.2);
    }

    private void record(int currStep) {
        int maxLength = Math.min(MAX_PATTERN_LENGTH, enemyHistory.length());
        for (int i = 0; i <= maxLength; ++i) {
            String pattern = enemyHistory.substring(0, i);
            int[] frequencies = matcher.computeIfAbsent(pattern, k -> new int[21 * 17]);

            // Frequency table needs to hold 21 possible dh values * 17 possible v values
            ++frequencies[currStep];
        }
    }

    private int predict(String pattern) {
        int[] frequencies = null;
        for (int patternLength = Math.min(pattern.length(), MAX_PATTERN_LENGTH); frequencies == null; --patternLength) {
            frequencies = matcher.get(pattern.substring(0, patternLength));
        }

        int nextTick = 0;
        for (int i = 1; i < frequencies.length; ++i) {
            if (frequencies[nextTick] < frequencies[i]) {
                nextTick = i;
            }
        }
        return nextTick;
    }

    private static Point2D.Double project(Point2D.Double p, double angle, double distance) {
        double x = p.x + distance * Math.sin(angle);
        double y = p.y + distance * Math.cos(angle);
        return new Point2D.Double(x, y);
    }
}
