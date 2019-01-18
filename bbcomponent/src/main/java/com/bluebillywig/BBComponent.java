package com.bluebillywig;

import java.io.IOException;
import java.util.Calendar;

import com.bluebillywig.BBPlayerSetup;
//import com.google.android.gms.ads.identifier.AdvertisingIdClient;
//import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
//import com.google.android.gms.common.GooglePlayServicesRepairableException;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.util.Log;

public class BBComponent {
	
	private String publication;
	private String vhost;
	private boolean secure;
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
	 * @param secure VMS uses https secure connection
	 */
	public BBComponent( String publication, String vhost, boolean secure ) {
		this( publication, vhost, secure, false );
	}

	/**
	 * Constructor to create BBComponent object
	 * @param publication Publication of the VMS to be used
	 * @param vhost Vhost of the publication, like "demo.bbvms.com"
	 * @param secure VMS uses https secure connection
	 * @param debug Use debugging for development
	 */
	public BBComponent( String publication, String vhost, boolean secure, boolean debug ) {
		this.publication = publication;
		this.vhost = vhost;
		this.secure = secure;
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
		uri.append("http");
	    
	    if( secure ){
	        uri.append("s");
	    }	    
	    if( component == null ){
	        uri.append("://").append( vhost ).append("/");
	    }
	    else{
	        uri.append("://").append( vhost ).append("/component?c=").append(component);

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
