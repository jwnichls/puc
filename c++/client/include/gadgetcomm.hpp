#ifndef GADGETCOMM_H
#define GADGETCOMM_H

#include <string>
#include <vector>

#include "message.hpp"
#include "pucspec.hpp"

XERCES_CPP_NAMESPACE_BEGIN

void WRITE(int sock, const Message& msg);
Message* READ(int sock);

class GadgetError {
  string str_;

public:
  GadgetError(string str) : str_(str) {}

  string str() const {return str_;}
};

class GadgetInitError : public GadgetError {
 public:
  GadgetInitError(string str) : GadgetError(str) {}
};

XERCES_CPP_NAMESPACE_END

#endif
