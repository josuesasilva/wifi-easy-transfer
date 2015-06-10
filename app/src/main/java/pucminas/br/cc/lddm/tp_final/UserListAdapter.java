package pucminas.br.cc.lddm.tp_final;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pDevice;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class UserListAdapter extends ArrayAdapter implements AdapterView.OnItemClickListener {

    private LayoutInflater inflater;
    private MainActivity mContext;
    private List<WifiP2pDevice> mUsers;

    public UserListAdapter(MainActivity context, Collection<WifiP2pDevice> list) {
        super(context, R.layout.user_list_item, new LinkedList<WifiP2pDevice>(list));
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mUsers = new LinkedList<>(list);
        mContext = context;
    }

    @Override
    public int getCount() {
        return mUsers.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;

        if (rowView == null) {
            rowView = inflater.inflate(R.layout.user_list_item, null);
        }

        if (mUsers.get(position) != null) {
            WifiP2pDevice device = mUsers.get(position);
            TextView name = (TextView) rowView.findViewById(R.id.deviceName);
            name.setText(device.deviceName);
            TextView address = (TextView) rowView.findViewById(R.id.deviceMAC);
            address.setText(device.deviceAddress);
        }

        return rowView;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (mUsers.get(position) != null) {

            WifiP2pDevice device = mUsers.get(position);
            Log.v("Wp2p", "click on " + position);

            //Intent intent = new Intent();
            //intent.setAction(Intent.ACTION_GET_CONTENT);
            //intent.setType("file/*");

            mContext.setDevice(device);
            mContext.connect();

            //((Activity) mContext).startActivityForResult(intent, 0);
        }
    }
}
