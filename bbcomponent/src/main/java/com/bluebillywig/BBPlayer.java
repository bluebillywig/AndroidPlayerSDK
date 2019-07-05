package com.bluebillywig;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.webkit.ConsoleMessage;
import android.webkit.JavascriptInterface; //Needed for android api > 16
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import static android.os.Build.*;

public class BBPlayer extends WebView {

	public static String VERSION = "1.4.6";

	private BBPlayer webView;
	private Map<String,String> BBPlayerReturnValues = new HashMap<>();
	private Map<String,Object[]> eventMap = new HashMap<>();
	private Map<String,Object> functionMap = new LinkedHashMap<>();

	private final static int KITKAT = 19;

	private boolean debug = false;
	private String url = "";
	private String clipId = "";
	private String playout = "";
	private String assetType = "";
	private String mediaclipUrl = "";
	private String parameter = "";
	private String adUnit = "";
	private boolean hasAdUnit = false;

	private boolean playerReady = false;

	private Context parent;
	private Context callbackParent = null;

	public class Playout {
		public String autoPlay = "false";
		public String startCollapsed = "false";
		public String hidePlayerOnEnd = "false";
		public String interactivity_inView = "false";
		public String interactivity_outView = "false";

		public String toString() {
			return "autoPlay: " + autoPlay + ", startCollapsed: " + startCollapsed + ", hidePlayerOnEnd: " + hidePlayerOnEnd +
					", interactivity_inView: " + interactivity_inView + ", interactivity_outView: " + interactivity_outView;
		}
	}

	private BBPlayer(Context context) {
		super(context);
	}

	protected BBPlayer(Context context, String uri, String clipId, String baseUrl, BBPlayerSetup setup  ){
		super(context);
		if(!isInEditMode()){
			initialize(context, uri, clipId, baseUrl, setup);
		}
	}

	@SuppressLint("SetJavaScriptEnabled")
	private void initialize(Context context, String uri, String clipId, String baseUrl, BBPlayerSetup setup  ){

		webView = this;
		parent = context;

		this.getSettings().setJavaScriptEnabled(true);
		webView.getSettings().setMediaPlaybackRequiresUserGesture(false);

		if(setup.isDebug() /* && Build.VERSION.SDK_INT >= KITKAT */ ) {
			try {
				Method method = this.getClass().getMethod("setWebContentsDebuggingEnabled", new Class[] { boolean.class });
				method.invoke(this, true);
			} catch (NoSuchMethodException e) {
				Log.e("BBPlayer","Trying to call a non existant method",e);
			} catch (IllegalAccessException e) {
				Log.e("BBPlayer","Exception caught",e);
			} catch (IllegalArgumentException e) {
				Log.e("BBPlayer","Exception caught",e);
			} catch (InvocationTargetException e) {
				Log.e("BBPlayer","Exception caught",e);
			}
		}

		this.setWebChromeClient(new WebChromeClient() {
			@Override
			public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
				Log.d("console " + consoleMessage.messageLevel() + " message", consoleMessage.message());
				return true;
			}
		});

		this.setWebViewClient(new WebViewClient(){
			@RequiresApi(VERSION_CODES.KITKAT)
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url){
                Log.d("setWebViewClient","url override " + url);
                return handleUrl(url);
            }

			@RequiresApi(VERSION_CODES.N)
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
				final Uri uri = request.getUrl();
				return handleUrl(uri.toString());
			}

			private boolean handleUrl(String url) {
				Log.d("setWebViewClient","url override " + url);
				if (webView.hasAdUnit) {
					if (!(url.contains("bbvms.com") || url.contains("mainroll.com"))) {
						Intent intent = new Intent("android.intent.action.VIEW", Uri.parse(url));
						Bundle b = new Bundle();
						b.putBoolean("new_window", true); //sets new window
						intent.putExtras(b);
						parent.startActivity(intent);
						return true;
					}
				} else {
					if (url.startsWith("http:")) {
						webView.loadUrl(url.replaceAll("^http:", "https:"));
					}
				}
				return false;
			}

