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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	private BluetoothAdapter mBTAdapter = null;
	private TextView txvCurrentBTDevice = null;
	private Button btnSearch = null;
	private BTConnectService mConnectService = null;
	public static String tag = "BTSearch";
	 private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
	 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		 checkBlueToothAdapter();
		findView();
	}
	 @Override
	    public void onStart() {
	        super.onStart();
	        mConnectService = new BTConnectService(MainActivity.this, mHandler); 
	 }
	 private final Handler mHandler = new Handler() {
	        @Override
	        public void handleMessage(Message msg) {
//	            switch (msg.what) {
//	                case Constants.MESSAGE_STATE_CHANGE:
//	                    switch (msg.arg1) {
//	                        case BluetoothChatService.STATE_CONNECTED:
//	                            setStatus(getString(R.string.title_connected_to, mConnectedDeviceName));
//	                            mConversationArrayAdapter.clear();
//	                            break;
//	                        case BluetoothChatService.STATE_CONNECTING:
//	                            setStatus(R.string.title_connecting);
//	                            break;
//	                        case BluetoothChatService.STATE_LISTEN:
//	                        case BluetoothChatService.STATE_NONE:
//	                            setStatus(R.string.title_not_connected);
//	                            break;
//	                    }
//	                    break;
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
//	                case Constants.MESSAGE_DEVICE_NAME:
//	                    // save the connected device's name
//	                    mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
//	                    if (null != activity) {
//	                        Toast.makeText(activity, "Connected to "
//	                                + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
//	                    }
//	                    break;
//	                case Constants.MESSAGE_TOAST:
//	                    if (null != activity) {
//	                        Toast.makeText(activity, msg.getData().getString(Constants.TOAST),
//	                                Toast.LENGTH_SHORT).show();
//	                    }
//	                    break;
//	            }
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
	private void findView() {
		txvCurrentBTDevice = (TextView) findViewById(R.id.txvCurrentBTDevice);
		btnSearch = (Button) findViewById(R.id.btnSearch);
		btnSearch.setOnClickListener(searchOnClickListener);
	}

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
				connectDevice(data, true);
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
    private void connectDevice(Intent data, boolean secure) {
        // Get the device MAC address
        String address = data.getExtras()
                .getString(BTDeviceListActivity.EXTRA_DEVICE_ADDRESS);
        // Get the BluetoothDevice object
        BluetoothDevice device = mBTAdapter.getRemoteDevice(address);
        // Attempt to connect to the device
        mConnectService.connect(device, secure);
    }
	@Override
	protected void onDestroy() {
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
