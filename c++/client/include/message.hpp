//message classes for encoding and decoding the PUC communications spec
//largely copied from Joseph Hughes' java version of the same

#ifndef PUCMESSAGE_H
#define PUCMESSAGE_H

#include <string>

#include <xercesc/dom/DOMDocument.hpp>

XERCES_CPP_NAMESPACE_USE

class Message {
public:
  static const char* ROOT_STRING;
  
  //returns null pointer if str is malformed
  static Message* decode(string str); 
  static string encode(const DOMDocument* doc);
  virtual string encode() const {return string();}
  virtual string getState() const {return string();}
  virtual string getValue() const {return string();}
  virtual string getCommand() const {return string();}
  virtual const DOMDocument* getSpec() const {return NULL;}
  
private:
  static DOMDocument* getDocument(string str);
};

class StateChangeRequest : public Message {
public:
  static const char* TYPE_STRING;

private:
  string state_;
  string value_;

public:
  StateChangeRequest(string state, string value) 
    : state_(state), value_(value) {}
  string encode() const;
  string getState() const {return state_;}
  string getValue() const {return value_;}
};

class StateChangeNotification : public Message {
public:
  static const char* TYPE_STRING;
  
private:
  string state_;
  string value_;

public:
  StateChangeNotification(const DOMElement* type);
  string getState() const {return state_;}
  string getValue() const {return value_;}
};

class SpecRequest : public Message {
public:
  static const char* TYPE_STRING;

  SpecRequest() {}
  string encode() const;
};

class CommandInvokeRequest : public Message {
public:
  static const char* TYPE_STRING;

private:
  string command_;

public:
  CommandInvokeRequest(string cmd) : command_(cmd) {}
  string encode() const;
  string getCommand() const {return command_;}
};

class DeviceSpec : public Message {
public:
  static const char* TYPE_STRING;

private:
  DOMDocument* spec_;

public:
  DeviceSpec(const DOMElement* type);
  const DOMDocument* getSpec() const {return spec_;}
  static DOMDocument* getDeviceSpecDoc(const string& str);
};

class FullStateRequest : public Message {
public:
  static const char* TYPE_STRING;

  FullStateRequest() {}
  string encode() const;
};

/*
class StateQuery : public Message {
public:
	static const char* TYPE_STRING;

private:
	string state_;

public:
	StateQuery(string state) : state_(state) {}
	string encode() const;
	string getState() const {return state_;}
};
*/

#endif
