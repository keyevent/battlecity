package Fanjuncai;


import robocode.*;

import java.awt.*;

public class FJC_Robot extends AdvancedRobot{


    @Override
    public void onDeath(DeathEvent event) {
        super.onDeath(event);
    }

    @Override
    public void run(){

        setBodyColor(new Color(255, 200, 200));
        setGunColor(new Color(255, 200, 200));
        setRadarColor(new Color(255, 200, 200));
        setBulletColor(new Color(255, 200, 200));
        setScanColor(new Color(255, 200, 200));


            while(true){

                doSan();
                goAhead(100);
                execute();
            }

}
    private void goAhead(double distance){
        double x = getX();
        double y = getY();
        double MAX_HEIGHT = this.getBattleFieldHeight();
        double MAX_WIDTH = this.getBattleFieldWidth();
        double distance_width = Math.sin(this.getHeadingRadians())*distance;
        double distance_height = Math.cos(this.getHeadingRadians())*distance;

        if(distance_height+y>20&&distance_height+y+20<MAX_HEIGHT
        &&distance_width+x>20&&distance_width+x+20<MAX_WIDTH){
            setMaxVelocity(Rules.MAX_VELOCITY);
            setAhead(distance);
            return;
        }
        else {
            setMaxVelocity(Rules.MAX_VELOCITY);
            setBack(100);
            setTurnRight(90);
            return;
        }

    }

    private void goBack(double distance){
        double x = getX();
        double y = getY();
        double MAX_HEIGHT = this.getBattleFieldHeight();
        double MAX_WIDTH = this.getBattleFieldWidth();
        double distance_width = Math.sin(this.getHeadingRadians())*distance;
        double distance_height = Math.cos(this.getHeadingRadians())*distance;

        if(y-distance_height>20&&y-distance_height+20<MAX_HEIGHT
                &&x-distance_width>20&&x-distance_width+20<MAX_WIDTH){
            setMaxVelocity(Rules.MAX_VELOCITY);
            setBack(distance);
            return;
        }
        else {
            setMaxVelocity(Rules.MAX_VELOCITY);
            setAhead(100);
            setTurnRight(90);
            return;
        }

    }


    private void doSan(){
        turnRadarRightRadians(4*Math.PI);
    }


    @Override
    public void onScannedRobot(ScannedRobotEvent event) {
            doFire(event);
    }

    @Override
    public void onHitByBullet(HitByBulletEvent e) {
        super.onHitByBullet(e);
    }

    @Override
    public void onHitRobot(HitRobotEvent event) {
        double bearing = event.getBearing();
        if (Math.abs(bearing) < 90) {
            out.println("mayday mayday, close combat, fire at will ");
            fire(Rules.MAX_BULLET_POWER);
        }
        super.onHitRobot(event);
    }

    private void doFire(ScannedRobotEvent event){
        double b = getHeadingRadians() + event.getBearingRadians() - getGunHeadingRadians();
        double bearing = event.getBearing();
        double distance = event.getDistance();
        double shiftVelocity = event.getVelocity()*Math.cos(
                getHeadingRadians()+event.getBearingRadians()+Math.PI/2-event.getHeadingRadians())
                -getVelocity()*Math.sin(event.getBearingRadians());
        if(distance<15){
            setTurnRight(bearing);
            fire(3);
            goBack(50);
            setTurnRight(90);
            return;
        }
        if(distance<30&&Math.abs(shiftVelocity)<1){
            double shiftBearing = Math.atan(shiftVelocity/11);
            turnGunRightRadians(b+shiftBearing);
            fire(3);
            setTurnRight(bearing);
            goBack(50);
            setTurnRight(90);
            return;
        }
        else{

            if(distance>200){
                setTurnRight(bearing);
                goAhead(150);
                return;
            }
            else if (distance>100){
                double shiftBearing = Math.atan(shiftVelocity/17);
                turnGunRightRadians(b+shiftBearing);
                fire(1);
                setTurnRight(bearing);
                goAhead(50);
                execute();
                return;
            }

            else if(distance>50&&Math.abs(bearing)<45){
                double shiftBearing = Math.atan(shiftVelocity/17);
                setTurnGunRightRadians(b+shiftBearing);
                setFire(1);
                setTurnRight(bearing);
                goBack(50);
                setTurnRight(90);
                return;
            }
            else if(distance>30&&Math.abs(bearing)<60){
                double shiftBearing = Math.atan(shiftVelocity/14);
                setTurnGunRightRadians(b+shiftBearing);
                setFire(2);
                setTurnRight(bearing);
                goBack(50);
                setTurnRight(90);
                return;
            }
            else {
                double shiftBearing = Math.atan(shiftVelocity/11);
                setTurnGunRightRadians(b+shiftBearing);
                setFire(3);
                setTurnRight(bearing);
                goBack(50);
                setTurnRight(90);
                return;
            }

        }
    }



}
