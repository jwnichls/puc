
#ifndef PUCSPEC_H
#define PUCSPEC_H

#include <string>
#include <vector>

#include <xercesc/dom/DOMElement.hpp>

namespace gadget {
enum parentT {GNOPARENT, GPARENT, GALL};
enum accessT {GNOACCESS, GREADONLY, GWRITEONLY};
}

XERCES_CPP_NAMESPACE_USE
using namespace std;

class refvalueT {
  string state_;

public:
  refvalueT(const DOMElement* d);

  string getState() const;
};

class numberT {
  string data_;
  
public:
  numberT(const DOMElement* d);
  
  string getData() const;
};

class minT {
  numberT *numbert_;
  refvalueT *refvaluet_;
  
public:
  minT(const DOMElement* d);
  minT(const minT&);
  ~minT() {delete numbert_; delete refvaluet_;}

  const numberT* getNumberT() const;
  const refvalueT* getRefvalueT() const;
};

class maxT {
  numberT *numbert_;
  refvalueT *refvaluet_;
  
public:
  maxT(const DOMElement* d);
  maxT(const maxT&);
  ~maxT() {delete numbert_; delete refvaluet_;}

  const numberT* getNumberT() const;
  const refvalueT* getRefvalueT() const;
};

class incrT {
  numberT *numbert_;
  refvalueT *refvaluet_;
  
public:
  incrT(const DOMElement* d);
  incrT(const incrT&);
  ~incrT() {delete numbert_; delete refvaluet_;}

  const numberT* getNumberT() const;
  const refvalueT* getRefvalueT() const;
};

class items {
  string data_;
  
public:
  items(const DOMElement* d);

  string getData() const;
};

class label {
  string data_;

public:
  label(const DOMElement* d);

  string getData() const;
};

class phonetic {
  string data_;

public:
  phonetic(const DOMElement* d);

  string getData() const;
};

class refstring {
  string state_;

public:
  refstring(const DOMElement* d);

  string getState() const;
};

class text_to_speech {
  string text_;
  string recording_;

public:
  text_to_speech(const DOMElement* d);

  string getText() const;
  string getRecording() const;
};

class mapT {
  string index_;
  string enable_;
  vector<label> labelv_;
  vector<phonetic> phoneticv_;
  vector<refstring> refstringv_;
  vector<text_to_speech> text_to_speechv_;

public:
  mapT(const DOMElement* d);

  string getIndex() const;
  string getEnable() const;
  const vector<label>& getLabelV() const;
  const vector<phonetic>& getPhoneticV() const;
  const vector<refstring>& getRefstringV() const;
  const vector<text_to_speech>& getText_To_SpeechV() const;
};

struct stringT {
  stringT(const DOMElement* d) {}
};

class floatingptT {
  maxT *maxt_;
  minT *mint_;

public:
  floatingptT(const DOMElement* d);
  floatingptT(const floatingptT&);
  ~floatingptT() {delete maxt_; delete mint_;}

  const maxT* getMax() const;
  const minT* getMin() const;
};

class integerT {
  incrT *incrt_;
  maxT *maxt_;
  minT *mint_;

public:
  integerT(const DOMElement* d);
  integerT(const integerT&);
  ~integerT() {delete incrt_; delete maxt_; delete mint_;}

  const incrT* getIncr() const;
  const maxT* getMax() const;
  const minT* getMin() const;
};

class fixedptT {
  incrT *incrt_;
  maxT *maxt_;
  minT *mint_;

public:
  fixedptT(const DOMElement* d);
  fixedptT(const fixedptT&);
  ~fixedptT() {delete incrt_; delete maxt_; delete mint_;}

  const incrT* getIncr() const;
  const maxT* getMax() const;
  const minT* getMin() const;
};

class enumeratedT {
  vector<items> itemsv_;

public:
  enumeratedT(const DOMElement* d);

  const vector<items>& getItemsV() const;
};

