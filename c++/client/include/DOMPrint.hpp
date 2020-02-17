#ifndef DOMPRINT_H
#define DOMPRINT_H

#include <xercesc/dom/DOMNode.hpp>

XERCES_CPP_NAMESPACE_BEGIN

std::ostream& operator<<(std::ostream& target, const XMLCh* s);
std::ostream& operator<<(std::ostream& target, const DOMNode* s);

XERCES_CPP_NAMESPACE_END

#endif
