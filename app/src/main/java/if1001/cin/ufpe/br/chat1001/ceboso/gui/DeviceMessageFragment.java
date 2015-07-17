package if1001.cin.ufpe.br.chat1001.ceboso.gui;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import if1001.cin.ufpe.br.chat1001.R;
import if1001.cin.ufpe.br.chat1001.ceboso.gui.message.Message;
import if1001.cin.ufpe.br.chat1001.ceboso.gui.message.MessageAdapter;
import if1001.cin.ufpe.br.chat1001.ceboso.wifi.TransferService;
import if1001.cin.ufpe.br.chat1001.ceboso.wifi.Utils;


public class DeviceMessageFragment extends Fragment implements ConnectionInfoListener {

	public static final String IP_SERVER = "192.168.49.1";
	public static int PORT = 8988;
	private static boolean server_running = false;

	protected static final int CHOOSE_FILE_RESULT_CODE = 20;
	private View mContentView = null;
	private WifiP2pDevice device;
	private WifiP2pInfo info;
	ProgressDialog progressDialog = null;

    private List<Message> mListMessage;
    private ListView mListViewMessage;
    private MessageAdapter mChatMessageAdapter;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mContentView = inflater.inflate(R.layout.device_message, null);

        this.device = WiFiDirectActivity.myBundle.getParcelable("DEVICE");
        mListMessage = new ArrayList<Message>();

        mListViewMessage = (ListView) mContentView.findViewById(R.id.listViewMessage);

        mChatMessageAdapter = new MessageAdapter(getActivity(), mListMessage);
        mListViewMessage.setAdapter(mChatMessageAdapter);

		mContentView.findViewById(R.id.btn_connect).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                WifiP2pConfig config = new WifiP2pConfig();
                config.deviceAddress = device.deviceAddress;
                config.wps.setup = WpsInfo.PBC;
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                progressDialog = ProgressDialog.show(getActivity(), "Clique na tela pra cancelar",
                        "Conectando com :" + device.deviceName, true, true
                );
                ((DeviceListFragment.DeviceActionListener) getActivity()).connect(config);

            }
        });

		mContentView.findViewById(R.id.btn_disconnect).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        ((DeviceListFragment.DeviceActionListener) getActivity()).disconnect();
                    }
                });

		mContentView.findViewById(R.id.btn_start_client).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        EditText textMessage = (EditText) mContentView.findViewById(R.id.text);
                        sendMessage(textMessage.getText().toString());

                        Message message = new Message(textMessage.getText().toString(), Message.TypeMessage.SENT_MESSAGE);

                        mListMessage.add(message);
                        mChatMessageAdapter.notifyDataSetChanged();

                        textMessage.setText("");
                    }
                });

		return mContentView;
	}

    public void sendMessage(String message) {
        String localIP = Utils.getLocalIPAddress();
        // Trick to find the ip in the file /proc/net/arp
        Log.i("info222", device+"");
        String client_mac_fixed = new String(device.deviceAddress).replace("99", "19");
        String clientIP = Utils.getIPFromMac(client_mac_fixed);


        TextView statusText = (TextView) mContentView.findViewById(R.id.status_text);
        statusText.setText(message);
        Log.d(WiFiDirectActivity.TAG, "Intent----------- " + message);
        Intent serviceIntent = new Intent(getActivity(), TransferService.class);
        serviceIntent.setAction(TransferService.SEND_MSG);
        serviceIntent.putExtra(TransferService.EXTRAS_MSG, message);

        if(localIP.equals(IP_SERVER)){
            serviceIntent.putExtra(TransferService.EXTRAS_ADDRESS, clientIP);
        }else{
            serviceIntent.putExtra(TransferService.EXTRAS_ADDRESS, IP_SERVER);
        }

        serviceIntent.putExtra(TransferService.EXTRAS_PORT, PORT);
        getActivity().startService(serviceIntent);
    }

	@Override
	public void onConnectionInfoAvailable(final WifiP2pInfo info) {

        //((WiFiDirectActivity) getActivity()).replaceFragments();

		if (progressDialog != null && progressDialog.isShowing()) {
			progressDialog.dismiss();
		}
		this.info = info;

		if (!server_running){
			new ServerAsyncTask(getActivity(), mContentView.findViewById(R.id.status_text), mListMessage, mChatMessageAdapter).execute();
			server_running = true;
		}

		mContentView.findViewById(R.id.btn_connect).setVisibility(View.GONE);
        mContentView.findViewById(R.id.btn_disconnect).setVisibility(View.VISIBLE);
	}

	public void showDetails() {
		this.device = WiFiDirectActivity.myBundle.getParcelable("DEVICE");

	}

	public void resetViews() {
		mContentView.findViewById(R.id.btn_connect).setVisibility(View.VISIBLE);
		mContentView.findViewById(R.id.btn_disconnect).setVisibility(View.GONE);
	}

	public static class ServerAsyncTask extends AsyncTask<Void, Void, String> {

		private final Context context;
		private final TextView statusText;
        private final List<Message> listMessage;
        private MessageAdapter mChatMessageAdapter;

		public ServerAsyncTask(Context context, View statusText, List<Message> listMessage, MessageAdapter mChatMessageAdapter) {
			this.context = context;
			this.statusText = (TextView) statusText;
            this.listMessage = listMessage;
            this.mChatMessageAdapter = mChatMessageAdapter;
		}

		@Override
		protected String doInBackground(Void... params) {
			try {
				ServerSocket serverSocket = new ServerSocket(PORT);
				Log.d(WiFiDirectActivity.TAG, "Server: Socket opened");
				Socket client = serverSocket.accept();
				Log.d(WiFiDirectActivity.TAG, "Server: connection done");

                String message = readMessage(client.getInputStream());

				serverSocket.close();
				server_running = false;
				return message;
			} catch (IOException e) {
				Log.e(WiFiDirectActivity.TAG, e.getMessage());
				return null;
			}
		}

		@Override
		protected void onPostExecute(String result) {
			if (result != null) {
				statusText.setText(result);
                Message message = new Message(result, Message.TypeMessage.RECEIVED_MESSAGE);

                listMessage.add(message);

                mChatMessageAdapter.notifyDataSetChanged();
			}

		}

		@Override
		protected void onPreExecute() {
			statusText.setText("Abrindo socket do servidor");
		}

        private String readMessage(InputStream inputStream) {
            StringBuilder out = new StringBuilder();
            char[] buffer = new char[1024];
            try {
                Reader in = new InputStreamReader(inputStream, "UTF-8");
                for (int count = 0; (count = in.read(buffer, 0, buffer.length)) > -1; ) {
                    out.append(buffer, 0, count);

                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


            return out.toString();
        }

	}

}
