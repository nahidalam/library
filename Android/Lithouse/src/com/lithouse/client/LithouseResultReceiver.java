package com.lithouse.client;

import java.util.List;

import com.lithouse.client.LithouseService.Callback;
import com.lithouse.client.exception.LithouseClientException;
import com.lithouse.client.model.Record;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import static com.lithouse.client.Constants.*;

class LithouseResultReceiver extends ResultReceiver {

	private Callback callback;
	
	public LithouseResultReceiver ( Handler handler, Callback callback ) {
		super ( handler );
		this.callback = callback;
	}
	
	public void removeCallback ( ) {
		callback = null;
	}

	private Throwable prepareException ( Bundle resultData ) {
		int errorType = resultData.getInt ( INTENT_EXTRA_ERROR_TYPE );
		String errorMessage = resultData.getString ( Intent.EXTRA_TEXT );	
		
		if ( errorType == ERROR_ILLEGAL_ARGUMENT ) {
			return new IllegalArgumentException ( errorMessage );
		} else if ( errorType == ERROR_LITHOUSE_CLIENT ) {
			return new LithouseClientException ( errorMessage );
		}
		
		return new Exception ( "Internal error" );
	}
	
	@Override
    protected void onReceiveResult ( int resultCode, Bundle resultData ) {
        if ( callback != null ) {
        	
        	if ( resultCode == STATUS_ERROR ) {
        		callback.onFailure ( prepareException ( resultData ) );
        	} else if ( resultCode == STATUS_RUNNING ) {
        		//callback.onReceiveResult ( resultCode, null, null );
        	} else if ( resultCode == STATUS_FINISHED ) {
        		List < Record > records = resultData.getParcelableArrayList ( INTENT_EXTRA_RECORDS );
        		callback.onSuccess ( records );
        	}
             
        }
    }
}
