package com.efolx.enrouteqnh;

import android.graphics.Color;
import android.os.AsyncTask;
import android.widget.TextView;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by Metti on 17.05.2015.
 */
public class FTPTask extends AsyncTask<String,Void,String> {

    private TextView qnhTextView;

    public FTPTask(TextView qnhTextView) {
        this.qnhTextView = qnhTextView;
    }

    @Override
    protected String doInBackground(String... airportId) {
        String baseURL = "tgftp.nws.noaa.gov";
        String remoteDirectory="/data/observations/metar/stations/";
        FTPClient client = new FTPClient();
        StringBuffer result = new StringBuffer();
        try {
            client.connect(baseURL);
            client.login("anonymous", "anonymous");
            client.enterLocalPassiveMode();
            client.setFileType(FTP.ASCII_FILE_TYPE);
            String remoteString = remoteDirectory+airportId[0] + ".TXT";
            InputStream qnhStream = client.retrieveFileStream(remoteString);
            if(qnhStream!=null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(qnhStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                client.disconnect();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result.toString();

    }
    @Override
    protected void onPostExecute(String result) {
        qnhTextView.setTextColor(Color.BLACK);
        qnhTextView.setText(result);
    }


}
