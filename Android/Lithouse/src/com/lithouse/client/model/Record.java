package com.lithouse.client.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Record implements Parcelable {
	private final String deviceId;
	private final String channel;
	private final String data;
	
	public static final Parcelable.Creator < Record > CREATOR = new Parcelable.Creator < Record > ( ) {
        public Record createFromParcel ( Parcel in ) {
            return new Record ( in ); 
        }

        public Record [] newArray ( int size ) {
            return new Record [ size ];
        }
    };
	
    public Record ( String deviceId, String channel, String data ) {
    	this.deviceId = deviceId;
    	this.channel = channel;
    	this.data = data;
    }
    
    public Record ( Parcel in ) {
    	deviceId = in.readString ( );
    	channel = in.readString ( );
    	data = in.readString ( );
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
		
	@Override
	public int describeContents () {
		return 0;
	}
	@Override
	public void writeToParcel ( Parcel out, int flag ) {
		out.writeString ( deviceId );
		out.writeString ( channel );
		out.writeString ( data );		
	}
	
	@Override
	public String toString ( ) {
		return "{\"deviceId\":\"" + deviceId + "\"," +
				"\"channel\":\"" + channel + "\"," +
				"\"data\":\"" + data + "\"}";
	}
}
