#include <xercesc/dom/DOM.hpp>
#include <xercesc/util/XMLString.hpp>

#include <iostream>
#include <strstream>

using namespace std;

#include "domutils.hpp"
#include "usiacspec.hpp"
#include "gadgetcomm.hpp"
#include "message.hpp"

Response::Response(const DOMElement* d) :
data_(XMLString::transcode(d->getFirstChild()->getNodeValue())) {}

const string& Response::getData() const {return data_;}

Var::Var(const DOMElement* d) : 
  data_(XMLString::transcode(d->getFirstChild()->getNodeValue())) {}

const string& Var::getData() const {return data_;}

Database::Database(const DOMElement* d) :
  host_(XMLString::transcode(d->getAttribute(XMLString::transcode("host")))),
  name_(XMLString::transcode(d->getAttribute(XMLString::transcode("name")))),
  table_(XMLString::transcode(d->getAttribute(XMLString::transcode("table")))),
  column_(XMLString::transcode(d->getAttribute(XMLString::transcode("column")))),
  explanation_(XMLString::transcode(d->getAttribute(XMLString::transcode("explanation")))) {}

const string& Database::getHost() const {return host_;}

const string& Database::getName() const {return name_;}

const string& Database::getTable() const {return table_;}

const string& Database::getColumn() const {return column_;}

const string& Database::getExplanation() const {return explanation_;}

Basic::Basic(const DOMElement* d) :
  type_(XMLString::transcode(d->getAttribute(XMLString::transcode("type")))) {}

const string& Basic::getType() const {return type_;}

Alias::Alias(const DOMElement* d) :
  data_(XMLString::transcode(d->getFirstChild()->getNodeValue())) {}

const string& Alias::getData() const {return data_;}

Canonical::Canonical(const DOMElement* d) :
  data_(XMLString::transcode(d->getFirstChild()->getNodeValue())) {}

const string& Canonical::getData() const {return data_;}

bool Canonical::operator==(const Canonical& x) const {return data_ == x.data_;}

bool Canonical::operator!=(const Canonical& x) const {return data_ != x.data_;}

Label::Label(const DOMElement* d) :
  canonical_(getd<Canonical>(d, "canonical")),
  aliasv_(getdlv<Alias>(d, "alias")),
  response_(getd<Response>(d, "response")),
  explanation_(XMLString::transcode(d->getAttribute(XMLString::transcode("explanation")))) {}

Label::Label(const Label& x) :
  canonical_(x.canonical_? new Canonical(*x.canonical_): NULL),
  aliasv_(x.aliasv_),
  response_(x.response_? new Response(*x.response_): NULL),
  explanation_(x.explanation_) {}

Label& Label::operator=(const Label& x) {
  if (this != &x) {
    delete canonical_;
    canonical_ = x.canonical_? new Canonical(*x.canonical_): NULL;
	delete response_;
	response_ = x.response_? new Response(*x.response_): NULL;
    aliasv_ = x.aliasv_;
    explanation_ = x.explanation_;
  }
  return *this;
}

const Canonical* Label::getCanonical() const {return canonical_;}

const vector<Alias>& Label::getAliasV() const {return aliasv_;}

const Response* Label::getResponse() const {return response_;}

const string& Label::getExplanation() const {return explanation_;}

vector<string> Label::getSurfaceV() const {
  vector<string> answer;
  answer.push_back(getCanonical()->getData());
  const vector<Alias>& aliasv = getAliasV();
  for(vector<Alias>::const_iterator i = aliasv.begin();
      i != aliasv.end();
      i++)
    answer.push_back(i->getData());
  return answer;
};

Action::Action(const DOMElement* d) :
  basic_(getd<Basic>(d, "basic")),
  database_(getd<Database>(d, "database")),
  label_(getd<Label>(d, "label")),
  var_(getd<Var>(d, "var")),
  readonly_(!strcmp("true", XMLString::transcode(d->getAttribute(XMLString::transcode("readonly"))))),
  name_(XMLString::transcode(d->getAttribute(XMLString::transcode("name")))) {}

