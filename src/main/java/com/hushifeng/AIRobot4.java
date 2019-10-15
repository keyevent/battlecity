package com.hushifeng;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Point2D.Double;

import robocode.AdvancedRobot;
import robocode.HitRobotEvent;
import robocode.HitWallEvent;
import robocode.RobotDeathEvent;
import robocode.ScannedRobotEvent;
import robocode.TurnCompleteCondition;


public class AIRobot4 extends AdvancedRobot {
	private static final int safeDistance = 30;
	private static final double stick = 150;
	private Rectangle2D.Double rect = null;
	
	Enemy enemy;// enemy
	final double PI = Math.PI;// PI
	double firePower;// fire bullet power
	int moveDirection = 1;// 1:forward -1:back
	int moveTime = 20;
	int moveAmount = 300;

	/**
	 * MyFirstRobot's run method
	 */
	public void run() {

		setBodyColor(Color.RED);
		setGunColor(Color.BLUE);
		setRadarColor(Color.YELLOW);

		enemy = new Enemy();
		enemy.distance = 99999;
		// indepent gun and radar
		setAdjustGunForRobotTurn(true);
		setAdjustRadarForGunTurn(true);
		turnRadarRightRadians(2 * PI);

		rect = new Rectangle2D.Double(safeDistance, safeDistance, getBattleFieldWidth()-2*safeDistance,getBattleFieldHeight()-2*safeDistance );
		Double pos = new Double();
		int orientation = -1;

		
		while (true) {
			// setAhead(300);
//			doMove();// move operator
			doFirePower();// define firePower value by distance
			doScan();// adjust scan direction
			doGun();// adjust gun direction
			// out.println(enemy.distance);
			// out.println(enemy.scanTime);
			// out.println(firePower);
			// if(getTime()%20==0){
			fire(firePower);// fire the enemy
			// }
			// not hit wall
			pos.setLocation(getX(), getY());
			double angle = getHeadingRadians();
			double expectAngle = wallsmooth(pos,angle,stick,orientation);
			setTurnRightRadians(expectAngle-angle);
//			setAhead(300);
			doMove();
			
			
			execute();
		}
	}

	private double wallsmooth(Double pos, double angle, double stick, int orientation) {
		// TODO Auto-generated method stub
		while(!rect.contains(newPos(pos,angle,stick))){
			angle +=0.01 * orientation;
		}
		return angle;
	}
	
	private Double newPos(Double pos, double angle, double distance){
		
		return new Double(pos.x+distance*Math.sin(angle), pos.y+distance*Math.cos(angle));
		
	}

	private void doGun() {
		// TODO Auto-generated method stub
		long time = getTime() + (int) (enemy.distance / (20 - 3 * firePower));// bulletArriveTime
		double gunOffset = getGunHeadingRadians() - absBearing(getX(), getY(), enemy.guessX(time), enemy.guessY(time));
		setTurnGunLeftRadians(NormalliseBearing(gunOffset));
	}

	private void doScan() {
		// TODO Auto-generated method stub
		double radarOffset;
		if (getTime() - enemy.scanTime > 4) {
			radarOffset = 360;
		} else {
			radarOffset = getRadarHeadingRadians() - absBearing(getX(), getY(), enemy.x, enemy.y);
			if (radarOffset < 0) {
				radarOffset -= PI / 8;
			} else {
				radarOffset += PI / 8;
			}
		}
		setTurnRadarLeftRadians(NormalliseBearing(radarOffset));
	}

	private double absBearing(double x, double y, double x2, double y2) {
		// calculte arcsin
		double x0 = x2 - x;
		double y0 = y2 - y;
		double s = Math.sqrt(x0 * x0 + y0 * y0);
		if (x0 > 0 && y0 > 0) {
			return Math.asin(x0 / s);
		}
		if (x0 > 0 && y0 < 0) {
			return PI - Math.asin(x0 / s);
		}
		if (x0 < 0 && y0 < 0) {
			return PI + Math.asin(-x0 / s);
		}
		if (x0 < 0 && y0 > 0) {
			return 2 * PI - Math.asin(-x0 / s);
		}
		return 0;
	}

	private double NormalliseBearing(double radarOffset) {
		// -PI- PI
		if (radarOffset > PI) {
			radarOffset -= 2 * PI;
		}
		if (radarOffset < -PI) {
			radarOffset += 2 * PI;
		}
		return radarOffset;
	}

	private void doFirePower() {
		// 0.1-3 firePower
		// firePower = 300 / enemy.distance;
		if (enemy.distance > 100) {
			firePower = 1.0;
		} else if (enemy.distance < 50) {
			firePower = 3.0;
		} else
//		if(enemy.distance < 50){
//			firePower = 3.0;
//		}else
			firePower = 2.5;
//		out.println("firePower:" + firePower);
	}

	private void doMove() {
		// TODO Auto-generated method stub
//		if (getTime() % moveTime == 0) {
//			moveDirection = moveDirection * -1;
			setAhead(moveDirection * moveAmount);
//		}
//		setTurnRightRadians(enemy.bearing + PI);// (PI / 2) ??can be adjust

	}

	/**
	 * init enemy args when we see a robot
	 */
	public void onScannedRobot(ScannedRobotEvent e) {
		// first find enemy and future find enemy
		if ((e.getDistance() < enemy.distance) || e.getName() == enemy.name) {
			double absBearingRad = (getHeadingRadians() + e.getBearingRadians()) % (2 * PI);
			enemy.name = e.getName();
			// out.println(e.getName());
			// out.println(enemy.name);
			enemy.x = getX() + Math.sin(absBearingRad) * e.getDistance();
			enemy.y = getY() + Math.cos(absBearingRad) * e.getDistance();
			enemy.bearing = e.getBearingRadians();
			enemy.head = e.getHeadingRadians();
			enemy.scanTime = getTime();
			enemy.speed = e.getVelocity();
			enemy.distance = e.getDistance();
		}

	}

	/**
	 * Hit wall operator
	 */
	public void onHitWall(HitWallEvent event) {
//		setTurnRightRadians(enemy.bearing + PI / 2);
//		setAhead(moveDirection * moveAmount);
		out.println("Ouch, I hit a wall bearing " + event.getBearing() + " degrees.");
	}

	/**
	 * onHitRobot: Move away a bit.
	 */
	public void onHitRobot(HitRobotEvent e) {
		// If he's in front of us, set back up a bit.
		if (e.getBearing() > -90 && e.getBearing() < 90) {
			setBack(200);
			
		} // else he's in back of us, so set ahead a bit.
			// else {
			// setBack(200);
			// }
	}

	/**
	 * when enemy die init enemy.distance
	 */
	public void onRobotDeath(RobotDeathEvent e) {
		if (e.getName() == enemy.name) {
			enemy.distance = 99999;
		}
	}
}

// class Enemy
// class Enemy {
// String name;
// public double bearing;
// public double head;
// public long scanTime;
// public double speed;
// public double x, y;
// public double distance;
//
// // forcast enemy next location x
// public double guessX(long bulletArriveTime) {
// long duringTime = bulletArriveTime - scanTime;
// return x + Math.sin(head) * speed * duringTime;
// }
//
// // forcast enemy next location y
// public double guessY(long bulletArriveTime) {
// long duringTime = bulletArriveTime - scanTime;
// return y + Math.cos(head) * speed * duringTime;
// }
// }
