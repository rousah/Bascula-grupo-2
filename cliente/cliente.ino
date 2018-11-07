#include "WiFi.h"
#include "AsyncUDP.h"
#include <TimeLib.h>
#include <ArduinoJson.h>


// --- Escucha del infrarrojo ---
int ledPin = 5;  // LED en el Pin 5 del Arduino
int pirPin = 21; // Input para HC-S501
int pirValue; // Para guardar el valor del pirPin

const char * ssid = "EQUIPO_2";
const char * password = "HoLaMuNDo";

//const char * ssid = "MASMOVIL_2sPj";
//const char * password = "6QSb3hZgFN22";


AsyncUDP udp;
StaticJsonBuffer<300> jsonBuffer;                 //tamaño maximo de los datos
JsonObject& envio = jsonBuffer.createObject();    //creación del objeto "envio"



void leerInfrarrojos() {

  if(digitalRead(pirPin)== HIGH) {
   Serial.println("Detectado movimiento por el sensor pir");
   digitalWrite(ledPin, HIGH);
   delay(1000);
   digitalWrite(ledPin, LOW);
  }
  else {
    Serial.println("Nada");
    }
  
}

void setup()
{
    Serial.begin(115200);
    // Cambiar cada vez que se tenga que subir
    setTime (20, 45, 0, 21, 10, 2018); //hora minuto segundo dia mes año
      
    WiFi.mode(WIFI_STA);
    WiFi.begin(ssid, password);
    if (WiFi.waitForConnectResult() != WL_CONNECTED) {
        Serial.println("WiFi Failed");
        while(1) {
            delay(1000);
        }
    }

if(udp.listen(1234)) {
        Serial.print("UDP Listening on IP: ");
        Serial.println(WiFi.localIP());
        
        udp.onPacket([](AsyncUDPPacket packet) {

            Serial.write(packet.data(), packet.length());
            Serial.println();

        });

    }

    pinMode(ledPin, OUTPUT);
  pinMode(pirPin, INPUT);
  digitalWrite(ledPin, LOW);
}


void loop()
{
  AsyncUDP udp;
  StaticJsonBuffer<300> jsonBuffer;                 //tamaño maximo de los datos
  JsonObject& envio = jsonBuffer.createObject();    //creación del objeto "envio"
  char texto[300];
  
  String date = String(day())+"-"+String(month())+"-"+String(year());
  String timeNow = String(hour())+":"+String(minute())+":"+String(second());

  envio["Date"] = date;
  envio["Time"] = timeNow;

  leerInfrarrojos();
  delay(5000);
}
