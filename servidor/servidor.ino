#include <M5Stack.h>

#include "WiFi.h"
#include "AsyncUDP.h"
#include <ArduinoJson.h>

const char * ssid = "EQUIPO_2";
const char * password = "HoLaMuNDo";

//const char * ssid = "MASMOVIL_2sPj";
//const char * password = "6QSb3hZgFN22";

char texto[300];    //array para recibir los datos como texto
int hora;
boolean rec=0;

AsyncUDP udp;

// Función para limpiar la pantalla M5
void LCD_Clear()
{
  M5.Lcd.fillScreen(BLACK);
  M5.Lcd.setCursor(0, 0);
  M5.Lcd.setTextColor(WHITE);
  M5.Lcd.setTextSize(1);
}
// Menú para M5Stack
void DrawMenu()
{
  M5.Lcd.setTextSize(2);
  
  M5.Lcd.setTextColor(RED);
  M5.Lcd.setCursor(44,215);
  M5.Lcd.printf("PESO");
  
  M5.Lcd.setTextColor(GREEN);
  M5.Lcd.setCursor(126,215);
  M5.Lcd.printf("ALTURA");
  
  M5.Lcd.setTextColor(BLUE);  
  M5.Lcd.setCursor(240,215);
  M5.Lcd.printf("RED");
  
  
}
void setup()
{
    M5.begin();
    Serial.begin(115200);
    WiFi.mode(WIFI_STA);
    WiFi.begin(ssid, password);
    if (WiFi.waitForConnectResult() != WL_CONNECTED) {
        M5.Lcd.printf("WiFi Failed");
        Serial.println("WiFi Failed");
        while(1) {
            delay(1000);
        }
    }
    if(udp.listen(1234)) {
        M5.Lcd.print("UDP Listening on IP: ");
        M5.Lcd.println(WiFi.localIP());
        Serial.print("UDP Listening on IP: ");
        Serial.println(WiFi.localIP());
        udp.onPacket([](AsyncUDPPacket packet) {

            int i=300;
            while (i--) {*(texto+i)=*(packet.data()+i);}
            rec=1;      //recepcion de un mensaje

        });
    }
}

void loop()
{
   //LCD_Clear();

   
   
   if (rec){
    LCD_Clear();
    DrawMenu();
    // Posición del cursor de nuevo en 0,0 
    M5.Lcd.setCursor(0, 0);
    
    
    rec=0;
    udp.broadcastTo("Recibido",1234); //Confirmación
    udp.broadcastTo(texto,1234);      //reenvía lo recibido
    hora=atol(texto);                 //paso de texto a int
    
    StaticJsonBuffer<300> jsonBufferRecv; //definición del buffer para almacenar el objero JSON, 200 máximo
    JsonObject& recibo = jsonBufferRecv.parseObject(texto); //paso de texto a formato JSON
    recibo.printTo(Serial);       //envio por el puerto serie el objeto "recibido"
    recibo.printTo(M5.Lcd); //imprimir al M5                    
    // M5
    M5.Lcd.println();             //nueva línea
    int segundo=recibo["Segundo"];  //extraigo el dato "Segundo" del objeto "recibido" y lo almaceno en la variable "segundo" 
    //M5.Lcd.println(segundo);      //envio por el puerto serie la variable segundo
    //M5.Lcd.print(recibo);
    // Serial
    Serial.println();             //nueva línea
    // int segundo=recibo["Segundo"];  //extraigo el dato "Segundo" del objeto "recibido" y lo almaceno en la variable "segundo" 
    //Serial.println(segundo);      //envio por el puerto serie la variable segundo
    
  }
  delay(5000);
  
}
