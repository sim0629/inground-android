package org.upnl.inground;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class SensorHelper {

	private SensorManager sensorManager;
	public SensorHelper(IngroundActivity inground) {
	    sensorManager = (SensorManager)inground.getSystemService(Context.SENSOR_SERVICE);
		accelerationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
		gravitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
		geomagneticSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
	}
	
	private float startX, startY, stopX, stopY;
	
	public void start(float x, float y) {
		startX = x;
		startY = y;
	}
	
	public void stop(float x, float y) {
		stopX = x;
		stopY = y;
	}
	
	private final float[] velocity = new float[3];
	public float[] getVelocity() {
		return new float[] {
			startX - stopX,
			stopY - startY
		};
	}
	
	private float[] getTransformedAcceleration() {
		float[] R = new float[9];
		if(!SensorManager.getRotationMatrix(R, null, gravity, geomagnetic)) return null;
		float[] transformedAcceleration = new float[] {
			R[0] * acceleration[0] + R[1] * acceleration[1] + R[2] * acceleration[2],
			R[3] * acceleration[0] + R[4] * acceleration[1] + R[5] * acceleration[2],
			R[6] * acceleration[0] + R[7] * acceleration[1] + R[8] * acceleration[2],
		};
		return transformedAcceleration;
	}
	
	// Acceleration
	private Sensor accelerationSensor;
	private float[] acceleration;
	private SensorEventListener accelerationListener = new SensorEventListener() {
		public void onSensorChanged(SensorEvent event) {
			acceleration = new float[] { event.values[0], event.values[1], event.values[2] };
			float[] ta = getTransformedAcceleration();
			if(ta == null) return;
			velocity[0] += ta[0]; velocity[1] += ta[1]; velocity[2] += ta[2];
		}
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
			// TODO
		}
	};
	
	private void registerAcceleration() {
		acceleration = null;
		sensorManager.registerListener(accelerationListener, accelerationSensor, SensorManager.SENSOR_DELAY_GAME);
	}
	private void unregisterAcceleration() {
		sensorManager.unregisterListener(accelerationListener);
	}
	
	// Gravity
	private Sensor gravitySensor;
	private float[] gravity;
	private SensorEventListener gravityListener = new SensorEventListener() {
		public void onSensorChanged(SensorEvent event) {
			gravity = new float[] { event.values[0], event.values[1], event.values[2] };
		}
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
			// TODO
		}
	};
	private void registerGravity() {
		gravity = null;
		sensorManager.registerListener(gravityListener, gravitySensor, SensorManager.SENSOR_DELAY_GAME);
	}
	private void unregisterGravity() {
		sensorManager.unregisterListener(gravityListener);
	}
	
	// Geomagnetic
	private Sensor geomagneticSensor;
	private float[] geomagnetic;
	private SensorEventListener geomagneticListener = new SensorEventListener() {
		public void onSensorChanged(SensorEvent event) {
			geomagnetic = new float[] { event.values[0], event.values[1], event.values[2] };
		}
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
			// TODO
		}
	};
	private void registerGeomagnetic() {
		geomagnetic = null;
		sensorManager.registerListener(geomagneticListener, geomagneticSensor, SensorManager.SENSOR_DELAY_GAME);
	}
	private void unregisterGeomagnetic() {
		sensorManager.unregisterListener(geomagneticListener);
	}
	
}
