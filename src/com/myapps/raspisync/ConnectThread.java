package com.myapps.raspisync;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Handler;


public class ConnectThread extends Thread {
	    private final BluetoothSocket mmSocket;
	    //private final BluetoothDevice mmDevice;
	    private ConnectedThread mConnectedThread = null;
	    private Handler mHandler;
	    
	    Context context;
	    String message = null;
	    String rcvMessage = null;
	    byte[] msg;
	 
	    public ConnectThread(BluetoothDevice device, Handler handler) {
	    	
	    	
	        // Use a temporary object that is later assigned to mmSocket,
	        // because mmSocket is final
	    	mHandler = handler;
	        BluetoothSocket tmp = null;
	        //mmDevice = device;
	 
	        // MY_UUID is the app's UUID string, also used by the server code
	      /*
	        try {
				tmp = device.createInsecureRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
			} catch (IOException e) {
		        mHandler.obtainMessage(MainActivity.MESSAGE_WRITE, -1, -1, ""+e).sendToTarget();
		        
				e.printStackTrace();
			}
	    */   
	    
			Method m=null;
			try {
				m = device.getClass().getMethod("createInsecureRfcommSocket", new Class[] {int.class});
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				tmp = (BluetoothSocket) m.invoke(device, Integer.valueOf(1));
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		    
	        mmSocket = tmp;
	    }
	    
	    public void run() {
	 
	        try {
	            // Connect the device through the socket. This will block
	            // until it succeeds or throws an exception
	            mmSocket.connect();
	        } catch (IOException connectException) {
	        	mHandler.obtainMessage(MainActivity.MESSAGE_WRITE, -1, -1, ""+connectException).sendToTarget();
	            try {
	                mmSocket.close();
	            } catch (IOException closeException) { }
	            return;
	        }
	 
	        // Do work to manage the connection (in a separate thread)
	        //manageConnectedSocket(mmSocket);
	        
	        message = "Successfully connected to server";
	        mHandler.obtainMessage(MainActivity.MESSAGE_WRITE, -1, -1, message).sendToTarget();
	        
	        
	        mConnectedThread = new ConnectedThread(mmSocket,mHandler);
	       
	       rcvMessage = "hello....this is ujjwal";
	       msg = rcvMessage.getBytes();
	       mConnectedThread.write(msg);
	        
	    }
	 
	    /** Will cancel an in-progress connection, and close the socket */
	    public void cancel() {
	        try {
	        	if(mConnectedThread != null)
	        		mConnectedThread.cancel();
	            mmSocket.close();
	        } catch (IOException e) { }
	    }
	}
