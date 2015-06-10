package pucminas.br.cc.lddm.tp_final;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by josue on 07/06/15.
 */
public class ServerService extends IntentService {

    private int port;
    private File saveLocation;

    public ServerService() {
        super("ServerService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        port = ((Integer) intent.getExtras().get("port")).intValue();
        saveLocation = (File) intent.getExtras().get("target");
        ServerSocket ss = null;
        Socket s = null;

        Log.d("Wp2p", "Loading Sever..");
        try {
            ss = new ServerSocket(port);

            // Listen for connections
            s = ss.accept();
            Log.d("Wp2p", "Server wating for connections");

            InputStream is = s.getInputStream();
            String saveAs = "wp2pfile" + System.currentTimeMillis();
            File file = new File(saveLocation, saveAs);

            FileOutputStream fos = new FileOutputStream(file);
            BufferedOutputStream bos = new BufferedOutputStream(fos);

            byte[] buffer = new byte[4096];
            int bytesRead;

            Log.d("Wp2p", "Downloading file...");
            while(true) {
                bytesRead = is.read(buffer, 0, buffer.length);

                if(bytesRead == -1) break;

                bos.write(buffer, 0, bytesRead);
                bos.flush();
            }

            bos.close();
            s.close();
            Log.d("Wp2p", "Downloading complete...");

        } catch (IOException e) {
            Log.d("Wp2p", "Error in Socket Server");
            Log.d("Wp2p", e.getMessage());
            e.printStackTrace();
        }
    }
}
