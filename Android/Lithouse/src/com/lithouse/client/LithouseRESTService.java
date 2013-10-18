package com.lithouse.client;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import com.lithouse.client.model.Record;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;
import static com.lithouse.client.Constants.*;

public class LithouseRESTService extends IntentService {
	private static final String apiEndpoint = "http://alpha-api.elasticbeanstalk.com/v1/groups/";
	private AsyncHttpClient httpClient = new AsyncHttpClient ( );
	
	public LithouseRESTService ( ) {
		super ( LithouseRESTService.class.getName ( ) );
	}

	@Override
	protected void onHandleIntent ( Intent intent ) {
		Log.d ( DEBUG_TAG, "service started");
		
		ResultReceiver receiver = intent.getParcelableExtra ( INTENT_EXTRA_RECEIVER );
		if ( receiver == null ) {
			//should not happen
			Log.e ( DEBUG_TAG, "callback is missing" );
			return;
		}
		
		String appKey = intent.getStringExtra ( INTENT_EXTRA_APP_KEY ) ;
		if ( appKey == null || appKey.isEmpty ( ) ) {
			sendError ( receiver, ERROR_ILLEGAL_ARGUMENT, "missing appKey" );
			return;
		}
		
		String groupId = intent.getStringExtra ( INTENT_EXTRA_GROUP_ID ) ;
		if ( groupId == null || groupId.isEmpty ( ) ) {
			sendError ( receiver, ERROR_ILLEGAL_ARGUMENT, "missing groupId" );
			return;
		}
		//for post
		List < Record > records = intent.getParcelableArrayListExtra ( INTENT_EXTRA_RECORDS );
		post ( records, appKey, groupId, receiver );
		
		Log.d ( DEBUG_TAG, "service stopping");
	}
	
	private String buildUri ( String appKey, String groupId ) {
		String uri = apiEndpoint + groupId + "/records?appKey=" + appKey;
		Log.d ( DEBUG_TAG, uri );
		return uri; 
	}
	
	private void httpPost ( List < Record > records, String appKey, String groupId, ResultReceiver receiver ) {
		if ( records == null || records.isEmpty ( ) ) {
			//sendError
			sendError ( receiver, ERROR_ILLEGAL_ARGUMENT, "missing record list" );
			return;
		}
		
		HttpClient  httpclient = new DefaultHttpClient();
	    HttpPost httppost = new HttpPost ( buildUri ( appKey, groupId ) );
	    
	    try {
	        httppost.setHeader ( "Content-type", "application/json" );
	        httppost.setEntity ( new StringEntity ( serializeRecordsToJSON ( records ) ));

	        // Execute HTTP Post Request
	        HttpResponse response = httpclient.execute ( httppost );
	        // TODO > 300
	        Log.d ( DEBUG_TAG, response.getStatusLine().toString() );
	        Log.d ( DEBUG_TAG, response.getEntity ( ).toString ( ) );
	        
	        receiver.send ( STATUS_FINISHED, Bundle.EMPTY );
	        
	    } catch ( Exception e) {
	    	sendError ( receiver, -1, "post error" );
	        
	    } 


	}
	
	private String serializeRecordsToJSON ( List < Record > records ) {
		StringBuilder result = new StringBuilder ( ); 
		result.append ( "{\"records\":[" );
		result.append ( records.get ( 0 ).toString ( ) );
		
		for ( int i = 1; i < records.size ( ) ; i++ ) {
			result.append ( "," );
			result.append ( records.get ( i ).toString ( ) );
		}
		result.append ( "]}" );
		
		Log.d ( DEBUG_TAG, result.toString ( ) );
		return result.toString ( );
	}
	
	private void post ( List < Record > records, String appKey, String groupId, ResultReceiver receiver ) {
		if ( records == null || records.isEmpty ( ) ) {
			//sendError
			sendError ( receiver, ERROR_ILLEGAL_ARGUMENT, "missing record list" );
			return;
		} 
		
		
		try {
			httpClient.post ( null, buildUri ( appKey, groupId ), 
					new StringEntity ( serializeRecordsToJSON ( records ) ), "application/json", new LitHouseHttpResponseHandler ( receiver ) );
		} catch ( UnsupportedEncodingException e ) {
			Log.e ( DEBUG_TAG, "Could not serialize" );
			sendError ( receiver, ERROR_INTERNAL_ERROR, "could not serialize record list" );
			
			e.printStackTrace();
		}		
	}

	private void sendError ( ResultReceiver receiver, int errorType, String errorMessage ) {
		Bundle bundle = new Bundle ( );
		bundle.putInt ( INTENT_EXTRA_ERROR_TYPE, errorType );
		bundle.putString ( Intent.EXTRA_TEXT, errorMessage );
		Log.e ( DEBUG_TAG, errorMessage );
		
		receiver.send ( STATUS_ERROR, bundle );
	}
	
	private class LitHouseHttpResponseHandler extends JsonHttpResponseHandler {
		private ResultReceiver receiver;
		public LitHouseHttpResponseHandler ( ResultReceiver receiver ) {
			this.receiver = receiver;
		}
		
		@Override
		public void onFailure ( Throwable throwable, String content ) {
			Log.e ( DEBUG_TAG, throwable.getMessage ( ) );
			Log.e ( DEBUG_TAG, content );
			
			// TODO: Convert to http error
			sendError ( receiver, -1, content );
		}
		
		@Override
		public void onSuccess ( int statusCode, JSONObject result ) {
			Log.d ( DEBUG_TAG, result.toString ( ) );
			Bundle bundle = new Bundle ( );
			bundle.putParcelableArrayList ( INTENT_EXTRA_RECORDS, new ArrayList < Record > ( ) );
			
			receiver.send ( STATUS_FINISHED, bundle );
		}
	}
}
