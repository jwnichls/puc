#include <string>
#include <vector>
#include <iostream>

#include <xercesc/dom/DOMDocument.hpp>
#include <xercesc/util/XMLString.hpp>
#include <xercesc/dom/DOMNodeList.hpp>

using namespace std;

XERCES_CPP_NAMESPACE_BEGIN

DOMElement* getElement(const DOMNode* n) {
  DOMElement* e;
  if (n->getNodeType() == DOMNode::ELEMENT_NODE)
    e = (DOMElement*) n;
  else {
    cerr << "Tried to downcast from non-element to element" << endl;
    if (n == 0) cerr << "node is null" << endl;
    else
      cerr << "Name is: " << XMLString::transcode(n->getNodeName())
	   << " Value is: " << XMLString::transcode(n->getNodeValue()) << endl;
  }
  return e;
}

DOMText* getText(const DOMNode* n) {
  DOMText* t;
  if (n->getNodeType() == DOMNode::TEXT_NODE)
    t = (DOMText*) n;
  else
    cerr << "Tried to downcast from non-text element to text node" << endl;
  return t;
}

DOMElement* getElement(const DOMNodeList* dnl, int item) {
  DOMNode* node = dnl->item(item);
  return getElement(node);
}

XERCES_CPP_NAMESPACE_END