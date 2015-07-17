package if1001.cin.ufpe.br.chat1001.ceboso.wifi;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.util.Log;

import if1001.cin.ufpe.br.chat1001.R;
import if1001.cin.ufpe.br.chat1001.ceboso.gui.DeviceMessageFragment;
import if1001.cin.ufpe.br.chat1001.ceboso.gui.DeviceListFragment;
import if1001.cin.ufpe.br.chat1001.ceboso.gui.WiFiDirectActivity;

public class WiFiDirectBroadcastReceiver extends BroadcastReceiver {

    private WifiP2pManager manager;
    private Channel channel;
    private WiFiDirectActivity activity;

    public WiFiDirectBroadcastReceiver(WifiP2pManager manager, Channel channel,
            WiFiDirectActivity activity) {
        super();
        this.manager = manager;
        this.channel = channel;
        this.activity = activity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {

            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                activity.setIsWifiP2pEnabled(true);
            } else {
                activity.setIsWifiP2pEnabled(false);
                activity.resetData();

            }
            Log.d(WiFiDirectActivity.TAG, "P2P state changed - " + state);
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {

            if (manager != null) {
                Fragment fragment = activity.getFragmentManager().findFragmentById(R.id.container_body);
                if(fragment instanceof DeviceListFragment) {
                    manager.requestPeers(channel, (PeerListListener) activity.getFragmentManager()
                            .findFragmentById(R.id.container_body));
                }
            }
            Log.d(WiFiDirectActivity.TAG, "P2P peers changed");
        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {

            if (manager == null) {
                return;
            }

            NetworkInfo networkInfo = (NetworkInfo) intent
                    .getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

            if (networkInfo.isConnected()) {

                Fragment fragment = activity.getFragmentManager().findFragmentById(R.id.container_body);
                if(fragment instanceof DeviceMessageFragment){
                    DeviceMessageFragment deviceMessageFragment = (DeviceMessageFragment) activity.getFragmentManager().findFragmentById(R.id.container_body);

                    manager.requestConnectionInfo(channel, deviceMessageFragment);
                }

            } else {
                activity.resetData();
            }
        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {

            Fragment fragment = activity.getFragmentManager().findFragmentById(R.id.container_body);
            if(fragment instanceof DeviceListFragment){
                DeviceListFragment deviceListFragment = (DeviceListFragment) activity.getFragmentManager()
                        .findFragmentById(R.id.container_body);
                deviceListFragment.updateThisDevice((WifiP2pDevice) intent.getParcelableExtra(
                        WifiP2pManager.EXTRA_WIFI_P2P_DEVICE));
            }

        }
    }
}
