//message classes for encoding and decoding the PUC communications spec
//largely copied from Joseph Hughes' java version of the same

//#include <sstream>
#include <strstream>

#include <xercesc/parsers/XercesDOMParser.hpp>
#include <xercesc/framework/MemBufInputSource.hpp>

XERCES_CPP_NAMESPACE_USE
using namespace std;

extern DOMImplementation* impl;

#include "DOMTreeErrorReporter.hpp"
#include "DOMPrint.hpp"
#include "message.hpp"
#include "gadgetcomm.hpp"
#include "domutils.hpp"

const char* Message::ROOT_STRING = "message";
const char* StateChangeRequest::TYPE_STRING = "state-change-request";
const char* StateChangeNotification::TYPE_STRING = "state-change-notification";
const char* SpecRequest::TYPE_STRING = "spec-request";
const char* CommandInvokeRequest::TYPE_STRING = "command-invoke-request";
const char* DeviceSpec::TYPE_STRING = "device-spec";
const char* FullStateRequest::TYPE_STRING = "full-state-request";
//const char* StateQuery::TYPE_STRING = "state-query";

Message* Message::decode(string str) {
  DOMDocument* doc = getDocument(str);
  Message* result = NULL;
  DOMElement* root = doc->getDocumentElement();
  string rootTag = XMLString::transcode(root->getTagName());
  string typeTag;
  DOMElement* type;
  if (rootTag != ROOT_STRING) {
    cerr << "rootTag '" << rootTag << "' is unknown" << endl;
    return NULL;
  } else {
    type = (DOMElement *) root->getFirstChild();
    typeTag = XMLString::transcode(type->getTagName());
  }
  if (typeTag == StateChangeNotification::TYPE_STRING)
    result = new StateChangeNotification(type);
  else if (typeTag == DeviceSpec::TYPE_STRING)
    result = new DeviceSpec(type);
  else
    cerr << "type '" << typeTag << "'is unknown" << endl;

  return result;
}

string Message::encode(const DOMDocument* doc) {
  cerr << "message::encode" << endl;
  ostrstream oresult;
  //oresult << "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" << endl;
  oresult << doc << endl << ends;
  return oresult.str();
}

DOMDocument* Message::getDocument(string str) {
  XercesDOMParser *parser = new XercesDOMParser;
  parser->setValidationScheme(XercesDOMParser::Val_Never);
  parser->setDoNamespaces(false);
  DOMTreeErrorReporter *errReporter = new DOMTreeErrorReporter();
  parser->setErrorHandler(errReporter);
  parser->setCreateEntityReferenceNodes(false);
  //parser->setToCreateXMLDeclTypeNode(false);
  
  MemBufInputSource in((XMLByte *) str.c_str(), str.length(), "testmsg");
  parser->parse(in);
  DOMDocument* doc = parser->adoptDocument();
  delete errReporter;
  delete parser;
  if (doc == NULL) {
    throw GadgetInitError("parsed doc is null");
  }
  DOMNode* e = doc->getDocumentElement();
  if (e == NULL) {
    throw GadgetInitError("message is null");
  } else 
    return doc;
}

string StateChangeRequest::encode() const {
  DOMDocument* doc = impl->createDocument(XMLString::transcode(""), 
					  XMLString::transcode(ROOT_STRING), 
					  NULL);
  DOMElement* root = doc->getDocumentElement();
  DOMElement* typeE = doc->createElement(XMLString::transcode(TYPE_STRING));
  root->appendChild(typeE);
  DOMElement* stateE = doc->createElement(XMLString::transcode("state"));
  stateE->appendChild(doc->createTextNode(XMLString::transcode(state_.c_str())));
  typeE->appendChild(stateE);
  DOMElement* valueE = doc->createElement(XMLString::transcode("value"));
  valueE->appendChild(doc->createTextNode(XMLString::transcode(value_.c_str())));
  typeE->appendChild(valueE);
  return Message::encode(doc);
}
  
StateChangeNotification::StateChangeNotification(const DOMElement* type) {
  DOMElement* stateE = (DOMElement *) type->getFirstChild();
  DOMText* cdata = getText(stateE->getFirstChild());
  state_ = XMLString::transcode(cdata->getData());
  DOMElement* valueE = getElement(type->getLastChild());
  if(valueE->hasChildNodes()) {
    cdata = getText(valueE->getFirstChild());
    value_ = XMLString::transcode(cdata->getData());
  } else value_ = string();
}

string SpecRequest::encode() const {
  DOMDocument* doc = impl->createDocument(XMLString::transcode(""), 
					  XMLString::transcode(ROOT_STRING), 
					  NULL);
  DOMElement* root = doc->getDocumentElement();
  DOMElement* typeE = doc->createElement(XMLString::transcode(TYPE_STRING));
  root->appendChild(typeE);
  return Message::encode(doc);
}

string CommandInvokeRequest::encode() const {
  DOMDocument* doc = impl->createDocument(XMLString::transcode(""), 
					  XMLString::transcode(ROOT_STRING), 
					  NULL);
  DOMElement* root = doc->getDocumentElement();
  DOMElement* typeE = doc->createElement(XMLString::transcode(TYPE_STRING));
  root->appendChild(typeE);
  DOMElement* commandE = doc->createElement(XMLString::transcode("command"));
  typeE->appendChild(commandE);
  DOMText* cdataE = doc->createTextNode(XMLString::transcode(command_.c_str()));
  commandE->appendChild(cdataE);
  return Message::encode(doc);
}

DeviceSpec::DeviceSpec(const DOMElement* type) {
  DOMElement* specE = getElement(type->getFirstChild());
  DOMText* textN = getText(specE->getFirstChild());
  string str(XMLString::transcode(textN->getData()));
  spec_ = getDeviceSpecDoc(str);
}

DOMDocument* DeviceSpec::getDeviceSpecDoc(const string& str) {
  DOMDocument* answer;
  XercesDOMParser *parser = new XercesDOMParser();
  parser->setValidationScheme(XercesDOMParser::Val_Never);
  parser->setDoNamespaces(false);
  ErrorHandler *errReporter = new DOMTreeErrorReporter();
  parser->setErrorHandler(errReporter);
  parser->setCreateEntityReferenceNodes(false);
  MemBufInputSource in((XMLByte *) str.c_str(), 
		       str.length(), 
		       "embedded device");
  parser->parse(in);
  answer = parser->adoptDocument();
  delete errReporter;
  delete parser;
  DOMElement* e = answer->getDocumentElement();
  if (e == NULL)
    throw GadgetInitError("message is null");
  return answer;
}

string FullStateRequest::encode() const {
  DOMDocument* doc = impl->createDocument(XMLString::transcode(""), 
					  XMLString::transcode(ROOT_STRING), 
					  NULL);
  DOMElement* root = doc->getDocumentElement();
  DOMElement* typeE = doc->createElement(XMLString::transcode(TYPE_STRING));
  root->appendChild(typeE);
  return Message::encode(doc);
}
  
