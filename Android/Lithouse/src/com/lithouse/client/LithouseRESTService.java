package com.lithouse.client;

import java.io.IOException;
import java.util.List;

import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;

import com.lithouse.client.exception.LithouseClientException;
import com.lithouse.client.model.Record;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;
import static com.lithouse.client.Constants.*;

public class LithouseRESTService extends IntentService {
	
	public LithouseRESTService ( ) {
		super ( LithouseRESTService.class.getName ( ) );
	}

	@Override
	protected void onHandleIntent ( Intent intent ) {
		Log.d ( DEBUG_TAG, "service started");
		
		ResultReceiver receiver = intent.getParcelableExtra ( INTENT_EXTRA_RECEIVER );
		if ( receiver == null ) {
			//fire and forget
			Log.d ( DEBUG_TAG, "callback is missing" );
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
		
		int command = intent.getIntExtra ( INTENT_EXTRA_COMMAND, -1 );
		
		try {
			if ( command == COMMAND_SEND ) {
				
				List < Record > records = intent.getParcelableArrayListExtra ( INTENT_EXTRA_RECORDS );
				send ( records, appKey, groupId, receiver ); 
			
			} else if ( command == COMMAND_RECEIVE ) {
			
				List < String > deviceIds = intent.getStringArrayListExtra ( INTENT_EXTRA_DEVICE_IDS );
				List < String > channels = intent.getStringArrayListExtra ( INTENT_EXTRA_CHANNELS );
				receive ( appKey, groupId, deviceIds, channels, receiver );
				
			} else {
				Log.e ( DEBUG_TAG, "unknown command" );
			}
		} catch ( LithouseClientException lite ) {
			sendError ( receiver, ERROR_LITHOUSE_CLIENT, lite.getMessage ( ) );
		} catch ( Exception e ) {
			// TODO: handle different exceptions
			sendError ( receiver, ERROR_LITHOUSE_CLIENT, "could not connect to server" );
		}
 		
		Log.d ( DEBUG_TAG, "service stopping");
	}
	
	private void send ( List < Record > records, String appKey, String groupId, ResultReceiver receiver ) 
			throws ClientProtocolException, ParseException, IOException, JSONException {
		HttpHelper.send ( records, appKey, groupId );
		
		if ( receiver != null ) {
			receiver.send ( STATUS_FINISHED, Bundle.EMPTY );
		}
	}
	
	private void receive ( String appKey, String groupId, List < String > deviceIds, List < String > channels, 
						   ResultReceiver receiver ) throws ParseException, ClientProtocolException, IOException, JSONException {
		
		if ( receiver == null ) return;
		
		Bundle bundle = new Bundle ( );		
		bundle.putParcelableArrayList ( INTENT_EXTRA_RECORDS, 
				HttpHelper.receive ( appKey, groupId, deviceIds, channels ) );
		
		receiver.send ( STATUS_FINISHED, bundle );
	}
	
	private void sendError ( ResultReceiver receiver, int errorType, String errorMessage ) {
		if ( receiver == null ) return;
		
		Bundle bundle = new Bundle ( );
		bundle.putInt ( INTENT_EXTRA_ERROR_TYPE, errorType );
		bundle.putString ( Intent.EXTRA_TEXT, errorMessage );
		Log.e ( DEBUG_TAG, errorMessage );
		
		receiver.send ( STATUS_ERROR, bundle );
	}
}
