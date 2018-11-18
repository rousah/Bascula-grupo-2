#include "WiFi.h"
#include "AsyncUDP.h"
#include <TimeLib.h>
#include <ArduinoJson.h>

//const char * ssid = "EQUIPO_2";
//const char * password = "HoLaMuNDo";

const char * ssid = "MASMOVIL_2sPj";
const char * password = "6QSb3hZgFN22";

AsyncUDP udp;
StaticJsonBuffer<200> jsonBuffer;                 //tamaño maximo de los datos
JsonObject& envio = jsonBuffer.createObject();    //creación del objeto "envio"

void setup()
{
    Serial.begin(115200);
    setTime (9, 15, 0, 7, 10, 2018); //hora minuto segundo dia mes año
      
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
    delay(1000);
    char texto[200];
    
    envio["Hora"]=hour();         //Datos introducidos en el objeto "envio"
    envio["Minuto"]=minute();     //3 campos 
    envio["Segundo"]=second();    //
      
    envio.printTo(texto);         //paso del objeto "envio" a texto para transmitirlo

    udp.broadcastTo(texto,1234);  //se emvía por el puerto 1234 el JSON 
                                  //como texto
    
}
