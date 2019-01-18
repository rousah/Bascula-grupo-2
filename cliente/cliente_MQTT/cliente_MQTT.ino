#include <WiFi.h>
#include <MQTT.h>


// Sensor de Gas Pines
const int MQ_PIN = 39;
const int MQ_DELAY = 2000;

int medidaGasRaw;
int medidaGasVoltios;
int controladorAlertaGas = 0;
// ------------------------

uint8_t PinGPIOPresencia = 12;
int8_t presencia = 0;


uint8_t PinGPIOMagnetic = 2;
uint8_t lectura;
uint8_t entrada = 1;


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
  WiFi.begin(ssid, pass);

  
  client.begin(broker, net);
  client.onMessage(messageReceived);

  connect();

// -----------------------------------------------------------------
  
  
  pinMode(PinGPIOMagnetic, INPUT);
  pinMode(PinGPIOPresencia,INPUT);
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
      client.publish("equipo2/bascula/alarma", "ALERTA_DE_GAS");
      controladorAlertaGas = 1;
    } 
  if((medidaGasVoltios <= 1) && (controladorAlertaGas == 1)){
      controladorAlertaGas = 0;         // Se resetea la alerta de gas para que pueda saltar otra vez.
    }

    // -------------------


  
if(touchRead(PinGPIOMagnetic) != 0){
  client.publish("equipo2/bascula/PRESENCIA", "PUERTA_ABIERTA");
  }
 
if(touchRead(PinGPIOMagnetic) == 0){
    client.publish("equipo2/bascula/PRESENCIA", "PUERTA_CERRADA");
  }
  
  leerInfrarrojos();
  delay(500);
}


// --------------- Función sensor presencia ------------------------

void leerInfrarrojos() {

if(digitalRead(PinGPIOPresencia)== HIGH) {
  if(presencia == 0){
    Serial.println("Detectado movimiento en la entrada");
   client.publish("equipo2/bascula/PRESENCIA", "IN_1");
    presencia = 1;
    return;
    }

    if(presencia == 1){
    Serial.println("Detectado movimiento en la sala 1");
   client.publish("equipo2/bascula/PRESENCIA", "IN_2");
   delay(500);
    presencia = 2;
    return;
    }

    if(presencia == 2){
    Serial.println("Detectado movimiento en la sala 3");
   client.publish("equipo2/bascula/PRESENCIA", "IN_3");
    presencia = 3;
    return;
    }

    if(presencia == 3){
    Serial.println("Detectado movimiento en la sala 2");
   client.publish("equipo2/bascula/PRESENCIA", "IN_2");
    presencia = 0;
    return;
    }
}
  
}
// -----------------------------------------------------------------



// ------------- Funciones sensor magnetico -------------------


bool touchRead(int pin) {
  bool toca = digitalRead(pin);
  if (toca) {
    return 1;
    }
    return 0;
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
