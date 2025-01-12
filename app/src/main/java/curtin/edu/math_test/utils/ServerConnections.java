package curtin.edu.math_test.utils;

import android.util.Log;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 *  Utility Class for server connections interactions
 */
public class ServerConnections {

    /**
     * Establishes connection with a server based on the url
     *
     * @param urlString used for opening up the connection
     * @return HttpsURLConnection    to interact with the server
     */
    public static HttpsURLConnection openConnection(String urlString) {
        HttpsURLConnection httpsConnection = null;

        try {
            URL url = new URL(urlString);
            httpsConnection = (HttpsURLConnection) url.openConnection();
        } catch (MalformedURLException e) {
            Log.e("MalformedURLException", e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            Log.e("IOException", e.getMessage());
            e.printStackTrace();
        }

        return httpsConnection;
    }

    /**
     * Checks if there are any problems with the connection to the server
     *
     * @param conn checks if it is ok to continue downloading data from server
     * @return true    when the connection is not ok
     */
    public static boolean noGoodConnection(HttpURLConnection conn) {
        try {
            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                return true;
            }
        } catch (IOException e) {
            Log.e("IOException", e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

}
