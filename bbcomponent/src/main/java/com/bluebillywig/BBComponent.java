package com.bluebillywig;

import java.util.Calendar;

import android.content.Context;

import android.util.Log;

public class BBComponent {
	
	private String publication;
	private String vhost;
	private boolean debug;

	/**
	 * Constructor to create BBComponent object
	 * @param publication Publication of the VMS to be used
	 * @param vhost Vhost of the publication, like "demo.bbvms.com"
	 */
	public BBComponent( String publication, String vhost ) {
		this( publication, vhost, false );
	}

	/**
	 * Constructor to create BBComponent object
	 * @param publication Publication of the VMS to be used
	 * @param vhost Vhost of the publication, like "demo.bbvms.com"
	 * @param debug Use debugging for development
	 */
	public BBComponent( String publication, String vhost, boolean debug ) {
		this.publication = publication;
		this.vhost = vhost;
		this.debug = debug;
	}

	/**
	 Protected function that will create the url for the player
	 @param vhost Base url to load, eg. bbvms.com
	 @return returns the uri based on the vhost
	 */
	protected String createUri( String vhost ){
	    return createUri( vhost, null );
	}

	/**
	 Protected function that will create the url for the player
	 @param vhost Base url to load, eg. bbvms.com
	 @param component Name of the component to load
	 @return returns the uri based on the vhost
	 */
	protected String createUri( String vhost, String component ){
		StringBuffer uri = new StringBuffer();
		uri.append("https://");
	    
	    if( component == null ){
	        uri.append( vhost ).append("/");
	    }
	    else{
	        uri.append( vhost ).append("/component?c=").append(component);

	        if( debug ){
	        	Calendar cal = Calendar.getInstance();
				uri.append("&ts=").append(cal.getTimeInMillis());
	        }
	    }
	    
	    return uri.toString();
	}

	/**
	 * Create a BBplayer object
	 * @param context Context of the activity that contains the BBPlayer object
	 * @param setup Player setup with playout, etc
	 * @return BBPlayer object
	 */
	public BBPlayer createPlayer( Context context, BBPlayerSetup setup ){
		return this.createPlayer(context, "0", setup);
	}
	
	/**
	 * Create a BBplayer object
	 * @param context Context of the activity that contains the BBPlayer object
	 * @param clipId Clip id of the mediaclip to be loaded
	 * @param setup Player setup with playout, etc
	 * @return BBPlayer object
	 */
	public BBPlayer createPlayer( Context context, String clipId, BBPlayerSetup setup ){
		
		String uri = createUri( this.vhost, "AndroidAppPlayer" );

		String baseUri = createUri( this.vhost );

		if( debug ){
			Log.d("BBComponent","Uri for player: " + uri);
			setup.setDebug(debug);
		}

		BBPlayer player = new BBPlayer(context, uri, clipId, baseUri, setup);
		
		return player;
	}
	
}
