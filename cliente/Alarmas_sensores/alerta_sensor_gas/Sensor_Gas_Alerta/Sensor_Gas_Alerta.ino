#include <WiFi.h>
#include <MQTT.h>

// Sensor de Gas Pines
const int MQ_PIN = 36;
const int MQ_DELAY = 2000;

int medidaGasRaw;
int medidaGasVoltios;
int controladorAlertaGas = 0;
// ------------------------


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

void setup() {
  Serial.begin(115200);
  WiFi.begin(ssid, pass);

  // Note: Local domain names (e.g. "Computer.local" on OSX) are not supported by Arduino.
  // You need to set the IP address directly.
  client.begin(broker, net);
  client.onMessage(messageReceived);

  connect();
}




void loop() {

  leerSensorGas();
  medidaGasVoltios = calcularValorVoltiosGas();


  

  if((medidaGasVoltios >= 2) && (controladorAlertaGas == 0)){
      client.publish("equipo2/bascula/alarma", "¡¡¡ALERTA SENSOR DE GAS!!!: ");
      controladorAlertaGas = 1;
    } 
  if((medidaGasVoltios <= 1) && (controladorAlertaGas == 1)){
      controladorAlertaGas = 0;         // Se resetea la alerta de gas para que pueda saltar otra vez.
    }

  
  client.loop();
  delay(10);  // <- fixes some issues with WiFi stability

  
  
  if (!client.connected()) {
    connect();
  }


}





// -------------- Funciones sensor MQ-2 Gases -----------------

// lee el valor en crudo del sensor
void leerSensorGas() {
  medidaGasRaw = analogRead(MQ_PIN);
}


int calcularValorVoltiosGas () {
  return (medidaGasRaw * (5.0 / 4095.0));
}

// ------------------------------------------------------------
