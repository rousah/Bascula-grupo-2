#include <WiFi.h>
#include <MQTT.h>


uint8_t PinGPIO = 2;


// ----------------- Conexión con eclipse ---------------------------

const char ssid[] = "EQUIPO_2";
const char pass[] = "HoLaMuNDo";
const char broker[] = "iot.eclipse.org";

WiFiClient net;
MQTTClient client;


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

// -----------------------------------------------------------------




void setup() {

// ------------------- Conexión eclipse ----------------------------
  Serial.begin(115200);
  WiFi.begin(ssid, pass);

  // Note: Local domain names (e.g. "Computer.local" on OSX) are not supported by Arduino.
  // You need to set the IP address directly.
  client.begin(broker, net);
  client.onMessage(messageReceived);

  connect();

// -----------------------------------------------------------------
  
  
  pinMode(PinGPIO, INPUT);
}


void loop() {



    client.loop();
  delay(10);  // <- fixes some issues with WiFi stability

  
  
  if (!client.connected()) {
    connect();
  }

  Serial.println(touchRead(PinGPIO));

  if(touchRead(PinGPIO) == 1){
  client.publish("equipo2/bascula/alarma", "PUERTA ABIERTA :D");
  } 

  if(touchRead(PinGPIO) == 0){
  client.publish("equipo2/bascula/alarma", "PUERTA CERRADA :C");
  } 
  
  delay(500);
}


// ------------- Funciones alarma sensor magneto -------------------


bool touchRead(int pin) {
  bool toca = digitalRead(pin);
  if (toca) {
    return true;
    }
    return false;
}

// -----------------------------------------------------------------
