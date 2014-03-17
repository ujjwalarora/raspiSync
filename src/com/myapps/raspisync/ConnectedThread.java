package com.myapps.raspisync;

import java.io.IOException;
import java.io.OutputStream;

import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Handler;

public class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        //private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private Handler mmHandler;
        
        Context context;

        public ConnectedThread(BluetoothSocket socket, Handler handler) {
            //Log.d(TAG, "create ConnectedThread: " + socketType);
            mmSocket = socket;
            //InputStream tmpIn = null;
            OutputStream tmpOut = null;
            mmHandler = handler;

            // Get the BluetoothSocket input and output streams
            try {
                //tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                //Log.e(TAG, "temp sockets not created", e);
            }

            //mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        /**
         * Write to the connected OutStream.
         * @param buffer  The bytes to write
         */
        public void write(byte[] buffer) {
            try {
                mmOutStream.write(buffer);

            } catch (IOException e) {
                //Log.e(TAG, "Exception during write", e);
            }
 
            String message = "Data Successfully Sent !";
	        mmHandler.obtainMessage(MainActivity.MESSAGE_WRITE, -1, -1, message).sendToTarget();
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                //Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }