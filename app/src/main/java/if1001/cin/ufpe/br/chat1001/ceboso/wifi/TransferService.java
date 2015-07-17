package if1001.cin.ufpe.br.chat1001.ceboso.wifi;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import if1001.cin.ufpe.br.chat1001.ceboso.gui.WiFiDirectActivity;

public class TransferService extends IntentService {

	private static final int SOCKET_TIMEOUT = 5000;
	public static final String SEND_MSG = "if1001.cin.ufpe.br.SEND_MSG";
	public static final String EXTRAS_MSG = "message";
	public static final String EXTRAS_ADDRESS = "go_host";
	public static final String EXTRAS_PORT = "go_port";

	public TransferService(String name) {
		super(name);
	}

	public TransferService() {
		super("TransferService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {

		Context context = getApplicationContext();
		if (intent.getAction().equals(SEND_MSG)) {
			String message = intent.getExtras().getString(EXTRAS_MSG);
			String host = intent.getExtras().getString(EXTRAS_ADDRESS);
			Socket socket = new Socket();
			int port = intent.getExtras().getInt(EXTRAS_PORT);

			try {
				Log.d(WiFiDirectActivity.TAG, "Opening client socket - ");
				socket.bind(null);
				socket.connect((new InetSocketAddress(host, port)), SOCKET_TIMEOUT);

				Log.d(WiFiDirectActivity.TAG, "Client socket - " + socket.isConnected());
				OutputStream stream = socket.getOutputStream();
				InputStream is = new ByteArrayInputStream(message.getBytes("UTF-8"));

				Utils.pipeStreams(is, stream);

				Log.d(WiFiDirectActivity.TAG, "Client: Data written");
			} catch (IOException e) {
				Log.e(WiFiDirectActivity.TAG, e.getMessage());
			} finally {
				if (socket != null) {
					if (socket.isConnected()) {
						try {
							socket.close();
						} catch (IOException e) {
							// Give up
							e.printStackTrace();
						}
					}
				}
			}

		}
	}
}
