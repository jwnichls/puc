
#ifndef USIACSPEC_H
#define USIACSPEC_H

#include <string>
#include <vector>
#include <list>

#include <xercesc/dom/DOMElement.hpp>

#ifdef WIN32
#include "windows.h"
#include <odbcinst.h>
#define OTL_ODBC
#else
#define OTL_ODBC_UNIX
#endif

#include <sqlext.h>
#include <sql.h>
#include <sqltypes.h>
#include "otlv4.h"
#include "gadgetcomm.hpp"

class Response {
	string data_;

public:
	Response(const DOMElement* d);
	Response() : data_("james here") {}

	const string& getData() const;
};

class Var {
  string data_;

public:
  Var(const DOMElement* d);

  const string& getData() const;
};

class Database {
  string host_;
  string name_;
  string table_;
  string column_;
  string explanation_;
  
public:
  Database(const DOMElement* d);
  
  const string& getHost() const;
  const string& getName() const;
  const string& getTable() const;
  const string& getColumn() const;
  const string& getExplanation() const;
};

class Basic {
  string type_;

public:
  Basic(const DOMElement* d);
  
  const string& getType() const;
};

class Alias {
  string data_;

public:
  Alias(const DOMElement* d);
  Alias(string x) : data_(x) {}

  const string& getData() const;
};

class Canonical {
  string data_;

public:
  Canonical(const DOMElement* d);
  Canonical() : data_("james") {} //make a root canonical

  const string& getData() const;
  bool operator==(const Canonical& x) const;
  bool operator!=(const Canonical& x) const;
};

class Label {
  Canonical *canonical_;
  vector<Alias> aliasv_;
  string explanation_;
  Response *response_;

public:
  Label(const DOMElement* d);
  Label() 
	  : canonical_(new Canonical()), 
	  explanation_("say, hello james, to start gadget interaction"),
	  response_(new Response()) {
	  aliasv_.push_back(Alias("start over"));
	  aliasv_.push_back(Alias("hello james"));
  } //make a root Label
  Label(const Label& l);
  Label& operator=(const Label& l);
  ~Label() {delete canonical_; delete response_;}

  const Canonical* getCanonical() const;
  const vector<Alias>& getAliasV() const;
  const string& getExplanation() const;
  vector<string> getSurfaceV() const;
  const Response* getResponse() const;
};

class Action {
  Basic *basic_;
  Database *database_;
  Label *label_;
  Var *var_;
  string name_;
  bool readonly_;

public:
  Action(const DOMElement* d);
  Action(const Action& a);
  Action& operator=(const Action& a);
  ~Action() {
	  delete basic_; 
	  delete database_; 
	  delete label_; 
	  delete var_;
  }

  bool getReadonly() const;
  const Basic* getBasic() const;
  const Database* getDatabase() const;
  const Label* getLabel() const;
  const Var* getVar() const;
  const string& getName() const;
};

class Node {
public:
	
	static char* voice[6];
	enum SPECVOICE {TIMEOFDAY, DAYOFWEEK, NUM10, NUM100, NUM1K};

private:
  Label *label_;
  Action *action_;
  vector<Node> nodev_;
  int nameNumber_;
  const Node *parent_;

public:
  static int namecounter;
  Node(const DOMElement* d); 
  Node(const Node&);
  Node(const vector<Node *>& childeren); //make a root node
  Node& operator=(const Node& x);
  ~Node() {delete label_; delete action_;}

  //basic operations
  const Label* getLabel() const;
  const Action* getAction() const;
  const vector<Node>& getNodeV() const;
  string getName() const;
  const Node* getParent() const;

  //complex operations
  bool isLabeled() const;
  const Node* getLabeledParent() const;
  list<const Node*> getLabeledChildren() const;
  vector<string> getSurfaceReps() const;
  vector<string> getTTSReps() const;

  //what does one respond with
  string getParaphrase(const vector<string>& n, const vector<string>& v) const;

  //what does one prompt with
  string getPrompt() const;

  //overloaded comparators
  bool operator==(const Node& x) const;
  bool operator!=(const Node& x) const;

protected:
  static vector<string> addunits(const vector<string> p, 
				 const vector<string> u);
};

class USIAC_spec {
  string name_;
  Node *node_;
 
public:
  USIAC_spec(const DOMDocument* d);
  USIAC_spec(const DOMElement* d);
  USIAC_spec& operator=(const USIAC_spec& x);
  USIAC_spec(const USIAC_spec&);
  ~USIAC_spec() {delete node_;}

  static otl_connect db_;
  string getName() const;
  Node* getNode() const;
  void getNodes(const Node* n, vector<const Node*>& npv) const;
  const vector<const Node*> getNodes() const;
};

#endif
