package lihaojie;

import robocode.*;
import robocode.util.Utils;

import java.awt.*;

import static robocode.util.Utils.normalRelativeAngleDegrees;

public class falcon extends AdvancedRobot {
    int tankLength=150;
    double pi=Math.PI;
    double dist =70;
    Enemy enemy = new Enemy();

    @Override
    public void onDeath(DeathEvent event) {
        super.onDeath(event);
    }
    public  boolean avoidHitWall(double distace,double xcur,double ycur, double radians){
        System.out.println("checking ");
        double x=Math.cos(radians)*distace+xcur;
        double y=Math.sin(radians)*distace+ycur;
        System.out.println("X:"+x+"--Y:"+y);

        double wideGround=getBattleFieldWidth();
        double heightLength=getBattleFieldHeight();
        System.out.println("wide:"+wideGround+"--height:"+heightLength);
        boolean willHit=false;
        if (radians>pi/2&&radians<=pi//2
                &&(x>wideGround-tankLength||y<tankLength+0)){willHit=true;}
        if (radians<pi/2&&radians>=0//1
                &&(x>wideGround-tankLength||y>heightLength-tankLength)){willHit=true;}
        if (radians>=pi*3/2//4
                &&(x<tankLength+0||y>heightLength-tankLength)){willHit=true;}
        if (radians>=pi&&radians<pi*3/2//3
                &&(x<tankLength||y<tankLength)){willHit=true;}
        System.out.println(radians);
        if (willHit){
            System.out.println("willHit------------");
            return false;

        }
        System.out.println("unhit");
        return true;
    }
    @Override
    public void run() {
        setBodyColor(new Color(99, 66, 99));
        setGunColor(new Color(99, 66, 135));
        setRadarColor(new Color(210, 200, 255));
        setBulletColor(new Color(255, 255, 100));
        setScanColor(new Color(255, 200, 25));

        double headDistance =5000;
        System.out.println("start game!");
        setMaxVelocity(7);

        setAdjustGunForRobotTurn( true );
        setAdjustRadarForGunTurn( true );
        while (true) {
            if(enemy.name == null){
                setTurnRadarRightRadians(2* pi*100);
                System.out.println("get target");
                execute();

            }
            else{
                turnRadarLeft(600);
                System.out.println("get null target");
                execute();
            }

        }
    }

    @Override
    public void onHitByBullet(HitByBulletEvent event) {
        setTurnRight(normalRelativeAngleDegrees(90 - (getHeading() - event.getHeading())));
        ahead(dist);
        dist *= -1;
        //scan();
    }

    @Override
    public void onHitRobot(HitRobotEvent event) {

        double turnGunAmt = normalRelativeAngleDegrees(event.getBearing() + getHeading() - getGunHeading());
        setTurnGunRight(turnGunAmt);
        setFire(3);
        setTurnRight(event.getBearing());

    }

    @Override
    public void onHitWall(HitWallEvent event) {
        setBack(100);

        System.out.println("hit wall");

    }

    @Override
    public void onScannedRobot(ScannedRobotEvent event) {
        enemy.update(event,this);
        double Offset = rectify( enemy.direction - getRadarHeadingRadians() );
        setTurnRadarRightRadians( Offset * 2);

        double turnGunAmt = normalRelativeAngleDegrees(event.getBearing() + getHeading() - getGunHeading());
        setTurnGunRight(turnGunAmt);
        if (event.getDistance() < 50 && getEnergy() > 50) {

            setFire(2.5);
            //setFire(1);
        } // otherwise, fire 1.
        else if (event.getDistance() <150 ){
            setFire(2.0);
            //setFire(0.5);
        }
        else {
            setFire(1);
        }

        //setTurnRadarRight(2.0 * Utils.normalRelativeAngleDegrees(getHeading() + event.getBearing() - getRadarHeading()));

        // Call scan again, before we turn the gun
        //scan();

        double headDistance=100;
        setAhead(headDistance);
        //setTurnRadarRight(Double.POSITIVE_INFINITY);
        if (!avoidHitWall(headDistance,getX(),getY(),Math.toRadians( getHeading()) /*getHeadingRadians()*/)){
            setTurnRight(77);
            setBack(50);
            //waitFor(new TurnCompleteCondition(this));
        }
        //enemy=null;
      //  System.out.println("go ahead"+headDistance);
    }
    public  double rectify ( double angle )
    {
        if ( angle < -Math.PI )
            angle += 2*Math.PI;
        if ( angle > Math.PI )
            angle -= 2*Math.PI;
        return angle;
    }
    private void shotHim(double distance, double bearing) {
        if (distance < 10) {
            out.println("fire Rules.MAX_BULLET_POWER");
            setFire(Rules.MAX_BULLET_POWER);
            return;
        }
        if (distance < 100 && Math.abs(bearing) < 30) {
            out.println("fire Rules.MAX_BULLET_POWER");
            setFire(Rules.MAX_BULLET_POWER);
            return;
        }
        if (distance < 200 && Math.abs(bearing) < 10) {
            out.println("fire Rules.MIN_BULLET_POWER");
            this.setFire(Rules.MIN_BULLET_POWER);
            return;
        }

    }

}
class Enemy {
    public double x,y;
    public String name = null;
    public double headingRadian = 0.0D;
    public double bearingRadian = 0.0D;
    public double distance = 1000D;
    public double direction = 0.0D;
    public double velocity = 0.0D;
    public double prevHeadingRadian = 0.0D;
    public double energy = 100.0D;


    public void update(ScannedRobotEvent e,AdvancedRobot me){
        name = e.getName();
        headingRadian = e.getHeadingRadians();
        bearingRadian = e.getBearingRadians();
        this.energy = e.getEnergy();
        this.velocity = e.getVelocity();
        this.distance = e.getDistance();
        direction = bearingRadian + me.getHeadingRadians();
        x = me.getX() + Math.sin( direction ) * distance;
        y=  me.getY() + Math.cos( direction ) * distance;
    }
}