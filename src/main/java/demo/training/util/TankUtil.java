package demo.training.util;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class TankUtil {
    private Rectangle2D.Double battleField;
    private final double SAFE_DISTANCE_FROM_WALL = 40D;

    public TankUtil(double width, double height) {
        this.battleField = new Rectangle2D.Double(SAFE_DISTANCE_FROM_WALL, SAFE_DISTANCE_FROM_WALL, width - SAFE_DISTANCE_FROM_WALL*2, height - SAFE_DISTANCE_FROM_WALL*2);

    }

    public double wallSmooth(Point2D.Double pos, double angle, double distance, int orientation) {
        while (!battleField.contains(nextPosition(pos, angle, distance))) {
            angle += 0.1 * orientation;
        }
        System.out.println("angle " + angle);
        return angle;
    }

    private Point2D.Double nextPosition(Point2D.Double pos, double angle, double distance) {
        return new Point2D.Double(pos.x + distance * Math.sin(angle), pos.y + distance * Math.cos(angle));

    }
}
