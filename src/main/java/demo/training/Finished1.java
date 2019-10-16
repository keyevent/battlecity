package demo.training;
import robocode.*;
import robocode.util.Utils;

import java.awt.Color;
import java.awt.geom.Point2D;

public class Finished1 extends AdvancedRobot {
        enemy target;
        double firePower;
        double direction=1;

        public void run()
        {
            target = new enemy();
            target.distance = 100000;
            setColors(Color.red,Color.blue,Color.green);
            setAdjustGunForRobotTurn(true);
            setAdjustRadarForGunTurn(true);
            turnRadarRightRadians(2*Math.PI);

            while(true)
            {
                doMovement();
                doFirePower();
                doScanner();
                doGun();
                fire(firePower);
                execute();
            }
        }

        void doFirePower()
        {
            firePower = 400/target.distance;
        }

        void doMovement()
        {
            if (getTime()%15 == 0)
            {
                out.println("turn--"+getTime());

                direction *= -1;
                //撞墙检测
                double newX = 0;
                double newY = 0;
                double dWidth = getBattleFieldWidth();  //战场的宽度
                double dHeight = getBattleFieldHeight();  //战场的长度
                double nowSpeed = getVelocity();
                double s = Math.pow(nowSpeed, 2) / (2 * Rules.ACCELERATION);
                double heading = getHeadingRadians();
                //移动move后将要达到的x坐标
                double x = getX() + s * Math.sin(heading);
                //移动move后将要达到的y坐标
                double y = getY() + s * Math.cos(heading);
                if (x < 40 || x > dWidth - 40 || y < 40 || y > dHeight - 40) {
                    setBack(300);
                }else{
                    setAhead(direction*300);
                }
            }

            setTurnRightRadians(target.bearing + (Math.PI/2));
        }
        void doScanner()
        {
            double radarOffset;
                radarOffset = getRadarHeadingRadians() - absbearing(getX(),getY(),target.x,target.y);
                if (radarOffset < 0)
                    radarOffset -= Math.PI/8;
                else
                    radarOffset += Math.PI/8;
            setTurnRadarLeftRadians(AngelSet(radarOffset)); //左转调整转动角度到PI内
        }
        void doGun()
        {
            long time = getTime() + (int)(target.distance/(20-(3*firePower)));
            double gunOffset = getGunHeadingRadians() - absbearing(getX(),getY(),target.guessX(time),target.guessY(time));
            setTurnGunLeftRadians(AngelSet(gunOffset));
        }
        double AngelSet(double ang)
        {
            if (ang > Math.PI)
                ang -= 2*Math.PI;
            if (ang < -Math.PI)
                ang += 2*Math.PI;
            return ang;
        }

        public double getrange( double x1,double y1, double x2,double y2 )
        {
            double xo = x2-x1;
            double yo = y2-y1;
            double h = Math.sqrt( xo*xo + yo*yo );
            return h;
        }
        public double absbearing( double x1,double y1, double x2,double y2 )
        {
            double xo = x2-x1;
            double yo = y2-y1;
            double h = getrange( x1,y1, x2,y2 );
            if( xo > 0 && yo > 0 )
            {
                return Math.asin( xo / h );
            }
            if( xo > 0 && yo < 0 )
            {
                return Math.PI - Math.asin( xo / h );
            }
            if( xo < 0 && yo < 0 )
            {
                return Math.PI + Math.asin( -xo / h );
            }
            if( xo < 0 && yo > 0 )
            {
                return 2.0*Math.PI - Math.asin( -xo / h );
            }
            return 0;
        }
        public void onScannedRobot(ScannedRobotEvent e)
        {
            if ((e.getDistance() < target.distance)||(target.name == e.getName()))
            {
                double absbearing_rad = (getHeadingRadians()+e.getBearingRadians())%(2*Math.PI);
                target.name = e.getName();
                target.x = getX()+Math.sin(absbearing_rad)*e.getDistance();
                target.y = getY()+Math.cos(absbearing_rad)*e.getDistance();
                target.bearing = e.getBearingRadians();
                target.head = e.getHeadingRadians();
                target.ctime = getTime();
                target.speed = e.getVelocity();
                target.distance = e.getDistance();
            }
        }
        public void onRobotDeath(RobotDeathEvent e)
        {
            if (e.getName() == target.name)
                target.distance = 10000;
        }

    }


    class enemy
    {
        String name;
        public double bearing;
        public double head;
        public long ctime;
        public double speed;
        public double x,y;
        public double distance;
        public double guessX(long when)
        {
            long diff = when - ctime;
            return x+Math.sin(head)*speed*diff;
        }
        public double guessY(long when)
        {
            long diff = when - ctime;
            return y+Math.cos(head)*speed*diff;
        }
}
