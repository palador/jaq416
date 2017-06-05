#ifndef __TCP_PROTO__
#define __TCP_PROTO__

#include <Arduino.h>
#include <Ethernet2.h>

#define TCP_PROTO__MSG_DATA_LEN 64
#define TCP_PROTO__SUCCESS 0
#define TCP_PROTO__CONNECTION_DOWN 1

struct MsgFormat {
  byte opCode;
  byte data[TCP_PROTO__MSG_DATA_LEN];
};

void establishServerConnection(byte* pServerAddr, unsigned short port, MsgFormat* pM);
byte sendReqToServer(MsgFormat* pM);

#endif
