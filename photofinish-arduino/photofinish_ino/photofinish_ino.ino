#define TRACK_1 5
#define TRACK_2 6
#define START_SWITCH 7

#define TEST 0
#define READY 1
#define RACE 2
#define TRACK_1_TIME 3
#define TRACK_2_TIME 4
#define FINISH 5


#include <SoftwareSerial.h> // use the software uart
SoftwareSerial bluetooth(2, 4); // RX, TX

unsigned long startMillis = 0;        // will store last time

int status = READY;

unsigned short track1Millis = 0;
unsigned short track2Millis = 0;

const int messageSize = 4;

void setup() {
  pinMode(13, OUTPUT); // for LED status
  pinMode(5, INPUT_PULLUP);
  pinMode(6, INPUT_PULLUP);
  bluetooth.begin(9600); // start the bluetooth uart at 9600 which is its default
  delay(200); // wait for voltage stabilize
  //bluetooth.print("AT+NAMEmcuhq.com"); // place your name in here to configure the bluetooth name.
                                       // will require reboot for settings to take affect. 
  bluetooth.print("AT+NAME=Photo Finish (Cool!)");
  bluetooth.print("AT+PSWD=0000");
  delay(3000); // wait for settings to take affect. 
  Serial.begin(9600);
  Serial.println("READY");
}

void loop() {
  commandController();
  raceController();
}

void commandController() {
  /**** PROTOCOL DETAILS **********
  
   message length: 4 bytes;
   +-------+-----------------------+
   |   #   |  description          |
   +-------+-----------------------+
   |   1   |  command              |
   |  2,3  |  data                 |
   |   4   |  message check summ   |
   +-------+-----------------------+
   
  *********************************/
  
  byte input[messageSize];
  int inputSize = bluetooth.available();
  inputSize = inputSize - inputSize % messageSize;
  
  if (inputSize >= messageSize) {
     for(int i = 0; i < inputSize; i++) {
         input[i] = bluetooth.read();
         digitalWrite(13, !digitalRead(13));
         
         if ((i + 1) % messageSize == 0) {
             handleMessage(input);  
         }
     }
  }
}

void handleMessage(byte input[]) {
  //TODO checksum algorithm
  byte checkSum = input[messageSize - 1];
  byte sum = checkSum;
 
  if (checkSum == sum) {
     byte command = input[0];
     byte dataByte1 = input[1];
     byte dataByte2 = input[2];
    
     switch(command) {
       case TEST:
         Serial.print("TEST COMMAND: ");
         sendEcho(input);
       break;
       case READY:
         status = READY;
         startMillis = 0;
         track1Millis = 0;
         track2Millis = 0;
         Serial.println("READY COMMAND");
         break;
       default:
         Serial.println("Unknown command, msg: " + String((char*)input));
     } 
  }
}

void raceController() {
  if (status == READY && isSwitchOff(START_SWITCH)) {
      status = RACE;
      startMillis = millis();
      
      sendMessage(RACE);
      Serial.println("RACE");
  }
  
  if (status == RACE) {
    if (track1Millis == 0 && isSwitchOff(TRACK_1)) {
      track1Millis = millis() - startMillis;

      byte data[] = {highByte(track1Millis), lowByte(track1Millis)};
      sendMessage(TRACK_1_TIME, data);
      Serial.println(String("Track 1 finished: " + String(track1Millis)));
    }
    if (track2Millis == 0 && isSwitchOff(TRACK_2)) {
      track2Millis = millis() - startMillis;
      
      byte data[] = {highByte(track2Millis), lowByte(track2Millis)};
      sendMessage(TRACK_2_TIME, data);
      Serial.println(String("Track 2 finished: " + String(track2Millis)));
    }
    if (track1Millis > 0 && track2Millis > 0) {
      status = FINISH;
      sendMessage(FINISH);
      Serial.println(String("RACE OVER"));
    }
  }
}

boolean isSwitchOn(int pin) {
  return digitalRead(pin) == HIGH;
}

boolean isSwitchOff(int pin) {
  return !isSwitchOn(pin);
}

void sendMessage(byte command) {
  byte data[] = {0,0};
  sendMessage(command, data);
}

void sendMessage(byte command, byte data[]) {
    byte msg[] = {command, data[0], data[1]};
    byte sum = 0;
    for (int i = 0; i < sizeof msg; i++) {
      byte b = msg[i];
      sum ^= b;
      bluetooth.write(b); 
    }
    bluetooth.write(sum);
}

void sendEcho(byte msg[]) {
    for (int i = 0; i < messageSize; i++) {
      byte b = msg[i];
      bluetooth.write(b);
      Serial.print(b);     
    }
    Serial.println();
}

