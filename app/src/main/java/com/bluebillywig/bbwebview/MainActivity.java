package com.bluebillywig.bbwebview;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bluebillywig.BBComponent;
import com.bluebillywig.BBPlayer;
import com.bluebillywig.BBPlayerSetup;

import androidx.constraintlayout.widget.ConstraintLayout;

import org.json.JSONException;
import org.json.JSONObject;


public class MainActivity extends Activity implements View.OnTouchListener {
    private WebView mainWebView;
    private BBPlayer webView;
    private LinearLayout buttonLayout;
    private boolean useAdvertisment = false;
    private BBPlayer.Playout playout = null;

    private MainActivity mainActivity = null;
    private String playoutName = "androidApp";

    private float aspectRatio = 0.0f;

    private int APP_NORMAL_DISPLAY_MODE =  View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
    private int APP_IMMERSIVE_DISPLAY_MODE = APP_NORMAL_DISPLAY_MODE | View.SYSTEM_UI_FLAG_IMMERSIVE | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN;
    private int APP_IMMERSIVE_STICKY_DISPLAY_MODE = APP_NORMAL_DISPLAY_MODE | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN;
    private int APP_LEAN_BACK_DISPLAY_MODE = APP_NORMAL_DISPLAY_MODE | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN;

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            // For more information: https://developer.android.com/training/system-ui/immersive
            View decorView = getWindow().getDecorView();

            // Sticky immersive mode
            decorView.setSystemUiVisibility( APP_IMMERSIVE_STICKY_DISPLAY_MODE );

            // Immersive mode
//            decorView.setSystemUiVisibility( APP_IMMERSIVE_DISPLAY_MODE );


            // Lean back mode
//            decorView.setSystemUiVisibility( APP_LEAN_BACK_DISPLAY_MODE );
        }
    }

    // Shows the system bars by removing all the flags
    // except for the ones that make the content appear under the system bars.
    private void showSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility( APP_NORMAL_DISPLAY_MODE );
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button play = (Button)this.findViewById(R.id.play_button);
        Button pause = (Button)this.findViewById(R.id.pause_button);
        Button fullscreen = (Button)this.findViewById(R.id.fullscreen_button);
        Button createPlayer = (Button)this.findViewById(R.id.create_button);
        Button destroyPlayer = (Button)this.findViewById(R.id.destroy_button);
        final EditText text = (EditText)this.findViewById(R.id.edit_text);
        this.buttonLayout = (LinearLayout)this.findViewById(R.id.buttonLayout);

        play.setOnClickListener(view -> {
            System.out.println("Play video was pressed");
            // if the text field is filled in the playbutton will open the clip
            if (text.getText().toString().equals("")) {
            // Call the play function
                webView.play();
            } else {
                webView.loadClip(text.getText().toString());
                webView.loadUrl(text.getText().toString());
                webView.expand(mainWebView, true);
                hideKeyboard();
            }
        });

        pause.setOnClickListener(view -> {
            System.out.println("Pause video was pressed");
            // Call the pause function
            webView.pause();
        });

        fullscreen.setOnClickListener(view -> {
            System.out.println("Fullscreen button was pressed");
            // Call the fullscreen function
            webView.fullscreen();
            buttonLayout.setVisibility( View.GONE );
            webView.setLayoutMode(1);
        });

        createPlayer.setOnClickListener(view -> {
            if (webView == null) {
                BBPlayerSetup playerSetup = new BBPlayerSetup();
                playerSetup.setPlayout(playoutName);

                String mediaclipId = "2119201";

                BBComponent bbComponent = new BBComponent("demo", "demo.bbvms.com", false);
                webView = bbComponent.createPlayer(mainActivity, mediaclipId, playerSetup);

                webView.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT));

                mainWebView.addView(webView);

                webView.play();
            }
        });

        destroyPlayer.setOnClickListener(view -> {
            if (webView != null) {
                mainWebView.removeAllViews();
                webView.destroy();
                webView = null;
            }
        });

        // Creating BBComponent
        BBComponent bbComponent = new BBComponent("demo", "demo.bbvms.com", false);
//        bbComponent = new BBComponent("bb.dev", "bb.dev.bbvms.com", false);

        playoutName = "androidapp";

        // Creating player setup with "androidapp" playout
        BBPlayerSetup playerSetup = new BBPlayerSetup();
        playerSetup.setPlayout(playoutName);

        // Use this when using the android native skin and using the native fullscreen button
        // The FrameLayout is defined in the activity_main.xml
        FrameLayout fullscreenFrameLayout = (FrameLayout) findViewById(R.id.fullscreenFrameLayout);
        playerSetup.setFullscreenFrameLayout(fullscreenFrameLayout);

        playerSetup.setOpenAdsInNewWindow(true);
//        playerSetup.setAdunit("companion_ad_test");
//        playerSetup.setAdunit("companion_ad_vertical_test");

        Log.d("MainActivity", "Sending setup: " + playerSetup);

        // Creating player with mediaclip with id 2119201
        String mediaclipId = "2119201";
        // To test with vertical video
