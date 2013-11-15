package com.lithouse.sample;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.lithouse.client.model.Record;
import com.lithouse.client.LithouseService;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.View;

public class MainActivity extends Activity {
	private static final String DEBUG_TAG = "DEMO";
	//appKey, groupId and deviceId can be retrieved from http://lithouse.co
	private final String appKey = "YOUR_APP_KEY";
	//You may target different groups, devices and channels in different send and receive calls;
	private final String groupId = "TARGET_GROUP_ID";
	private final String deviceId = "TARGET_DEVICE_ID";
	
	private final String sendChannel = "LED";
	private final String receiveChannel = "FSR";
	
	//Initialize the Lithouse service handler
	private final LithouseService mLithouseService = new LithouseService ( this, appKey );	
	
	private EditText editTextSend;
	private TextView textViewReceive;
	
	//Callback for 'send' api call
	private LithouseService.Callback mSendCallback = new LithouseService.Callback( ) {
		
		@Override
		public void onSuccess ( List < Record > results ) {
			Log.d ( DEBUG_TAG, "records sent to server" );
			Toast.makeText ( MainActivity.this, "data is sent", Toast.LENGTH_LONG ).show ( );
		}

		@Override
		public void onFailure ( Throwable t ) {
			Log.e ( DEBUG_TAG, t.getMessage ( ) );
			Toast.makeText ( MainActivity.this, "failed to send data", Toast.LENGTH_LONG ).show ( );
		}

	};
	
	//Callback for 'receive' api call
	private LithouseService.Callback mReceiveCallback = new LithouseService.Callback( ) {
		
		@Override
		public void onSuccess ( List < Record > results ) {
			Log.d ( DEBUG_TAG, "records received from server" );
			Toast.makeText ( MainActivity.this, results.size ( ) + " record received", Toast.LENGTH_LONG ).show ( );
			
			//since we are targeting a channel of a unique device 
			if ( results.size ( ) == 1 ) {
				textViewReceive.setText ( results.get ( 0 ).getData ( ) );
			}
		}

		@Override
		public void onFailure ( Throwable t ) {
			Log.e ( DEBUG_TAG, t.getMessage ( ) );
			Toast.makeText ( MainActivity.this, "failed to receive data", Toast.LENGTH_LONG ).show ( );
		}

	};  

	
	@Override
	protected void onCreate ( Bundle savedInstanceState ) {
		super.onCreate ( savedInstanceState );
		setContentView ( R.layout.activity_main );
		
		final Button buttonSend = ( Button ) findViewById ( R.id.buttonSend );
		buttonSend.setOnClickListener ( new View.OnClickListener( ) {
			
			@Override
			public void onClick ( View arg0 ) {				
				Log.d ( DEBUG_TAG, "send button clicked" );
				
				ArrayList < Record > recordsForSend = new ArrayList < Record > ( );
				recordsForSend.add ( new Record ( deviceId, sendChannel, 
						editTextSend.getText ( ).toString ( ) ) );
				
				mLithouseService.send ( mSendCallback, groupId, recordsForSend );
			}
		} );
		
		final Button buttonReceive = ( Button ) findViewById ( R.id.buttonReceive );
		buttonReceive.setOnClickListener ( new View.OnClickListener( ) {
			
			@Override
			public void onClick ( View arg0 ) {				
				Log.d ( DEBUG_TAG, "receive button clicked" );
				
				mLithouseService.receive ( mReceiveCallback, groupId, Arrays.asList ( deviceId ), Arrays.asList ( receiveChannel ) );
				//Possible usages
				//All records from all devices of this group
				//mLithouseService.receive ( mReceiveCallback, groupId, null, null );
				//All records from all channels of this device
				//mLithouseService.receive ( mReceiveCallback, groupId, Arrays.asList ( deviceId ), null );
				//All records from different device with the same channel name
				//mLithouseService.receive ( mReceiveCallback, groupId, null, Arrays.asList ( receiveChannel ) );
				
			}
		} );
		
		editTextSend = ( EditText ) findViewById ( R.id.editTextSend );
		textViewReceive = ( TextView ) findViewById ( R.id.textViewReceive );
	}

	@Override
	public boolean onCreateOptionsMenu ( Menu menu ) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater ( ).inflate ( R.menu.main, menu );
		return true;
	}
	
	@Override
    public void onPause() {
		super.onPause();
		mLithouseService.removeAllCallbacks ( );
    }
	
}
