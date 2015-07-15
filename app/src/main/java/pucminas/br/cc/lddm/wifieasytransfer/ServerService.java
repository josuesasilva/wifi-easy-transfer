package pucminas.br.cc.lddm.wifieasytransfer;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

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

import pucminas.br.cc.lddm.tp_final.R;

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

        NotificationManager mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);;
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext());

        mBuilder.setContentTitle("Download file")
                .setContentText("Download in progress")
                .setSmallIcon(R.drawable.cellphone_img);


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

            //byte[] buffer = new byte[4096];
            byte[] buffer = new byte[1024];
            int bytesRead;
            int c = 0;

            Log.d("Wp2p", "Downloading file...");

            // start notification
            mBuilder.setContentTitle("Download file")
                    .setContentText("Download in progress")
                    .setSmallIcon(R.drawable.cellphone_img);
            mBuilder.setProgress(0, 0, true);

            while(true) {
                bytesRead = is.read(buffer, 0, buffer.length);

                if(bytesRead == -1) break;

                bos.write(buffer, 0, bytesRead);
                bos.flush();

                c++;
                Log.d("Wp2p", "write part -> "+c);
            }

            bos.close();
            s.close();
            ss.close();

            Log.d("Wp2p", "Download complete...");

            mBuilder.setContentText("Download complete");

            // Removes the progress bar
            mBuilder.setProgress(0, 0, false);
            mNotifyManager.notify(1, mBuilder.build());

            Toast.makeText(getApplicationContext(), "Download complete", Toast.LENGTH_SHORT).show();

        } catch (IOException e) {
            Log.d("Wp2p", "Error in Socket Server");
            Log.d("Wp2p", e.getMessage());
            e.printStackTrace();
        }
    }

}
