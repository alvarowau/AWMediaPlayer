package org.alvarowau.awmediaplayer;

import android.os.AsyncTask;
import java.net.HttpURLConnection;
import java.net.URL;

public class CheckUrlTask extends AsyncTask<String, Void, Boolean> {

    private OnUrlCheckedListener listener;

    public CheckUrlTask(OnUrlCheckedListener listener) {
        this.listener = listener;
    }

    @Override
    protected Boolean doInBackground(String... urls) {
        String urlString = urls[0];
        if (urlString.isEmpty()) return false;

        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000); // Tiempo de espera de 5 segundos
            connection.connect();

            // Comprobamos si el tipo de contenido es un archivo de video
            String contentType = connection.getContentType();
            return contentType != null && contentType.startsWith("video/");
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean isVideo) {
        if (listener != null) {
            listener.onUrlChecked(isVideo);
        }
    }

    public interface OnUrlCheckedListener {
        void onUrlChecked(boolean isVideo);
    }
}
