#include <LithouseClient.h>
#include <SPI.h>

const char* LithouseClient::API_ENDPOINT = "alpha-api.elasticbeanstalk.com";
const char* LithouseClient::USER_AGENT = "User-Agent: Arduino-lib/1.0";
const char* LithouseClient::CONTENT_TYPE = "Content-Type: application/json";

LithouseClient::LithouseClient ( WiFlyClient&_client, const char* groupId, const char* groupKey, const char* deviceId )
		: _client (_client )
{
	strcpy ( _apiUri, "/v1/groups/" );
	strcat ( _apiUri, groupId );
	strcat ( _apiUri, "/records/" );
	strcat ( _apiUri, deviceId );
	strcat ( _apiUri, "?groupKey=" );
	strcat ( _apiUri, groupKey );
	strcat ( _apiUri, " HTTP/1.0" );
}

int LithouseClient::send ( LithouseRecord records [], int recordCount ) {
	if ( recordCount < 1 ) {
		return recordCount;
	}

	if ( _client.connect ( API_ENDPOINT, API_PORT ) ) {
		Serial.println ( "post connected" );
		createRequestBody ( records, recordCount );

		_client.print ( "POST " );
		_client.println ( _apiUri ); 
		_client.println ( CONTENT_TYPE );
		_client.println ( USER_AGENT );
		_client.println ( "Connection: close" );
		_client.print ( "Content-Length: " );
		_client.println ( strlen ( _requestBodyBuffer ) );
		_client.println ( );

		_client.println ( _requestBodyBuffer );

	} else {
		Serial.println ( "connection failed" );
		return -1;
	}
  
	delay ( API_CALL_DELAY );
	_client.stop();
	
	return recordCount;
}

int LithouseClient::receive ( LithouseRecord records [], int MAX_SIZE ) { 
	if ( _client.connect ( API_ENDPOINT, API_PORT ) ) {
		Serial.println ( "get connected" );
		_client.print ( "GET " );
		_client.println ( _apiUri );
		_client.println ( USER_AGENT );
		_client.println ( );		
	} else {
		Serial.println ( "connection failed" );
		return -1;
	}
  
	while ( _client.connected ( ) ) {
		readLine ( );
		
	}
	Serial.println ( _requestBodyBuffer );
	
	_client.stop ( );
	delay ( API_CALL_DELAY );
	return parseResponseBody ( records, MAX_SIZE );
}

int LithouseClient::readLine ( ) {
    char character;
    int currentLength = 0;
	
	while ( _client.connected ( ) && ( character = _client.read ( ) ) != '\n' ) {
		if ( character != '\r' && character != -1 ) {
			if ( (currentLength + 1) == MAX_INPUT_LINE_LENGTH ) {
				break;
			}
			_requestBodyBuffer [ currentLength++ ] = character;
		} 
	}
    
	_requestBodyBuffer [ currentLength ] = 0;
	return currentLength;
}

//void LithouseClient::printRecrods ( LithouseRecord records [], int recordCount ) {
//	if ( recordCount < 1 ) return; 
//	_client.print ( "{\"" );
//	_client.print ( Constants::RECORDS );
//	_client.print ( "\":[" );
//	_client.print ( records [ 0 ] );
//
//	for ( int i=1; i < recordCount; i++ ) {
//		_client.print ( "," );
//		_client.print ( records [ i ] );
//	}
//	_client.println ( "]}" );
//}

void LithouseClient::createRequestBody ( LithouseRecord records [], int recordCount ) {
	strcpy ( _requestBodyBuffer, "" ); 
	if ( recordCount < 1 ) return; 
	strcat ( _requestBodyBuffer, "{\"" );
	strcat ( _requestBodyBuffer, Constants::RECORDS );
	strcat ( _requestBodyBuffer, "\":[" );
	records [ 0 ].concatRecord ( _requestBodyBuffer, MAX_INPUT_LINE_LENGTH );

	for ( int i=1; i < recordCount; i++ ) {
		strcat ( _requestBodyBuffer, "," );
		records [ i ].concatRecord ( _requestBodyBuffer, MAX_INPUT_LINE_LENGTH );

	}
	strcat ( _requestBodyBuffer, "]}" );

	Serial.println ( _requestBodyBuffer );
}

int LithouseClient::parseResponseBody ( LithouseRecord records [], int MAX_SIZE ) {
	int count = 0;
	char data [Constants::MAX_VALUE_LENGTH], channel [Constants::MAX_VALUE_LENGTH];
	char* ptr = strstr ( _requestBodyBuffer, Constants::CHANNEL );
	
	while ( ptr != NULL ) {
		if ( ( ptr = extractNextJSONValue ( ptr, channel ) ) == NULL ) break;
		if ( ( ptr = strstr ( ptr, Constants::DATA ) ) == NULL ) break;
		if ( ( ptr = extractNextJSONValue ( ptr, data ) ) == NULL ) break;
		
		records [ count++ ].updateRecord ( channel, data );
		ptr = strstr ( ptr, Constants::CHANNEL );
	}

	return count;
}

char* LithouseClient::extractNextJSONValue ( const char* jsonString, char* value ) {
	char* ptr = strstr ( jsonString, ":\"" );
	if ( ptr == NULL ) return ptr;

	ptr += 2;
	int i;
	for ( i=0; ptr [i] && ptr [i] != '"' ; i++ ) {
		value [i] = ptr[i];
	}
	value [i] = 0;
	ptr += i;

	Serial.println ( value );
	return ptr;
}