package liushiying;

import robocode.*;
import robocode.robotinterfaces.peer.IAdvancedRobotPeer;

import java.awt.geom.Point2D;
import java.util.Random;
import java.util.Vector;


public class ganganRobot extends AdvancedRobot {
    private boolean noTarget=true;
    private Enemy enemy=new Enemy();

    @Override
    public void run() {
        setAdjustGunForRobotTurn( true );
        setAdjustRadarForGunTurn( true );
        while(true){
            setTurnRadarRightRadians(Math.PI*2);
            scan();
            move();
            fire();
            execute();
        }
    }

    @Override
    public void setAhead(double distance){

        boolean flag=isGoingToHitWall(distance);
        out.println("enter setAhead flag:"+flag);
       if(flag){
            super.setBack(distance);
        }else{
            super.setAhead(distance);
        }
    }

    @Override
    public void setBack(double distance){
        boolean flag=isGoingToHitWall(distance);
        out.println("enter setBack flag:"+flag);
        if(flag){

            super.setAhead(distance);
        }else{
            super.setBack(distance);
        }
    }

   private boolean isGoingToHitWall(double moveStep){
        double heading=getHeadingRadians();
        double x=getX()+moveStep*Math.sin(heading);
        double y=getY()+moveStep*Math.cos(heading);
        double dWidth=getBattleFieldWidth();
        double dHeight=getBattleFieldHeight();
        if(x<30||x>dWidth-30||y<30||y>dHeight){
            out.println("is going to hit the wall");
           return true;
        }else{
            return false;
        }
    }

    public void onHitWall(){

    }


    //扫描到的时候雷达可能会偏离方向
    public void scan(){
        double offSet=normalizeBearing(enemy.direction-getRadarHeadingRadians());
        setTurnRadarRightRadians(offSet*1.2);
    }

    public void fire(){
        double bulletPower=calcFirePower(enemy.energy);
        double bulletVelocity=20-3*bulletPower;
        double bulletTravelTime=enemy.distance/bulletVelocity;
        double headingOffset=enemy.headingRadian-enemy.prevHeadingRadian+0.00001;
        double r=enemy.velocity/headingOffset;
        //预测目标在bulletTravelTime后出现的位置
        double predictX,predictY;
        double predictHeadingRadian=enemy.headingRadian+headingOffset*bulletTravelTime;
        predictX=enemy.xCoordinate-r*Math.cos(predictHeadingRadian)+
                r*Math.cos(enemy.headingRadian);
        predictY=enemy.yCoordinate+r*Math.sin(predictHeadingRadian)-
                r*Math.sin(enemy.headingRadian);
        double predictDirection;
        predictDirection=getRadians(getX(),getY(),predictX,predictY);
        double gunOffset;
        gunOffset=normalizeBearing(predictDirection-getGunHeadingRadians());
        setTurnGunRightRadians(gunOffset);
        if(getGunHeat()==0){
            setFire(bulletPower);
        }
    }

    public void move(){
        double offSet=normalizeBearing(enemy.direction-getRadarHeadingRadians());
        setTurnRadarRightRadians(offSet*1.2);
        out.println("enter do move");
        //上一次的distance以及跑完
        if(Math.abs(getDistanceRemaining())<1){
            out.println("enter random");
            double myX=getX();
            double myY=getY();
            double nextX=Math.random()*(getBattleFieldWidth()-100)+50;
            double nextY=Math.random()*(getBattleFieldHeight()-100)+50;
            out.println("nextX: "+nextX);
            out.println("nextY: "+nextY);
            double turnRadians=getRadians(myX,myY,nextX,nextY);
            turnRadians=normalizeBearing(turnRadians-getHeadingRadians());
            double moveDistance = Point2D.distance( myX, myY, nextX, nextY );
            double moveDirection=1;
            //旋转角度大于PI/2则后退，减少旋转的时间
            if(Math.abs(turnRadians)>Math.PI/2){
                turnRadians=normalizeBearing(turnRadians+Math.PI);
                moveDirection=-1;
            }
            out.println("turnRadias: "+turnRadians);
            setTurnRightRadians(turnRadians);
            setAhead(moveDirection*moveDistance);
        }
    }

    @Override
    public void onScannedRobot(ScannedRobotEvent e) {
        noTarget=false;
        enemy.updateStat(e,this);

    }


    @Override
    public void onHitByBullet(HitByBulletEvent e) {
        setTurnLeft(90 - e.getBearing());

    }

    public static double normalizeBearing( double radians) {
        if ( radians < -Math.PI )
            radians += Math.PI*2;
        if ( radians > Math.PI )
            radians -= Math.PI*2;
        return radians;
    }

    public static double normalizeRadians(double radians){
        if(radians<0){
            radians+=Math.PI;
        }
        if(radians>Math.PI*2){
            radians-=Math.PI*2;
        }
        return radians;
    }

    // 获取两个坐标点所成的弧度
    public static double getRadians(double x1, double y1, double x2, double y2){
        return Math.atan2( x2 - x1, y2 - y1 );
    }

    //用于计算火力大小
    public static double calcFirePower(double energy){
        return Math.min(3,energy/4+0.1D);
    }
}


class Enemy{
    public double headingRadian = 0.0D;
    public double bearingRadian = 0.0D;
    public double distance = 1000D;
    public double direction = 0.0D;
    public double xCoordinate = 0.0D;
    public double yCoordinate = 0.0D;
    public double velocity = 0.0D;
    public double prevVelocity=0.0D;
    public double prevHeadingRadian = 0.0D;
    public double energy = 100.0D;
    // 更新对方机器人状态
    public void updateStat(ScannedRobotEvent e, AdvancedRobot ar) {
        prevHeadingRadian = headingRadian;
        prevVelocity = velocity;
        headingRadian = e.getHeadingRadians();
        bearingRadian = e.getBearingRadians();
        distance = e.getDistance();
        //direction是以自己的机器人为圆心，目标机器人相对圆心的角度
        direction = bearingRadian + ar.getHeadingRadians();
        xCoordinate = ar.getX() + Math.sin( direction ) * distance;
        yCoordinate = ar.getY() + Math.cos( direction ) * distance;
        velocity = e.getVelocity();
        energy = e.getEnergy();
    }

}
