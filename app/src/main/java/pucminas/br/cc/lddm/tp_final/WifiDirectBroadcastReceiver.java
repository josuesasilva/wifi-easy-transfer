package pucminas.br.cc.lddm.tp_final;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.net.InetAddress;
import java.util.Collection;

import butterknife.InjectView;

public class WifiDirectBroadcastReceiver extends BroadcastReceiver {

    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private MainActivity mContext;

    private ListView mList;
    private TextView mStatus;

    private Intent serverServiceIntent;
    private Intent clientServiceIntent;

    private ProgressDialog progress;

    private final File DOWNLOAD_TARGET = getDir();
    private final int PORT = 8888;

    public WifiDirectBroadcastReceiver(WifiP2pManager manager, WifiP2pManager.Channel channel, MainActivity context, ListView list, TextView status) {
        super();
        this.mManager = manager;
        this.mChannel = channel;
        this.mContext = context;
        this.mList = list;
        this.mStatus = status;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {

            Log.v("Wp2p", "WIFI_P2P_STATE_CHANGED_ACTION");

            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                // Wifi P2P is enabled
                Toast.makeText(mContext, "Wifi P2P is enabled", Toast.LENGTH_SHORT).show();
            } else {
                // Wi-Fi P2P is not enabled
                Toast.makeText(mContext, "Wifi P2P is not enabled", Toast.LENGTH_SHORT).show();
            }


        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {

            Log.v("Wp2p", "new peer!");

            mManager.requestPeers(mChannel, new WifiP2pManager.PeerListListener() {

                @Override
                public void onPeersAvailable(WifiP2pDeviceList wifiP2pDeviceList) {
                    Collection<WifiP2pDevice> ll = wifiP2pDeviceList.getDeviceList();
                    UserListAdapter adapter = new UserListAdapter(mContext, ll);
                    Log.v("Wp2p", "new peer: " + ll.size());
                    mList.setAdapter(adapter);
                    mList.setOnItemClickListener(adapter);
                }
            });

        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            Log.v("Wp2p", "WIFI_P2P_CONNECTION_CHANGED_ACTION");

            NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

            if (networkInfo.isConnected()) {

                // We are connected with the other device, request connection
                // info to find group owner IP

                mContext.setConnection(true);

                mManager.requestConnectionInfo(mChannel, new WifiP2pManager.ConnectionInfoListener() {
                    @Override
                    public void onConnectionInfoAvailable(WifiP2pInfo info) {

                        // InetAddress from WifiP2pInfo struct.
                        InetAddress groupOwnerAddress = info.groupOwnerAddress;

                        // After the group negotiation, we can determine the group owner.
                        if (info.groupFormed && info.isGroupOwner) {

                            serverServiceIntent = new Intent(mContext, ServerService.class);
                            serverServiceIntent.putExtra("target", DOWNLOAD_TARGET);
                            serverServiceIntent.putExtra("port", new Integer(PORT));

                            mContext.startService(serverServiceIntent);

                            mStatus.setText("This device is a Group Owner");

                            progress = mContext.getProgress();
                            if (progress != null) progress.dismiss();

                            Log.d("Wp2p", "Server works!");
                        } else if (info.groupFormed) {
                            // The other device acts as the client. In this case,
                            // you'll want to create a client thread that connects to the group
                            // owner.

                            //File f = mContext.getData();
                            clientServiceIntent = new Intent(mContext, ClientService.class);
                            //clientServiceIntent.putExtra("file", f);
                            clientServiceIntent.putExtra("port", PORT);
                            clientServiceIntent.putExtra("address", groupOwnerAddress.getHostAddress());

                            if (mContext.getDataUri() != null)
                                clientServiceIntent.putExtra("data", mContext.getDataUri().toString());
                            else
                                clientServiceIntent.putExtra("data", "-");

                            //Log.d("Wp2p", "File: " + f.getName());
                            Log.d("Wp2p", "Port: " + PORT);
                            Log.d("Wp2p", "Address: " + groupOwnerAddress.getHostAddress());

                            mContext.startService(clientServiceIntent);

                            mStatus.setText("This device is a Client");

                            progress = mContext.getProgress();
                            if (progress != null) progress.dismiss();

                            mContext.enableBtn();

                            Log.d("Wp2p", "Client start request.");
                        }

                    }
                });
            } else {
                mStatus.setText("Not connected!");
                mContext.setConnection(false);
                mContext.disableBtn();
            }


        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {

            Log.v("Wp2p", "WIFI_P2P_THIS_DEVICE_CHANGED_ACTION");

        }
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
