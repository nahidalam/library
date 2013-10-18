package com.lithouse.client.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Record implements Parcelable {
	private final String deviceId;
	private final String channel;
	private final String data;
	private final String timestamp;
	
	public static final Parcelable.Creator < Record > CREATOR = new Parcelable.Creator < Record > ( ) {
        public Record createFromParcel ( Parcel in ) {
            return new Record ( in ); 
        }

        public Record [] newArray ( int size ) {
            return new Record [ size ];
        }
    };
	
    public Record ( String deviceId, String channel, String data, String timestamp ) {
    	this.deviceId = deviceId;
    	this.channel = channel;
    	this.data = data;
    	this.timestamp = timestamp;
    }
    
    public Record ( String deviceId, String channel, String data ) {
    	this ( deviceId, channel, data, null );
    }
    
    public Record ( Parcel in ) {
    	deviceId = in.readString ( );
    	channel = in.readString ( );
    	data = in.readString ( );
    	timestamp = in.readString ( );
    }

	public String getDeviceId () {
		return deviceId;
	}
	public String getChannel () {
		return channel;
	}
	public String getData () {
		return data;
	}
	public String getTimestamp () {
		return timestamp;
	}
		
	@Override
	public int describeContents () {
		return 0;
	}
	@Override
	public void writeToParcel ( Parcel out, int flag ) {
		out.writeString ( deviceId );
		out.writeString ( channel );
		out.writeString ( data );
		out.writeString ( timestamp );
	}
	
	@Override
	public String toString ( ) {
		return "{\"deviceId\":\"" + deviceId + "\"," +
				"\"channel\":\"" + channel + "\"," +
				"\"data\":\"" + data + "\"}";
	}
}
