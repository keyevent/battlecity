package com.hushifeng;

public class Enemy {
	String name;
	public double bearing;
	public double head;
	public long scanTime;
	public double speed;
	public double x, y;
	public double distance;
	public double energy;

	// forcast enemy next location x
	public double guessX(long bulletArriveTime) {
		long duringTime = bulletArriveTime - scanTime;
		return x + Math.sin(head) * speed * duringTime;
	}

	// forcast enemy next location y
	public double guessY(long bulletArriveTime) {
		long duringTime = bulletArriveTime - scanTime;
		return y + Math.cos(head) * speed * duringTime;
	}
}
