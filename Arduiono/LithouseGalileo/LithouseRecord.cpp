#include <LithouseRecord.h>

LithouseRecord::LithouseRecord ( ) { }


LithouseRecord::LithouseRecord ( const char* channel, const char* data ) {
	//TODO: consider overflow
	strcpy ( _channel, channel );
	strcpy ( _data, data );
}

void LithouseRecord::updateRecord ( const char* channel, const char*  data ) {
	//TODO: consider overflow
	strcpy ( _channel, channel );
	strcpy ( _data, data );
}

size_t LithouseRecord::printTo ( Print& print ) const {
	int len = print.print ( "{\"" );
	len += print.print ( Constants::CHANNEL );
	len += print.print ( "\":\"" );
	len += print.print ( _channel );
	len += print.print ( "\",\"" );

	len += print.print ( Constants::DATA );
	len += print.print ( "\":\"" );
	len += print.print ( _data );
	
	len += print.print ( "\"}" );
	return len;
}

//TODO: consider overflow
void LithouseRecord::concatRecord ( char* buffer, int MAX_SIZE ) {
	strcat ( buffer, "{\"" );
	strcat ( buffer, Constants::CHANNEL );
	strcat ( buffer, "\":\"" );
	strcat ( buffer, _channel );
	strcat ( buffer, "\",\"" );

	strcat ( buffer, Constants::DATA );
	strcat ( buffer, "\":\"" );
	strcat ( buffer, _data );
	
	strcat ( buffer, "\"}" );
}

void LithouseRecord::getData ( char* data ) {
	strcpy ( data, _data );
}