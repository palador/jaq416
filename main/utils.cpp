#include "utils.h"

unsigned long readULong(byte* buf, unsigned int offset) {
  unsigned long retval;
  byte* a = buf + offset;
  retval  = (unsigned long) a[0] << 24 | (unsigned long) a[1] << 16;
  retval |= (unsigned long) a[2] << 8 | a[3]; 
  return retval;
}