Action::Action(const Action& x) :
  basic_(x.basic_? new Basic(*x.basic_): NULL),
  database_(x.database_? new Database(*x.database_): NULL),
  var_(x.var_? new Var(*x.var_): NULL),
  label_(x.label_? new Label(*x.label_): NULL),
  readonly_(x.readonly_),
  name_(x.name_) {}

Action& Action::operator=(const Action& x) {
  if (this != &x) {
    delete basic_;
    basic_ = x.basic_? new Basic(*x.basic_): NULL;
    delete database_;
    database_ = x.database_? new Database(*x.database_): NULL;
    delete var_;
    var_ = x.var_? new Var(*x.var_): NULL;
    delete label_;
    label_ = x.label_? new Label(*x.label_): NULL;
	readonly_ = x.readonly_;
    name_ = x.name_;
  }
  return *this;
}

bool Action::getReadonly() const {return readonly_;}

const Basic* Action::getBasic() const {return basic_;}

const Database* Action::getDatabase() const {return database_;}

const Label* Action::getLabel() const {return label_;}

const Var* Action::getVar() const {return var_;}

const string& Action::getName() const {return name_;}

Node::Node(const DOMElement* d) :
  label_(getd<Label>(d, "label")),
  action_(getd<Action>(d, "action")),
  nodev_(getdlv<Node>(d, "node")),
  nameNumber_(namecounter++),
  parent_(NULL) {
  for(vector<Node>::iterator i = nodev_.begin(); i != nodev_.end(); i++)
    i->parent_ = this;
}

Node::Node(const vector<Node*>& children) : //make a root node
  label_(new Label()),
  action_(NULL),
  nameNumber_(0),
  parent_(NULL) {
  for(vector<Node*>::const_iterator i = children.begin();
		  i != children.end();
	      i++) {
	 //cout << "new parent child" << (*i == NULL? "NULL": (*i)->getName()) << endl;
    (*i)->parent_ = this;
	nodev_.push_back(**i);
  }
  //cout << "new node parent " << (parent_ == NULL? "NULL": parent_->getName()) << endl;
}


Node::Node(const Node& x) :
  label_(x.label_? new Label(*x.label_): NULL),
  action_(x.action_? new Action(*x.action_): NULL),
  nodev_(x.nodev_),
  nameNumber_(x.nameNumber_),
  parent_(x.parent_) {
  for(vector<Node>::iterator i = nodev_.begin(); i != nodev_.end(); i++)
    i->parent_ = this;
}

Node& Node::operator=(const Node& x) {
  if (this != &x) {
    delete label_;
    label_ = x.label_? new Label(*x.label_): NULL;
    delete action_;
    action_ = x.action_? new Action(*x.action_): NULL;
    nodev_ = x.nodev_;
    parent_ = x.parent_;
    for(vector<Node>::iterator i = nodev_.begin(); i != nodev_.end(); i++)
      i->parent_ = this;
  }
  return *this;
}
    
const Label* Node::getLabel() const {return label_;}

const Action* Node::getAction() const {return action_;}

const vector<Node>& Node::getNodeV() const {return nodev_;}

string Node::getName() const {
  ostrstream name;
  name << "NODE" << nameNumber_ << ends;
  return name.str();
}

bool Node::isLabeled() const {
  return label_ != NULL || (action_ != NULL && 
			    ((action_->getBasic() != NULL && action_->getBasic()->getType() != "read-only") ||
			     action_->getDatabase() != NULL ||
			     action_->getLabel() != NULL));
}

const Node* Node::getParent() const {return parent_;}

const Node* Node::getLabeledParent() const {
  if (parent_ == NULL || parent_->isLabeled()) return parent_;
  return parent_->getLabeledParent();
}

list<const Node*> Node::getLabeledChildren() const {
  list<const Node*> answer;
  if(isLabeled()) {
	  answer.push_back(this);
  } else {
	  vector<Node>::const_iterator i;
	  for(i = nodev_.begin(); i != nodev_.end(); i++) {
	     list<const Node*> retval = i->getLabeledChildren();
		 answer.splice(answer.end(), retval);
	  }
  }
  return answer;
}

