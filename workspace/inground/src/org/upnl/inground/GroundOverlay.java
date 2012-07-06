package org.upnl.inground;

import java.util.ArrayList;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

public class GroundOverlay extends Overlay {

	private final int[] colors = new int[] {
		Color.RED,
		Color.BLUE,
		Color.GREEN,
		Color.CYAN,
		Color.YELLOW,
		Color.MAGENTA
	};
	private final int cellRadius = 4;
	private final int transparency = 128;
	
	private ArrayList<Cell> cells = new ArrayList<Cell>();
	private ArrayList<String> accounts = new ArrayList<String>();
	private final ArrayList<GeoPoint> path = new ArrayList<GeoPoint>();
	private final ArrayList<GeoPoint> failedPath = new ArrayList<GeoPoint>();
	
	public GroundOverlay() {
	}
	
	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		super.draw(canvas, mapView, shadow);
		
		if(shadow) return;
		
		Projection projection = mapView.getProjection();
		Paint paint = new Paint();
		
		for(int i = 0; i < cells.size(); i++) {
			Cell cell = cells.get(i);
			
			Point p = new Point();
			projection.toPixels(cell.getPoint(), p);
			
			int who = cell.getWho();
			int color = Color.BLACK;
			if(who == Cell.NONE) {
				paint.setStyle(Paint.Style.STROKE);
				color = Color.GRAY;
			}else {
				paint.setStyle(Paint.Style.FILL);
				if(who >= 0 && who < colors.length)
					color = colors[who];
			}
			
			paint.setColor(color);
			paint.setAlpha(transparency);
			canvas.drawRect(p.x - cellRadius, p.y - cellRadius, p.x + cellRadius, p.y + cellRadius, paint);
		}
		
		paint.setStrokeWidth(5);
		
		if(path.size() > 1) {
			paint.setColor(Color.BLACK);
			float[] pts = new float[2 * path.size()];
			for(int i = 0; i < path.size(); i++) {
				Point p = new Point();
				projection.toPixels(path.get(i), p);
				pts[2 * i] = p.x; pts[2 * i + 1] = p.y;
			}
			canvas.drawLines(pts, paint);
		}
		
		if(failedPath.size() > 1) {
			paint.setColor(Color.RED);
			float[] pts = new float[2 * failedPath.size()];
			for(int i = 0; i < failedPath.size(); i++) {
				Point p = new Point();
				projection.toPixels(failedPath.get(i), p);
				pts[2 * i] = p.x; pts[2 * i + 1] = p.y;
			}
			canvas.drawLines(pts, paint);
		}
		
	}
	
	public void addCell(double lat, double lng) {
		cells.add(new Cell(new GeoPoint((int)(lat * 1E6), (int)(lng * 1E6))));
	}
	
	public void setCell(int index, String account) {
		if(index < 0 || index >= cells.size()) return; // TODO
		int who = accounts.indexOf(account);
		if(who < 0) {
			accounts.add(account);
			who = accounts.size() - 1;
		}
		cells.get(index).setWho(who);
	}
	
	public boolean isInitialThrowing() {
		return path.size() == 0;
	}
	
	public void addPath(double lat, double lng) {
		addPath(new GeoPoint((int)(lat * 1E6), (int)(lng * 1E6)));
		failedPath.clear();
	}
	
	public void addPath(GeoPoint gp) {
		if(path.size() > 3) path.clear(); // TODO 변이 무조건 세 개가 아닐 수 있음
		path.add(gp);
	}
	
	public void preserveFailedPath() {
		failedPath.addAll(path);
	}
	
	public void clearPath() {
		path.clear();
	}
	
}
