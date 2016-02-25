package tw.sendmessageusebluetooth;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Contacts;
import android.util.Log;


public class BTConnectService {
	  // Unique UUID for this application
//    private static final UUID MY_UUID_SECURE = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
    private static final UUID MY_UUID_SECURE = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private final BluetoothAdapter mBTAdapter;
    private final Handler mHandler;
    private ConnectThread mConnectThread = null;
//    private AcceptThread mSecureAcceptThread = null;
    private ConnectedThread mConnectedThread;
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
    	 StackTraceElement ste = Thread.currentThread().getStackTrace()[2];
   		Log.e(MainActivity.tag, "44-2/" + ste.getFileName()+ " in "+ste.getMethodName());
        mState = state;

        // Give the new state to the Handler so the UI Activity can update
        mHandler.obtainMessage(Constants.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
    }
    
    public synchronized int getState(){
    	return mState;
    }
    public synchronized void start() {
    	 StackTraceElement ste = Thread.currentThread().getStackTrace()[2];
   		Log.e(MainActivity.tag, "44-3/" + ste.getFileName()+ " in "+ste.getMethodName());
        // Cancel any thread attempting to make a connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }
        
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        setState(STATE_LISTEN);
        
    }
    
    /**
     * Start the ConnectThread to initiate a connection to a remote device.
     *
     * @param device The BluetoothDevice to connect
     * @param secure Socket Security type - Secure (true) , Insecure (false)
     */
    public synchronized void connect(BluetoothDevice device) {
    	 StackTraceElement ste = Thread.currentThread().getStackTrace()[2];
   		Log.e(MainActivity.tag, "44-4/" + ste.getFileName()+ " in "+ste.getMethodName());
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
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice
            device) {
    	StackTraceElement ste = Thread.currentThread().getStackTrace()[2];
		Log.e(MainActivity.tag, "96/" + ste.getFileName()+ " in "+ste.getMethodName()+" mConnectThread="+mConnectThread);
		Log.e(MainActivity.tag, "96-2/" + ste.getFileName()+ " in "+ste.getMethodName()+" mConnectedThread="+mConnectedThread);
        // Cancel the thread that completed the connection
        if (mConnectThread != null) {
//            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

//        // Cancel the accept thread because we only want to connect to one device
//        if (mSecureAcceptThread != null) {
//            mSecureAcceptThread.cancel();
//            mSecureAcceptThread = null;
//        }
//        if (mInsecureAcceptThread != null) {
//            mInsecureAcceptThread.cancel();
//            mInsecureAcceptThread = null;
//        }

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();

        // Send the name of the connected device back to the UI Activity
        Message msg = mHandler.obtainMessage(Constants.MESSAGE_DEVICE_NAME);
        Bundle bundle = new Bundle();
        bundle.putString("device_name", device.getName());
        msg.setData(bundle);
        mHandler.sendMessage(msg);

        setState(STATE_CONNECTED);
    }
    public void write(byte[] out) {
    	 StackTraceElement ste = Thread.currentThread().getStackTrace()[2];
    	 Log.e(MainActivity.tag, "135/" + ste.getFileName()+ " in "+ste.getMethodName());
    	synchronized (this) {
    		if(mState != STATE_CONNECTED) {
    			Message msg = mHandler.obtainMessage(Constants.MESSAGE_TOAST);
    			Bundle mBundle = new Bundle();
    			mBundle.putString(Constants.TOAST, "the connect is broken !!");
    			msg.setData(mBundle);
    			mHandler.sendMessage(msg);
    			return;
    		}
    		 	mConnectedThread.write(out);
		}
    }
    public synchronized void stop() {

        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        setState(STATE_NONE);
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
    		 StackTraceElement ste = Thread.currentThread().getStackTrace()[2];
    	  		Log.e(MainActivity.tag, "44-5/" + ste.getFileName()+ " in "+ste.getMethodName());
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
    		
    		// Start the connected thread
    		connected(mBTSocket, mBTDevice);
    	}
    	
        public void cancel() {
            try {
            	mBTSocket.close();
            } catch (IOException e) {
                Log.e(MainActivity.tag, "my thread cancel", e);
            }
           
        }
    }
    
    private class ConnectedThread extends Thread {
    	private final BluetoothSocket mBTSocket;
    	private final InputStream mInputStream;
    	private final OutputStream mOutputStream;
    	
    	private ConnectedThread (BluetoothSocket socket){
    		this.mBTSocket = socket;
    		InputStream tempIn = null;
    		OutputStream tempOut = null;
    		
    		try {
				tempIn = socket.getInputStream();
				tempOut = socket.getOutputStream();
			} catch (IOException e) {
				Log.e(MainActivity.tag,"cann't get socket.getOutputStream()",e);
				// TODO: handle exception
			} 
    		this.mInputStream = tempIn;
    		this.mOutputStream = tempOut;
    	}
    	 public void run() {
             byte[] buffer = new byte[1024];
             int bytes;

             // Keep listening to the InputStream while connected
             while (true) {
                 try {
                     // Read from the InputStream
                     bytes = mInputStream.read(buffer);

                     // Send the obtained bytes to the UI Activity
                     mHandler.obtainMessage(Constants.MESSAGE_READ, bytes, -1, buffer)
                             .sendToTarget();
                 } catch (IOException e) {
                     Log.e(MainActivity.tag, "lost connected", e);
                     connectionLost();
                     // Start the service over to restart listening mode
                     BTConnectService.this.start();
                     break;
                 }
             }
         }
    	
    	
    	 /**
         * Write to the connected OutStream.
         *
         * @param buffer The bytes to write
         */
    	public void write(byte[] buffer){
    		try {
    			 StackTraceElement ste = Thread.currentThread().getStackTrace()[2];
    	    	 Log.e(MainActivity.tag, "96-4/" + ste.getFileName()+ " in "+ste.getMethodName()+" mOutputStream="+mOutputStream);
    			
    	    	 
    	    	 mOutputStream.write(buffer);
        		// Share the sent message back to the UI Activity
                mHandler.obtainMessage(Constants.MESSAGE_WRITE, -1, -1, buffer).sendToTarget();
			} catch (Exception e) {
				Log.e(MainActivity.tag,"cann't write mOutputStream.write(buffer);",e);
			}
    		
    	}
    	
    	public void cancel(){
    		try {
    			mBTSocket.close();
    			 StackTraceElement ste = Thread.currentThread().getStackTrace()[2];
    	    	 Log.e(MainActivity.tag, "96-3/" + ste.getFileName()+ " in "+ste.getMethodName()+" mBTSocket="+mBTSocket);
    	    	
			} catch (Exception e) {
				Log.e(MainActivity.tag,"cann't close socket",e);
			}
    		
    	}
    }

    private void connectionFailed() {
    	StackTraceElement ste = Thread.currentThread().getStackTrace()[2];
		Log.e(MainActivity.tag, "44-6/" + ste.getFileName()+ " in "+ste.getMethodName());
	   Message msg = mHandler.obtainMessage(Constants.MESSAGE_TOAST);
	   Bundle mBundle = new Bundle();
	   mBundle.putString(Constants.TOAST, "Unable to connect device");
	   msg.setData(mBundle);
	   mHandler.sendMessage(msg);
	   
	   BTConnectService.this.start();
    }
    private void connectionLost() {
    	Message msg = mHandler.obtainMessage(Constants.MESSAGE_TOAST);
    	Bundle mBundle = new Bundle();
    	mBundle.putString(Constants.TOAST, "Device connection was lost");
    	msg.setData(mBundle);
    	mHandler.sendMessage(msg);
    	
    	BTConnectService.this.start();
		
	}
}
