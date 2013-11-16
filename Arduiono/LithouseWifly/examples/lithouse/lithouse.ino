#include <SPI.h>
#include <WiFly.h>
#include <Lithouse.h>
#include "Credentials.h"

char deviceKey [] = "a28e6923-3cf5-4ba1-ad35-358df76cbd6a";
//char ledChannel [] = "LED";
WiFlyClient  client;
LithouseClient litClient ( client, deviceKey );
const int MAX_SIZE = 1;
LithouseRecord records [MAX_SIZE]; 

int LED_OUT = 7;

char fsrChannel [] = "FSR";
char dataRecord [10];
int fsrPressure = 0;
int FSR_IN = 0;
int fsrReading;
char fsrBuffer[5];

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
  
  delay ( 10000 );
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
  
  int currentPressure = (fsrReading > 700) ? (fsrReading/10) : 0;
  currentPressure = (currentPressure < 100) ? currentPressure : 99;
  if ( currentPressure != fsrPressure ) {
    fsrPressure = currentPressure; 
    itoa ( fsrPressure, fsrBuffer, 10 );
    records[0].updateRecord (fsrChannel, fsrBuffer );
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

