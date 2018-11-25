#include <WiFi.h>
#include <MQTT.h>
#include <SR04.h> // Sensor de altura

// Definicion de los pines del sensor de altura
#define TRIG_PIN 15
#define ECHO_PIN 13
// ---------------------

SR04 sr04 = SR04(ECHO_PIN,TRIG_PIN);
long a;
float ultimaAltura;
String stringMedida;

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

  // Se registra la medida actual
  a = sr04.Distance();
  // -------------------------
  
  client.loop();
  delay(10);  // <- fixes some issues with WiFi stability

  
  
  if (!client.connected()) {
    connect();
  }

  // publish a message when a critical change occurs.
  if ((calcularAltura(a) - ultimaAltura > 0,3) && (calcularAltura(a) - ultimaAltura < (-0,3)) ) {
     
    String stringMedida = (String) calcularAltura(a);
    
    client.publish("equipo2/bascula/saludoDeCarlos", "Medicion altura: " + stringMedida);
  }

  // Aquí se guarda la ultima medida de la altura 
  
  ultimaAltura = calcularAltura(a);;
}




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
