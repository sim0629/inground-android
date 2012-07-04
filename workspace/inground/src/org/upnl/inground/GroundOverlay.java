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
	
	private ArrayList<Cell> cells = new ArrayList<Cell>();
	
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
			canvas.drawRect(p.x - cellRadius, p.y - cellRadius, p.x + cellRadius, p.y + cellRadius, paint);
		}
	}
	
	public void addCell(double lat, double lng) {
		cells.add(new Cell(new GeoPoint((int)(lat * 1E6), (int)(lng * 1E6))));
	}
	
}
