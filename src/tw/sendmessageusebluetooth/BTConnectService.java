package tw.sendmessageusebluetooth;

import java.io.IOException;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;


public class BTConnectService {
	  // Unique UUID for this application
    private static final UUID MY_UUID_SECURE = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
//    private static final UUID SERIAL_PORT_SERVICE_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private final BluetoothAdapter mBTAdapter;
    private final Handler mHandler;
    private ConnectThread mConnectThread = null;
//    private ConnectedThread mConnectedThread;
    private int mState = 0;
    
    // Constants that indicate the current connection state
    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_LISTEN = 1;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device

    
    public BTConnectService(Context context, Handler mHandler){
    	mBTAdapter = BluetoothAdapter.getDefaultAdapter();
    	this.mHandler = mHandler;
    	mState = STATE_NONE;
    }
    
    private synchronized void setState(int state) {
        mState = state;

        // Give the new state to the Handler so the UI Activity can update
        mHandler.obtainMessage(Constants.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
    }
    public synchronized void start() {

        // Cancel any thread attempting to make a connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
//        if (mConnectedThread != null) {
//            mConnectedThread.cancel();
//            mConnectedThread = null;
//        }
        setState(STATE_LISTEN);
    }

    
    /**
     * Start the ConnectThread to initiate a connection to a remote device.
     *
     * @param device The BluetoothDevice to connect
     * @param secure Socket Security type - Secure (true) , Insecure (false)
     */
    public synchronized void connect(BluetoothDevice device, boolean secure) {

        // Cancel any thread attempting to make a connection
        if (mState == STATE_CONNECTING) {
            if (mConnectThread != null) {
                mConnectThread.cancel();
                mConnectThread = null;
            }
        }

        // Cancel any thread currently running a connection
//        if (mConnectedThread != null) {
//            mConnectedThread.cancel();
//            mConnectedThread = null;
//        }

        // Start the thread to connect with the given device
        mConnectThread = new ConnectThread(device);  
        mConnectThread.start();
        setState(STATE_CONNECTING);
    }
    
    /**
     * This thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails.
     */
    private class ConnectThread extends Thread{
    	private final BluetoothDevice mBTDevice;
    	private final BluetoothSocket mBTSocket;
    	
    	public ConnectThread(BluetoothDevice device){
    		mBTDevice = device;
    		BluetoothSocket tmp = null;
    		try{
//    			
    			tmp = device.createRfcommSocketToServiceRecord(MY_UUID_SECURE);
    		}catch(IOException e){
    			Log.e(MainActivity.tag, "my socket create failed",e);
    		}
    		mBTSocket=tmp;
    	}
    	
    	public void run(){
    		mBTAdapter.cancelDiscovery();
    		try{
    			mBTSocket.connect();
    		}catch(Exception e2){
    			try{
    				mBTSocket.close();
    			}catch(Exception e3){
    				Log.e(MainActivity.tag, "cann't close socket",e3);
    			}
    			 connectionFailed();
    			 return;
    		}
//    		connected(mBTSocket, mBTDevice);
    	}
    	
        public void cancel() {
            try {
            	mBTSocket.close();
            } catch (IOException e) {
                Log.e(MainActivity.tag, "my thread cancel", e);
            }
        }
    }
    
   private void connectionFailed() {
	   Message msg = mHandler.obtainMessage(Constants.MESSAGE_TOAST);
	   Bundle mBundle = new Bundle();
	   mBundle.putString("connection fail", "Unable to connect device");
	   msg.setData(mBundle);
	   mHandler.sendMessage(msg);
	   
	   BTConnectService.this.start();
}
}
