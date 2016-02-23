package tw.sendmessageusebluetooth;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Set;

import android.R.integer;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.TextureView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	private BluetoothAdapter mBTAdapter = null;
	private TextView txvCurrentBTDevice = null;
	private Button btnSearch = null;
	private Button btnSend = null;
	private BTConnectService mConnectService = null;
	private String mConnectedDeviceName = null;
	private ListView lvMessage = null;
	private ArrayAdapter<String> mMessageAdapter =null ;
	public static String tag = "BTSend";
	 private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
	 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		checkBlueToothAdapter();
		initialMessageAdapter();
		findView();
	}
	 @Override
	    public void onStart() {
	        super.onStart();
	        mConnectService = new BTConnectService(MainActivity.this, mHandler); 
	 }
	 
	 private void initialMessageAdapter() {
		 mMessageAdapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1);
		 mMessageAdapter.add("整合科技ABCDEFG ");
		 mMessageAdapter.add("0130304130333130BEE3A658ACECA7DE414243444546472002BA");
		 mMessageAdapter.add(getString(R.string.add_new_message));
		
	}
	 private void findView() {
			txvCurrentBTDevice = (TextView) findViewById(R.id.txvCurrentBTDevice);
			btnSearch = (Button) findViewById(R.id.btnSearch);
			btnSearch.setOnClickListener(searchOnClickListener);
			lvMessage = (ListView)findViewById(R.id.lvMessage);
			lvMessage.setAdapter(mMessageAdapter);
			lvMessage.setOnItemClickListener(messagechange);
			btnSend = (Button)findViewById(R.id.btnSend);
			btnSend.setOnClickListener(btnSendOnClickListener);
			
		}
	 
	 private OnClickListener btnSendOnClickListener = new OnClickListener(){

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			sendMessage();
		}
		 
	 };
	 
	 /**
	     * Sends a message.
	     *
	     * @param message A string of text to send.
	     */
	    private void sendMessage() {
	    	byte[] processedMessage =null;
	    	StackTraceElement ste= Thread.currentThread().getStackTrace()[2];
	    	Log.e(MainActivity.tag, "91/"+ste.getFileName()+" ,in "+ste.getMethodName());

	        // Check that we're actually connected before trying anything
//	        if (mConnectService.getState() != mConnectService.STATE_CONNECTED) {
//	            Toast.makeText(MainActivity.this, R.string.not_connected, Toast.LENGTH_SHORT).show();
//	            return;
//	        }

	        // Check that there's actually something to send
	        if (mMessageAdapter.getCount() > 1) {
	        	
	            // Get the message bytes and tell the BluetoothChatService to write
	        	for(int i=0;i<mMessageAdapter.getCount()-1;i++)
	        	Log.e(MainActivity.tag, "109/"+ste.getFileName()+" ,in "+ste.getMethodName()+" lvMessage.toString()="+mMessageAdapter.getItem(i));
	            
	        	processedMessage = processMessage();
//	        	processedMessage = processMessage2();
	        }
	        
	            mConnectService.write(processedMessage);

	            // Reset out string buffer to zero and clear the edit text field
//	            mOutStringBuffer.setLength(0);
//	            mOutEditText.setText(mOutStringBuffer);
//	        }
	    }
	    private byte[] processMessage(){
			 StackTraceElement ste= Thread.currentThread().getStackTrace()[2];
			 ByteBuffer ByteBuffer_processedMessage =null ;
			 byte[] processedMessage = null;
			 String AdapterMessage = null;
	         byte send_start =0x1;
	         byte send_end = 0x2;
	         int CheckNumber =send_start^send_end;
//	         String CheckNumber = "BA";
				try {
					AdapterMessage = mMessageAdapter.getItem(0);
					AdapterMessage = "00A0310"+AdapterMessage;
					processedMessage = AdapterMessage.getBytes("big5");
					Log.e(MainActivity.tag, "109/"+ste.getFileName()+" ,in "+ste.getMethodName()+" AdapterMessage="+AdapterMessage);
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				for (int j=0;j<processedMessage.length;j++){
					CheckNumber = CheckNumber^ processedMessage[j];
				}
				
				ByteBuffer_processedMessage = ByteBuffer.allocate(processedMessage.length+3);
				ByteBuffer_processedMessage.put(send_start);
				ByteBuffer_processedMessage.put(processedMessage,0,processedMessage.length);
				ByteBuffer_processedMessage.put(send_end);
				ByteBuffer_processedMessage.put((byte)CheckNumber);
				processedMessage = ByteBuffer_processedMessage.array();
				
				
				String temp=" ";
				for (int j=0;j<processedMessage.length;j++){
					temp = temp+String.format("%02X", processedMessage[j])+" ";
//					Log.e(MainActivity.tag, " %x "+send[j]);
				}
	         Log.e(MainActivity.tag, "processedMessage2="+temp);
			 return processedMessage;
		 }
//	 private byte[] processMessage2(){   //do XOR step by step 
//		 StackTraceElement ste= Thread.currentThread().getStackTrace()[2];
//		 ByteBuffer ByteBuffer_processedMessage =null ;
//		 byte[] processedMessage = null;
//		 String AdapterMessage = null;
//         byte send_start =0x1;
//         byte send_end = 0x2;
//         int CheckNumber = 0;
////         String CheckNumber = "BA";
//			try {
//				AdapterMessage = mMessageAdapter.getItem(0);
//				AdapterMessage = "00A0310"+AdapterMessage;
//				processedMessage = AdapterMessage.getBytes("big5");
//				Log.e(MainActivity.tag, "109/"+ste.getFileName()+" ,in "+ste.getMethodName()+" AdapterMessage="+AdapterMessage);
//			} catch (UnsupportedEncodingException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			ByteBuffer_processedMessage = ByteBuffer.allocate(processedMessage.length+3);
//			ByteBuffer_processedMessage.put(send_start);
//			ByteBuffer_processedMessage.put(processedMessage,0,processedMessage.length);
//			ByteBuffer_processedMessage.put(send_end);
//			processedMessage = ByteBuffer_processedMessage.array();
//			
//			for (int j=0;j<processedMessage.length;j++){
//				CheckNumber = CheckNumber^ processedMessage[j];
//			}
//			ByteBuffer_processedMessage.put((byte)CheckNumber);
//			processedMessage = ByteBuffer_processedMessage.array();
//			
//			
//			String temp=" ";
//			for (int j=0;j<processedMessage.length;j++){
//				temp = temp+String.format("%02X", processedMessage[j])+" ";
////				Log.e(MainActivity.tag, " %x "+send[j]);
//			}
//         Log.e(MainActivity.tag, "processedMessage="+temp);
//		 return processedMessage;
//	 }
	 
	 
	
	 private final Handler mHandler = new Handler() {
	        @Override
	        public void handleMessage(Message msg) {
	            switch (msg.what) {
	                case Constants.MESSAGE_STATE_CHANGE:
	                    switch (msg.arg1) {
	                        case BTConnectService.STATE_CONNECTED:
	                        	txvCurrentBTDevice.setText(mConnectedDeviceName);
	                            break;
	                        case BTConnectService.STATE_CONNECTING:
	                        	txvCurrentBTDevice.setText(R.string.title_connecting);
	                            break;
	                        case BTConnectService.STATE_LISTEN:
	                        case BTConnectService.STATE_NONE:
	                        	txvCurrentBTDevice.setText(R.string.title_not_connected);
	                            break;
	                    }
	                    break;
//	                case Constants.MESSAGE_WRITE:
//	                    byte[] writeBuf = (byte[]) msg.obj;
//	                    // construct a string from the buffer
//	                    String writeMessage = new String(writeBuf);
//	                    mConversationArrayAdapter.add("Me:  " + writeMessage);
//	                    break;
//	                case Constants.MESSAGE_READ:
//	                    byte[] readBuf = (byte[]) msg.obj;
//	                    // construct a string from the valid bytes in the buffer
//	                    String readMessage = new String(readBuf, 0, msg.arg1);
//	                    mConversationArrayAdapter.add(mConnectedDeviceName + ":  " + readMessage);
//	                    break;
	                case Constants.MESSAGE_DEVICE_NAME:
	                    // save the connected device's name
	                    mConnectedDeviceName = msg.getData().getString("device_name");
	                        Toast.makeText(MainActivity.this, "Connected to "
	                                + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
	                    break;
	                case Constants.MESSAGE_TOAST:
	                        Toast.makeText(MainActivity.this, msg.getData().getString(Constants.TOAST),
	                                Toast.LENGTH_SHORT).show();
	                    break;
	            }
	        }
	    };
	/**
	 * device may not support bluetooth
	 */
	private void checkBlueToothAdapter() {
		mBTAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBTAdapter == null) {
			Toast.makeText(MainActivity.this, "Bluetooth is not available",
					Toast.LENGTH_SHORT).show();
			finish();
		}
	}
	
	
	private OnItemClickListener messagechange = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			// TODO Auto-generated method stub
		
		}
	};

	

	OnClickListener searchOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			Intent it = new Intent(MainActivity.this,BTDeviceListActivity.class);
			startActivityForResult(it, REQUEST_CONNECT_DEVICE_SECURE);
		}
	};
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_CONNECT_DEVICE_SECURE:
			if(resultCode==Activity.RESULT_OK){
				connectDevice(data);
			}
			break;

		default:
			break;
		}
	
	};
	
    /**
     * Establish connection with other divice
     *
     * @param data   An {@link Intent} with {@link DeviceListActivity#EXTRA_DEVICE_ADDRESS} extra.
     * @param secure Socket Security type - Secure (true) , Insecure (false)
     */
    private void connectDevice(Intent data) {
        // Get the device MAC address
        String address = data.getExtras().getString("extra_device_address");
//        String name = data.getExtras().getString("extra_device_name");
        StackTraceElement ste = Thread.currentThread().getStackTrace()[2];
   	 Log.e(MainActivity.tag, "143/" + ste.getFileName()+ " in "+ste.getMethodName()+" address="+address);
        // Get the BluetoothDevice object
//   	 	txvCurrentBTDevice.setText(name);
        BluetoothDevice device = mBTAdapter.getRemoteDevice(address);
        // Attempt to connect to the device
        mConnectService.connect(device);
    }
	@Override
	protected void onDestroy() {
		   if (mConnectService != null) {
			   mConnectService.stop();
	        }
		super.onDestroy();

		// Make sure we're not doing discovery anymore
		// Unregister broadcast listeners
		// this.unregisterReceiver(mReceiver);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
