package demo.training;
import java.awt.geom.Point2D;
import robocode.AdvancedRobot;
import robocode.ScannedRobotEvent;
import robocode.util.Utils;
import java.util.ArrayList;

public class Battle {
    private static double DISTANCE_WEIGHT = 200;
    private AdvancedRobot robot;
    private Point2D.Double enemyPosition;
    private double distance;
    private double verticalSeg;
    private double horizontalSeg ;
    private double bearing;
    private ArrayList<Wave> waves = new ArrayList<Wave>();

    public Battle(AdvancedRobot robot) {
        this.robot = robot;
    }

    public void update(ScannedRobotEvent event) {
        bearing = event.getBearingRadians();
        distance = event.getDistance();
        enemyPosition = computeEnemyPosition(distance,bearing += robot.getHeadingRadians());
        horizontalSeg = event.getVelocity() * Math.cos(event.getHeadingRadians() - bearing);
        verticalSeg = event.getVelocity() * Math.sin(event.getHeadingRadians() - bearing);
    }
    public void createWave() {
        Wave wave = new Wave(this);
        //雷达锁定敌人
        if(robot.getGunHeat() <= 0){
            wave.setPosition(new Point2D.Double(robot.getX(),robot.getY()));
            wave.setBearing(bearing);
            wave.setScanTime(robot.getTime());
            wave.setVerticalSeg(horizontalSeg);
            wave.setHorizontalSeg(verticalSeg);
            wave.setDistance(distance);
            robot.addCustomEvent(wave);
        }
    }

    public double getBestMatchFireAngle(){
        double aim = 0;
        double eDistance;
        System.out.println("the wave size="+waves.size());
        double maxMatch = java.lang.Double.POSITIVE_INFINITY;
        for(Wave wave:waves){

            eDistance = Math.pow(horizontalSeg - wave.getHorizontalSeg(),2)
                    +Math.pow((verticalSeg - wave.getVerticalSeg()),2)
                    +Math.pow((wave.getDistance() - this.distance)/DISTANCE_WEIGHT,2);
            if(eDistance < maxMatch){
                maxMatch = eDistance;
                aim = wave.getAngle();
            }
        }

        return Utils.normalRelativeAngle(bearing - robot.getGunHeadingRadians() + aim);
    }

    public Point2D.Double computeEnemyPosition(double dist, double d) {
        return new Point2D.Double(robot.getX()
                    + dist * Math.sin(d),robot.getY()
                    + dist *Math.cos(d));
    }
    public AdvancedRobot getRobot() {
        return robot;
    }
    public ArrayList<Wave> getWaves() {
        return waves;
    }
    public Point2D.Double getEnemyPosition() {
        return enemyPosition;
    }

}
