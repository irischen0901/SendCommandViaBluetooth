package tw.sendmessageusebluetooth;

import java.util.Set;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.nfc.Tag;
import android.os.Bundle;
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
	public static String tag = "BTSearch";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		findView();
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
			startActivity(it);
		}
	};

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
