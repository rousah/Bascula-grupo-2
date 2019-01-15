#include <M5Stack.h>
#include "WiFi.h"
#include "AsyncUDP.h"
#include <ArduinoJson.h>
#include <HX711.h>
#include "soc/rtc.h"; //Libreria para poder bajar la frecuencia
#include <SR04.h>
// Para el ultrasonido PINES
#define TRIG_PIN 22
#define ECHO_PIN 21


// Para la báscula PINES
#define DOUT  2
#define CLK  5
// Función de la bascula
HX711 balanza(DOUT, CLK);

// --- Escucha del ultrasonido ---
SR04 sr04 = SR04(ECHO_PIN,TRIG_PIN);
long a;

const char * ssid = "EQUIPO_2";
const char * password = "HoLaMuNDo";

//const char * ssid = "MASMOVIL_2sPj";
//const char * password = "6QSb3hZgFN22";

char texto[300];    //array para recibir los datos como texto
int hora;
boolean rec=0;

AsyncUDP udp;


/*CALCULAR ALTURA*/
/**
función que recibe la distancia y calcula basandose en la altura a la que
está el sensor restandole la distancia que hay del sensor a la cabeza
(distancia recogida por el sensor con la función "sr04.Distance()")
una vez obtenida esa altura se hace la transformación a metros
*/
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

/**
 * Función que lee la distancia, hace la función de calcularAltura, y dependiendo 
 * de el resultado de calcular altura, devolvera la altura.
 */
 float devolverAltura(){
        a = sr04.Distance();
        
       if(calcularAltura(a) <= 0 | calcularAltura(a) > 2.00)
       {
         //Serial.print("2.00");
         //Serial.println(" m");
         
         //envio["Altura"]= 2.00;
         return 2.00;
       }
       else
       {
         //Serial.print(calcularAltura(a));
         //Serial.println(" m");
         //envio["Altura"]= calcularAltura(a);
          return calcularAltura(a);
       }
 }

// Función para limpiar la pantalla M5
void LCD_Clear()
{
  M5.Lcd.fillScreen(BLACK);
  M5.Lcd.setCursor(0, 0);
  M5.Lcd.setTextColor(WHITE);
  M5.Lcd.setTextSize(1);
}
/**
 * Está función dibuja las opciones que contiene cada botón, da
 * un aspecto a la pantalla del M5Stack
 */
void DrawMenu()
{
  M5.Lcd.setTextSize(2);
  
  M5.Lcd.setTextColor(RED);
  M5.Lcd.setCursor(40,215);
  M5.Lcd.printf("DATOS");
  
  M5.Lcd.setTextColor(GREEN);
  M5.Lcd.setCursor(135,215);
  M5.Lcd.printf("SUBIR");
  
  M5.Lcd.setTextColor(BLUE);  
  M5.Lcd.setCursor(240,215);
  M5.Lcd.printf("RED");
  
  
}

/**
 * Esta función contiene el estilo de las letras, el tamaño, y la posición
 */
void style(){

    M5.Lcd.setCursor(50, 90);
    M5.Lcd.setTextSize(3);
    M5.Lcd.setTextColor(WHITE);
}

/**
 * Esta función contiene el estilo de las letras, el tamaño, y la posición
 */
void styleData(){

    M5.Lcd.setCursor(50, 50);
    M5.Lcd.setTextSize(3);
    M5.Lcd.setTextColor(WHITE);
}
/** FUNCIÓN PARA EL PESO
 *  
 * Función llamada en el setup que configura la bascula, es decir, 
 * destara la báscula al iniciar el M5Stack, una vez iniciado solo 
 * hace falta pesarse.
 * 
 */
void configBasc(){
  
    rtc_clk_cpu_freq_set(RTC_CPU_FREQ_80M); //bajo la frecuencia a 80MHz
    Serial.print("Lectura del valor del ADC:  ");
    Serial.println(balanza.read());
    Serial.println("No ponga ningun  objeto sobre la balanza");
    Serial.println("Destarando...");
    balanza.set_scale(24000); //La escala por defecto es 1
    balanza.tare(20);  //El peso actual es considerado Tara.
    Serial.println("LISTO!!");
    
}


