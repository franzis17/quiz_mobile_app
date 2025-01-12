package curtin.edu.math_test.activities;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

import curtin.edu.math_test.R;
import curtin.edu.math_test.models.MathTestSystem;
import curtin.edu.math_test.utils.ServerConnections;
import curtin.edu.math_test.utils.UserInterface;

public class BrowsePhotosOnline extends AppCompatActivity {

    private final Activity THIS_ACTIVITY = BrowsePhotosOnline.this;

    public static final int NUM_GRIDS_PER_ROW = 3;

    private MathTestSystem mathTestSystem;

    private ProgressBar progressBar;
    private RecyclerView onlinePhotosRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse_photos_online);

        mathTestSystem = MathTestSystem.getInstance(THIS_ACTIVITY);

        progressBar = findViewById(R.id.progressBar);
        onlinePhotosRecyclerView = findViewById(R.id.onlinePhotosRecyclerView);
        EditText searchInput = findViewById(R.id.searchInput);

        progressBar.setVisibility(View.INVISIBLE);

        Button backButton = findViewById(R.id.backButton);
        Button searchButton = findViewById(R.id.searchButton);

        backButton.setOnClickListener(view -> finish());
        searchButton.setOnClickListener(view -> {
            progressBar.setVisibility(View.VISIBLE);
            String searchValues = searchInput.getText().toString();
            new PictureRetrievalTask().execute(searchValues);
        });
    }

    /**
     *  Downloads Array of Images from the Web, based on search input and displays on the screen
     */
    public class PictureRetrievalTask extends AsyncTask<String, Void, ArrayList<Bitmap>> {

        @Override
        protected ArrayList<Bitmap> doInBackground(String... strings) {
            ArrayList<Bitmap> imgBitmaps = null;

            try {
                String siteData = searchRemoteAPI(strings[0]);                // Site data results from the search
                if (siteData != null) {
                    ArrayList<String> imageUrl = getImageLargeUrl(siteData);  // Image data from the site data
                    if (imageUrl != null) {
                        imgBitmaps = getImageFromUrl(imageUrl);               // Image from image data
                    }
                }
            } catch (MalformedURLException e) {
                Log.e("MalformedURLException", e.getMessage());
                e.printStackTrace();
            } catch (IOException e) {
                Log.e("IOException", e.getMessage());
                e.printStackTrace();
            } catch (GeneralSecurityException e) {
                Log.e("GeneralSecurityException", e.getMessage());
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                Log.e("IllegalArgumentException", e.getMessage());
                e.printStackTrace();
            }

            return imgBitmaps;
        }

        @Override
        protected void onPostExecute(ArrayList<Bitmap> imgBitmaps) {
            if (imgBitmaps != null) {
                progressBar.setVisibility(View.INVISIBLE);
                displayAllImages(imgBitmaps);
            }
        }

        /**
         *  Searches through the Remote API of the Site and get raw site data from
         *
         *  @param   searchKey  used for building the remote URL to connect to the site data
         *  @return  String     data of the site to search the image from and parse to JSON
         *  @throws  IOException               IO Related
         *  @throws  GeneralSecurityException  DownloadUtils class throws when adding certificate
         *  @throws  IllegalArgumentException  When no site data is returned upon downloading the
         *                                     String
         */
        private String searchRemoteAPI(String searchKey)
            throws IOException, GeneralSecurityException, IllegalArgumentException {

            String siteData = null;

            String urlString = buildRemoteURL(searchKey);
            HttpsURLConnection connection = ServerConnections.openConnection(urlString);

            if (connection == null) {
                UserInterface.toastNotifyUser(THIS_ACTIVITY, "Check Internet");
            } else if (ServerConnections.noGoodConnection(connection)) {
                UserInterface.toastNotifyUser(THIS_ACTIVITY, "Problems with downloading");
            } else {
                siteData = downloadToString(connection);
                if (siteData != null) {
                    Log.d("Site Data", siteData);
                } else {
                    throw new IllegalArgumentException("No site data returned");
                }
                connection.disconnect();
            }

            return siteData;
        }

        /**
         *  Parse JSON data of the siteData to the url of the image to be viewed
         *
         *  @param   siteData  parse the image of the 1st image of what was found
         *  @return  String    the url of the 1st image found
         */
        private ArrayList<String> getImageLargeUrl(String siteData) {
            ArrayList<String> imageUrl = new ArrayList<>();

            try {
                // Get JSON Object and the amount of pictures "hits"
                JSONObject jBase = new JSONObject(siteData);
                JSONArray jHits = jBase.getJSONArray("hits");

                if (jHits.length() > 0) {

                    // Get all URL's of the images found
                    for (int i = 0; i < jHits.length(); i++) {
                        JSONObject jHitsItem = jHits.getJSONObject(i);
                        String url = jHitsItem.getString("largeImageURL");
                        Log.d("ImageURL", url);
                        imageUrl.add(url);

                        // Don't fetch any more than 50 images
                        if (i == 49) {
                            break;
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return imageUrl;
        }

        /**
         *  Converts the imageUrl in to a Bitmap so that it can be displayed in the ImageView
         *
         *  @param   imageUrl  contains all URL of the images found within the whole site
         *  @return  Bitmap    Array of images that will be displayed
         *  @throws  IOException               Issues with connection or adding certificate
         *  @throws  GeneralSecurityException  Issues with adding certificate
         */
        private ArrayList<Bitmap> getImageFromUrl(ArrayList<String> imageUrl)
            throws IOException, GeneralSecurityException, IllegalArgumentException {
            ArrayList<Bitmap> imgBitmaps = new ArrayList<>();

            // Parse the url for each imageUrl and download them to be stored in imgBitmaps
            for (int i = 0; i < imageUrl.size(); i++) {
                Uri.Builder url = Uri.parse(imageUrl.get(i)).buildUpon();
                String urlString = url.build().toString();
                Log.d("ImageUrl", urlString);

                HttpsURLConnection connection = ServerConnections.openConnection(urlString);

                if (connection == null) {
                    UserInterface.toastNotifyUser(THIS_ACTIVITY, "Check internet");
                } else if (ServerConnections.noGoodConnection(connection)) {
                    UserInterface.toastNotifyUser(THIS_ACTIVITY, "Problem with downloading");
                } else {
                    Bitmap bitmapImg = downloadToBitmap(connection);
                    if (bitmapImg == null) {
                        throw new IllegalArgumentException("Nothing returned");
                    }
                    imgBitmaps.add(bitmapImg);
                    connection.disconnect();
                }
            }

            return imgBitmaps;
        }

        /**
         *  Builds a Remote URL to connect to and search images from
         *
         *  @param   searchKey  used to search Pixabay given the input key
         *  @return  String     URL String to connect to
         */
        private String buildRemoteURL(String searchKey) {
            String urlString = "";

            Uri.Builder url = Uri.parse("https://pixabay.com/api/").buildUpon();
            url.appendQueryParameter("key", "19544963-7a08c2fc66f4560b6acc43988");
            url.appendQueryParameter("q", searchKey);
            urlString = url.build().toString();
            Log.d("URL", "pictureRetrievalTask: " + urlString);

            return urlString;
        }

        private String downloadToString(HttpsURLConnection connection) throws IOException {
            String data = null;
            InputStream inputStream = connection.getInputStream();

            byte[] byteData = IOUtils.toByteArray(inputStream);
            data = new String(byteData, StandardCharsets.UTF_8);

            inputStream.close();
            return data;
        }

        private Bitmap downloadToBitmap(HttpsURLConnection connection) throws IOException {
            Bitmap imageData = null;
            InputStream inputStream = connection.getInputStream();

            byte[] byteData = getByteArrayFromInputStream(inputStream);
            imageData = BitmapFactory.decodeByteArray(byteData, 0, byteData.length);

            inputStream.close();
            return imageData;
        }

        private byte[] getByteArrayFromInputStream(InputStream inputStream) throws IOException {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            int nRead;                      // How much byte is read
            byte[] data = new byte[4096];   // 4 KB Chunk
            int progress = 0;

            // Read data from the input stream
            while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
                outputStream.write(data, 0, nRead);
                progress = progress + nRead;
            }

            outputStream.close();
            return outputStream.toByteArray();
        }

    }  // END OF PictureRetrievalTask Class

    /**
     *  Called after all images has been downloaded from the PictureRetrievalTask
     *  Calls the Fragment which displays photos in a grid
     *
     *  @param imgBitmaps    Array of Bitmaps containing all the downloaded images
     */
    private void displayAllImages(ArrayList<Bitmap> imgBitmaps) {
        onlinePhotosRecyclerView.setLayoutManager(new GridLayoutManager(
            THIS_ACTIVITY, NUM_GRIDS_PER_ROW, GridLayoutManager.VERTICAL, false
        ));
        onlinePhotosRecyclerView.setAdapter(new DisplayPhotosAdapter(imgBitmaps));
    }

    /**
     *  View Holder for Displaying Photos
     */
    public class DisplayPhotosViewHolder extends RecyclerView.ViewHolder {

        protected ImageView onlineImageView;

        public DisplayPhotosViewHolder(@NonNull View itemView, ViewGroup parent) {
            super(itemView);

            onlineImageView = itemView.findViewById(R.id.onlineImageView);

            int size = (parent.getMeasuredHeight() / 3) + 1;
            ViewGroup.LayoutParams layoutParams = itemView.getLayoutParams();
            layoutParams.height = size;
        }

        public void bind(Bitmap imageBitmap) {
            onlineImageView.setImageBitmap(imageBitmap);

            // Store selected photo to be displayed on registration
            onlineImageView.setOnClickListener(view -> mathTestSystem.setOnlinePhoto(imageBitmap));
        }

    }  // END OF DisplayPhotosViewHolder

    /**
     *  Adapter for Displaying Photos
     */
    public class DisplayPhotosAdapter extends RecyclerView.Adapter<DisplayPhotosViewHolder> {

        private ArrayList<Bitmap> imgBitmaps;

        public DisplayPhotosAdapter(ArrayList<Bitmap> imgBitmaps) {
            this.imgBitmaps = imgBitmaps;
        }

        @NonNull
        @Override
        public DisplayPhotosViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            View itemView = layoutInflater.inflate(R.layout.list_images, parent, false);
            return new DisplayPhotosViewHolder(itemView, parent);
        }

        @Override
        public void onBindViewHolder(@NonNull DisplayPhotosViewHolder viewHolder, int position) {
            viewHolder.bind(imgBitmaps.get(position));
        }

        @Override
        public int getItemCount() {
            return imgBitmaps.size();
        }

    }  // END OF DisplayPhotosAdapter

}