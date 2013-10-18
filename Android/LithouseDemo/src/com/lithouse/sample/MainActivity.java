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
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;

public class MainActivity extends Activity {
	private static final String DEBUG_TAG = "DEMO";
	private final String appKey = "4e6f08f3-6b11-4fd3-bea5-f76081820b8c";
	//You may target different groups, devices and channels in different send/receive calls;
	private final String groupId = "1655c124-c3a7-4303-b28f-081594545eb4";
	private final String deviceId = "0a894a89-63fd-4687-b096-efe67991da84";
	
	private final String sendChannel = "LED1";
	private final String receiveChannel = "FSR1";
	
	private final LithouseService mLithouseService = new LithouseService ( this, appKey );	
	
	private EditText editTextSend;
	private LithouseService.Receiver mSendCallback = new LithouseService.Receiver () {

		@Override
		public void onSuccess ( List < Record > results ) {
			Log.d ( DEBUG_TAG, "records sent to server" );
			Toast.makeText ( MainActivity.this, "data is sent", Toast.LENGTH_LONG ).show ( );
		}

		@Override
		public void onFailure ( Exception e ) {
			Log.e ( DEBUG_TAG, e.toString ( ) );
			Toast.makeText ( MainActivity.this, "failed to send data", Toast.LENGTH_LONG ).show ( );
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
				
			}
		} );
		
		editTextSend = ( EditText ) findViewById ( R.id.editTextSend );
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
		mLithouseService.stopAllReceivers ( );
    }
	
}
