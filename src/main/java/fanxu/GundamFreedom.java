package fanxu;

import java.awt.geom.Point2D;

import robocode.AdvancedRobot;
import robocode.Rules;
import robocode.ScannedRobotEvent;
import robocode.util.Utils;

public class GundamFreedom extends AdvancedRobot {
    private double nowTurnTime = 0;
    private Enemy enemy = new Enemy();
    private boolean discover = false;
    private double heading = 0.0;
    private double radarHeading = 0.0;
    private double bulletPower = 3;
    private double bulletArriveTime = 0;
    private double distance = 3000;
    private double safeDistance = 100;
    private double preHeading = 0.0;
    private double preTime = 0.0;


    @Override
    public void onScannedRobot(ScannedRobotEvent e) {
        discover = true;
        preTime = nowTurnTime;
        nowTurnTime = getTime();
        preHeading = enemy.getHeading();
        enemy.setHeading(e.getHeadingRadians());
        enemy.setBearing(e.getBearingRadians());
        enemy.setSpeed(e.getVelocity());
        enemy.setDistance(e.getDistance());
        //根据距离，决定bulletPower
        if (distance < 50) {
            bulletPower = Rules.MAX_BULLET_POWER;
        }else if(distance<100){
            bulletPower = Rules.MAX_BULLET_POWER/2;
        }else if(distance<200){
            bulletPower = Rules.MAX_BULLET_POWER/3;
        }else {
            bulletPower = Rules.MIN_BULLET_POWER;
        }
        bulletArriveTime = distance / Rules.getBulletSpeed(bulletPower);
    }


    private void setAdjust() {
        setAdjustGunForRobotTurn(true);
        setAdjustRadarForGunTurn(true);
    }

    private void loseTarget() {
        if ((getTime() - nowTurnTime) >= 4)
            discover = false;
    }
    //移动
    private void move() {
        double newX = 0;
        double newY = 0;
        double dWidth = getBattleFieldWidth();  //战场的宽度
        double dHeight = getBattleFieldHeight();  //战场的长度
        newX = Math.random() * (dWidth- 2 * safeDistance) + safeDistance;
        newY = Math.random() * (dHeight - 2 * safeDistance) + safeDistance;
        double head = 90 - Math.atan2(newY - getY(), newX - getX());
        head = Utils.normalAbsoluteAngle(head);
        double dis = Point2D.distance(getX(), getY(), newX, newY);
        //如果距离走完了，可以随机到下一个地方。
        if (getDistanceRemaining() < 10) {
            if (head - getHeadingRadians() > Math.PI / 2) {
                setTurnRightRadians(head - getHeadingRadians() + Math.PI);
                setAhead(-dis);
            } else {
                setTurnRightRadians(head - getHeadingRadians());
                setAhead(dis);
            }
        }
        double nowSpeed = getVelocity();
        double s = Math.pow(nowSpeed,2)/(2*Rules.ACCELERATION);
        double heading = getHeadingRadians();
        //移动move后将要达到的x坐标
        double x = getX() + s * Math.sin(heading);
        //移动move后将要达到的y坐标
        double y = getY() + s * Math.cos(heading);
        if (x < 40 || x > dWidth - 40 || y < 40 || y > dHeight - 40) {
            setBack(dis);
        }
    }


    private void track() {
        //如果发现敌人的行踪，跟踪他
        if (discover) {
            //得到tank方向
            heading = this.getHeadingRadians();
            //得到雷达方向
            radarHeading = this.getRadarHeadingRadians();
            //计算雷达应该转动的方向
            double temp = radarHeading - heading - enemy.getBearing();
            temp = Utils.normalRelativeAngle(temp);
            temp *= 1.5;
            //转动雷达方向跟踪，坦克本体一直在移动
            setTurnRadarLeftRadians(temp);
        }
    }

    private double getFirePower() {
        return bulletPower;
    }

    //瞄准！！
    private void aim() {
        double gunTurnRadians = line();
        setTurnGunRightRadians(gunTurnRadians);
    }

    //假设敌方的移动轨迹为线性
    private double line() {
        double ea = Utils.normalAbsoluteAngle(getHeadingRadians() + enemy.getBearing());
        double ex = getX() + enemy.getDistance() * Math.sin(ea);
        double ey = getY() + enemy.getDistance() * Math.cos(ea);
        double s = 0;
        if (enemy.getSpeed() >= Rules.MAX_VELOCITY - 0.1) {
            s = enemy.getSpeed() * bulletArriveTime;
        } else if (enemy.getSpeed() > 0.0) {
            double as = (Math.pow(Rules.MAX_VELOCITY, 2) - Math.pow(
                    enemy.getSpeed(), 2))
                    / 2 * Rules.ACCELERATION;
            double vs = (bulletArriveTime - (Rules.MAX_VELOCITY - enemy.getSpeed())
                    / Rules.ACCELERATION)
                    * Rules.MAX_VELOCITY;
            s = as + vs;
        } else {
            s = 0.0;
        }
        double nextx = ex + s * Math.sin(enemy.getHeading());
        double nexty = ey + s * Math.cos(enemy.getHeading());
        distance = Point2D.distance(getX(), getY(), nextx, nexty);
        double t = Math.atan2(nexty - getY(), nextx - getX());
        return Utils.normalRelativeAngle((Math.PI / 2 - t - getGunHeadingRadians()) % (2 * Math.PI));
    }

    public void run() {
        setAdjust();
        while (true) {
            if (!discover) {
                setTurnRadarLeftRadians(Math.PI * 2.1);
                execute();
            } else {
                move();//移动
                track();//追踪
                aim();//瞄准
                setFire(getFirePower());//发射
                out.println("turn:"+getTime()+"   火力:"+getFirePower());
                execute();
                loseTarget();
                execute();
            }
        }
    }
}
