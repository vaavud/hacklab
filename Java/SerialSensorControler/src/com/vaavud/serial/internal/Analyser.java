package com.vaavud.serial.internal;

import com.vaavud.sensor.Sensor;
import com.vaavud.sensor.Sensor.Type;
import com.vaavud.sensor.SensorEvent;
import com.vaavud.sensor.SensorEvent1D;
import com.vaavud.sensor.SensorEvent3D;
import com.vaavud.sensor.SensorListener;

public class Analyser {
	
	private SensorListener listener;
	private static StringBuffer stringBuffer;
	private Sensor magSensor = new Sensor(Type.MAGNETIC_FIELD, "HMC5883L");
	private Sensor accSensor = new Sensor(Type.ACCELEROMETER, "ADXL345");
	private Sensor gyroSensor = new Sensor(Type.GYROSCOPE, "ITG3205");
	private Sensor tempSensor = new Sensor(Type.TEMPERATURE, "ITG3205");
	
	public Analyser(SensorListener listener) {
		stringBuffer = new StringBuffer();
		this.listener = listener;
	}
	
	public void append(String serialString) {
		stringBuffer.append(serialString);
	}
	
	public void readMeasurement() throws Exception {
		
		// if no complete line return null 
		if (stringBuffer.indexOf("\n") == -1) {
			return;
		}
		
		// if no decimal, clear string and return null 
		if (stringBuffer.indexOf(",") == -1){
			stringBuffer.delete(0, stringBuffer.indexOf("\n"));
			return;
			
		}
		
		long timeUs = getNextValue(",");
		int type = (int) (long) getNextValue(",");
		
		double[] values = null;
		SensorEvent event = null;

		switch (type) {
		case 0: // magnetic field
			values = new double[] {getNextValue(","), getNextValue(","), getNextValue("\n")};
			event = new SensorEvent3D(magSensor, timeUs, values);
			listener.newEvent(event);
			return;
		case 1: // accelerometer
			values = new double[] {getNextValue(","), getNextValue(","), getNextValue("\n")};
			event = new SensorEvent3D(accSensor, timeUs, values);
			listener.newEvent(event);
			return;
		case 2: // gyroscope
			values = new double[] {getNextValue(","), getNextValue(","), getNextValue(","), getNextValue("\n")};
			
			event = new SensorEvent3D(gyroSensor, timeUs, new double[]{values[0], values[1], values[2]});
			listener.newEvent(event);
			
			event = new SensorEvent1D(tempSensor, timeUs, values[3]);
			listener.newEvent(event);
			return;
		default:
			break;
		}
	}
	
	private Long getNextValue(String seperator) {

	    int	sepIndex = stringBuffer.indexOf(seperator);
		if (sepIndex != -1) {
		    long value;
		    try {
		        value = Long.parseLong(stringBuffer.substring(0,sepIndex));
            } catch (NumberFormatException e) {
                stringBuffer.delete(0, sepIndex+1);
                return null;
            }
		    stringBuffer.delete(0, sepIndex+1);
			return value;
		} else {
			return null;
		}
	}
 
	
}
