package fanjuncai;


import robocode.*;

import java.awt.*;

public class Ace extends AdvancedRobot{

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

        turnRight(90-this.getHeading());//direct to right

            while(true){

                ahead(100);
                if(this.getWidth()<100||this.getBattleFieldWidth()-this.getWidth()<100)
                    turnRight(90);
                if(this.getHeight()<100||this.getBattleFieldHeight()-this.getHeight()<100)
                    turnRight(90);
                turnRadarLeftRadians(2*Math.PI);
                turnRadarRightRadians(2*Math.PI);
            }

}

    @Override
    public void onScannedRobot(ScannedRobotEvent event) {
        double heading = event.getHeading();
        double bearing = event.getBearing();
//        setTurnRadarRight(event.getBearing());
        setTurnRight(bearing);
        setAhead(100);
        shotHim(event.getDistance(), bearing);
    }

    private void shotHim(double distance, double bearing) {
        if (distance < 10) {
            out.println("fire Rules.MAX_BULLET_POWER");
            fire(Rules.MAX_BULLET_POWER);
            return;
        }
        if (distance < 100 && Math.abs(bearing) < 30) {
            out.println("fire Rules.MAX_BULLET_POWER");
            fire(Rules.MAX_BULLET_POWER);
            return;
        }
        if (distance < 200 && Math.abs(bearing) < 10) {
            out.println("fire Rules.MIN_BULLET_POWER");
            this.fire(Rules.MIN_BULLET_POWER);
            return;
        }

    }


}
