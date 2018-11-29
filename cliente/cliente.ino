#include "WiFi.h"
#include "AsyncUDP.h"
#include <TimeLib.h>
#include <ArduinoJson.h>
#include <DHT.h>
#include <WiFi.h>
#include <MQTT.h>


// ---------------- MQTT -------------------------

// --- Sensor de Gas Pines ---
const int MQ_PIN = 36;
const int MQ_DELAY = 2000;

int medidaGasRaw;
int medidaGasVoltios;
int controladorAlertaGas = 0;
// ------------------

const char broker[] = "iot.eclipse.org";

WiFiClient net;
MQTTClient client;

// ------------------------------------------------



// --- Escucha del infrarrojo ---
int ledPin = 5;  // LED en el Pin 5 del Arduino
int pirPin = 21; // Input para HC-S501
int pirValue; // Para guardar el valor del pirPin


// --- Escucha del sensor hum temp ---
// Definimos el pin digital donde se conecta el sensor DHT
#define DHTPIN 5
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


// -------------- Funciones sensor MQ-2 Gases -----------------

// lee el valor en crudo del sensor
void leerSensorGas() {
  medidaGasRaw = analogRead(MQ_PIN);
}


int calcularValorVoltiosGas () {
  return (medidaGasRaw * (5.0 / 4095.0));
}

// ------------------------------------------------------------
void leerInfrarrojos() {

  if(digitalRead(pirPin)==HIGH) {
   Serial.println("Detectado movimiento por el sensor pir");
   digitalWrite(ledPin, HIGH);
   delay(500);
  }
  else {
  digitalWrite(ledPin, LOW);  
  }
}

void leerHumTemp() {

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
    
}


// ----------- MQTT --------------


void connect() {
  Serial.print("checking wifi...");
  while (WiFi.status() != WL_CONNECTED) {
    Serial.print(".");
    delay(1000);
  }

  Serial.print("\nconnecting...");
  while (!client.connect("lens_wPVhV9y2ni29xc4vAAmHU6VFAIW", "try", "try")) {
    Serial.print(".");
    delay(1000);
  }

  Serial.println("\nconnected!");

  client.subscribe("equipo2/bascula/#");
  // client.unsubscribe("equipo2/bascula/#");
}

void messageReceived(String &topic, String &payload) {
  Serial.println("incoming: " + topic + " - " + payload);
}

// ---------------------------------

void setup()
{

  // Iniciar conexión con eclipse para MQTT.
  client.begin(broker, net);  
  client.onMessage(messageReceived);
  // ------


  
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


  connect(); // <-- Iniciar conexión con eclipse.
}


void loop()
{


  // ----- Alerta Sensor de Gas -----

  leerSensorGas();    // Lectura del sensor.
  medidaGasVoltios = calcularValorVoltiosGas();   // Calcula los datos a Voltios.


  // * Registra si ha ocurrido una alerta, y manda un único mensaje de alerta. *
  if((medidaGasVoltios >= 2) && (controladorAlertaGas == 0)){
      client.publish("equipo2/bascula/alarma", "¡¡¡ALERTA SENSOR DE GAS!!!: ");
      controladorAlertaGas = 1;
    } 
  if((medidaGasVoltios <= 1) && (controladorAlertaGas == 1)){
      controladorAlertaGas = 0;         // Se resetea la alerta de gas para que pueda saltar otra vez.
    }

    


  // --------------------------------
  
  AsyncUDP udp;
  StaticJsonBuffer<300> jsonBuffer;                 //tamaño maximo de los datos
  JsonObject& envio = jsonBuffer.createObject();    //creación del objeto "envio"
  char texto[300];
  
  String date = String(day())+"-"+String(month())+"-"+String(year());
  String timeNow = String(hour())+":"+String(minute())+":"+String(second());

  envio["Date"] = date;
  envio["Time"] = timeNow;

  
  unsigned long currentMillis = millis();
  // Se hace únicamente cuando pase el intervalo
  if (currentMillis - previousMillis >= interval) {
    previousMillis = currentMillis;
    leerHumTemp();
  }
  
  //Se hace continuamente 
  leerInfrarrojos();


 // ----- MQTT -----
client.loop();
  delay(10);  // <- fixes some issues with WiFi stability

  
  
  if (!client.connected()) {
    connect();
  }

  // ----------------
  
  
}
