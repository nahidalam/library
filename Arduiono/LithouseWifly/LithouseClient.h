#ifndef _LITHOUSECLIENT_H_
#define _LITHOUSECLIENT_H_

#include <LithouseConstant.h>
#include <LithouseRecord.h>
#include <Wifly.h>

class LithouseClient
{
public:
	LithouseClient ( WiFlyClient&_client, const char* deviceKey );
	
	int receive ( LithouseRecord records [], int MAX_SIZE );
	int send ( LithouseRecord records [], int recordCount );

private:
	int readLine ( );
	void createRequestBody ( LithouseRecord records [], int recordCount );
	int parseResponseBody ( LithouseRecord records [], int MAX_SIZE );
	char* extractNextJSONValue ( const char* jsonString, char* value );

	WiFlyClient& _client;

	static const int MAX_URI_LENGTH = 75;
	static const int MAX_INPUT_LINE_LENGTH = 300;
	static const int API_PORT = 80;
	static const int API_CALL_DELAY = 2000;

	static const char* API_ENDPOINT;
	static const char* USER_AGENT;
	static const char* CONTENT_TYPE;
	char _apiUri [MAX_URI_LENGTH];
	char _requestBodyBuffer [MAX_INPUT_LINE_LENGTH];
};


#endif