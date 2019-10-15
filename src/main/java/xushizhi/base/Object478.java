package xushizhi.base;

import xushizhi.firing.DirectFire;
import xushizhi.movement.Orbiting;

public class Object478 extends RobotBase {

    public Object478() {
        super(new DirectFire(0.3, 3.0, 100.0, 500.0), new Orbiting());
    }
}
