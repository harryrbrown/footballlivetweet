package com.example.harry.footballlivetweet;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.app.Activity;
import android.os.Bundle;
import android.view.View.OnClickListener;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import android.util.Log;
import android.widget.Button;

public class MainActivity extends AppCompatActivity implements OnClickListener {

    /**developer account key for this app*/
    public final static String TWIT_KEY = "znOj3lBfSUMIxvmlNLDT61bVa";
    /**developer secret for the app*/
    public final static String TWIT_SECRET = "tiTKmFuapIDsctiXsPBT2PdmdeVZo66FThRtPXbOj2SdNJA7lV";
    /**app url*/
    public final static String TWIT_URL = "tnice-android:///";

    /**Twitter instance*/
    private Twitter niceTwitter;
    /**request token for accessing user account*/
    private RequestToken niceRequestToken;
    /**shared preferences to store user details*/
    private SharedPreferences nicePrefs;

    //for error logging
    private String LOG_TAG = "TwitNiceActivity";//alter for your Activity name

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        nicePrefs = getSharedPreferences("TwitNicePrefs", 0);
        if (nicePrefs.getString("user_token", null) == null) {
            setContentView(R.layout.activity_login);

            //new Twitter instance
            niceTwitter = new TwitterFactory().getInstance();
            niceTwitter.setOAuthConsumer(TWIT_KEY, TWIT_SECRET);

            //try to get request token
            try
            {
                //get authentication request token
                niceRequestToken = niceTwitter.getOAuthRequestToken(TWIT_URL);
            }
            catch(TwitterException te) { Log.e(LOG_TAG, "TE " + te.getMessage()); }

            Button signIn = (Button)findViewById(R.id.signin);
            signIn.setOnClickListener(this);

        } else {
            setupTimeline();
        }
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.signin:
                String authURL = niceRequestToken.getAuthenticationURL();
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(authURL)));
                break;
            default:
                break;
        }
    }

    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Uri twitUri = intent.getData();
        if (twitUri != null && twitUri.toString().startsWith(TWIT_URL)) {
            String oaVerifier = twitUri.getQueryParameter("oauth_verifier");

            try
            {
                //try to get an access token using the returned data from the verification page
                AccessToken accToken = niceTwitter.getOAuthAccessToken(niceRequestToken, oaVerifier);

                //add the token and secret to shared prefs for future reference
                nicePrefs.edit()
                        .putString("user_token", accToken.getToken())
                        .putString("user_secret", accToken.getTokenSecret())
                        .apply();

                //display the timeline
                setupTimeline();
            }
            catch (TwitterException te)
            { Log.e(LOG_TAG, "Failed to get access token: " + te.getMessage()); }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
