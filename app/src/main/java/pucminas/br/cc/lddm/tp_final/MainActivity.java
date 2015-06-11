package pucminas.br.cc.lddm.tp_final;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;


public class MainActivity extends AppCompatActivity implements WifiP2pManager.ChannelListener {

    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private WifiP2pDevice mDevice;

    private String mData;
    private Uri mDataUri;

    private BroadcastReceiver mReceiver;
    private IntentFilter mIntentFilter;

    private boolean hasConnection;

    private ProgressDialog mProgress;

    @InjectView(R.id.listView) ListView listView;
    @InjectView(R.id.labelStatus) TextView status;
    @InjectView(R.id.sendFile) Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.inject(this);

        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);
        mReceiver = new WifiDirectBroadcastReceiver(mManager, mChannel, this, listView, status);

        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        setConnection(false);

        discoverPeers();
    }

    @Override
    protected void onResume() {
        super.onResume();
        status.setText("Not connected!");
        mReceiver = new WifiDirectBroadcastReceiver(mManager, mChannel, this, listView, status);
        registerReceiver(mReceiver, mIntentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        setConnection(false);
        disconnect();
    }

    @OnClick(R.id.sendFile)
    public void submit(View view) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("file/*");
        startActivityForResult(intent, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

         // Make sure the request was successful
         if (resultCode == RESULT_OK) {
             setData(data.getData());
             Log.d("Wp2p", data.getData().getPath());
//             Uri dt = data.getData();
//             File ft = new File(dt.getPath());
//             String[] arrt = ft.getName().split(".");
//             String ds = dt.toString() + "|" + arrt[arrt.length-1];
//             Log.d("Wp2p", ds);
//
//             setData(ds);
             connect();
         } else {
             return;
         }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_disconnect) {
            disconnect();
            status.setText("Not Connected!");
            return true;
        } else if (id == R.id.action_refresh) {
            discoverPeers();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onChannelDisconnected() {
        mManager.initialize(this, getMainLooper(), this);
        Toast.makeText(this, "Channel lost. Trying again", Toast.LENGTH_LONG).show();
    }

    private void discoverPeers() {
        mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                Log.d("Wp2p", "new peers");
                Toast.makeText(MainActivity.this, "Discovery Initiated", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int reasonCode) {

                Log.d("Wp2p", "failure on fetch peers");
                Toast.makeText(MainActivity.this, "Discovery Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void setDevice(WifiP2pDevice device) {
        mDevice = device;
    }

    public WifiP2pDevice getmDevice() {
        return mDevice;
    }

    public void setData(String data) {
        mData = data;
    }

    public void setData(Uri data) {
        mDataUri = data;
    }

    public String getData() {
        return mData;
    }

    public Uri getDataUri() {
        return mDataUri;
    }

    public void setConnection(boolean s) {
        hasConnection = s;
    }

    public boolean hasConnection() {
        return hasConnection;
    }

    public void setProgess(String title, String msg) {
        mProgress = ProgressDialog.show(this, title, msg, true);
        mProgress.setCancelable(true);
    }

    public ProgressDialog getProgress() {
        return mProgress;
    }

    public void enableBtn() {
        button.setEnabled(true);
    }

    public void disableBtn() {
        button.setEnabled(false);
    }

    public void connect() {

        setProgess("Synchronizing peers.", "Please wait.");

        WifiP2pDevice device = mDevice;
        WifiP2pConfig config = new WifiP2pConfig();

        config.deviceAddress = device.deviceAddress;
        config.wps.setup = WpsInfo.PBC;
        config.groupOwnerIntent = 0;

        mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                // WiFiDirectBroadcastReceiver will notify.
                Toast.makeText(MainActivity.this, "Connecting...", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int reason) {
                Toast.makeText(MainActivity.this, "Connect failed. Retry.", Toast.LENGTH_SHORT).show();
            }
        });

    }

    public void disconnect() {

        if (mManager != null && mChannel != null) {

            mManager.requestGroupInfo(mChannel, new WifiP2pManager.GroupInfoListener() {

                @Override
                public void onGroupInfoAvailable(WifiP2pGroup group) {
                    if (group != null && mManager != null && mChannel != null && group.isGroupOwner()) {

                        mManager.removeGroup(mChannel, new WifiP2pManager.ActionListener() {

                            @Override
                            public void onSuccess() {
                                Log.d("Wp2p", "removeGroup onSuccess -");
                            }

                            @Override
                            public void onFailure(int reason) {
                                Log.d("Wp2p", "removeGroup onFailure -" + reason);
                            }
                        });
                    }
                }

            });

        }
    }

}