package tw.sendmessageusebluetooth;

import java.util.Set;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

;

public class BTDeviceListActivity extends Activity {
	private BluetoothAdapter mBTAdapter = null;
	private ArrayAdapter<String> pairedDevicesArrayAdapter = null,mNewDevicesArrayAdapter = null;
	public static String EXTRA_DEVICE_ADDRESS = "device_address";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		 requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		 setContentView(R.layout.activity_device_list);
		 setResult(Activity.RESULT_CANCELED);
		 
		 pairedDevicesArrayAdapter = new ArrayAdapter<String>(this,
					R.layout.device_name);
		 mNewDevicesArrayAdapter = new ArrayAdapter<String>(this,
					R.layout.device_name);
			
		 checkBlueToothAdapter();
		 searchBondedDevice();
		 
		 // Register for broadcasts when a device is discovered
	        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
	        this.registerReceiver(mReceiver, filter);

	        // Register for broadcasts when discovery has finished
	        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
	        this.registerReceiver(mReceiver, filter);
		 
	      Button scanButton = (Button) findViewById(R.id.button_scan);
	        scanButton.setOnClickListener(new View.OnClickListener() {
	            public void onClick(View v) {
	                searchNewBTDevice();
	                v.setVisibility(View.GONE);
	            }
	        });
	}
	private void checkBlueToothAdapter() {
		mBTAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBTAdapter == null) {
			Toast.makeText(BTDeviceListActivity.this, "Bluetooth is not available",
					Toast.LENGTH_SHORT).show();
			finish();
		}
	}
	private void searchBondedDevice() {
		StackTraceElement ste = Thread.currentThread().getStackTrace()[2];
		Log.e(MainActivity.tag, "35/"+ste.getFileName()+" in "+ste.getMethodName());
		Set<BluetoothDevice> pairedDevices = mBTAdapter.getBondedDevices();

		if (pairedDevices.size() > 0) {
			for (BluetoothDevice device : pairedDevices) {
				pairedDevicesArrayAdapter.add(device.getName() + "\n"
						+ device.getAddress());
				Log.e(MainActivity.tag, "49/"+ste.getFileName()+" in "+ste.getMethodName()+" device.getName()="+device.getName());
			}
		} else {
			String noDevices = getResources()
					.getText(R.string.no_paired_device).toString();
			pairedDevicesArrayAdapter.add(noDevices);
		}

	    ListView pairedListView = (ListView) findViewById(R.id.paired_devices);
        pairedListView.setAdapter(pairedDevicesArrayAdapter);
        pairedListView.setOnItemClickListener(mDeviceClickListener);
		
	}



	private void searchNewBTDevice() {
		
		setProgressBarIndeterminateVisibility(true);
        
		// If we're already discovering, stop it
		StackTraceElement ste = Thread.currentThread().getStackTrace()[2];
		Log.e(MainActivity.tag, "77/"+ste.getFileName()+" in "+ste.getMethodName());
		setTitle(R.string.scanning);
		if (mBTAdapter.isDiscovering()) {
			mBTAdapter.cancelDiscovery();
		}

		// Request discover from BluetoothAdapter
		mBTAdapter.startDiscovery();
		 ListView newDevicesListView = (ListView) findViewById(R.id.new_devices);
	        newDevicesListView.setAdapter(mNewDevicesArrayAdapter);
	        newDevicesListView.setOnItemClickListener(mDeviceClickListener);
		// Set<BluetoothDevice> pairedDevices = mBTAdapter.getBondedDevices();
		// if (pairedDevices.size() > 0) {
		// findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);
		// for (BluetoothDevice device : pairedDevices) {
		// pairedDevicesArrayAdapter.add(device.getName() + "\n" +
		// device.getAddress());
		// }
		// } else {
		// String noDevices =
		// getResources().getText(R.string.no_paired_device).toString();
		// pairedDevicesArrayAdapter.add(noDevices);
		// }

	}
    /**
     * The on-click listener for all devices in the ListViews
     */
    private AdapterView.OnItemClickListener mDeviceClickListener
            = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            // Cancel discovery because it's costly and we're about to connect
            mBTAdapter.cancelDiscovery();
            StackTraceElement ste = Thread.currentThread().getStackTrace()[2];
            // Get the device MAC address, which is the last 17 chars in the View
            String info = ((TextView) v).getText().toString();
            Log.e(MainActivity.tag, "138/"+ste.getFileName()+" in "+ste.getMethodName()+" info="+info);
           
            String address = info.substring(info.length() - 17);
            
            // Create the result Intent and include the MAC address
            Intent intent = new Intent();
            intent.putExtra(EXTRA_DEVICE_ADDRESS, address);

            // Set result and finish this Activity
            setResult(Activity.RESULT_OK, intent);
            finish();
        }
    };
    
   
	 /**
     * The BroadcastReceiver that listens for discovered devices and changes the title when
     * discovery is finished
     */
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // If it's already paired, skip it, because it's been listed already
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    mNewDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                }
                // When discovery is finished, change the Activity title
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                setProgressBarIndeterminateVisibility(false);
                setTitle(R.string.select_device_to_connect);
                if (mNewDevicesArrayAdapter.getCount() == 0) {
                    String noDevices = getResources().getText(R.string.none_found).toString();
                    mNewDevicesArrayAdapter.add(noDevices);
                }
            }
        }
    };

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		if (mBTAdapter != null) {
			mBTAdapter.cancelDiscovery();
		}
		 this.unregisterReceiver(mReceiver);
		super.onDestroy();
	}

}
