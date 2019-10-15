package demo.training.why;

import robocode.*;

import java.util.Random;

public class WhyRobot extends AdvancedRobot {
    private double fieldWidth;
    private double fieldHeight;
    private double robotWidth;
    private double robotHeight;
    private RobotStatus status;
    private Enemy enemy=new Enemy();
    private Random random=new Random();
    @Override
    public void run() {
        fieldWidth=getBattleFieldWidth();
        fieldHeight=getBattleFieldHeight();
        robotWidth=getWidth();
        robotHeight=getHeight();
        setAdjustGunForRobotTurn(true);
        setAdjustRadarForGunTurn( true );
        setMaxVelocity(8);
        while (true){
            if(enemy.name == null){
                setTurnRadarRightRadians(2*Math.PI);
                execute();
            }
            else{
                //setTurnRadarRightRadians(2*Math.PI);
                System.out.printf("before execute x is %f,y is %f\n",getX(),getY());
                execute();
            }
        }
    }

    private void randomMove(){
        double turn;
        double angle;
        double length=random.nextInt((int) Math.min(fieldHeight,fieldWidth));
        double x=getX();
        double y=getY();
        System.out.println("the origin x is"+x+",the origin y is "+y);
        do {
            turn = random.nextDouble() * 360;
            angle=getHeading()+turn;
        }
        while (!isSafeMove(x,y,angle,length));
        //this.setTurnRight(turn);
        //this.setAhead(length);
        setTurnRight(turn);
        setAhead(length);
    }

    private boolean isSafeMove(double x,double y,double direction,double length){
        double max=Math.sqrt(robotWidth*robotWidth+robotHeight*robotHeight)/2+30;

        x+=length*Math.sin(Math.toRadians(direction));
        y+=length*Math.cos(Math.toRadians(direction));

        if (x>max&&x<fieldWidth-max&&y>max&&y<fieldHeight-max){
            return true;
        }
        return false;
    }


    @Override
    public void onScannedRobot(ScannedRobotEvent event) {

        //--------------------跟踪代码2
        enemy.update(event,this);
        double Offset = rectify( enemy.direction - getRadarHeadingRadians());
        setTurnRadarRightRadians( Offset * 1.5);

        double length=200;
        double enemyPosition = event.getBearing()+getHeading();//enemy position
        fireInAdvance(enemy);
        dogging(enemyPosition+180,length);

    }


    private void fireInAdvance(Enemy enemy){
        double direction=getHeadingRadians()+enemy.bearingRadian;
        double enemyHeading=enemy.headingRadian;
        double power=getPowerByDistance(enemy.distance);
        double speed=Rules.getBulletSpeed(power);
        double A=adjust(enemyHeading-direction);
        double sinB=enemy.velocity/speed*Math.sin(A);
        double B=Math.asin(sinB);
        fireAt(direction+B,power);
    }

    //fire at that direction
    private void fireAt(double direction,double power){
        double turn=adjust(direction-getGunHeadingRadians());
        if (turn<Math.PI)setTurnGunRightRadians(turn);
        else setTurnGunLeftRadians(2*Math.PI-turn);
        setFire(power);
    }

    //adjust radian to [0,2PI]
    private double adjust(double radian){
        radian%=2*Math.PI;
        if (radian<0)radian+=2*Math.PI;
        return radian;
    }


    private double getPowerByDistance(double distance){
        if (distance<100)return 3;
        else if (distance<200)return 2;
        else if (distance<300) return 1;
        else return 0.5;
    }
    /**
     *
     * @param doggingDirection assume this is the absolute direction bullet comes
     * @param length the length to move
     */
    private void dogging(double doggingDirection,double length){
        System.out.printf("in dogging x is %f,y is %f\n",getX(),getY());
        double turnHeading=doggingDirection-90;
        if (isSafeMove(getX(),getY(),turnHeading,length)){
            turnHeading=turnHeading;
        }else if (isSafeMove(getX(),getY(),turnHeading+180,length)){
            turnHeading=turnHeading+180;
        }else if (isSafeMove(getX(),getY(),turnHeading+45,length)){
            turnHeading=turnHeading+45;
        }else if (isSafeMove(getX(),getY(),turnHeading+135,length)){
            turnHeading=turnHeading+135;
        }else if (isSafeMove(getX(),getY(),turnHeading-45,length)){
            turnHeading=turnHeading-45;
        }else if (isSafeMove(getX(),getY(),turnHeading-135,length)){
            turnHeading=turnHeading-135;
        }else  throw new RuntimeException();
        turnHeading=turnHeading-getHeading();
        turnHeading%=360;
        if (turnHeading<0)turnHeading+=360;

        if (turnHeading<180)setTurnRight(turnHeading);
        else setTurnLeft(360-turnHeading);

        setAhead(length);
    }

    public  double rectify ( double angle )
    {
        if ( angle < -Math.PI )
            angle += 2*Math.PI;
        if ( angle > Math.PI )
            angle -= 2*Math.PI;
        return angle;
    }

    @Override
    public void onHitWall(HitWallEvent event) {
        System.out.println("撞墙了-----------");
        System.out.println("x="+getX()+", y="+getY());
    }

    @Override
    public void onStatus(StatusEvent e) {
        status=e.getStatus();
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