            @Override
			public void onReceivedError(WebView view, int errorCode, String description, String failingUrl ){
				super.onReceivedError(view, errorCode, description, failingUrl);

				if( debug ){
					Log.d("BBPlayer","Received error: " + errorCode + " with description: " + description + " and url: " + failingUrl);
				}

			}
		});

		this.playout = setup.getPlayout();
		this.assetType = setup.getAssetType();
		this.clipId = clipId;
		this.debug = setup.isDebug();
		this.parameter = setup.getParameter();
		this.adUnit = setup.getAdunit();
		if( this.adUnit.length() > 0 ) {
			this.hasAdUnit = true;
		}

		// Class to handle javascript calls to the android application, the second argument
		// is can be used directly from javascript with functions defined in the JavascriptAppInterface
		// defined below
		this.addJavascriptInterface(new JavaScriptAppInterface( this.getContext() ), "NativeBridge");

		if( debug ){
			WebView.setWebContentsDebuggingEnabled(BuildConfig.DEBUG);
			Log.d("BBPlayer","Loading url: " + uri);
		}
		this.loadUrl(uri);

 		if( hasAdUnit ) {
			mediaclipUrl = baseUrl + "a/" + this.adUnit + ".json";
		} else {
			mediaclipUrl = baseUrl + "p/" + this.playout + "/" + this.assetType + "/" + this.clipId + ".json";
		}

		if (!setup.getShowCommercials()) {
			mediaclipUrl += "?commercials=false";
		}
	}

	/**
	 Function to attach to a player event, like fullscreen or playing
	 @see "https://support.bluebillywig.com/blue-billywig-v5-player/events-modes-and-phases"
	 @param event Event to attach to
	 @param parent The object to which the function belongs
	 @param function Name of the function that will get the event
	 */
	public void on( String event, Object parent, String function ){
		Object []eventArray = { parent, false, null };
		if( playerReady ){
			eventArray[1] = true;
			this.loadUrl( javascriptUrl( "bbAppBridge.on('" + event + "','" + function + "');" ) );
		}
		else{
			eventArray[2] = event;
		}
		eventMap.put(function, eventArray);
	}

	/**
	 Function to attach to the playout loaded player event
	 @param parent The object to which the function belongs
	 @param function Name of the function that will get the event
	 */
	public void onLoadedPlayoutData( Object parent, String function ){
		Object []eventArray = { parent, false, null };
		if( playerReady ){
			eventArray[1] = true;
			this.loadUrl( javascriptUrl( "bbAppBridge.on('loadedadplayoutdata','" + function + "');" ) );
		}
		else{
			eventArray[2] = "loadedadplayoutdata";
		}
		eventMap.put(function, eventArray);
	}


	/**
	 Call a method on the player embedded in the webview
	 @see "https://support.bluebillywig.com/blue-billywig-v5-player/functions"
	 @param function Name of the function that will be called
	 */
	public String call( String function ){
		return call( function, null, null, null );
	}

	/**
	 Call a method on the player embedded in the webview
	 @see "https://support.bluebillywig.com/blue-billywig-v5-player/functions"
	 @param function Name of the function that will be called
	 @param argument Argument that's needed in the function
	 */
	public String call( String function, String argument ){
		return call( function, null, argument, null );
	}

    /**
     Call a method on the player embedded in the webview
     @see "https://support.bluebillywig.com/blue-billywig-v5-player/functions"
     @param function Name of the function that will be called
     @param argument Argument that's needed in the function
     @param callbackFunction Callback that will receive the value[s]
     */
    public String call( String function, String argument, String callbackFunction ){
        return call( function, null, argument, callbackFunction );
    }


    /**
	 Call a method on the player embedded in the webview
	 @see "https://support.bluebillywig.com/blue-billywig-v5-player/functions"
	 @param function Name of the function that will be called
	 @param arguments Arguments that are needed in the function
	 */
	public String call( String function, Map<String,String> arguments ){
		return call( function, arguments, null, null );
	}

    /**
     Call a method on the player embedded in the webview
     @see "https://support.bluebillywig.com/blue-billywig-v5-player/functions"
     @param function Name of the function that will be called
     @param arguments Arguments that are needed in the function
     @param callbackFunction Callback that will receive the value[s]
     */
    public String call( String function, Map<String,String> arguments, String callbackFunction ){
        return call( function, arguments, null, callbackFunction );
    }

	/**
	 * Use a different parent for the call functions callback
	 * @param context The context of the parent that will receive the callback
	 */
	public void setCallbackParent(Context context) {
		callbackParent = context;
	}

	private String call( String function, Map<String,String> arguments, String argument, String callbackFunction ){

		Calendar c = Calendar.getInstance();
		String identifier = "" + c.getTimeInMillis() + ":" + callbackFunction;

		String jsonString = null;

		if( function.contains("(") && function.endsWith(")") ){
			function = function.replace("()", "");
		}

		Context context = parent;
		if (callbackParent != null) {
			context = callbackParent;
		}

		if (callbackFunction != null) {
            Object[] eventArray = {context, false, function, String.class};
            eventMap.put(callbackFunction, eventArray);
        }

		if( arguments != null ){
			JSONObject jsonArguments = new JSONObject();

			for( String key: arguments.keySet() ){
				try {
					jsonArguments.put(key, arguments.get(key));
				} catch (JSONException e) {
					Log.e("BBPlayer",e.toString());
				}
			}

			jsonString = jsonArguments.toString();

		}
		else if( argument != null ){
			jsonString = argument;
		}

		if( jsonString == null ){
			url = javascriptUrl( "bbAppBridge.call('" + function + "', undefined, '" + identifier + "');" );
			if( debug ){
				Log.d("BBPlayer","Loading url: " + url);
			}
			this.loadUrl(url);
		}
		else{
			url = javascriptUrl( "bbAppBridge.call('" + function + "', '" + jsonString + "', '" + identifier + "');" );
			if( debug ){
				Log.d("BBPlayer","Loading url: " + url);
			}
			this.loadUrl(url);
		}

		String returnValue = null;

		int i = 0;
		while( !BBPlayerReturnValues.containsKey(identifier) && i < 10 ){
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			i++;
		}
		if( debug ){
			Log.d("BBPlayer","found identifier: " + identifier + " with value: " + BBPlayerReturnValues.get(identifier));
		}
		returnValue = BBPlayerReturnValues.remove(identifier);

		return returnValue;
	}

	private void addFunctionLate( String function ){
		addFunctionLate( function, null );
	}

	private void addFunctionLate( String function, Object arguments ){
		function = functionMap.size() + "-" + function;
		functionMap.put( function, arguments );
	}

	/**
	 Use this function to start or resume the player.
	 */
	public void play(){
		if( playerReady ) {
			this.call("play");
		}
		else{
			addFunctionLate("play");
		}
	}

	/**
	 Use this function to pause the player.
	 */
	public void pause(){
		if( playerReady ) {
			this.call("pause");
		}
		else{
			addFunctionLate("pause");
		}
	}

	/**
	 Use this function to mute/unmute the player.
	 */
	public void mute(boolean mute){
		String muted = "false";
		if (mute) {
			muted = "true";
		}
		if( playerReady ) {
			this.call("setMuted", muted);
		}
		else{
			addFunctionLate("setMuted", muted);
		}
	}

	/**
	 Use this function to seek to a point in the video.
	 @param timeInSeconds time to seek to
	 */
	public void seek (float timeInSeconds ){
		if( playerReady ) {
			this.call("pause");
		}
		else{
			addFunctionLate("seek", Float.toString(timeInSeconds));
		}
	}

	/**
	 Use this function to load a clip using it's clip id.
	 @param clipId Id of clip to load
	 */
	public void loadClip( String clipId ){
		loadClip(clipId, true);
	}

	/**
	 Use this function to load a clip using it's clip id.
	 @param clipId Id of clip to load
	 @param autoPlay Start playing clip immediately?
	 */
	public void loadClip( String clipId, boolean autoPlay ){
		Map<String,String> arguments = new HashMap<String,String>();
		arguments.put("clipId", clipId);
		arguments.put("autoPlay", Boolean.toString(autoPlay) );
		if( playerReady ) {
			this.call("load",arguments);
		}
		else{
			addFunctionLate("load", arguments);
		}
	}

	/**
	 Use this function to make the player go to fullscreen mode.
	 */
	public void fullscreen(){
		if( playerReady ) {
			this.call("fullscreen");
		}
		else{
			addFunctionLate("fullscreen");
		}
	}

	/**
	 Use this function to make the player go out of fullscreen mode.
	 */
	public void retractFullscreen(){
		if( playerReady ) {
			this.call("retractFullscreen");
		}
		else{
			addFunctionLate("retractFullscreen");
		}
	}

	/**
	 Use this function to get the position of the video.
	 @return Position as a float value
	 */
	public float getCurrentTime(){
		if( playerReady ) {
			String returnValue = this.call("getCurrentTime");
			return Float.parseFloat(returnValue);
		}
		return 0.0f;
	}

	/**
	 Use this function to check if the video is playing.
	 @return True when the video is playing
	 */
	public boolean isPlaying(){
		if( playerReady ) {
			String returnValue = this.call("isPlaying");
			return Boolean.parseBoolean(returnValue);
		}
		return false;
	}

	/**
	 Use this function to check if the player is fullscreen.
	 @return True when the player is fullscreen
	 */
	public boolean isFullscreen(){
		if( playerReady ) {
			String returnValue = this.call("isFullscreen");
			return Boolean.parseBoolean(returnValue);
		}
		return false;
	}

	public void showCommercials(boolean showCommercials) {
		Map<String,String> arguments = new HashMap<>();
		if (showCommercials) {
			arguments.put("commercials", "true");
		} else {
			arguments.put("commercials", "false");
		}

		if( playerReady ) {
			this.call("updatePlayout", arguments);
		}
		else{
			addFunctionLate("updatePlayout", arguments);
		}
	}

	/**
	 * Call this function to consent or not consent to show personalised ads
	 */
	public void adConsentFromUser(boolean consent) {
		String androidId = Settings.Secure.getString(this.parent.getContentResolver(), Settings.Secure.ANDROID_ID);

		Map<String,String> arguments = new HashMap<>();
		arguments.put("adsystem_idtype", "aaid");
		arguments.put("adsystem_rdid", androidId);
		if (consent) {
			arguments.put("adsystem_is_lat", "0");
		} else {
			arguments.put("adsystem_is_lat", "1");
		}

		Log.d("MainActivity", "Updating playout after user consent: " + arguments.toString());

		if( playerReady ) {
			this.call("updatePlayout", arguments);
		}
		else{
			addFunctionLate("updatePlayout", arguments);
		}
	}

	private String javascriptUrl( String url ){
		if( debug ){
			url = "try{ " + url + " }catch(error){ alert(error); };";
		}
		return "javascript:" + url;
	}

	// This class is used to bind the javascript to java
	// The functions defined by @JavascriptInterface can be called from javascript
	// using the "NativeBridge". So NativeBridge.call( "function", args[], callbackFunction ).
	private class JavaScriptAppInterface {
		Context mContext;

		JavaScriptAppInterface(Context c) {
			mContext = c;
		}

		@JavascriptInterface //Needed for android api > 16
		public void call( String function ) {
			this.call(function,null,null);
		}

		@JavascriptInterface //Needed for android api > 16
		public void call( String function, String[] arguments ) {
			this.call(function,arguments,null);
		}

		/**
		 * This function is called from javascript
		 * @param function Function to call
		 * @param arguments Arguments for the function
		 * @param callbackFunction The callback javascript function
		 */
		@JavascriptInterface //Needed for android api > 16
		public void call( String function, String[] arguments, String callbackFunction ) {
			if( debug ){
				Log.d("BBPlayer","Received function " + function + " from javascript");
			}
			if( function != null ){
				if( function.contentEquals("prompt") ){
					String message = "";
					for( String argument: arguments ){
						message += argument + " ";
					}
					// Show text on android
					Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();

					if( callbackFunction != null && callbackFunction.length() > 0 ){
						// This is used because the toast takes some time to show,
						// so call the javascript callback in a thread
						webView.post( new CallbackThread( callbackFunction ) );
					}
				}
				else if( function.contentEquals("BBPlayerReturnValues") ){
					if( arguments.length > 1 ){
						BBPlayerReturnValues.put(arguments[0], arguments[1]);

						if (arguments[0].contains(":")) {
                            String parentFunction = arguments[0].split(":", 2)[1].replace("'", "");
                            this.callParent(parentFunction, arguments);
                        }
					}
				}
				else if( function.contentEquals("appbridgeready") ){
					if( debug ){
						Log.d("BBPlayer","Mediaclip load url: " + mediaclipUrl);
					}

					playerReady = true;

					webView.post(new Runnable() {
						@Override
						public void run() {
							if( debug ){
								Log.d("BBPlayer","Calling: ");
							}
							webView.evaluateJavascript( javascriptUrl( "bbAppBridge.placePlayer('" + mediaclipUrl + "');" ), null );

							for( String callFunction: functionMap.keySet() ){
								Object arguments = functionMap.get(callFunction);
								callFunction = callFunction.replaceFirst("^[\\d]+\\-", "");
								if( debug ){
									Log.d("BBPlayer","Late loading function: " + callFunction + " with arguments: " + arguments );
								}
								if( arguments != null ) {
									if( arguments instanceof String ){
										webView.call( callFunction, (String)arguments );
									}
									else if( arguments instanceof HashMap ){
										webView.call( callFunction, (HashMap)arguments );
									}
								} else {
									webView.call( callFunction );
								}
							}
						}
					});

					for( String key: eventMap.keySet() ){
						final Object []eventValue = eventMap.get(key);

						if( eventValue.length > 2 && eventValue[1] instanceof Boolean && !((Boolean)eventValue[1]) && eventValue[2] != null ){
							if( debug ){
								Log.d("BBPlayer","Late loading function callback: " + key + " attached to event: " + eventValue[2]);
							}
							final String keyString = key;
							webView.post(new Runnable() {
								@Override
								public void run() {
									webView.evaluateJavascript(  javascriptUrl( "bbAppBridge.on('" + eventValue[2] + "','" + keyString + "');" ), null );
								}
							});
						}
					}
				}
				else if( eventMap.containsKey(function) ){
					this.callParent(function, arguments);
				}
			}
		}

		private void callParent(String function, String[] arguments) {
			Object[] array = eventMap.get(function);
			if (array == null) {
				return;
			}

			Object parent = array[0];
			Class[] paramTypes = null;
			Object params = null;
			if (array.length > 3) {
				paramTypes = new Class[]{ (Class)array[3] };
				if (arguments.length > 1) {
					params = arguments[1];
				}
			}

			Log.i("BBPlayer_return","Trying to call function " + function);

			if (arguments != null && arguments.length > 1 && arguments[0] != null) {
				paramTypes = new Class[1];

				if (arguments[0].equals("loadedAdPlayoutData")) {
					Gson gson = new Gson();

					paramTypes[0] = Playout.class;
					params = gson.fromJson(arguments[1], Playout.class);
				} else {
					paramTypes[0] = Object.class;

                    if (arguments[1].startsWith("{")) {
						try {
							params = new JSONObject(arguments[1]);
						} catch (JSONException e) {
							e.printStackTrace();
						}
					} else if (arguments[1].startsWith("[")) {
						try {
							params = new JSONArray(arguments[1]);
						} catch (JSONException e) {
							e.printStackTrace();
						}
					} else {
						params = arguments[1];
                    }

				}

			}
			if( parent != null ){
				try {
					Method method;
					if (paramTypes == null) {
						method = parent.getClass().getMethod(function);
					} else {
						method = parent.getClass().getMethod(function, paramTypes);
					}
					if (params == null) {
						method.invoke(parent, (Object[]) params);
					} else {
						method.invoke(parent, params);
					}
				} catch (NoSuchMethodException e) {
					Log.e("BBPlayer","Trying to call a non existant method",e);
				} catch (IllegalAccessException e) {
					Log.e("BBPlayer","Exception caught",e);
				} catch (IllegalArgumentException e) {
					Log.e("BBPlayer","Exception caught",e);
				} catch (InvocationTargetException e) {
					Log.e("BBPlayer","Exception caught",e);
				}
			}
		}
	}

	/**
	 * Thread used to call callback function in javascript using webview
	 * @author Floris Groenendijk
	 */
	private class CallbackThread extends Thread{
		private String callbackFunction = "";

		public CallbackThread( String callbackFunction ){
			this.callbackFunction = callbackFunction;
		}

		@Override
		public void run() {
			try {
				Thread.sleep(3500);
				if( debug ){
					Log.d("BBPlayer","Calling javascript function '" + callbackFunction + "' after 3500 milliseconds");
				}
				webView.evaluateJavascript("javascript:" + callbackFunction + "(['alpha','beta'])", null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};

	public void expand(final View v) {
		this.expand(v, false);
	}

	public void expand(final View v, final boolean force) {
		if (v.getVisibility() == View.VISIBLE && !force) {
			return;
		}
		TranslateAnimation anim = new TranslateAnimation(0.0f, 0.0f, -v.getHeight(), 0.0f);
		v.setVisibility(View.VISIBLE);

		anim.setDuration(300);
		anim.setInterpolator(new AccelerateInterpolator(0.5f));
		v.startAnimation(anim);
	}

	public void collapse(final View v) {
		this.collapse(v, false);
	}

	public void collapse(final View v, final boolean force) {
		if (v.getVisibility() != View.VISIBLE && !force) {
			return;
		}
		TranslateAnimation anim = new TranslateAnimation(0.0f, 0.0f, 0.0f, -v.getHeight());
		Animation.AnimationListener collapseListener= new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				v.setVisibility(View.GONE);
			}
		};

		anim.setAnimationListener(collapseListener);
		anim.setDuration(300);
		anim.setInterpolator(new AccelerateInterpolator(0.5f));
		v.startAnimation(anim);
	}
}
