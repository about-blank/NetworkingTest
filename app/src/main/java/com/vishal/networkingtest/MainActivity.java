package com.vishal.networkingtest;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.RecoverySystem;
import android.support.annotation.IdRes;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Request;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import okhttp3.OkHttpClient;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    ImageView imageView;
    RadioButton radioUrl, radioOkHttp, radioPicasso;
    Button loadBtn;
    EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        radioUrl = (RadioButton) findViewById(R.id.radioButton_URL);
        radioOkHttp = (RadioButton) findViewById(R.id.radioButton_OKHTTP);
        radioPicasso = (RadioButton) findViewById(R.id.radioButton_PICASSO);

        editText = (EditText) findViewById(R.id.editTextURL);

        loadBtn = (Button) findViewById(R.id.load_btn);
        loadBtn.setOnClickListener(this);

        imageView = (ImageView) findViewById(R.id.imageView);

       loadBtn.requestFocus();
    }

    public boolean isNetworkConnectivityAvailable() {

        boolean available = false;

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if(networkInfo != null && networkInfo.isConnected()) {

            Log.v(getString(R.string.app_name), "Network Type : " + networkInfo.getType());
            available = true;
        }

        return available;
    }

    @Override
    public void onClick(View view) {

        String url = editText.getText().toString().trim();
        String option = null;

        if(view.getId() == R.id.load_btn)
        {
            if(!isNetworkConnectivityAvailable()) {

                Toast.makeText(this,"Network connection not available. Please check your connection.", Toast.LENGTH_SHORT).show();
                return;
            }
            if(url == null || url.isEmpty())
            {
                Toast.makeText(this,"Please specify a valid image URL to be loaded.", Toast.LENGTH_SHORT).show();
                return;
            }

            if(radioUrl.isChecked()) {
                option = "OPTION_HTTPURL";
            }
            else if(radioOkHttp.isChecked()) {
                option = "OPTION_OKHTTP";
            }
            else if(radioPicasso.isChecked()) {
                Picasso.with(this).load(url).placeholder(R.drawable.progress_animation).into(imageView);
            }

            if(option != null) {
                ImageDownloaderAsyncTask task = new ImageDownloaderAsyncTask();
                task.execute(option, url);
            }
        }
    }

    class ImageDownloaderAsyncTask extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Bitmap doInBackground(String... strings) {


            String option = strings[0];
            String url = strings[1];

            if(option.equals("OPTION_HTTPURL")) {

                try {
                    URL u = new URL(url);
                    URLConnection connection = u.openConnection();

                    InputStream in = connection.getInputStream();
                    Bitmap bitmap = BitmapFactory.decodeStream(in);
                    return bitmap;


                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else if(option.equals("OPTION_OKHTTP")) {

                try {
                    OkHttpClient client = new OkHttpClient();
                    okhttp3.Request request = new okhttp3.Request.Builder().url(url).build();
                    Response response = client.newCall(request).execute();
                    InputStream inputStream = response.body().byteStream();
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    return  bitmap;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);

            imageView.setImageBitmap(bitmap);
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }
    }
}