class customT {
  string data_;

public:
  customT(const DOMElement* d);

  string getData() const;
};

struct booleanT {
  booleanT(const DOMElement* d) {};
};

class valueSpace {
  booleanT *booleant_;
  customT *customt_;
  enumeratedT *enumeratedt_;
  fixedptT *fixedptt_;
  integerT *integert_;
  stringT *stringt_;
  floatingptT *floatingptt_;

public:
  valueSpace(const DOMElement* d);
  valueSpace(const valueSpace&);
  ~valueSpace() {
    delete booleant_;
    delete customt_;
    delete enumeratedt_;
    delete fixedptt_;
    delete integert_;
    delete stringt_;
    delete floatingptt_;
  }

  const booleanT* getBooleanT() const;
  const customT* getCustomT() const;
  const enumeratedT* getEnumeratedT() const;
  const fixedptT* getFixedptT() const;
  const integerT* getIntegerT() const;
  const stringT* getStringT() const;
  const floatingptT* getFloatingptT() const;
};
  
class valueLabels {
  vector<mapT> maptv_;

public:
  valueLabels(const DOMElement* d);
  
  const vector<mapT>& getMapTV() const;
};

class expectedValues {
  booleanT *booleant_;
  customT *customt_;
  enumeratedT *enumeratedt_;
  fixedptT *fixedptt_;
  integerT *integert_;
  stringT *stringt_;
  floatingptT *floatingptt_;

public:
  expectedValues(const DOMElement* d);
  expectedValues(const expectedValues&);
  ~expectedValues() {
    delete booleant_;
    delete customt_;
    delete enumeratedt_;
    delete fixedptt_;
    delete integert_;
    delete stringt_;
    delete floatingptt_;
  }

  const booleanT* getBooleanT() const;
  const customT* getCustomT() const;
  const enumeratedT* getEnumeratedT() const;
  const fixedptT* getFixedptT() const;
  const integerT* getIntegerT() const;
  const stringT* getStringT() const;
  const floatingptT* getFloatingptT() const;
};

class apply_type {
  string name_;

public:
  apply_type(const DOMElement* d);

  string getName() const;
};

class type {
  string id_;
  valueSpace *valueSpace_;
  expectedValues *expectedValues_;
  valueLabels *valueLabels_;

public:
  type(const DOMElement* d);
  type(const type&);
  ~type() {delete valueSpace_; delete expectedValues_; delete valueLabels_;}

  string getId() const;
  const valueSpace* getValueSpace() const;
  const expectedValues* getExpectedValues() const;
  const valueLabels* getValueLabels() const;

  void setId(string s) {id_ = s;};
};

class equals {
  string state_;

public:
  equals(const DOMElement* d);

  string getState() const;
};

class lessthan {
  string state_;
  string data_;

public:
  lessthan(const DOMElement* d);

  string getState() const;
  string getData() const;
};

class greaterthan {
  string state_;
  string data_;

public:
  greaterthan(const DOMElement* d);

  string getState() const;
  string getData() const;
};

class orT {
  vector<equals> equalsv_;
  vector<greaterthan> greaterthanv_;
  vector<lessthan> lessthanv_;

public:
  orT(const DOMElement* d);

  const vector<equals>& getEqualsV() const;
  const vector<greaterthan>& getGreaterthanV() const;
  const vector<lessthan>& getLessthanV() const;
};
 
class andT {
  vector<equals> equalsv_;
  vector<greaterthan> greaterthanv_;
  vector<lessthan> lessthanv_;

public:
  andT(const DOMElement* d);

  const vector<equals>& getEqualsV() const;
  const vector<greaterthan>& getGreaterthanV() const;
  const vector<lessthan>& getLessthanV() const;
};

class object_ref {
  string name_;

public:
  object_ref(const DOMElement* d);
  
  string getName() const;
};

