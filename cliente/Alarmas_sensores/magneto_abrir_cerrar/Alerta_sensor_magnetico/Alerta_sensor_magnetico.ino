#include <WiFi.h>
#include <MQTT.h>


const int pinMagnetico = 26;
int cambiaEstado=0;
unsigned long lastMillis = 0;


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
  
  
}


void loop() {



    client.loop();
  delay(10);  // <- fixes some issues with WiFi stability

 int valorMagnetico = digitalRead(pinMagnetico);
  
  
  if (!client.connected()) {
    connect();
  }

  if (millis() - lastMillis > 1000) {
      lastMillis = millis();
if (valorMagnetico == LOW) {   
  if(cambiaEstado==0){
      Serial.println("puerta cerrada");
      client.publish("equipo2/bascula/alarma_magnetico", "CERRADA");
      cambiaEstado=1;
  } 
} else {
  if(cambiaEstado==1){
    Serial.println("puerta abierta");
    client.publish("equipo2/bascula/alarma_magnetico", "ABIERTA");
    cambiaEstado=0;
  }
}
    }
  
  delay(500);
}


// ------------- Funciones alarma sensor magneto -------------------


bool touchRead(int pinMagnetico) {
  bool toca = digitalRead(pinMagnetico);
  if (toca) {
    return true;
    }
    return false;
}

// -----------------------------------------------------------------
