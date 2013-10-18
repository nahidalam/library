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
	
	public interface Callback {
		public void onSuccess ( List < Record > results );
		public void onFailure ( Throwable t );
	}
	
	private final String appKey;
	private final Context context;
	private Map < Callback, LithouseResultReceiver > receiverMap 
		= new HashMap < Callback, LithouseResultReceiver > (  );  
	
	public LithouseService ( Context context, String appKey ) {
		if ( appKey == null || appKey.isEmpty ( ) ) {
			throw new IllegalArgumentException ( "missing appKey" );
		}
		
		this.context = context;
		this.appKey = appKey;
	}
	
	public void receive ( Callback callback, String groupId, 
						  List < String > deviceIds, List < String > channels ) {
		Log.d ( DEBUG_TAG, "receive ( )" );
		
		Intent intent = prepareIntent ( callback, groupId ); 
		intent.putExtra ( INTENT_EXTRA_COMMAND, COMMAND_RECEIVE );
		if ( deviceIds != null ) {
			intent.putStringArrayListExtra ( INTENT_EXTRA_DEVICE_IDS, new ArrayList < String > ( deviceIds ) );
		}
		if ( channels != null ) {
			intent.putStringArrayListExtra ( INTENT_EXTRA_CHANNELS, new ArrayList < String > ( channels ) );
		}
		
		context.startService ( intent );    
	}
	
	public void send ( Callback callback, String groupId, ArrayList < Record > records ) {
		Log.d ( DEBUG_TAG, "send ( )" );
		
		if ( records == null || records.isEmpty ( ) ) {
			throw new IllegalArgumentException ( "missing record list" );			
		}
		
		Intent intent = prepareIntent ( callback, groupId ); 
		intent.putExtra ( INTENT_EXTRA_COMMAND, COMMAND_SEND );
		intent.putParcelableArrayListExtra ( INTENT_EXTRA_RECORDS, records );
		
		context.startService ( intent );    
	}

	public void removeAllCallbacks ( ) {
		for ( LithouseResultReceiver receiver : receiverMap.values ( ) ) {
			receiver.removeCallback ( );
		}
	}
	
	private Intent prepareIntent ( Callback callback, String groupId ) {
		if ( groupId == null || groupId.isEmpty ( ) ) {
			throw new IllegalArgumentException ( "missing groupId" );
		}
		
		Intent intent = new Intent ( Intent.ACTION_SYNC, null, context, LithouseRESTService.class );
	 	
		intent.putExtra ( INTENT_EXTRA_APP_KEY, appKey ) ;
		intent.putExtra ( INTENT_EXTRA_GROUP_ID, groupId ) ;
		if ( callback != null ) {
			intent.putExtra ( INTENT_EXTRA_RECEIVER, getLithouseReceiver ( callback )  );
		}
		
		return intent;
	}
	
	private LithouseResultReceiver getLithouseReceiver ( Callback callback ) {
		LithouseResultReceiver lithouseReceiver = receiverMap.get ( callback );
		
		if ( lithouseReceiver == null ) {
			lithouseReceiver = new LithouseResultReceiver ( new Handler ( ), callback );			
			receiverMap.put ( callback, lithouseReceiver );
		}
		
		return lithouseReceiver;
	}
}
