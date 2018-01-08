package com.example.yousafsaleem.buslocation;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Created by Yousaf on 5/16/2016.
 */
public class BusAsync extends AsyncTask<String,String,String> {
    Context ctx;
    ProgressDialog dialog;

    public BusAsync(Context ctx)
    {
        this.ctx=ctx;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        dialog = new ProgressDialog(ctx);
        dialog.setMessage("please wait...");
        dialog.setCancelable(false);
        dialog.show();
    }

    @Override
    protected String doInBackground(String... params) {
        String reg_Url="http://demo.lbspak.com/BTS/yousaf/busLocation.php";

        String loc=params[0];

        try {
            URL url=new URL(reg_Url);
            HttpURLConnection httpURLConnection=(HttpURLConnection)url.openConnection();
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setDoOutput(true);
            OutputStream oS=httpURLConnection.getOutputStream();
            BufferedWriter bufferedWriter=new BufferedWriter(new OutputStreamWriter(oS,"UTF-8"));
            //AS WE USING POST SO ENDCODE DATA FIRST
            String data= URLEncoder.encode("loc","UTF-8")+"="+URLEncoder.encode(loc,"UTF-8");

            bufferedWriter.write(data);
            bufferedWriter.flush();
            bufferedWriter.close();
            oS.close();
            InputStream iS=httpURLConnection.getInputStream();
            iS.close();

            return "Reg Success";
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        return "Error Occured";
    }

    @Override
    protected void onPostExecute(String result) {
        dialog.cancel();
        Toast.makeText(ctx,result,Toast.LENGTH_LONG).show();
    }

}

