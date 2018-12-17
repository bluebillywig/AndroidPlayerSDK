package com.bluebillywig.bbwebview;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.bluebillywig.BBComponent;
import com.bluebillywig.BBPlayer;
import com.bluebillywig.BBPlayerSetup;


public class MainActivity extends Activity {
    private BBPlayer webView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

        Button play = (Button)this.findViewById(R.id.play_button);
        Button pause = (Button)this.findViewById(R.id.pause_button);
        final EditText text = (EditText)this.findViewById(R.id.edit_text);

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("Play video was pressed");
                // if the text field is filled in the playbutton will open the clip
                if (text.getText().toString().equals("")) {
                    // Call the play function
                    webView.play();
                } else {
                    webView.loadClip(text.getText().toString());
                }
            }
        });

        pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("Pause video was pressed");
                // Call the pause function
                webView.pause();
            }
        });

        // Creating BBComponent
        BBComponent bbComponent = new BBComponent("demo", "demo.bbvms.com", true, false);
//        bbComponent = new BBComponent("bb.dev", "bb.dev.bbvms.com", true, false);

        // Creating player setup with "androidapp" playout
        BBPlayerSetup playerSetup = new BBPlayerSetup();
        playerSetup.setPlayout("androidapp");

        Log.d("MainActivity", "Sending setup: " + playerSetup);

        // Creating player with mediaclip with id 2119201
        String mediaclipId = "2119201";
//        mediaclipId = "1081520";

        webView = bbComponent.createPlayer(this,mediaclipId,playerSetup);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);

        // Add player with layout parameters
        webView.setLayoutParams(params);

        // Add player to main webview
        WebView view = (WebView)findViewById(R.id.webView);
        view.addView(webView, 0);

        webView.play();

        // This will catch onPlay events and send them to the onPlay callback function defined below
        webView.on("play",this,"onPlay");
        webView.on("fullscreen",this,"fullscreen");
        webView.on("retractFullscreen",this,"retractFullscreen");
	}

    public void onPlay(){
        Log.d("MainActivity","onPlay event caught");
    }

    public void fullscreen(){
        Log.d("MainActivity","fullscreen event caught");
    }

    public void retractFullscreen(){
        Log.d("MainActivity","retractFullscreen event caught");
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
}
