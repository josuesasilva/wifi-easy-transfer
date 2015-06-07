package pucminas.br.cc.lddm.tp_final;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;
import android.widget.ListView;

import java.util.Collection;

public class WifiDirectBroadcastReceiver extends BroadcastReceiver {

    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private MainActivity mContext;
    private ListView mList;

    public WifiDirectBroadcastReceiver(WifiP2pManager manager, WifiP2pManager.Channel channel, MainActivity context, ListView list) {
        super();
        this.mManager = manager;
        this.mChannel = channel;
        this.mContext = context;
        this.mList = list;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {

            Log.v("Wp2p", "WIFI_P2P_STATE_CHANGED_ACTION");

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

        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {

            Log.v("Wp2p", "WIFI_P2P_THIS_DEVICE_CHANGED_ACTION");

        }
    }

}