vector<string> Node::addunits(const vector<string> p, 
			      const vector<string> u) {
  if (p.size() == 0) return u;
  vector<string> answer;
  for(vector<string>::const_iterator i = p.begin(); i != p.end(); i++) {
    answer.push_back(*i);
    for(vector<string>::const_iterator j = u.begin(); j != u.end(); j++) {
      ostrstream t;
      t << *i << ' ' << *j << ends;
      answer.push_back(t.str());
    }
  }
  return answer;
};

otl_connect USIAC_spec::db_ = otl_connect();

vector<string> Node::getSurfaceReps() const {
  vector<string> u;
  vector<string> p;
  if (label_ != NULL) u = label_->getSurfaceV();
  if (action_ != NULL) {
    if (action_->getBasic() != NULL && action_->getBasic()->getType() != "read-only") {
      ostrstream surface;
      surface << '[' << action_->getBasic()->getType() << ']' << ends;
      p.push_back(surface.str());
    } else if (action_->getDatabase() != NULL) {
	  const Database *d = action_->getDatabase();
      //open db connection
      try {
		string connectstring = "DSN=" + d->getName();
		cerr << connectstring << endl;
		USIAC_spec::db_.rlogon(connectstring.c_str());
		string querystring = "select " + d->getColumn() + " from " + d->getTable();
		cerr << querystring << endl;
		otl_stream i(200, querystring.c_str(), USIAC_spec::db_);
        //get query vector
		while(!i.eof()) {
			char f[100];
			i >> f;
			p.push_back(f);
		}
		USIAC_spec::db_.logoff();
	  } catch(otl_exception& e) {
		  cerr << e.msg << endl;
		  cerr << e.stm_text << endl;
		  cerr << e.sqlstate << endl;
		  cerr << e.var_info << endl;
	  }
    } else if (action_->getLabel() != NULL)
      p = action_->getLabel()->getSurfaceV();
  }
  return addunits(p, u);
}

