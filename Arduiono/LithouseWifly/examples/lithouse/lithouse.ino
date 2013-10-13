#include <SPI.h>
#include <WiFly.h>
#include <Lithouse.h>
#include "Credentials.h"

//char apiEndpoint [] = "alpha-api.elasticbeanstalk.com";
char groupId [] = "1655c124-c3a7-4303-b28f-081594545eb4";
char groupKey [] = "83db345d-c34f-40b2-9d27-72f784bb26bc";
char deviceId [] = "0a894a89-63fd-4687-b096-efe67991da84";
//char userAgent [] = "User-Agent: Arduino-lib/1.0";
//char ledChannel [] = "LED1";
char fsrChannel [] = "FSR1";
char dataRecord [10];
WiFlyClient client;
LithouseClient litClient ( client, groupId, groupKey, deviceId );
const int MAX_SIZE = 1;
LithouseRecord records [MAX_SIZE]; 
int LED_OUT = 5;
int fsrPressure = 0;
int FSR_IN = 0;
int fsrReading;

void setup() {
  pinMode(LED_OUT, OUTPUT);
  Serial.begin ( 9600 );
  
  WiFly.begin ( );
  
  if ( !WiFly.join ( ssid, passphrase ) ) {
    Serial.println ( "Association failed." );
    while ( 1 ) {
      // Hang on failure.
    }
  }  
  Serial.println ( "Connected to wifi" );
}

void loop() {
  downloadLedState ( );
  uploadFSRState ( );
  
  delay ( 1000 );        
}

void uploadFSRState ( ) {
  fsrReading = analogRead ( FSR_IN );
  Serial.print("Analog reading = ");
  Serial.println(fsrReading);
  
  int currentPressure = (fsrReading > 800) ? 80 : 0;  
  if ( currentPressure != fsrPressure ) {
    fsrPressure = currentPressure;
    if ( currentPressure >= 80 ) {
      records[0].updateRecord (fsrChannel, "80%");
    } else {
      records[0].updateRecord (fsrChannel, "0%");
    }
    litClient.send ( records, 1 );   
  }
}

void downloadLedState ( ) {
  if ( litClient.receive ( records, MAX_SIZE ) == 1 ) {
    records[0].getData (dataRecord );
    
    if ( 0 == strcmp ( dataRecord, "on" ) ) {
      Serial.println ( "turn led on" );
      digitalWrite(LED_OUT, HIGH);
    } else {
      Serial.println ( "turn led off" );
      digitalWrite ( LED_OUT , LOW );    
    }
  }
}

//bool getBlinkEvent ( ) {
//  if ( client.connect ( apiEndpoint, 80 ) ) {
//    Serial.println ( "connected" );
//    client.println ( "GET /v1/developers/dsfkjre?apiKey=sdflkjoirj341 HTTP/1.0" );
//    client.println ( userAgent );
//    client.println ( );
//  } else {
//    Serial.println ( "connection failed" );
//    return false;
//  }
//  
//  char character;
//  String data = "" ;
//  while ( client.connected ( ) ) {
//    if ( client.available ( ) ) {
//      character = client.read ( );
//      if ( character != -1 ) {
//        data += character;
//      }
//    }
//  }
//  
//  Serial.print ( data );    
//  client.stop ( );
//  delay ( 2000 );
//}
//
//void postForceEvent ( String value ) {
// String data = "{\"records\":[{\"channel\":\"LED\", \"data\":\"mynewtest\"}]}";
//  if( client.connect ( apiEndpoint , 80 ) ) {
//    Serial.println ( data );
//    client.println ( "POST /v1/groups/1655c124-c3a7-4303-b28f-081594545eb4/records/fa5ab86f-f1c3-4e7e-8705-4d189d2d224e?groupKey=83db345d-c34f-40b2-9d27-72f784bb26bc HTTP/1.0" );
//    client.println ( "Content-Type: application/json" );
//    client.println ( userAgent );
//    client.println ( "Connection: close" );
//    client.print ( "Content-Length: " );
//    client.println ( data.length ( ) );
//    client.println ( );
//    client.print ( data );
//    client.println ( );
//  }
//  delay ( 2000 );
//  client.stop();
//}

