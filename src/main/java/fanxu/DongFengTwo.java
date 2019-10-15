package fanxu;

import java.awt.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;
import robocode.*;
import robocode.util.*;

public class DongFengTwo extends AdvancedRobot {
    //initial

    static Boolean hithithit = false;
    //敌人
    enemyState enemy = new enemyState();
    //pattern match
    private static final int MAX_PATTERN_LENGTH = 30;
    private static Map<String, int[]> matcher = new HashMap<String, int[]>(40000);
    //历史敌人
    private static String enemyHistory;
    //预测
    private static double FIRE_POWER = 3;
    private static double FIRE_SPEED = Rules.getBulletSpeed(FIRE_POWER);
    private static List<Point2D.Double> predictions = new ArrayList<Point2D.Double>();
    //movement
    static final double BASE_MOVEMENT = 180; //基础的移动量
    static final double BASE_TURN = Math.PI / 1.5; //基础的转弯角度 120度
    //具体的移动量
    static double movement;

    public void run() {
        //枪独立于机器人
        setAdjustGunForRobotTurn(true);
        //雷达独立于枪
        setAdjustRadarForGunTurn(true);
        setBodyColor(Color.BLACK);
        setGunColor(Color.BLACK);
        setBulletColor(Color.RED);
        setRadarColor(Color.BLACK);
        setScanColor(Color.PINK);
        enemyHistory = "";
        movement = Double.POSITIVE_INFINITY;
        //雷达右转400
        setTurnRadarRight(400);
        do {
            scan();
            if (getDistanceRemaining() == 0) {//返回剩余的移动距离
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

    public void onRobotDeath(RobotDeathEvent e) {
        //当一个机器人死的时候，重新启动雷达
        setTurnRadarRight(400);
    }

    public void onHitByBullet(HitByBulletEvent e) {
        //当被子弹打中，重新启动雷达
        setTurnRadarRight(400);
    }

    public void onHitRobot(HitRobotEvent e) {
        //当碰到其它机器人的时候
        if (hithithit == false) {
            double absoluteBearing = e.getBearingRadians() + getHeadingRadians();
            //将雷达转到敌人那个方向
            turnRadarRightRadians(Utils.normalRelativeAngle(absoluteBearing - getRadarHeadingRadians()));
            hithithit = true;
        }
    }

    public void onScannedRobot(ScannedRobotEvent e) {
        //更新敌人信息
        enemy.update(e, this);
        //当炮的角度没有改变的时候，开火！
        if (getGunTurnRemaining() == 0 && getEnergy() > 1) {
            smartFire();
        }
        //跟踪
        trackHim();
        // 记录
        if (enemy.thisStep == (char) -1) {
            return;
        }
        record(enemy.thisStep);
        enemyHistory = (char) enemy.thisStep + enemyHistory;
        // 目标
        predictions.clear();
        //我的位置
        Point2D.Double myP = new Point2D.Double(getX(), getY());
        //敌人的位置
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

    public void smartFire() {
        FIRE_POWER = Math.min(Math.min(getEnergy() / 6d, 1000d / enemy.distance), enemy.energy / 3d);
        FIRE_SPEED = Rules.getBulletSpeed(FIRE_POWER);
        setFire(FIRE_POWER);
    }

    public void trackHim() {
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

    private static Point2D.Double project(Point2D.Double p, double angle,
                                          double distance) {
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
    //addition
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
        //取正
        //-10<toDegrees(dh)<10 ; -8<v<8 ;
        //so we add with 10 and 8
        int dhCode = (int) Math.rint(Math.toDegrees(dh)) + 10;
        int vCode = (int) Math.rint(v + 8);
        return (char) (17 * dhCode + vCode);
    }

    public void decode(int symbol) {
        headingRadian += Math.toRadians(symbol / 17 - 10);
        velocity = symbol % 17 - 8;
    }
}
