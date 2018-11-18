/* mifare ultralight example (25-02-2018)
 * 
 *   RFID-RC522 (SPI connexion)
 *   
 *   CARD RC522      Arduino (UNO)
 *     SDA  -----------  10 (Configurable, see SS_PIN constant)
 *     SCK  -----------  13
 *     MOSI -----------  11
 *     MISO -----------  12
 *     IRQ  -----------  
 *     GND  -----------  GND
 *     RST  -----------  9 (onfigurable, see RST_PIN constant)
 *     3.3V ----------- 3.3V
 *     
 */
#define BLANCO 0XFFFF
#define NEGRO 0
#define ROJO 0xF800
#define VERDE 0x07E0
#define AZUL 0x001F
  
  
  
  #include <SPI.h>
  #include <MFRC522.h>
  #include <M5Stack.h>
  
  
  #define SS_PIN          21
  #define RST_PIN         2   //No es necesario conectarlo

  MFRC522 mfrc522(SS_PIN, RST_PIN);  // Create MFRC522 instance
  MFRC522::StatusCode status; //variable to get card status

  byte buffer_1[18]; //buffer intermedio para leer 16 bytes
  byte buffer[66];  //data transfer buffer (64+2 bytes data+CRC)
  byte tam = sizeof(buffer);
  byte tam1= sizeof(buffer_1);
  uint8_t pageAddr = 0x06;  //In this example we will write/read 64 bytes (16 paginas, page 6,7,8 hasta la 21).
                            //Ultraligth mem = 16 pages. 4 bytes per page.  
                            //Pages 0 to 4 are for special functions.           
  
void setup() {
  M5.begin();
  Serial.begin(9600); // Initialize serial communications with the PC
  SPI.begin(); // Init SPI bus
  mfrc522.PCD_Init(); // Init MFRC522 card  
  Serial.println(F("Sketch has been started!"));
  Serial.println (tam);
  memcpy(buffer,"Para la alergia, uno al dia por la manyana                        ",(tam-2));  //Mensaje para medicamento 2
  //Serial.println(buffer);
  for(int i = 0; i < tam; i++)
{
  char z=buffer[i];
  Serial.print(z);
}
  Serial.println();
}

void loop() {
  // Look for new cards
  if ( ! mfrc522.PICC_IsNewCardPresent())
    return;

  // Select one of the cards
  if ( ! mfrc522.PICC_ReadCardSerial())
    return;

// Show some details of the PICC (that is: the tag/card)
    M5.Lcd.fillScreen(NEGRO);
    Serial.print(F("Card UID:"));
    dump_byte_array(mfrc522.uid.uidByte, mfrc522.uid.size);
    Serial.println();
    Serial.print(F("PICC type: "));
    MFRC522::PICC_Type piccType = mfrc522.PICC_GetType(mfrc522.uid.sak);
    Serial.println(mfrc522.PICC_GetTypeName(piccType));

  // Write data ***********************************************
  for (int i=0; i < (tam-2)/4; i++) {
    //data is writen in blocks of 4 bytes (4 bytes per page)
    status = (MFRC522::StatusCode) mfrc522.MIFARE_Ultralight_Write(pageAddr+i, &buffer[i*4], 4);
    if (status != MFRC522::STATUS_OK) {
      Serial.print(F("MIFARE_Read() failed: "));
      Serial.println(mfrc522.GetStatusCodeName(status));
      return;
    }
  }

  Serial.println(F("MIFARE_Ultralight_Write() OK "));
  Serial.println();


  // Read data ***************************************************
  Serial.println(F("Reading data ... "));
  for (int i=0; i<(tam-2)/16; i++)
  {
  //data in 4 block is readed at once.
  status = (MFRC522::StatusCode) mfrc522.MIFARE_Read(pageAddr+i*4, buffer_1, &tam1);
  if (status != MFRC522::STATUS_OK) {
    Serial.print(F("MIFARE_Read() failed: "));
    Serial.println(mfrc522.GetStatusCodeName(status));
    return;
  }
  for (int j=0; j<16; j++)
  {
    buffer[j+i*16]=buffer_1[j];
  }
  }
  Serial.print(F("Readed data: "));
  //Dump a byte array to Serial
  for (byte i = 0; i < (tam-2); i++) {
    Serial.write(buffer[i]);
    
  }
  
  M5.Lcd.setTextSize(2);
  M5.Lcd.setCursor(0, 60);
  M5.Lcd.setTextColor(VERDE);
  for (byte i = 0; i < (tam-2); i++) {
   M5.Lcd.print((char)buffer[i]);
    
  }
                    
  
  Serial.println();

  mfrc522.PICC_HaltA();

}

void dump_byte_array(byte *buffer, byte bufferSize) {
    M5.Lcd.setTextSize(2);
    M5.Lcd.setCursor(0, 10);
    M5.Lcd.setTextColor(BLANCO);
    for (byte i = 0; i < bufferSize; i++) {
        Serial.print(buffer[i] < 0x10 ? " 0" : " ");
        M5.Lcd.print(mfrc522.uid.uidByte[i] < 0x10 ? " 0" : " ");
        Serial.print(buffer[i], HEX);
        M5.Lcd.print(mfrc522.uid.uidByte[i], HEX);
    }
}
