package pucminas.br.cc.lddm.tp_final;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import java.io.File;
import java.util.Collection;

import butterknife.ButterKnife;
import butterknife.InjectView;


public class MainActivity extends AppCompatActivity {

    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private WifiP2pDevice mDevice;

    private BroadcastReceiver mReceiver;
    private IntentFilter mIntentFilter;

    private boolean serverStatus = false;

    private Intent serverServiceIntent;
    private Intent clientServiceIntent;

    private final File DOWNLOAD_TARGET = getDir();
    private final int PORT = 8888;

    private final MainActivity activity = this;

    @InjectView(R.id.listView) ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.inject(this);

        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = (WifiP2pManager.Channel) mManager.initialize(this, getMainLooper(), null);
        mReceiver = new WifiDirectBroadcastReceiver(mManager, mChannel, this, listView);

        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        discoverPeers();
        startServer();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mReceiver, mIntentFilter);
        startServer();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //stopServer();
        //unregisterReceiver(mReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopServer();
        unregisterReceiver(mReceiver);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        File f = new File(data.getData().getPath());

        clientServiceIntent = new Intent(this, ClientService.class);
        clientServiceIntent.putExtra("file", f);
        clientServiceIntent.putExtra("port", PORT);
        clientServiceIntent.putExtra("device", mDevice);

        Log.v("Wp2p", "File: " + f.getName());
        Log.v("Wp2p", "Port: " + PORT);
        Log.v("Wp2p", "Device address: " + mDevice.deviceAddress);

        startService(clientServiceIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.action_refresh) {
            updateList();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void updateList() {

        mManager.requestPeers(mChannel, new WifiP2pManager.PeerListListener() {

            @Override
            public void onPeersAvailable(WifiP2pDeviceList wifiP2pDeviceList) {

                Collection<WifiP2pDevice> ll = wifiP2pDeviceList.getDeviceList();

                UserListAdapter adapter = new UserListAdapter(activity, ll);

                Log.v("Wp2p", "new peer: " + ll.size());

                listView.setAdapter(adapter);
            }
        });
    }

    private void discoverPeers() {
        mManager.discoverPeers((WifiP2pManager.Channel) mChannel, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                Log.v("Wp2p", "new peers");
            }

            @Override
            public void onFailure(int reasonCode) {
                Log.v("Wp2p", "failure on fetch peers");
            }
        });
    }

    private void startServer() {
        if (serverStatus == false) {

            serverServiceIntent = new Intent(this, ServerService.class);
            serverServiceIntent.putExtra("target", DOWNLOAD_TARGET);
            serverServiceIntent.putExtra("port", new Integer(PORT));

            serverStatus = true;

            startService(serverServiceIntent);

            Log.v("Wp2p", "Server works!");
        }
    }

    private void stopServer() {
        if (serverStatus == true)
            stopService(serverServiceIntent);
    }

    public void setDevice(WifiP2pDevice device) {
        mDevice = device;
    }

    private File getDir() {

        File directory = null;

        if (Environment.getExternalStorageState() == null) {
            //create new file directory object
            directory = new File(Environment.getDataDirectory()
                    + "/wifip2p/");

            // if no directory exists, create new directory
            if (!directory.exists()) {
                directory.mkdir();
            }

            // if phone DOES have sd card
        } else if (Environment.getExternalStorageState() != null) {
            // search for directory on SD card
            directory = new File(Environment.getExternalStorageDirectory()
                    + "/wifip2p/");

            if (!directory.exists()) {
                directory.mkdir();
            }
        }

        return directory;
    }

}