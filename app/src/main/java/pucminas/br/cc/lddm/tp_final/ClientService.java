package pucminas.br.cc.lddm.tp_final;

import android.app.IntentService;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Created by josue on 07/06/15.
 */
public class ClientService extends IntentService {


    private int port;
    private File fileToSend;
    private String address;


    public ClientService() {
        super("ClientService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        port = (Integer) intent.getExtras().get("port");
        //fileToSend = (File) intent.getExtras().get("file");
        address = (String) intent.getExtras().get("address");

        Socket clientSocket = new Socket();
        OutputStream os = null;

        try {
            clientSocket.bind(null);
            clientSocket.connect((new InetSocketAddress(address, port)), 5000);

//            PrintWriter pw = new PrintWriter(os);
//
//            InputStream is = clientSocket.getInputStream();
//
//            InputStreamReader isr = new InputStreamReader(is);
//            BufferedReader br = new BufferedReader(isr);
//
//            FileInputStream fis = new FileInputStream(fileToSend);
//            BufferedInputStream bis = new BufferedInputStream(fis);
//
//            byte[] buffer = new byte[4096];
//
//            Log.d("Wp2p", "Uploading file...");
//            while (true) {
//
//                int bytesRead = bis.read(buffer, 0, buffer.length);
//
//                if (bytesRead == -1) {
//                    break;
//                }
//
//                os.write(buffer, 0, bytesRead);
//                os.flush();
//            }
//
//            fis.close();
//            bis.close();
//
//            br.close();
//            isr.close();
//            is.close();
//
//            pw.close();
//            os.close();

            clientSocket.close();

            Log.d("Wp2p", "File uploaded...");

        } catch (IOException e) {
            Log.d("Wp2p", "Erro in Socket Client");
            Log.d("Wp2p", e.getMessage());
            e.printStackTrace();
        }

    }
}
