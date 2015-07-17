package if1001.cin.ufpe.br.chat1001.ceboso.gui;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.ChannelListener;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import if1001.cin.ufpe.br.chat1001.R;
import if1001.cin.ufpe.br.chat1001.ceboso.wifi.WiFiDirectBroadcastReceiver;

public class WiFiDirectActivity extends Activity implements ChannelListener, DeviceListFragment.DeviceActionListener {

    public static final String TAG = "wifidirect";
    private WifiP2pManager manager;
    private boolean isWifiP2pEnabled = false;
    private boolean retryChannel = false;

    private final IntentFilter intentFilter = new IntentFilter();
    private Channel channel;
    private BroadcastReceiver receiver = null;

    public static Bundle myBundle = new Bundle();

    public void setIsWifiP2pEnabled(boolean isWifiP2pEnabled) {
        this.isWifiP2pEnabled = isWifiP2pEnabled;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        DeviceListFragment fragment = new DeviceListFragment();
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.container_body, fragment);
        fragmentTransaction.commit();

        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(this, getMainLooper(), null);
    }

    @Override
    public void onResume() {
        super.onResume();
        receiver = new WiFiDirectBroadcastReceiver(manager, channel, this);
        registerReceiver(receiver, intentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    public void replaceFragments() {
        DeviceMessageFragment fragment = new DeviceMessageFragment();
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.container_body, fragment);
        fragmentTransaction.commit();
    }

    public void resetData() {
        Fragment fragment = getFragmentManager().findFragmentById(R.id.container_body);
        DeviceListFragment fragmentList = null;
        DeviceMessageFragment fragmentDetails = null;

        if(fragment instanceof DeviceMessageFragment) {
            fragmentDetails = (DeviceMessageFragment) getFragmentManager()
                    .findFragmentById(R.id.container_body);
        }

        if(fragment instanceof DeviceListFragment) {
            fragmentList = (DeviceListFragment) getFragmentManager()
                    .findFragmentById(R.id.container_body);
        }

        if (fragmentList != null) {
            fragmentList.clearPeers();
        }
        if (fragmentDetails != null) {
            fragmentDetails.resetViews();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_items, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.atn_direct_discover:
                if (!isWifiP2pEnabled) {
                    Toast.makeText(WiFiDirectActivity.this, R.string.p2p_off_warning,
                            Toast.LENGTH_SHORT).show();
                    return true;
                }

                Fragment fragment = getFragmentManager().findFragmentById(R.id.container_body);

                if(fragment instanceof DeviceListFragment) {

                    final DeviceListFragment deviceListFragment = (DeviceListFragment) getFragmentManager()
                            .findFragmentById(R.id.container_body);
                    deviceListFragment.onInitiateDiscovery();
                    manager.discoverPeers(channel, new ActionListener() {

                        @Override
                        public void onSuccess() {
                            Toast.makeText(WiFiDirectActivity.this, "Busca iniciada",
                                    Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(int reasonCode) {
                            Toast.makeText(WiFiDirectActivity.this, "Busca falhou : " + reasonCode,
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void showDetails(WifiP2pDevice device) {

        Fragment fragment = getFragmentManager().findFragmentById(R.id.container_body);

        if(fragment instanceof DeviceMessageFragment) {
            DeviceMessageFragment deviceMessageFragment = (DeviceMessageFragment) getFragmentManager()
                    .findFragmentById(R.id.container_body);
            deviceMessageFragment.showDetails();
        }

    }



    @Override
    public void connect(WifiP2pConfig config) {
        manager.connect(channel, config, new ActionListener() {

            @Override
            public void onSuccess() {
            }

            @Override
            public void onFailure(int reason) {
                Toast.makeText(WiFiDirectActivity.this, "Conexão falhou. Tente novamente.",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void disconnect() {

        Fragment fragment = getFragmentManager().findFragmentById(R.id.container_body);

        if(fragment instanceof DeviceMessageFragment) {

            final DeviceMessageFragment deviceMessageFragment = (DeviceMessageFragment) getFragmentManager()
                    .findFragmentById(R.id.container_body);
            deviceMessageFragment.resetViews();
            manager.removeGroup(channel, new ActionListener() {

                @Override
                public void onFailure(int reasonCode) {
                    Log.d(TAG, "Disconnect failed. Reason :" + reasonCode);

                }

                @Override
                public void onSuccess() {
                }

            });
        }
    }

    @Override
    public void onChannelDisconnected() {
        if (manager != null && !retryChannel) {
            Toast.makeText(this, "Channel perdido. Tente novamente", Toast.LENGTH_LONG).show();
            resetData();
            retryChannel = true;
            manager.initialize(this, getMainLooper(), this);
        } else {
            Toast.makeText(this,
                    "Severe! Channel is probably lost premanently. Try Disable/Re-Enable P2P.",
                    Toast.LENGTH_LONG).show();
        }
    }
}
