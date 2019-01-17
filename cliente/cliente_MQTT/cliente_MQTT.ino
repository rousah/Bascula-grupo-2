#include <WiFi.h>
#include <MQTT.h>


// Sensor de Gas Pines
const int MQ_PIN = 36;
const int MQ_DELAY = 2000;

int medidaGasRaw;
int medidaGasVoltios;
int controladorAlertaGas = 0;
// ------------------------


uint8_t PinGPIOMagnetic_1 = 2;
uint8_t PinGPIOMagnetic_2 = 4;
uint8_t lectura_1;
uint8_t lectura_2;
uint8_t entrada_1 = 1;
uint8_t entrada_2 = 1;


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
  while (!client.connect("lens_BonIgxgNcor9mkOmJshKqbNTGSs", "try", "try")) {
    Serial.print(".");
    delay(1000);
  }

  Serial.println("\nconnected!");

  client.subscribe("equipo2/bascula/#");
}

void messageReceived(String &topic, String &payload) {
  Serial.println("incoming: " + topic + " - " + payload);
}

// -----------------------------------------------------------------




void setup() {

// ------------------- Conexión eclipse ----------------------------
  Serial.begin(115200);
  //WiFi.begin(ssid, pass);

  
  client.begin(broker, net);
  client.onMessage(messageReceived);

  connect();

// -----------------------------------------------------------------
  
  
  pinMode(PinGPIOMagnetic_1, INPUT);
  pinMode(PinGPIOMagnetic_2, INPUT);
}


void loop() {



    client.loop();
  delay(10);  // <- fixes some issues with WiFi stability

  
  
  if (!client.connected()) {
    connect();
  }




  // -- Sensor de Gas ---

  leerSensorGas();
  medidaGasVoltios = calcularValorVoltiosGas();


  

  if((medidaGasVoltios >= 2) && (controladorAlertaGas == 0)){
      client.publish("equipo2/bascula/alarma", "¡¡¡ALERTA SENSOR DE GAS!!!: ");
      controladorAlertaGas = 1;
    } 
  if((medidaGasVoltios <= 1) && (controladorAlertaGas == 1)){
      controladorAlertaGas = 0;         // Se resetea la alerta de gas para que pueda saltar otra vez.
    }

    // -------------------


  
lectura_1 = touchRead(PinGPIOMagnetic_1);
  if(lectura_1 == 0){

    if(entrada_1 == 1){
    client.publish("equipo2/bascula/PRESENCIA", "IN_1");
    entrada_1 = 2;
    return;
    }
    if(entrada_1 == 2){
      client.publish("equipo2/bascula/PRESENCIA", "OUT");
      entrada_1 = 1;
      return;
      }
    }


    lectura_2 = touchRead(PinGPIOMagnetic_2);
  if(lectura_2 == 0){

    if(entrada_2 == 1){
    client.publish("equipo2/bascula/PRESENCIA", "IN_2");
    entrada_2 = 2;
    return;
    }
    if(entrada_2 == 2){
      client.publish("equipo2/bascula/PRESENCIA", "IN_1");
      entrada_2 = 1;
      return;
      }
    }

    

  
  delay(500);
}


// ------------- Funciones alarma sensor magneto -------------------


String touchRead(int pin) {
  bool toca = digitalRead(pin);
  if (toca) {
    return "ON";
    }
    return "OFF";
}

// -----------------------------------------------------------------


// -------------- Funciones sensor MQ-2 Gases -----------------

// lee el valor en crudo del sensor
void leerSensorGas() {
  medidaGasRaw = analogRead(MQ_PIN);
}


int calcularValorVoltiosGas () {
  return (medidaGasRaw * (5.0 / 4095.0));
}

// ------------------------------------------------------------
