package if1001.cin.ufpe.br.chat1001.ceboso.gui;

import android.app.ListFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import if1001.cin.ufpe.br.chat1001.R;
import if1001.cin.ufpe.br.chat1001.ceboso.gui.message.Message;
import if1001.cin.ufpe.br.chat1001.ceboso.gui.message.MessageAdapter;

public class DeviceListFragment extends ListFragment implements PeerListListener {

    private List<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();
    ProgressDialog progressDialog = null;
    View mContentView = null;
    private WifiP2pDevice device;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.setListAdapter(new WiFiPeerListAdapter(getActivity(), R.layout.row_devices, peers));

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContentView = inflater.inflate(R.layout.device_list, null);

        return mContentView;
    }

    public WifiP2pDevice getDevice() {
        return device;
    }

    private static String getDeviceStatus(int deviceStatus) {
        Log.d(WiFiDirectActivity.TAG, "Peer status :" + deviceStatus);
        switch (deviceStatus) {
            case WifiP2pDevice.AVAILABLE:
                return "Disponível";
            case WifiP2pDevice.INVITED:
                return "Convidado";
            case WifiP2pDevice.CONNECTED:
                return "Conectado";
            case WifiP2pDevice.FAILED:
                return "Falha";
            case WifiP2pDevice.UNAVAILABLE:
                return "Indisponível";
            default:
                return "Desconhecido";

        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {

        WifiP2pDevice device = (WifiP2pDevice) getListAdapter().getItem(position);

        WiFiDirectActivity.myBundle.putParcelable("DEVICE", device);

        ((DeviceListFragment.DeviceActionListener) getActivity()).disconnect();

        DeviceMessageFragment nextFrag = new DeviceMessageFragment();
        this.getFragmentManager().beginTransaction()
                .replace(R.id.container_body, nextFrag, null)
                .addToBackStack(null)
                .commit();

    }

    private class WiFiPeerListAdapter extends ArrayAdapter<WifiP2pDevice> {

        private List<WifiP2pDevice> items;

        public WiFiPeerListAdapter(Context context, int textViewResourceId,
                List<WifiP2pDevice> objects) {
            super(context, textViewResourceId, objects);
            items = objects;

        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater) getActivity().getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.row_devices, null);
            }
            WifiP2pDevice device = items.get(position);
            if (device != null) {
                TextView top = (TextView) v.findViewById(R.id.device_name);
                TextView bottom = (TextView) v.findViewById(R.id.device_details);
                if (top != null) {
                    top.setText(device.deviceName);
                }
                if (bottom != null) {
                    bottom.setText(getDeviceStatus(device.status));
                }
            }

            return v;

        }
    }

    public void updateThisDevice(WifiP2pDevice device) {
        this.device = device;
    }

    @Override
    public void onPeersAvailable(WifiP2pDeviceList peerList) {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        peers.clear();
        peers.addAll(peerList.getDeviceList());
        ((WiFiPeerListAdapter) getListAdapter()).notifyDataSetChanged();
        if (peers.size() == 0) {
            Log.d(WiFiDirectActivity.TAG, "No devices found");
            return;
        }

    }

    public void clearPeers() {
        peers.clear();
        ((WiFiPeerListAdapter) getListAdapter()).notifyDataSetChanged();
    }


    public void onInitiateDiscovery() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        progressDialog = ProgressDialog.show(getActivity(), "Clique na tela pra cancelar", "Buscando peers", true,
                true, new DialogInterface.OnCancelListener() {

                    @Override
                    public void onCancel(DialogInterface dialog) {
                        
                    }
                });
    }

    public interface DeviceActionListener {

        void showDetails(WifiP2pDevice device);

        void connect(WifiP2pConfig config);

        void disconnect();
    }

}
