package com.lithouse.client;

import static com.lithouse.client.Constants.DEBUG_TAG;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.lithouse.client.exception.LithouseClientException;
import com.lithouse.client.model.Record;

class HttpHelper {
	private static final String apiEndpoint = "http://alpha-api.elasticbeanstalk.com/v1/groups/";
	private static final int CONNECTION_TIMEOUT = 10000;
	
	private static String buildUri ( String appKey, String groupId ) {
		String uri = apiEndpoint + groupId + "/records?appKey=" + appKey;
		Log.d ( DEBUG_TAG, uri );
		return uri; 
	}
	
	public static void send ( List < Record > records, String appKey, String groupId ) 
			throws ClientProtocolException, IOException, ParseException, JSONException {
		if ( records == null || records.isEmpty ( ) ) {
			throw new LithouseClientException ( "missing record list" );			
		}
		
		HttpPost httpPost = new HttpPost ( buildUri ( appKey, groupId ) );
	    	    
	    httpPost.setHeader ( "Content-type", "application/json" );
	    httpPost.setEntity ( new StringEntity ( serializeRecordsToJSON ( records ) ));

        convertHttpResponseToJSON ( getHttpClient ( ).execute ( httpPost ) );
	}
	
	public static ArrayList < Record > receive ( String appKey, String groupId, 
					List < String > deviceIds, List < String > channels ) 
							throws ParseException, ClientProtocolException, IOException, JSONException {
		
		StringBuilder uri = new StringBuilder ( buildUri ( appKey, groupId ) );
		
		if ( deviceIds != null && !deviceIds.isEmpty ( ) ) {
			for ( String deviceId : deviceIds ) {
				uri.append ( "&deviceId=" );
				uri.append ( deviceId );
			}
		}
		
		if ( channels != null && !channels.isEmpty ( ) ) {
			for ( String channel : channels ) {
				uri.append ( "&channel=" );
				uri.append ( channel );
			}
		}
		
	    return deSerializeJSONToReocrds ( convertHttpResponseToJSON ( 
	    			getHttpClient ( ).execute ( new HttpGet ( uri.toString ( )))));
	}
	
	private static JSONObject convertHttpResponseToJSON ( HttpResponse response ) 
			throws ParseException, IOException, JSONException {
		JSONObject httpReturn; 
				
		HttpEntity entity = response.getEntity ( );
		if ( entity != null ) {
			String returnString = EntityUtils.toString ( entity );
			Log.d ( DEBUG_TAG, returnString );
			httpReturn = new JSONObject ( returnString );
        } else {
        	throw new LithouseClientException ( "server failed to respond" );
        }
		
		int httpStatusCode = response.getStatusLine ( ).getStatusCode ( );
		if ( httpStatusCode >= 300 ) {
			throw new LithouseClientException ( httpReturn.getString ( "message" ) );
		}
		
		return httpReturn;
	}
	
	private static String serializeRecordsToJSON ( List < Record > records ) {
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
	
	private static ArrayList < Record > deSerializeJSONToReocrds ( JSONObject jsonResult ) {
		ArrayList < Record > records = new ArrayList < Record > ( );
		
		if ( jsonResult != null ) {
			try {
				JSONArray jsonRecords = jsonResult.getJSONArray ( "records" );
				
				if ( jsonRecords != null ) {
					for ( int i =0; i < jsonRecords.length ( ); i++ ) {
						JSONObject jsonRecord = jsonRecords.getJSONObject ( i );
						records.add ( new Record ( 
								jsonRecord.getString ( "deviceId" ),
								jsonRecord.getString ( "channel" ),
								jsonRecord.getString ( "data" ),
								jsonRecord.getString ( "timestamp" )));
					}
				} 
			} catch ( JSONException e ) {
				Log.e ( DEBUG_TAG, e.getMessage ( ) );
			}
		}
		
		return records;
	}

	private static HttpClient getHttpClient ( ) {
		HttpClient httpClient = new DefaultHttpClient ( );
		
		final HttpParams httpParameters = httpClient.getParams ( );
		HttpConnectionParams.setConnectionTimeout ( httpParameters, CONNECTION_TIMEOUT );

		return httpClient;
	}
}
