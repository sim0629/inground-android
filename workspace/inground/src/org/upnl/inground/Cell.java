package org.upnl.inground;

import com.google.android.maps.GeoPoint;

public class Cell {
	public static final int NONE = -1;
	
	private GeoPoint point;
	private int who = NONE;
	
	public Cell(GeoPoint point) {
		this.point = point;
	}
	
	public GeoPoint getPoint() {
		return point;
	}
	
	public int getWho() {
		return who;
	}
	
	public void setWho(int who) {
		this.who = who;
	}
}
