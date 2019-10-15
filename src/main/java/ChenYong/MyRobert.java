package ChenYong;

import robocode.*;

import java.awt.*;

import static robocode.util.Utils.normalRelativeAngleDegrees;


/**
 * MyFirstRobot - a sample robot by Mathew Nelson.
 * <p>
 * Moves in a seesaw motion, and spins the gun around at each end.
 *
 * @author Mathew A. Nelson (original)
 */
public class MyRobert  extends AdvancedRobot
{
    Enemy enemy = new Enemy();
    public static double PI = Math.PI;

    public void run()
    {
        setAdjustGunForRobotTurn( true );
        setAdjustRadarForGunTurn( true );
        this.setColors(Color.red, Color.blue, Color.yellow, Color.black, Color.green);

        while(true){
            if(enemy.name == null){
                setTurnRadarRightRadians(2*PI);
                execute();
            }
            else{
                execute();
            }
        }
    }



    public void onScannedRobot(ScannedRobotEvent e)
    {
        enemy.update(e,this);
        double Offset = rectify( enemy.direction - getRadarHeadingRadians() );
        setTurnRadarRightRadians( Offset * 1.5);
        double bearing=getRadarHeading();
        double heading=getRadarHeading();
        double turnGunAmt = normalRelativeAngleDegrees(e.getBearing() + getHeading() - getGunHeading());
        setTurnGunRight(turnGunAmt);
        out.println(heading);
        setTurnLeft(40);
        setTurnLeft(13);
        setFire(2);
        myMoveAhead(100);

    }
    //角度修正方法，重要
    public  double rectify ( double angle )
    {
        if ( angle < -Math.PI )
            angle += 2*Math.PI;
        if ( angle > Math.PI )
            angle -= 2*Math.PI;
        return angle;
    }


    public double getAngle(Enemy enemy)
    {
        //double enemyVec=
        return 0;
    }

    public void myMoveAhead(int move) {
        double heading = getHeadingRadians();
        double x = getX() + move * Math.sin(heading);
        double y = getY() + move * Math.cos(heading);
        double buttleFieldWidth = getBattleFieldWidth();
        double buttleFieldHeight = getBattleFieldHeight();
        double width = getWidth() * 5 / 4;
        if ((x + width > buttleFieldWidth) || (y + width > buttleFieldHeight) || (x < width) || (y < width)) {
            move = move-10;
            myMoveAhead(move);
        } else {
            setAhead(move);
            //moveDistance*=2;
        }
    }

    public class Enemy {
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


}



