#include "WiFi.h"
#include "AsyncUDP.h"
#include <TimeLib.h>
#include <ArduinoJson.h>
#include <SR04.h>
#define TRIG_PIN 2
#define ECHO_PIN 4
// --- Escucha del ultrasonido ---
SR04 sr04 = SR04(ECHO_PIN,TRIG_PIN);
long a;

const char * ssid = "EQUIPO_2";
const char * password = "HoLaMuNDo";

//const char * ssid = "MASMOVIL_2sPj";
//const char * password = "6QSb3hZgFN22";

AsyncUDP udp;
StaticJsonBuffer<300> jsonBuffer;                 //tamaño maximo de los datos
JsonObject& envio = jsonBuffer.createObject();    //creación del objeto "envio"

/*CALCULAR ALTURA*/
/**
función que recibe la distancia y calcula basandose en la altura a la que
está el sensor restandole la distancia que hay del sensor a la cabeza
(distancia recogida por el sensor con la función "sr04.Distance()")
una vez obtenida esa altura se hace la transformación a metros
*/
float calcularAltura(int d)
{
  //altura de la báscula
  // en cm
  float altBasc = 200;
  //altura de la persona
  // en cm
  float altPers = altBasc - d;
  
  altPers = altPers/100;

  return altPers;
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
  
  if (Serial.available() > 0) {
    char command = (char) Serial.read();
    switch (command) {
     case 'H':
       Serial.println("Hola Mundo");
       break;
     case 'D':
       a = sr04.Distance();
        
       if(calcularAltura(a) <= 0 && calcularAltura(a) > 2.00)
       {
         Serial.print("2.00");
         Serial.println(" m");
         
         envio["Altura"]= 2.00;
       }
       else
       {
         Serial.print(calcularAltura(a));
         Serial.println(" m");
         envio["Altura"]= calcularAltura(a);
    
       }
        
          
       envio.printTo(texto);         //paso del objeto "envio" a texto para transmitirlo
    
       udp.broadcastTo(texto,1234);  //se emvía por el puerto 1234 el JSON 
                                      //como texto
    }
  }
  
  a = sr04.Distance();
        
  if(calcularAltura(a) <= 0 && calcularAltura(a) > 2.00)
  {
    Serial.print("2.00");
    Serial.println(" m");
    envio["Altura"]= 2.00;
  }
  else
  {
    Serial.print(calcularAltura(a));
    Serial.println(" m");
    envio["Altura"]= calcularAltura(a);
  
  }
        
          
  envio.printTo(texto);         //paso del objeto "envio" a texto para transmitirlo
    
  udp.broadcastTo(texto,1234);  //se emvía por el puerto 1234 el JSON 
                                      //como texto
  delay(5000);
}
