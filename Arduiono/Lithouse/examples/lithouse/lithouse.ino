#include <SPI.h>
#include <Ethernet.h>
#include <Lithouse.h>

char deviceKey [] = "a28e6923-3cf5-4ba1-ad35-358df76cbd6a";
//char ledChannel [] = "LED";
byte mac[] = { 0x00, 0x13, 0x20, 0xFF, 0x16, 0x7E };
EthernetClient client;
LithouseClient litClient ( client, deviceKey );
const int MAX_SIZE = 1;
LithouseRecord records [MAX_SIZE]; 

int LED_OUT = 5;

char fsrChannel [] = "FSR";
char dataRecord [10];
int fsrPressure = 0;
int FSR_IN = 0;
int fsrReading;

void setup() {
  pinMode(LED_OUT, OUTPUT);
  Serial.begin ( 9600 );
  while (!Serial) {
    ; // wait for serial port to connect. Needed for Leonardo only
  }
  
  Serial.println("connecting...to ethernet");  
  // start the Ethernet connection:
  if (Ethernet.begin(mac) == 0) {
    Serial.println("Failed to configure Ethernet using DHCP");
    while ( 1 ) {
      // Hang on failure  
    }    
  }
  // give the Ethernet shield a second to initialize:
  delay(1000);
  Serial.println("connecting...to lithouse");  
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
  
  int currentPressure = (fsrReading > 700) ? 80 : 0;  
  if ( currentPressure != fsrPressure ) {
    fsrPressure = currentPressure;
    if ( currentPressure >= 80 ) {
      records[0].updateRecord (fsrChannel, "80");
    } else {
      records[0].updateRecord (fsrChannel, "0");
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

