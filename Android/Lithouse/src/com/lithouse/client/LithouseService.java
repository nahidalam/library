package com.lithouse.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.lithouse.client.model.Record;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import static com.lithouse.client.Constants.*;

public class LithouseService {
	
	private final String appKey;
	private final Context context;
	private Map < Receiver, LithouseResultReceiver > sendReceivers 
		= new HashMap < LithouseService.Receiver, LithouseResultReceiver > (  );  
	
	public LithouseService ( Context context, String appKey ) {
		this.context = context;
		this.appKey = appKey;
	}
	
	public void send ( Receiver receiver, String groupId, ArrayList < Record > records ) {
		Log.d ( DEBUG_TAG, "send ( )" );
		
		final Intent intent = new Intent ( Intent.ACTION_SYNC, null, context, LithouseRESTService.class );
	 	
		intent.putExtra ( INTENT_EXTRA_APP_KEY, appKey ) ;
		intent.putExtra ( INTENT_EXTRA_GROUP_ID, groupId ) ;
		intent.putExtra ( INTENT_EXTRA_RECEIVER, getLithouseReceiver ( sendReceivers, receiver )  );
		intent.putParcelableArrayListExtra ( INTENT_EXTRA_RECORDS, records );
		//intent.putExtra("command", "query");
	    context.startService ( intent );    
	}

	public void stopAllReceivers ( ) {
		stopAllReceivers ( sendReceivers );
	}
	
	private void stopAllReceivers ( Map < Receiver, LithouseResultReceiver > sendReceivers ) {
		for ( LithouseResultReceiver receiver : sendReceivers.values ( ) ) {
			receiver.setReceiver ( null );
		}
	}
	
	private LithouseResultReceiver getLithouseReceiver ( 
			Map < Receiver, LithouseResultReceiver > receivers, Receiver receiver ) {
		LithouseResultReceiver lithouseReceiver = receivers.get ( receiver );
		
		if ( lithouseReceiver == null ) {
			lithouseReceiver = new LithouseResultReceiver ( new Handler ( ), receiver );			
			receivers.put ( receiver, lithouseReceiver );
		}
		
		return lithouseReceiver;
	}
	
	public interface Receiver {
		public void onSuccess ( List < Record > results );
		public void onFailure ( Exception e );
	}
}
