package com.lithouse.client;

class Constants {
	static final int STATUS_RUNNING = 0;
	static final int STATUS_FINISHED = 1;
	static final int STATUS_ERROR = 2;
	
	static final String DEBUG_TAG = "LITHOUSE";
	static final String INTENT_EXTRA_RECEIVER = "com.lithouse.client.receiver";
	static final String INTENT_EXTRA_APP_KEY = "com.lithouse.client.appKey";
	static final String INTENT_EXTRA_GROUP_ID = "com.lithouse.client.groupId";
	static final String INTENT_EXTRA_RECORDS = "com.lithouse.client.records";
	static final String INTENT_EXTRA_ERROR_TYPE = "com.lithouse.client.errorType";
	
	static final int ERROR_INTERNAL_ERROR = 10;
	static final int ERROR_ILLEGAL_ARGUMENT = 11;	
}