//        mediaclipId = "2766042";


        // bb.dev test clips
        // mediaclipId = "1081520";
        // To test with vertical video
        // mediaclipId = "1076954"; // vertical video

        webView = bbComponent.createPlayer(this, mediaclipId, playerSetup);
        mainActivity = this;

        if (!webView.isNetworkAvailable()) {
            Toast.makeText(this, "No internet connection available", Toast.LENGTH_SHORT).show();
        }

        // This initialization should be used for ads
        // webView = bbComponent.createPlayer(this, playerSetup);

        // If the user has given permission (or not) for personalised ads, call the function below
        webView.adConsentFromUser(true);

        webView.setOnTouchListener(this);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);

        // Add player with layout parameters
        webView.setLayoutParams(params);

        // Add player to main webview
        mainWebView = (WebView)findViewById(R.id.webView);
        mainWebView.addView(webView);

        if (playerSetup.getAdunit() != null && playerSetup.getAdunit().length() > 0) {
            useAdvertisment = true;
        }

        if (useAdvertisment) {
            mainWebView.setVisibility(View.GONE);
            webView.collapse(mainWebView, true);
        } else {
            webView.play();
        }

        // Check to see if webview is garbage collected after pressing "destroy player"
        Leakcanary.expectWeaklyReachable(webView, "Webview was detached");

        // This will catch onPlay events and send them to the onPlay callback function defined below
        webView.on("play",this,"onPlay");
        webView.on("fullscreen",this,"fullscreen");
        webView.on("retractFullscreen",this,"retractFullscreen");
        webView.on("resized", this, "resized");

        // When using fullscreenFrameLayout these callbacks are called when going to fullscreen and back
        webView.on("fullscreenView",this,"fullscreenView");
        webView.on("retractFullscreenView",this,"retractFullscreenView");

        // These are specific events for Ad usage
        webView.on("adstarted", this, "adStarted");
        webView.on("adfinished", this, "adFinished");
        webView.onLoadedPlayoutData(this, "loadedAdPlayoutData");

        webView.on("started", this, "started");

        webView.on("loadedclipdata", this, "onLoadedClipData");
    }

    public void onNetworkChange(boolean hasInternet) {
        Log.d("MainActivity","on network change event caught; is network available ? " + (hasInternet ? "yes" : "no"));
    }

    public void onPlay(){
        Log.d("MainActivity","onPlay event caught");
    }

    public void fullscreenView(){
        // This is thrown when the player is placed inside the fullscreenFrameLayout
        Log.d("MainActivity","fullscreenView event caught");
    }

    public void retractFullscreenView(){
        // This is thrown when the player is removed from the fullscreenFrameLayout
        Log.d("MainActivity","retractFullscreenView view event caught");
    }

    public void fullscreen(){
        Log.d("MainActivity","fullscreen event caught");
    }

    public void retractFullscreen(){
        Log.d("MainActivity","retractFullscreen event caught");
        buttonLayout.setVisibility( View.VISIBLE );
    }

    public void started() {
        Log.d("MainActivity","Mediaclip started");
    }

    public void resized() {
        Log.d("MainActivity","Player resized");
        mainWebView.post(new Runnable() {
            @Override
            public void run() {
                webView.call("getDimensions", "", "getDimensions" );
            }
        });
    }

    public void onLoadedClipData() {
        Log.d("MainActivity","onLoadedClipData");
        mainWebView.post(new Runnable() {
            @Override
            public void run() {
                webView.call("getClipData", "", "getClipdata" );
            }
        });
    }

    public void getClipdata(Object result) {
        Log.d("MainActivity","clip data: " + result);
        if (result instanceof JSONObject) {
            JSONObject json = (JSONObject)result;
            try {
                final String title = json.getString("title");

                Log.d("MainActivity","Title of clip is: " + title);

            } catch (JSONException e) {
                // Json parsing problem occurred
            }
        }
    }

    public void getDimensions(Object result) {
        Log.d("MainActivity","Player dimensions: " + result);
        if (result instanceof JSONObject) {
            JSONObject json = (JSONObject)result;
            try {
                final int width = json.getInt("width");
                final int height = json.getInt("height");

                if (width > 0 && height > 0) {
                    final float aspectRatio = (float)height / width;
                    if (aspectRatio > 0 && this.aspectRatio != aspectRatio) {
                        this.aspectRatio = aspectRatio;
                        mainWebView.post(new Runnable() {
                            @Override
                            public void run() {
                                ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams)mainWebView.getLayoutParams();
                                layoutParams.dimensionRatio = "H," + width + ":" + height;
                                Log.d("MainActivity", "set dimension to: H," + width + ":" + height);
                                mainWebView.setLayoutParams(layoutParams);
                            }
                        });
                    }
                }
            } catch (JSONException e) {
                // Json parsing problem occurred
            }
        }

        // The following else statements are just an example, getDimensions will always return an JSONObject
        /*
        else if (result instanceof JSONArray) {
            JSONArray json = (JSONArray)result;
        }
        else {
            String value = (String)result;
        }
        */
    }

    public void adStarted() {
        Log.d("MainActivity","Ad started");
        this.expand(true);
        if (this.playout != null && this.playout.interactivity_inView.contains("unmute")) {
            mainWebView.post(new Runnable() {
                @Override
                public void run() {
                    webView.mute(false);
                }
            });
        }
    }

    public void adFinished() {
        Log.d("MainActivity","Ad finished");
        if (this.playout != null && this.playout.hidePlayerOnEnd.equals("true")) {
            this.expand(false);
        }
    }

    public void expand( final boolean expand ) {
        mainWebView.post(new Runnable() {
            @Override
            public void run() {
                Log.d("MainActivity","Expand mainWebView: " + expand);
                if (expand) {
                    webView.expand(mainWebView);
                } else {
                    webView.collapse(mainWebView);
                }
            }
        });
    }

    public void loadedAdPlayoutData(BBPlayer.Playout playout){
        Log.d("MainActivity","loaded playout data event caught: " + playout);
        this.playout = playout;
        if (!this.playout.startCollapsed.equals("true")) {
            this.expand(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
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

    public void hideKeyboard() {
        Activity activity = this;
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        if(imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if( buttonLayout.getVisibility() == View.GONE ){
            buttonLayout.setVisibility( View.VISIBLE );
        }
        return false;
    }
}