// Configuración de la conexión WIFI
void configWifi(){
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
// Configuración
void setup()
{
    M5.begin();
    Serial.begin(115200);
    configWifi();
    configBasc();
}

void loop()
{
   //LCD_Clear();
   DrawMenu();

  // Botón A devolvera lo que el usuario pesa y mide
   if(M5.BtnA.wasPressed()){
    LCD_Clear();
    DrawMenu();
    styleData();

    M5.Lcd.print("Peso: ");
    M5.Lcd.print(balanza.get_units(20),3);
    M5.Lcd.println(" Kg");

    M5.Lcd.setCursor(50, 80);
    M5.Lcd.print("Alt:  ");
    M5.Lcd.print(devolverAltura());
    M5.Lcd.println(" m");
    
    // Envía a la UART, para que muestre en LOGCAT
    Serial.print("M");
    
  }
  // Botón B devolvera lo que el usuario mide
   if(M5.BtnB.wasPressed()){
    LCD_Clear();
    DrawMenu();
    style();
    M5.Lcd.setCursor(10, 90);
    M5.Lcd.print("SUBIENDO DATOS...");
    // Envía a la UART, recoge para subir los datos recogidos actualmente
    Serial.print("S");
    
    /**   
     *    Sí se han guardado los datos, avisar con un mensaje
     *    M5.Lcd.print("¡GUARDADO!");
     */
    
    
   }
  // Botón C devolvera si hay o no conección a la red
  // para saber si los demás sensores pueden enviar al M5Stack
   if(M5.BtnC.wasPressed()){
    LCD_Clear();
    DrawMenu();
    style();
    M5.Lcd.setCursor(75, 90);
    if(WiFi.localIP()){
      //style();
      
      M5.Lcd.print("CONECTADO");
    }else
    {
      //style();
      M5.Lcd.print("DESCONECTADO");
    }
    M5.Lcd.setCursor(90, 10);
    M5.Lcd.setTextSize(1);
    M5.Lcd.print("Versión: 1.0");
   }

   
   /**
    * Conversación con la RaspBerry por UART
    */
   if (Serial.available() > 0) {
     char command = (char) Serial.read();
    
     switch (command) {
      // Al comenzar Android Things envia 1 para ver si el M5 está conectado con
      // con los demás dispositivos en la red
      case '1':
           if(WiFi.localIP()){
              Serial.print("CONECTADO");
           }else
           {
              Serial.print("DESCONECTADO");
           }
       break;
       /**
        * Entonces, si presionamos el botón de enviar los datos por UART, Android things nos 
        * enviara el número 2, por lo que luego M5 enviará los datos actuales
        */
       case '2':
         
        StaticJsonBuffer<300> jsonBufferRecv; //definición del buffer para almacenar el objero JSON, 200 máximo
        JsonObject& bascula = jsonBufferRecv.createObject(); //paso de texto a formato JSON
        
        bascula["Peso"] = balanza.get_units(20);
        bascula["Altura"] = devolverAltura();
        bascula.printTo(Serial);       //envio por el puerto serie el objeto "recibido"         
       break;
     }
   }
   Serial.flush();

   if (rec){
    
    //LCD_Clear();
    //DrawMenu();
    // Posición del cursor de nuevo en 0,0 
    M5.Lcd.setCursor(0, 0);

    rec=0;
    udp.broadcastTo("Recibido",1234); //Confirmación
    udp.broadcastTo(texto,1234);      //reenvía lo recibido
    hora=atol(texto);                 //paso de texto a int
    
    StaticJsonBuffer<300> jsonBufferRecv; //definición del buffer para almacenar el objero JSON, 200 máximo
    JsonObject& sensores = jsonBufferRecv.parseObject(texto); //paso de texto a formato JSON
    sensores.printTo(Serial);       //envio por el puerto serie el objeto "recibido"    
    
  }

  
  
  M5.update();
  //delay(5000);
}
