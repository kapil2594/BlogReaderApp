package project.com.blogreader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import static android.R.layout.simple_list_item_1;

public class MainListActivity extends ListActivity {

    protected String[] mBlogPostTitles;
    public static final int NUMBER_OF_POSTS = 30;
    public static final String TAG = MainListActivity.class.getSimpleName();
    protected JSONObject mBlogData;
    //protected ListView listView = (ListView) findViewById(android.R.id.list);
   public ProgressBar MprogressBar;
    private static String Key_title = "title";
    private static String Key_author = "author";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

       // ActionBar actionBar = getActionBar();
       // actionBar.show();

        MprogressBar = (ProgressBar) findViewById(R.id.progressBar);

        if (isNetworkAvailable()) {
            GetBlogPostsTask getBlogPostsTask = new GetBlogPostsTask();
            getBlogPostsTask.execute();
        } else {
            Toast.makeText(this, "Network is unavailable!", Toast.LENGTH_LONG).show();
        }

        //Toast.makeText(this, getString(R.string.no_items), Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
            super.onListItemClick(l, v, position, id);
            try
            {
                JSONArray jsonArray = mBlogData.getJSONArray("posts");
                JSONObject jsonObject = jsonArray.getJSONObject(position);
                String blogURL = jsonObject.getString("url");
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(blogURL));
                startActivity(intent);

            }
            catch (JSONException e)
            {
                Log.e(TAG,"Exception Caught",e);
            }
    }

    private boolean isNetworkAvailable() {
      MprogressBar.setVisibility(View.VISIBLE);
        MprogressBar.setProgress(0);
        MprogressBar.setMax(100);
        ConnectivityManager manager = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();

        boolean isAvailable = false;
        if (networkInfo != null && networkInfo.isConnected()) {
            isAvailable = true;
        }

        return isAvailable;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    public void updateList() {
        MprogressBar.setVisibility(View.INVISIBLE);
        if(mBlogData==null)
        {

            TextView textView = (TextView)findViewById(R.id.textView);
            textView.setText("No Item to Display");
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Sorry!!!");
            builder.setMessage("Some Error in Connection");
            builder.setPositiveButton(android.R.string.ok, null);
          AlertDialog dialog = builder.create();
           dialog.show();

        }
        else
        {
            try {
                JSONArray jsonArray = mBlogData.getJSONArray("posts");
                //mBlogPostTitles = new String[jsonArray.length()];
                ArrayList<HashMap<String,String>> blogPosts = new ArrayList<HashMap<String, String>>();
                for(int i=0;i<jsonArray.length();i++)
                {
                    JSONObject post = jsonArray.getJSONObject(i);
                    String title = post.getString("title");
                    title = Html.fromHtml(title).toString();
                   // mBlogPostTitles[i] = title;
                    String author = post.getString("author");
                    author = Html.fromHtml(title).toString();
                    HashMap<String,String> blogpost = new HashMap<String,String>();
                    blogpost.put(Key_title,title);
                    blogpost.put(Key_author,author);
                    blogPosts.add(blogpost);
                }
                String[] keys = {Key_title,Key_author};
                int[] ids = {android.R.id.text1,android.R.id.text2};
                SimpleAdapter adapter = new SimpleAdapter(this,blogPosts,android.R.layout.simple_list_item_2,keys,ids);
               //ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,simple_list_item_1,mBlogPostTitles);
                setListAdapter(adapter);
                Log.d(TAG,mBlogData.toString(2));
            }
            catch(Exception e)
            {
                Log.e(TAG,"Exception caught",e);

            }

        }
    }


    //Async Task MultiThreading to run the task to get information from url simultaneously



    private class GetBlogPostsTask extends AsyncTask<Object, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(Object... arg0) {
            int responseCode = -1;
            JSONObject jsonResponse = null;
            try
            {
                     URL blogFeedUrl = new URL("http://blog.teamtreehouse.com/api/get_recent_summary/?count="+NUMBER_OF_POSTS);
                      HttpURLConnection connection = (HttpURLConnection) blogFeedUrl.openConnection();
                        connection.connect();

                        responseCode = connection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK)
                    {
                        InputStream inputStream = connection.getInputStream();
                        Reader reader = new InputStreamReader(inputStream);
                       /* int contentLength = connection.getContentLength();
                        char[] charArray = new char[contentLength];
                        reader.read(charArray);
                        String responseData = new String(charArray);
                         */
                            int nextCharacter; // read() returns an int, we cast it to char later
                            String responseData = "";
                            while (true)
                                { // Infinite loop, can only be stopped by a "break" statement
                                     nextCharacter = reader.read(); // read() without parameters returns one character
                                    if (nextCharacter == -1)
                                    { // A return value of -1 means that we reached the end
                                         break;
                                    }
                                    responseData += (char) nextCharacter; // The += operator appends the character to the end of the string
                                }
                             jsonResponse = new JSONObject(responseData);

                    }
                    else
                    {
                        Log.i(TAG, "Unsuccessful HTTP Response Code: " + responseCode);
                    }
            }
            catch (MalformedURLException e) {
                Log.e(TAG, "Exception caught: ", e);
            } catch (IOException e) {
                Log.e(TAG, "Exception caught: ", e);
            } catch (Exception e) {
                Log.e(TAG, "Exception caught: ", e);
            }

            return jsonResponse;
        }

        @Override
        protected void onPostExecute(JSONObject result) {
                mBlogData = result;
                updateList();
        }


    }



}