vector<string> Node::getTTSReps() const {
	vector<string> answer;
	if (label_ != NULL) {
		answer.push_back(label_->getCanonical()->getData());
		if (label_->getResponse() != NULL) {
			answer.push_back(label_->getResponse()->getData());
		}
	}
	if (action_ != NULL) {
		const Basic* b = action_->getBasic();
		const Label* al = action_->getLabel();
		const Database* db = action_->getDatabase();
		if (b != NULL) {
			string btype = b->getType();
			if (btype == "time-of-day") {
				answer.push_back("midnight");
				answer.push_back("a m");
				answer.push_back("p m");
				answer.push_back("morning");
				answer.push_back("afternoon");
				answer.push_back("noon");
				answer.push_back("evening");
				answer.push_back("night");
				answer.push_back("past");
				answer.push_back("till");
				answer.push_back("quarter");
				answer.push_back("half");
				answer.push_back("o'clock");
			} else if (btype == "day-of-week") {
				answer.push_back("sunday");
				answer.push_back("monday");
				answer.push_back("tuesday");
				answer.push_back("wednesday");
				answer.push_back("thursday");
				answer.push_back("friday");
				answer.push_back("saturday");
				answer.push_back("weekend");
				answer.push_back("weekday");
            } else if (btype == "Num-10") {
                answer.push_back("zero");
                answer.push_back("one");
                answer.push_back("two");
                answer.push_back("three");
                answer.push_back("four");
                answer.push_back("five");
                answer.push_back("six");
                answer.push_back("seven");
				answer.push_back("eight");
				answer.push_back("nine");
			} else if (btype == "Num-100") {
				answer.push_back("zero");
				answer.push_back("one");
				answer.push_back("two");
				answer.push_back("three");
				answer.push_back("four");
				answer.push_back("five");
				answer.push_back("six");
				answer.push_back("seven");
				answer.push_back("eight");
				answer.push_back("nine");
				answer.push_back("ten");
				answer.push_back("eleven");
				answer.push_back("twelve");
				answer.push_back("thirteen");
				answer.push_back("fourteen");
				answer.push_back("fifteen");
				answer.push_back("sixteen");
				answer.push_back("seventeen");
				answer.push_back("eighteen");
				answer.push_back("nineteen");
				answer.push_back("twenty");
				answer.push_back("thirty");
				answer.push_back("forty");
				answer.push_back("fifty");
				answer.push_back("sixty");
				answer.push_back("seventy");
				answer.push_back("eighty");
				answer.push_back("ninety");
			} else if (btype == "Num-1K") {
				answer.push_back("zero");
				answer.push_back("one");
				answer.push_back("two");
				answer.push_back("three");
				answer.push_back("four");
				answer.push_back("five");
				answer.push_back("six");
				answer.push_back("seven");
				answer.push_back("eight");
				answer.push_back("nine");
				answer.push_back("ten");
				answer.push_back("eleven");
				answer.push_back("twelve");
				answer.push_back("thirteen");
				answer.push_back("fourteen");
				answer.push_back("fifteen");
				answer.push_back("sixteen");
				answer.push_back("seventeen");
				answer.push_back("eighteen");
				answer.push_back("nineteen");
				answer.push_back("twenty");
				answer.push_back("thirty");
				answer.push_back("forty");
				answer.push_back("fifty");
				answer.push_back("sixty");
				answer.push_back("seventy");
				answer.push_back("eighty");
				answer.push_back("ninety");
				answer.push_back("hundred");
			} else if (btype == "Decimal-3.1") {
				answer.push_back("zero");
				answer.push_back("one");
				answer.push_back("two");
				answer.push_back("three");
				answer.push_back("four");
				answer.push_back("five");
				answer.push_back("six");
				answer.push_back("seven");
				answer.push_back("eight");
				answer.push_back("nine");
				answer.push_back("ten");
				answer.push_back("eleven");
				answer.push_back("twelve");
				answer.push_back("thirteen");
				answer.push_back("fourteen");
				answer.push_back("fifteen");
				answer.push_back("sixteen");
				answer.push_back("seventeen");
				answer.push_back("eighteen");
				answer.push_back("nineteen");
				answer.push_back("twenty");
				answer.push_back("thirty");
				answer.push_back("forty");
				answer.push_back("fifty");
				answer.push_back("sixty");
				answer.push_back("seventy");
				answer.push_back("eighty");
				answer.push_back("ninety");
				answer.push_back("hundred");
				answer.push_back("point");
			} else cerr << "Node::getTTSReps: unknown basic type: " << btype << endl;
			}
			if(al != NULL) {
				//cerr << "al not null for " << al->getCanonical()->getData() << endl;
				answer.push_back(al->getCanonical()->getData());
				if (al->getResponse() != NULL) {
					//cerr << "r not null for " << al->getResponse()->getData() << endl;
					answer.push_back(al->getResponse()->getData());
				}
			}
			if(db != NULL) {
				answer.push_back(db->getExplanation());
				const Database *d = action_->getDatabase();
				//open db connection
				try {
					string connectstring = "DSN=" + d->getName();
					cerr << connectstring << endl;
					USIAC_spec::db_.rlogon(connectstring.c_str());
					string querystring = "select " + d->getColumn() + " from " + d->getTable();
					cerr << querystring << endl;
					otl_stream i(200, querystring.c_str(), USIAC_spec::db_);
					//get query vector
					while(!i.eof()) {
						char f[100];
						i >> f;
						answer.push_back(f);
					}
					USIAC_spec::db_.logoff();
				} catch(otl_exception& e) {
					cerr << e.msg << endl;
					cerr << e.stm_text << endl;
					cerr << e.sqlstate << endl;
					cerr << e.var_info << endl;
				}
			}
			}
			return answer;
}