class labels {
  string recording_;
  vector<label> labelv_;
  vector<refstring> refstringv_;
  vector<phonetic> phoneticv_;
  vector<text_to_speech> text_to_speechv_;
  
public:
  labels(const DOMElement* d);

  string getRecording() const;
  const vector<label>& getLabelV() const;
  const vector<refstring>& getRefstringV() const;
  const vector<phonetic>& getPhoneticV() const;
  const vector<text_to_speech>& getText_To_SpeechV() const;
  friend ostream& operator<<(ostream&, const labels&);
};

class activ_if {
  gadget::parentT parent_;
  andT *andt_;
  orT *ort_;
  vector<equals> equalsv_;
  vector<greaterthan> greaterthanv_;
  vector<lessthan> lessthanv_;

public:
  activ_if(const DOMElement* d);
  activ_if(const activ_if&);
  ~activ_if() {delete andt_; delete ort_;}
  
  gadget::parentT getParent() const;
  const andT* getAnd() const;
  const orT* getOr() const;
  const vector<equals>& getEqualsV() const;
  const vector<greaterthan>& getGreaterthanV() const;
  const vector<lessthan>& getLessthanV() const;
  friend ostream& operator<<(ostream&, const activ_if&);
};

class state {
  string name_;
  gadget::accessT access_;
  int priority_;
  type *type_;
  apply_type *apply_type_;
  labels *labels_;
  activ_if *activ_if_;

public:
  state(const DOMElement* d);
  state(const state&);
  ~state() {
    delete type_; 
    delete apply_type_; 
    delete labels_; 
    delete activ_if_;
  }

  string getName() const;
  gadget::accessT getAccess() const;
  int getPriority() const;
  const type* getType() const;
  const apply_type* getApply_Type() const;
  const labels* getLabels() const;
  const activ_if* getActiv_If() const;
};

class explanation {
  string name_;
  int priority_;
  labels *labels_;
  activ_if *activ_if_;

public:
  explanation(const DOMElement* d);
  explanation(const explanation&);
  ~explanation() {delete labels_; delete activ_if_;}
  
  string getName() const;
  int getPriority() const;
  const labels* getLabels() const;
  const activ_if* getActiv_If() const;
};

class command {
  string name_;
  int priority_;
  labels *labels_;
  activ_if *activ_if_;

public:
  command(const DOMElement* d);
  command(const command&);
  ~command() {delete labels_; delete activ_if_;}
  
  string getName() const;
  int getPriority() const;
  const labels* getLabels() const;
  const activ_if* getActiv_If() const;
};

class group {
  int priority_;
  activ_if *activ_if_;
  labels *labels_;
  vector<command> commandv_;
  vector<explanation> explanationv_;
  vector<group> groupv_;
  vector<state> statev_;
  vector<object_ref> object_refv_;
  int nameNumber_;

public:
  group(const DOMElement* d);
  group(const group&);
  ~group() {delete labels_; delete activ_if_;}
  
  int getPriority() const;
  const activ_if* getActiv_If() const;
  const labels* getLabels() const;
  const vector<command>& getCommandV() const;
  const vector<explanation>& getExplanationV() const;
  const vector<group>& getGroupV() const;
  const vector<state>& getStateV() const;
  const vector<object_ref>& getObject_RefV() const;
  friend ostream& operator<<(ostream&, const group&);
  static int namecounter;
  int getNameNumber() const;
};

class groupings {
  vector<group> groupv_;

public:
  groupings(const DOMElement* d);

  const vector<group>& getGroupV() const;
};

class puc_spec {
  string name_;
  groupings *groupings_;

public:
  puc_spec(const DOMDocument* d);
  puc_spec(const DOMElement* d);
  puc_spec(const puc_spec&);
  ~puc_spec() {delete groupings_;}

  string getName() const;
  const groupings* getGroupings() const;
  type* resolveType(const state&) const;
  vector<group> getGroupV() const;
  vector<state> getStateV() const;
  vector<command> getCommandV() const;
  vector<type> getTypeV() const;
};

#endif
