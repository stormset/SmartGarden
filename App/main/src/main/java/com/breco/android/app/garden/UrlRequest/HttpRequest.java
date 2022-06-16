package com.breco.android.app.garden.UrlRequest;

import android.os.AsyncTask;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by stormset on 2016. 04. 28.
 */

public class HttpRequest {
    public static class HttpGetRequest extends AsyncTask<String, Void, String> {
        public AsyncResponse delegate = null;
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .build();

        public HttpGetRequest(AsyncResponse delegate) {
            this.delegate = delegate;
        }

        @Override
        protected String doInBackground(String... params) {
            String returnValue = null;
            Request request = new Request.Builder()
                    .url(params[0])
                    .build();

            Response response = null;
            try {
                response = client.newCall(request).execute();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (response != null && response.body() != null) {
                    returnValue = response.body().string();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return returnValue;
        }

        @Override
        protected void onPostExecute(String result) {
            delegate.processFinish(result);
        }

        public interface AsyncResponse {
            void processFinish(String output);
        }
    }
}
