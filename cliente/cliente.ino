#include "WiFi.h"
#include "AsyncUDP.h"
#include <TimeLib.h>
#include <ArduinoJson.h>
#include <DHT.h>
#define BLANCO 0XFFFF
#define NEGRO 0
#define ROJO 0xF800
#define VERDE 0x07E0
#define AZUL 0x001F
#include <SPI.h>
#include <MFRC522.h>


// --- Escucha del infrarrojo ---
int ledPin = 5;  // LED en el Pin 5 del Arduino
int pirPin = 21; // Input para HC-S501
int pirValue; // Para guardar el valor del pirPin

// --- Escucha del RFID ---
#define RST_PIN  2    //Pin 9 para el reset del RC522 no es necesario conctarlo
#define SS_PIN  21   //Pin 10 para el SS (SDA) del RC522
MFRC522 mfrc522(SS_PIN, RST_PIN); ///Creamos el objeto para el RC522
MFRC522::StatusCode status; //variable to get card status


// --- Escucha del sensor hum temp ---
// Definimos el pin digital donde se conecta el sensor DHT
#define DHTPIN 35
// Dependiendo del tipo de sensor
#define DHTTYPE DHT11
// Inicializamos el sensor DHT11
DHT dht(DHTPIN, DHTTYPE);

// --- Info Router ---
const char * ssid = "EQUIPO_2";
const char * password = "HoLaMuNDo";

//const char * ssid = "MASMOVIL_2sPj";
//const char * password = "6QSb3hZgFN22";


AsyncUDP udp;
StaticJsonBuffer<300> jsonBuffer;                 //tamaño maximo de los datos
JsonObject& envio = jsonBuffer.createObject();    //creación del objeto "envio"


// --- Para los tiempos distintos ---
unsigned long previousMillis = 0;        // guardará cuándo se lee los sensores
const long interval = 5000;           // intervalo de cuándo se leerán los sensores


void leerInfrarrojos() {

  if(digitalRead(pirPin)== HIGH) {
   Serial.println("Detectado movimiento por el sensor pir");
   digitalWrite(ledPin, HIGH);
   delay(500);
  }
  else {
  digitalWrite(ledPin, LOW);  
  }
}

void leerHumTemp(float *pdatos) {

  // Leemos la humedad relativa
  float h = dht.readHumidity();
  // Leemos la temperatura en grados centígrados (por defecto)
  float t = dht.readTemperature();
  // Leemos la temperatura en grados Fahrenheit
  float f = dht.readTemperature(true);

  // Comprobamos si ha habido algún error en la lectura
  if (isnan(h) || isnan(t) || isnan(f)) {
    Serial.println("Error obteniendo los datos del sensor DHT11");
    return;
  }

  // Calcular el índice de calor (sensación térmica)
  //en Fahrenheit
  float hif = dht.computeHeatIndex(f, h);
  //en grados centígrados
  float hic = dht.computeHeatIndex(t, h, false);

  Serial.print("Humedad: ");
  Serial.print(h);
  Serial.println(" %");
  Serial.print("Temperatura: ");
  Serial.print(t);
  Serial.println(" *C");
  Serial.print("Índice de calor: ");
  Serial.print(hic);
  Serial.println(" *C");

  pdatos[0] = h;
  pdatos[1] = t;
  pdatos[2] = hic;
      
}

void setup()
{
    Serial.begin(115200);
    SPI.begin();        //Iniciamos el Bus SPI
    mfrc522.PCD_Init(); // Iniciamos el MFRC522
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

    if (udp.listen(1234)) {
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
    
    // Comenzamos el sensor DHT
    dht.begin();
  
}


void loop()
{
  AsyncUDP udp;
  StaticJsonBuffer<300> jsonBuffer;                 //tamaño maximo de los datos
  JsonObject& envio = jsonBuffer.createObject();    //creación del objeto "envio"
  char texto[300];
  float datosDHT[3];
  
  String date = String(day())+"-"+String(month())+"-"+String(year());
  String timeNow = String(hour())+":"+String(minute())+":"+String(second());
  float temp;
  float hum;
  float calor;

  

  
  unsigned long currentMillis = millis();
  // Se hace únicamente cuando pase el intervalo
  if (currentMillis - previousMillis >= interval) {
    previousMillis = currentMillis;
    leerHumTemp(&datosDHT[0]);
    envio["Date"] = date;
    envio["Time"] = timeNow;
    envio["Temperatura"] = datosDHT[0];
    envio["Humedad"] = datosDHT[1];
    envio["Calor"] = datosDHT[2];
  }
  
  //Se hace continuamente 
  //leerInfrarrojos();

  envio.printTo(texto);         //paso del objeto "envio" a texto para transmitirlo
  udp.broadcastTo(texto,1234);  //se envía por el puerto 1234 el JSON 
                                //como texto
}