string Node::getParaphrase(const vector<string>& n, const vector<string>& v) const {
	const Label* l = getLabel();
	if (l == NULL) {
		int i;
		for(i = 0; i < n.size(); i++) if (n[i] == getName()) break;
		if (i == n.size()) {
			const Action* a = getAction();
			if (a == NULL) return "NULL Label";
			const Basic* b = a->getBasic();
			const Label* al = a->getLabel();
			const Database* db = a->getDatabase();
			if (b != NULL) {
				string btype = b->getType();
				if (btype == "Time-Of-Day") {
					return voice[TIMEOFDAY];
				} else if (btype == "day-of-week") {
					return voice[DAYOFWEEK];
				} else if (btype == "Num-10") {
					return voice[NUM10];
				} else if (btype == "Num-100") {
					return voice[NUM100];
				} else if (btype == "Num-1K") {
					return voice[NUM1K];
				} else {
					//shouldn't get here
					cerr << "Node::getParaphrase: unknown basic type: "
						<< btype << endl;
				}
			} else if(al != NULL) {
				return al->getCanonical()->getData();
			} else if(db != NULL) {
				return db->getExplanation();
			}
			return "NULL Label";
		}
		return v[i];
	}
	const Response* resp = l->getResponse();
	if (resp != NULL) {
		return resp->getData();
	}
	const Canonical* c = l->getCanonical();
	if (c == NULL) return "NULL Canon";
	return getLabel()->getCanonical()->getData();
}

string Node::getPrompt() const {
	const Label* l = getLabel();
	if (l == NULL) {
		const Action* a = getAction();
		if (a == NULL) return "NULL Label";
		const Basic* b = a->getBasic();
		const Label* al = a->getLabel();
		const Database* db = a->getDatabase();
		if (b != NULL) {
			string btype = b->getType();
			if (btype == "Time-Of-Day") {
				return voice[TIMEOFDAY];
			} else if (btype == "day-of-week") {
				return voice[DAYOFWEEK];
			} else if (btype == "Num-10") {
				return voice[NUM10];
			} else if (btype == "Num-100") {
				return voice[NUM100];
			} else if (btype == "Num-1K") {
				return voice[NUM1K];
			} else {
				//shouldn't get here
				cerr << "Node::getPrompt: unknown basic type: "
					<< btype << endl;
			}
		} else if(al != NULL) {
			return al->getCanonical()->getData();
		} else if(db != NULL) {
			return db->getExplanation();
		}
		return "NULL Label";
	}
	const Canonical* c = l->getCanonical();
	if (c == NULL) return "NULL Canon";
	return getLabel()->getCanonical()->getData();
}

bool Node::operator==(const Node& x) const {
	return nameNumber_ == x.nameNumber_;
}

bool Node::operator!=(const Node& x) const {
	return nameNumber_ != x.nameNumber_;
}

USIAC_spec::USIAC_spec(const DOMDocument* d) {
  DOMElement* e = d->getDocumentElement();
  if (e == NULL)
    throw GadgetInitError("doc is null");
  else
    *this = e;
}

USIAC_spec::USIAC_spec(const DOMElement* d) :
  node_(getd<Node>(d, "node")) {
  if (node_ == NULL) throw GadgetInitError("gadget has no node");
  const Label* l = node_->getLabel();
  if (l == NULL) {
	  const Action* a = node_->getAction();
	  if (a == NULL || a->getLabel() == NULL)
		  throw GadgetInitError("gadget node has no label");
	  l = a->getLabel();
  }
  name_ = l->getCanonical()->getData();
  otl_connect::otl_initialize();
  //cerr << "after making " << node_->getName() << endl;
}

USIAC_spec::USIAC_spec(const USIAC_spec& x) :
  node_(x.node_? new Node(*x.node_): NULL),
  name_(x.name_) {}

USIAC_spec& USIAC_spec::operator=(const USIAC_spec& x) {
  if (this != &x) {
    delete node_;
    node_ = x.node_? new Node(*x.node_): NULL;
    name_ = x.name_;
  }
  return *this;
}

string USIAC_spec::getName() const {return name_;}

Node* USIAC_spec::getNode() const {return node_;}

void USIAC_spec::getNodes(const Node* n, vector<const Node*>& npv) const {
  npv.push_back(n);
  const vector<Node>& children = n->getNodeV();
  for(vector<Node>::const_iterator i = children.begin(); 
      i != children.end(); 
      i++)
    getNodes(&*i, npv);
}

const vector<const Node*> USIAC_spec::getNodes() const {
  vector<const Node*> answer;
  getNodes(node_, answer);
  return answer;
}

