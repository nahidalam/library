package com.lithouse.client;

import java.util.List;

import com.lithouse.client.LithouseService.Receiver;
import com.lithouse.client.model.Record;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import static com.lithouse.client.Constants.*;

class LithouseResultReceiver extends ResultReceiver {

	private Receiver mReceiver;
	
	public LithouseResultReceiver ( Handler handler, Receiver receiver ) {
		super ( handler );
		this.mReceiver = receiver;
	}
	
	public void setReceiver ( Receiver receiver ) {
        mReceiver = receiver;
    }

	private Exception prepareException ( Bundle resultData ) {
		int errorType = resultData.getInt ( INTENT_EXTRA_ERROR_TYPE );
		String errorMessage = resultData.getString ( Intent.EXTRA_TEXT );	
		
		if ( errorType == ERROR_ILLEGAL_ARGUMENT ) {
			return new IllegalArgumentException ( errorMessage );
		}
		return new Exception ( "Internal error" );
	}
	
	@Override
    protected void onReceiveResult ( int resultCode, Bundle resultData ) {
        if (mReceiver != null) {
        	
        	if ( resultCode == STATUS_ERROR ) {
        		mReceiver.onFailure ( prepareException ( resultData ) );
        	} else if ( resultCode == STATUS_RUNNING ) {
        		//mReceiver.onReceiveResult ( resultCode, null, null );
        	} else if ( resultCode == STATUS_FINISHED ) {
        		List < Record > records = resultData.getParcelableArrayList ( INTENT_EXTRA_RECORDS );
        		mReceiver.onSuccess ( records );
        	}
             
        }
    }
}
