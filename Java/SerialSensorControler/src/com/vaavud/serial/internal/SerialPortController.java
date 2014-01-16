package com.vaavud.serial.internal;

import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import jssc.SerialPortList;

import com.vaavud.sensor.SensorEvent;
import com.vaavud.sensor.SensorListener;

public class SerialPortController implements SerialPortEventListener{
	
	private static SerialPortController instance;
	private static SerialPort serialPort;
	private Analyser analyser;
	private SensorListener listener = null;
	
	public synchronized static SerialPortController getInstance()
    {   
        if(instance == null)
            instance = new SerialPortController();

        return instance;
    }
	
	private SerialPortController() {
		analyser = new Analyser();
	}
	
	public void setListener(SensorListener listener) {
		this.listener = listener;
	}
	
	public void start() {
		
		if (listener == null) {
			throw new RuntimeException("SerialPortController has zero listeners!");
		}
		
		String[] portNames = SerialPortList.getPortNames();
        for(int i = 0; i < portNames.length; i++){
            System.out.println(portNames[i]);
        }
        
        serialPort = new SerialPort(portNames[0]); // chose first port
        try {
            serialPort.openPort(); //Open serial port
            try {
				Thread.sleep(100); // allow time to arduino too reboot.
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
            serialPort.setParams(
            		SerialPort.BAUDRATE_115200, 
            		SerialPort.DATABITS_8, 
            		SerialPort.STOPBITS_1, 
            		SerialPort.PARITY_NONE);//Set params.
            
            serialPort.purgePort(SerialPort.PURGE_RXCLEAR);
            serialPort.addEventListener(this);
            
        }
        catch (SerialPortException e) {
            System.out.println(e);
        }
        
		try {
        	Thread.sleep(10000); // timeout after 1 minute
        } catch (InterruptedException e) {
        	e.printStackTrace();
        }
        closeConnection();
	}
	
	@Override
	public void serialEvent(SerialPortEvent arg0) {
		try {
			analyser.append(serialPort.readString());
			SensorEvent event = analyser.readMeasurement();
			
			if (event != null) {
				listener.newEvent(event);
			}
			
		} catch (Exception e) {
			closeConnection();
			e.printStackTrace();
		}
	}
	
	public static void closeConnection() {
		
		try {
    		if (serialPort.isOpened()) {
    			serialPort.closePort();
    		}
		} catch (SerialPortException e) {
			e.printStackTrace();
		}
	}
}
