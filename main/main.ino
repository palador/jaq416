#include "tcp_proto.h"
#include "utils.h"

byte server[] = {192, 168, 178, 25};
unsigned short port = 2233;

/*
   The Protocol defines the following request format:

    content: | size | op | data |
    size:    |  1B  | 1B | 126B |

    size - size from the first byte (size) until the last data byte (max: 128)
    op   - op code
    data -representation depends on op code, max. size is 126B

    The response format is the same.

    Independ on the given size, all 128B are sent and received.
*/
MsgFormat msg;

// the setup function runs once when you press reset or power the board
void setup() {
  // initialize digital pin LED_BUILTIN as an output.
  pinMode(LED_BUILTIN, OUTPUT);
  pinMode(3, OUTPUT);
  byte mac[] = {0x90, 0xA2, 0xDA, 0x10, 0x85, 0x16};
  byte ip[] = {192, 168, 178, 5 };
  byte subnet[] = {255, 255, 255, 0 };
  byte dnsAndGateway[] = { 192, 168, 178, 1 };

  Serial.begin(9600);
  delay(3000);

  Serial.println("st eth");
  Ethernet.begin(mac, ip, dnsAndGateway, dnsAndGateway, subnet);
  delay(1000);

  Serial.println("con svr");
  establishServerConnection(server, port, &msg);
}

// the loop function runs over and over again forever
void loop() {
  establishServerConnection(server, port, &msg);
  
  msg.opCode = 1;
  if(sendReqToServer(&msg) == TCP_PROTO__SUCCESS) {
    byte pinA = msg.data[0];
    byte pinB = msg.data[1];
    unsigned long dPinA = readULong(msg.data, 2);
    unsigned long dBreakA = readULong(msg.data, 6);
    unsigned long dPinB = readULong(msg.data, 10);
    unsigned long dBreakB = readULong(msg.data, 14);

    digitalWrite(pinA, LOW);
    digitalWrite(pinB, LOW);

    digitalWrite(pinA, HIGH);
    delay(dPinA);
    digitalWrite(pinA, LOW);
    delay(dBreakA);
    digitalWrite(pinB, HIGH);
    delay(dPinB);
    digitalWrite(pinB, LOW);
    delay(dBreakB);
  }
}
