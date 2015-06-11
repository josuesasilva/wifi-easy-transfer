package pucminas.br.cc.lddm.tp_final;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Created by josue on 07/06/15.
 */
public class ClientService extends IntentService {


    private int port;
    private File fileToSend;
    private String address;
    private String data;

    public ClientService() {
        super("ClientService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        port = (Integer) intent.getExtras().get("port");
        //fileToSend = (File) intent.getExtras().get("file");
        data = (String) intent.getExtras().get("data");
        address = (String) intent.getExtras().get("address");

        if (data != null && !data.equals("-")) {
            Context context = getApplicationContext();

            Socket clientSocket = new Socket();
            OutputStream os = null;

            try {
                clientSocket.bind(null);
                clientSocket.connect((new InetSocketAddress(address, port)), 5000);

                // send file
                OutputStream stream = clientSocket.getOutputStream();

                ContentResolver cr = context.getContentResolver();
                InputStream is = null;

                try {
                    is = cr.openInputStream(Uri.parse(data));
                } catch (FileNotFoundException e) {
                    Log.d("Wp2p", e.toString());
                }

                NotificationManager mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);;
                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext());

                // start notification
                mBuilder.setContentTitle("Upload file")
                        .setContentText("Upload in progress")
                        .setSmallIcon(R.drawable.cellphone_img);
                mBuilder.setProgress(0, 0, true);

                byte buf[] = new byte[1024];
                //byte buf[] = new byte[4096];
                int len;
                int c = 0;

                try {

                    while ((len = is.read(buf)) != -1) {
                        stream.write(buf, 0, len);
                        c++;
                        Log.d("Wp2p", "send part -> "+c);
                    }

                    stream.close();
                    is.close();

                } catch (IOException e) {
                    Log.d("Wp2p", e.toString());
                }

                mBuilder.setContentText("Upload complete");

                // Removes the progress bar
                mBuilder.setProgress(0, 0, false);
                mNotifyManager.notify(1, mBuilder.build());

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
//            os.close();

                clientSocket.close();

                Log.d("Wp2p", "File uploaded...");
                Toast.makeText(getApplicationContext(), "File uploaded", Toast.LENGTH_SHORT).show();


            } catch (IOException e) {
                Log.d("Wp2p", "Erro in Socket Client");
                Log.d("Wp2p", e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
