package tw.sendmessageusebluetooth;

import java.util.Set;

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
		 mMessageAdapter.add("整合科技ABCDEFG");
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
			
		}
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
