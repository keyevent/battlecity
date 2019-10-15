package sensei;

import robocode.*;
import robocode.util.Utils;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Sensei extends AdvancedRobot {
    private static Boolean hithithit = false;
    private enemyState enemy = new enemyState();

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
    private static final double BASE_MOVEMENT = 180;  // Set base movement to 180
    private static final double BASE_TURN = Math.PI / 1.5;  // Set base turn degree to 120 degrees

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
                hithithit = false;
            }
        } while (true);
    }

    ////////////////////////////**EVENT**/////////////////////////////////////

    public void onHitWall(HitWallEvent e) {
        if (Math.abs(movement) > BASE_MOVEMENT) {
            movement = BASE_MOVEMENT;
        }
    }

    // Restart radar if the robot is dead
    public void onRobotDeath(RobotDeathEvent e) {
        setTurnRadarRight(400);
    }

    // Restart radar if the robot is hit by a bullet
    public void onHitByBullet(HitByBulletEvent e) {
        setTurnRadarRight(400);
    }

    // Turn the radar towards the enemy if got hit
    public void onHitRobot(HitRobotEvent e) {
        if (!hithithit) {
            double absoluteBearing = e.getBearingRadians() + getHeadingRadians();
            turnRadarRightRadians(Utils.normalRelativeAngle(absoluteBearing - getRadarHeadingRadians()));
            hithithit = true;
        }
    }

    public void onScannedRobot(ScannedRobotEvent e) {
        enemy.update(e, this);
        // Fire if the gun doesn't change angle
        if (getGunTurnRemaining() == 0 && getEnergy() > 1) {
            smartFire();
        }
        trackHim();

        if (enemy.thisStep == (char) -1) {
            return;
        }
        record(enemy.thisStep);
        enemyHistory = (char) enemy.thisStep + enemyHistory;

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

    ////////////////////////////////**MYFUNCTION**/////////////////////////////

    private void smartFire() {
        FIRE_POWER = Math.min(Math.min(getEnergy() / 6d, 1000d / enemy.distance), enemy.energy / 3d);
        FIRE_SPEED = Rules.getBulletSpeed(FIRE_POWER);
        setFire(FIRE_POWER);
    }

    private void trackHim() {
        double RadarOffset;
        RadarOffset = Utils.normalRelativeAngle(enemy.absoluteBearing - getRadarHeadingRadians());
        setTurnRadarRightRadians(RadarOffset * 1.2);
    }

    private void record(int thisStep) {
        int maxLength = Math.min(MAX_PATTERN_LENGTH, enemyHistory.length());
        for (int i = 0; i <= maxLength; ++i) {
            String pattern = enemyHistory.substring(0, i);
            int[] frequencies = matcher.get(pattern);

            if (frequencies == null) {
                // frequency tables need to hold 21 possible dh values times 17 possible v values
                frequencies = new int[21 * 17];
                matcher.put(pattern, frequencies);
            }
            ++frequencies[thisStep];
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

//////////////////////**ENEMY_CLASS**///////////////////////////////////////

class enemyState {
    public double headingRadian = 0.0D;
    public double bearingRadian = 0.0D;
    public double distance = 0.0D;
    public double absoluteBearing = 0.0D;
    public double x = 0.0D;
    public double y = 0.0D;
    public double velocity = 0.0D;
    public double energy = 100.0D;

    public double lastEnemyHeading = 0;
    public int thisStep = 0;

    //the currently data is important, we should get it when we use it.
    public void update(ScannedRobotEvent e, AdvancedRobot me) {
        headingRadian = e.getHeadingRadians();
        bearingRadian = e.getBearingRadians();
        distance = e.getDistance();
        absoluteBearing = bearingRadian + me.getHeadingRadians();
        x = me.getX() + Math.sin(absoluteBearing) * distance;
        y = me.getY() + Math.cos(absoluteBearing) * distance;
        velocity = e.getVelocity();
        energy = e.getEnergy();
        //addition
        thisStep = encode(headingRadian - lastEnemyHeading, velocity);
        lastEnemyHeading = headingRadian;
    }

    public static int encode(double dh, double v) {
        if (Math.abs(dh) > Rules.MAX_TURN_RATE_RADIANS) {
            return (char) -1;
        }
        // -10 < toDegrees(dh) < 10 ; -8 < v < 8 ;
        // Add with 10 and 8
        int dhCode = (int) Math.rint(Math.toDegrees(dh)) + 10;
        int vCode = (int) Math.rint(v + 8);
        return (char) (17 * dhCode + vCode);
    }

    public void decode(int symbol) {
        headingRadian += Math.toRadians(symbol / 17 - 10);
        velocity = symbol % 17 - 8;
    }
}
