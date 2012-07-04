package org.upnl.inground;

import java.util.List;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpResponseHandler;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.widget.EditText;
import android.widget.Toast;

public class IngroundActivity extends MapActivity {
	
	private final IngroundActivity me = this;
	private GroundOverlay ground;
	private Network network;
	private MapView mapView;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        mapView = (MapView)findViewById(R.id.mapView);
        mapView.setBuiltInZoomControls(false);
        
        MapController mapController = mapView.getController();
        mapController.setZoom(19);
        
        List<Overlay> mapOverlays = mapView.getOverlays();
        mapOverlays.add(ground = new GroundOverlay());
        
        network = new Network("http://neria.kr:16330/", this);
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
		return result;
	}
	
	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}
	
	private void doLogin() {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setTitle("Log in");
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
						ErrorResponseData data = new Gson().fromJson(content, ErrorResponseData.class);
						String message = "";
						if(data == null) {
							message = error.toString();
						}else {
							message = data.message;
						}
						Toast.makeText(me, "Login Failed : " + message, Toast.LENGTH_SHORT).show();
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
				mapView.getController().setCenter(new GeoPoint((int)((minLat + maxLat) / 2 * 1E6), (int)((minLng + maxLng) / 2 * 1E6)));
				Toast.makeText(me, "Map Loaded", Toast.LENGTH_SHORT).show();
			}
		});
	}
}