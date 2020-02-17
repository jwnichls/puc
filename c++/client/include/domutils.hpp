#ifndef DOMUTILS_H
#define DOMUTILS_H

#include <string>
#include <vector>

#include <xercesc/dom/DOM.hpp>
#include <xercesc/util/XMLString.hpp>

XERCES_CPP_NAMESPACE_BEGIN

DOMElement* getElement(const DOMNode* n);
DOMText* getText(const DOMNode* n);
DOMElement* getElement(const DOMNodeList* dnl, int item);

template<class T> vector<T> getdlv(const DOMElement* d, 
				   const string& tagname) {
  vector<T> answer;
  DOMNodeList* dnl = d->getChildNodes();
  for(int i=0; i<dnl->getLength(); i++) {
    DOMNode* n = dnl->item(i);
    if (n->getNodeType() == DOMNode::ELEMENT_NODE) {
      DOMElement* e = (DOMElement*) n;
      if (tagname == XMLString::transcode(e->getTagName()))
	answer.push_back(T(e));
    }
  }
  return answer;
}

template<class T> T* getd(const DOMElement* d, const string& tagname) {
  DOMNodeList* dnl = d->getChildNodes();
  for(int i=0; i<dnl->getLength(); i++) {
    DOMNode* n = dnl->item(i);
    if (n->getNodeType() == DOMNode::ELEMENT_NODE) {
      DOMElement* e = (DOMElement*) n;
      if (tagname == XMLString::transcode(e->getTagName()))
	return new T(e);
    }
  }
  return (T*) NULL;
}

XERCES_CPP_NAMESPACE_END

#endif
