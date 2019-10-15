package demo.training;
import java.awt.geom.Point2D;

import robocode.Condition;
import robocode.Rules;
import robocode.util.Utils;


public class Wave extends Condition{

        private Point2D.Double position;
        private double bearing;
        private double scanTime;
        private double distance;
        private double verticalSeg;
        private double horizontalSeg;
        private double angle;
        private Battle battle;
        private IcyGrain robot;

        public Wave(Battle battle){
            this.battle = battle;
            this.robot = (IcyGrain) battle.getRobot();
        }
        public Point2D.Double getPosition() {
            return this.position;
        }
        public void setPosition(Point2D.Double position) {
            this.position = position;
        }
        public double getBearing() {
            return this.bearing;
        }
        public void setBearing(double bearing) {
            this.bearing = bearing;
        }
        public double getScanTime() {
            return this.scanTime;
        }
        public void setScanTime(double scanTime) {
            this.scanTime = scanTime;
        }
        public double getDistance() {
            return this.distance;
        }
        public void setDistance(double distance) {
            this.distance = distance;
        }
        public double getVerticalSeg() {
            return this.verticalSeg;
        }
        public void setVerticalSeg(double verticalSeg) {
            this.verticalSeg = verticalSeg;
        }
        public double getHorizontalSeg() {
            return this.horizontalSeg;
        }
        public void setHorizontalSeg(double horizontalSeg) {
            this.horizontalSeg = horizontalSeg;
        }
        public double getAngle() {
            return angle;
        }



        public boolean test() {
            System.out.println(battle.getEnemyPosition().getX() + ":" + battle.getEnemyPosition().getY());
            double bulletSpeed = Rules.getBulletSpeed(robot.BULLET_POWER);
            double distance = Point2D.distance(battle.getEnemyPosition().getX(),battle.getEnemyPosition().getY(),
                    position.getX(),position.getY());
            if((robot.getTime() - scanTime) * bulletSpeed >= distance){
                this.angle = Utils.normalRelativeAngle(Utils.
                    normalAbsoluteAngle(Math.atan2(battle.getEnemyPosition().getX()
                               - position.getX(),battle.getEnemyPosition().getY()
                              - position.getY())) - bearing );

                battle.getWaves().add(this);
                robot.removeCustomEvent(this);
            }
            return false;
        }
}
