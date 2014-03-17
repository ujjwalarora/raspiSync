package com.myapps.raspisync;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import android.R.bool;
import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    /** Called when the activity is first created. */
	BluetoothAdapter mBluetoothAdapter = null;	
	BluetoothDevice device = null;
	ConnectThread mConnectThread = null;
	decrypt mDecrypt = null;
	
    final int REQUEST_ENABLE_BT = 3;
    public static final int MESSAGE_WRITE = 4;
    public static final int SHOW_SUCCESS = 5;
    public static final int SHOW_FILE = 6;
    String status = null;
    public String rcvmessage = null;
    String device_addr = "90:C1:15:3D:C7:12";

    ProgressDialog mDialog = null ;
    private ListView mListView ;  
    private ArrayAdapter<String> mArrayAdapter ;
    
    boolean flag,sent;
    
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        sent=false;
        mArrayAdapter = new ArrayAdapter<String>(this, R.layout.message);
        mListView = (ListView) findViewById(R.id.in);
        mListView.setAdapter(mArrayAdapter);

        mArrayAdapter.add("App Started");
        
        //Samsung Galaxy SIII :: device = mBluetoothAdapter.getRemoteDevice(device_addr);
        
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        
        //Micromax A60
        //device = mBluetoothAdapter.getRemoteDevice("98:F5:37:D3:D6:63");
        // raspberry pi : device = mBluetoothAdapter.getRemoteDevice("11:11:11:11:11:11");
        //device = mBluetoothAdapter.getRemoteDevice("10:BF:48:CA:5D:A2");
        //Xperia U :: device = mBluetoothAdapter.getRemoteDevice("90:C1:15:3D:C7:12");
        //HTC      :: device = mBluetoothAdapter.getRemoteDevice("18:87:96:4B:31:D8");
        
         // Get local Bluetooth adapter
        	mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

            
            if (mBluetoothAdapter == null) {
            	Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
                finish();
                return;
            }
    	}
    	
	
    	@Override
        public void onStart() {
            super.onStart();
            
            // If BT is not on, request that it be enabled.
         
            if (!mBluetoothAdapter.isEnabled()) {
            	Toast.makeText(this, "Turning Bluetooth ON", Toast.LENGTH_LONG).show();   
            	mBluetoothAdapter.enable();
            }      

            if(sent==false)
            {
            	sent=true;
            mArrayAdapter.add("\"Key.txt\" found on SD card");
            mArrayAdapter.add("Sending to raspberry-pi_0");
          
            /*
            File sourceFile = new File("//mnt/sdcard/key.txt"); 
        	Intent intent = new Intent();
        	intent.setAction(Intent.ACTION_SEND);
        	intent.setType("image/jpeg"); 
        	intent.putExtra(Intent.EXTRA_STREAM,Uri.fromFile(sourceFile));
        	startActivity(intent);
            */
            
            
            if (mBluetoothAdapter.isEnabled()) {
				Set<BluetoothDevice> devices = mBluetoothAdapter.getBondedDevices();
				final String btDeviceName = "raspberrypi-0";//"Micromax A60"; 
				BluetoothDevice device = null;

				for (BluetoothDevice itDevice : devices) {
					if (btDeviceName.equals(itDevice.getName())) {
						device = itDevice;
					}
				}
				if (device != null) {

					ContentValues values = new ContentValues();
					values.put(BluetoothShare.URI, Uri.fromFile(new File("//mnt/sdcard/key.txt")).toString());
					//values.put(BluetoothShare.URI, uri.toString());
					values.put(BluetoothShare.MIMETYPE, "image/jpeg");
					values.put(BluetoothShare.DESTINATION, device.getAddress());
					values.put(BluetoothShare.DIRECTION, BluetoothShare.DIRECTION_OUTBOUND);
					Long ts = System.currentTimeMillis();
					values.put(BluetoothShare.TIMESTAMP, ts);
					final Uri contentUri = getApplicationContext().getContentResolver().insert(BluetoothShare.CONTENT_URI, values);
					//Log.v(TAG, "Insert contentUri: " + contentUri + "  to device: " + device.getName());
				} else {
					mArrayAdapter.add("Bluetooth remote device not found");
				}
			} else {
				mArrayAdapter.add("Bluetooth not activated");
			}
            
            mDialog = new ProgressDialog(MainActivity.this);
            mDialog = ProgressDialog.show(this,null, "Sending file...",true);

            WaitTime wait = new WaitTime();
            wait.execute();
            }
            else
            	mHandler.obtainMessage(MainActivity.SHOW_FILE, -1, -1, "").sendToTarget();
            
            //mHandler.obtainMessage(MainActivity.SHOW_FILE, -1, -1, "").sendToTarget();
            
        }
    
    	
    	@Override
        public synchronized void onResume() {
    		super.onResume();
 

    	}

    	
    	@Override
        public synchronized void onPause() {
            super.onPause();
    	}
    	
    	@Override
        public void onStop() {
            super.onStop();
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            if(mConnectThread != null)
            	mConnectThread.cancel();
        }
        
        private final Handler mHandler = new Handler() {
        	  @Override
              public void handleMessage(Message msg) {
                  switch (msg.what) {
                  
                  case MESSAGE_WRITE:
                  	String writemsg = (String) msg.obj;
                  	Toast.makeText(getApplicationContext(), writemsg , Toast.LENGTH_LONG).show();  
                    break;
                    
                  case SHOW_SUCCESS:
                	  mArrayAdapter.add("Success Sent");
                	  mArrayAdapter.add("Waiting for Encrypted file");
                	  
                	  mDialog = ProgressDialog.show(MainActivity.this,null, "Receiving file...",true);
                      FileTime filer = new FileTime();
                      filer.execute();
                	  break;
                	  
                  case SHOW_FILE:
                	  File file = new File("//mnt/sdcard/bluetooth/dat2.txt");
                      
                      if(file.exists())
                      {
                      	setContentView(R.layout.view_file);
                      	File sdcard = Environment.getExternalStorageDirectory();

                      	String keyPath = "//mnt/sdcard/key.txt";
                		String inputPath = "//mnt/sdcard/bluetooth/dat2.txt";
                		String outputPath = "//mnt/sdcard/out.txt";
                		try {
                			decrypt.process(keyPath, inputPath, outputPath);
                		} catch (Exception e) {
                			Toast.makeText(getApplicationContext(), ""+e , Toast.LENGTH_LONG).show();
                		}
                      	
                      	File efile = new File("//mnt/sdcard/out.txt");
                      	
                      	StringBuilder text = new StringBuilder();

                      	try {
                      	    BufferedReader br = new BufferedReader(new FileReader(efile));
                      	    String line;

                      	    while ((line = br.readLine()) != null) {
                      	        text.append(line);
                      	        text.append('\n');
                      	    }
                      	}
                      	catch (IOException e) {
                      	    //You'll need to add proper error handling here
                      	}

                      	TextView tv = (TextView)findViewById(R.id.file_view);

                      	tv.setText(text);
                      }
                      
                      else
                      {
                      	mArrayAdapter.add("File not recieved");
                      }
                      break;
                  }
        	  }
        };
        
        private class WaitTime extends AsyncTask<Void, Void, Void> {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                mDialog.show();
            }
            protected void onPostExecute() {
                mDialog.dismiss();
            }

        @Override
        protected void onCancelled() {
            mDialog.dismiss();
            super.onCancelled();
        }

            @Override
            protected Void doInBackground(Void... params) {
                long delayInMillis = 8000;
                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        mDialog.dismiss();
                        mHandler.obtainMessage(MainActivity.SHOW_SUCCESS, -1, -1, "").sendToTarget();
                    }
                }, delayInMillis);
                return null;
            }
        }
        
        private class FileTime extends AsyncTask<Void, Void, Void> {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                mDialog.show();
            }
            protected void onPostExecute() {
                mDialog.dismiss();
            }

        @Override
        protected void onCancelled() {
            mDialog.dismiss();
            super.onCancelled();
        }

            @Override
            protected Void doInBackground(Void... params) {
                long delayInMillis = 30000;
                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        mDialog.dismiss();
                        mHandler.obtainMessage(MainActivity.SHOW_FILE, -1, -1, "").sendToTarget();
                    }
                }, delayInMillis);
                return null;
            }
        }
      
}
