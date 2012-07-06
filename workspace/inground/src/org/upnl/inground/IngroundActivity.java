package org.upnl.inground;

import java.util.List;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;
import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpResponseHandler;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.EditText;
import android.widget.Toast;

public class IngroundActivity extends MapActivity {
	
	private final IngroundActivity me = this;
	private GroundOverlay ground;
	private Network network;
	private MapView mapView;
	private MapController mapController;
	private MyLocationOverlay myLocationOverlay;
	private boolean started;
	private SensorHelper sensorHelper;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        mapView = (MapView)findViewById(R.id.mapView);
        mapView.setBuiltInZoomControls(false);
        mapView.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
                if(event.getPointerCount() > 1) {
                    return true;
                }
                return false;
            }
        });

        mapController = mapView.getController();
        mapController.setZoom(19);
        
        List<Overlay> mapOverlays = mapView.getOverlays();
        mapOverlays.add(ground = new GroundOverlay());
        myLocationOverlay = new MyLocationOverlay(this, mapView);
        mapOverlays.add(myLocationOverlay);
        
        network = new Network("http://neria.kr:16330/", this);
        sensorHelper = new SensorHelper(this);
        
    }
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean result = super.onCreateOptionsMenu(menu);
		menu.add(Menu.NONE, 1, 1, "login").setOnMenuItemClickListener(new OnMenuItemClickListener() {
			public boolean onMenuItemClick(MenuItem item) {
				me.doLogin();
				return true;
			}
		});
		menu.add(Menu.NONE, 2, 2, "start").setOnMenuItemClickListener(new OnMenuItemClickListener() {
			public boolean onMenuItemClick(MenuItem item) {
				me.doStart();
				return true;
			}
		});
		menu.add(Menu.NONE, 3, 3, "throw").setOnMenuItemClickListener(new OnMenuItemClickListener() {
			public boolean onMenuItemClick(MenuItem item) {
				me.doThrow();
				return true;
			}
		});
		return result;
	}

	private void doLogin() {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setTitle("Login");
		alert.setMessage("Input your name");
		final EditText name = new EditText(this);
		name.setLines(1);
		alert.setView(name);
		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				String value = name.getText().toString().trim();
				if(value.equals("")) {
					Toast.makeText(me, "Empty Field", Toast.LENGTH_SHORT).show();
					return;
				}
				network.post(new LoginRequestData(value), new AsyncHttpResponseHandler() {
					@Override
					public void onSuccess(String response) {
						@SuppressWarnings("unused")
						LoginResponseData data = new Gson().fromJson(response, LoginResponseData.class);
						Toast.makeText(me, "Login Succeeded", Toast.LENGTH_SHORT).show();
						me.doMap();
					}
					@Override
					public void onFailure(Throwable error, String content) {
						me.onFailureHelper(error, content, "Login");
					}
				});
			}
		});
		alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				Toast.makeText(me, "Canceled", Toast.LENGTH_SHORT).show();
			}
		});
		alert.show();
	}

	private void doMap() {
		network.post(new MapRequestData(), new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(String response) {
				MapResponseData data = new Gson().fromJson(response, MapResponseData.class);
				double[][] map = data.map;
				double minLat = Double.MAX_VALUE, maxLat = Double.MIN_VALUE, minLng = Double.MAX_VALUE, maxLng = Double.MIN_VALUE;
				for(int i = 0; i < map.length; i++) {
					if(map[i].length < 2) continue; // TODO
					double lat = map[i][0], lng = map[i][1];
					ground.addCell(lat, lng);
					if(lat < minLat) minLat = lat;
					else if(lat > maxLat) maxLat = lat;
					if(lng < minLng) minLng = lng;
					else if(lng > maxLng) maxLng = lng;
				}
				mapController.setCenter(new GeoPoint((int)((minLat + maxLat) / 2 * 1E6), (int)((minLng + maxLng) / 2 * 1E6)));
				Toast.makeText(me, "Map Loaded", Toast.LENGTH_SHORT).show();
			}
			@Override
			public void onFailure(Throwable error, String content) {
				me.onFailureHelper(error, content, "Map");
			}
		});
	}
	
	private void doStart() {
		GeoPoint gp = myLocationOverlay.getMyLocation();
		if(gp == null) {
			Toast.makeText(me, "Null Location", Toast.LENGTH_SHORT).show();
			return;
		}
		network.post(new StartRequestData(gp.getLatitudeE6() / 1E6d, gp.getLongitudeE6() / 1E6d), new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(String response) {
				StartResponseData data = new Gson().fromJson(response, StartResponseData.class);
				if(data != null && data.success) {
					started = true;
					doPoll();
				}
				Toast.makeText(me, String.format("Start %s", String.valueOf(data.success)), Toast.LENGTH_SHORT).show();
			}
			@Override
			public void onFailure(Throwable error, String content) {
				me.onFailureHelper(error, content, "Start");
			}
		});
	}
	
	private void doThrow() {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setTitle("Throw");
		View view = new View(this);
		view.setBackgroundColor(Color.GRAY);
		view.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				switch(event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					sensorHelper.start();
					return true;
				case MotionEvent.ACTION_UP:
					sensorHelper.stop();
					float[] velocity = sensorHelper.getVelocity();
					Toast.makeText(me, String.format("%f\n%f\n%f", velocity[0], velocity[1], velocity[2]), Toast.LENGTH_LONG).show();
					return true;
				}
				return false;
			}
		});
		alert.setView(view);
		alert.show();
	}
	
	private void doPoll() {
		if(!started) return;
		network.post(new PollRequestData(), new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(String response) {
				ResponseData abstractData = new Gson().fromJson(response, ResponseData.class);
				if(abstractData == null) return; // TODO
				if(abstractData.kind.equals("poll")) {
					// TODO
				}else if(abstractData.kind.equals("ground")) {
					GroundResponseData data = new Gson().fromJson(response, GroundResponseData.class);
					for(int i : data.ground) {
						ground.setCell(i, data.account);
					}
					mapView.invalidate();
				}else if(abstractData.kind.equals("finish")) {
					FinishResponseData data = new Gson().fromJson(response, FinishResponseData.class);
					started = false;
					StringBuilder result = new StringBuilder("Finish\n");
					result.append("======\n");
					for(FinishResponseData.FinishResultData r : data.result) {
						result.append(r.account);
						result.append(':');
						result.append(r.nofcells);
						result.append('\n');
					}
					result.append("======");
					Toast.makeText(me, result.toString(), Toast.LENGTH_LONG).show();
				}else {
					// TODO
				}
				doPoll();
			}
			@Override
			public void onFailure(Throwable error, String content) {
				me.onFailureHelper(error, content, "Poll");
			}
		});
	}
	
	private void onFailureHelper(Throwable error, String content, String title) {
		ErrorResponseData data = new Gson().fromJson(content, ErrorResponseData.class);
		String message = "";
		if(data == null) {
			message = error.toString();
		}else {
			message = data.message;
		}
		Toast.makeText(me, String.format("%s Failed : %s", title, message), Toast.LENGTH_SHORT).show();
	}
	
    @Override
    protected void onResume() {
    	super.onResume();
    	myLocationOverlay.enableMyLocation();
    }
    
    @Override
    protected void onPause() {
    	super.onPause();
    	myLocationOverlay.disableMyLocation();
    }

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}
	
	@Override
	public void onBackPressed() {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setTitle("Exit");
		alert.setMessage("Are you sure?");
		alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				me.finish();
			}
		});
		alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        });
		alert.show();
	}
	
}