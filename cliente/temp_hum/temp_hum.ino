// Incluimos librería
#include <DHT.h>
#include <M5Stack.h>
// Definimos el pin digital donde se conecta el sensor DHT
#define DHTPIN 3
// Dependiendo del tipo de sensor
#define DHTTYPE DHT11

#define NEGRO 0
 
// Inicializamos el sensor DHT11
DHT dht(DHTPIN, DHTTYPE);


void setup() {
  // Inicializamos comunicación serie
  Serial.begin(9600);
 
  // Comenzamos el sensor DHT
  dht.begin();
  M5.begin();
}

void loop() {
    // Esperamos 5 segundos entre medidas
  delay(5000);
  M5.Lcd.fillScreen(NEGRO);

 
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
 
  // Calcular el índice de calor en Fahrenheit
  float hif = dht.computeHeatIndex(f, h);
  // Calcular el índice de calor en grados centígrados
  float hic = dht.computeHeatIndex(t, h, false);
 
  Serial.print("Humedad: ");
  Serial.print(h);
  Serial.print(" %\t");
  Serial.print("Temperatura: ");
  Serial.print(t);
  Serial.print(" *C ");
  Serial.print(f);
  Serial.print(" *F\t");
  Serial.print("Índice de calor: ");
  Serial.print(hic);
  Serial.print(" *C ");
  Serial.print(hif);
  Serial.println(" *F");

  M5.Lcd.setCursor(0,16);
  M5.Lcd.setTextSize(2);
  M5.Lcd.print("Humedad: ");
  M5.Lcd.print(h);
  M5.Lcd.println(" %\t");
  M5.Lcd.print("Temperatura: ");
  M5.Lcd.print(t);
  M5.Lcd.println(" *C ");
  M5.Lcd.print("Indice de calor: ");
  M5.Lcd.print(hic);
  M5.Lcd.println(" *C ");
 
}
