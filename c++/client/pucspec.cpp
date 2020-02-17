
#include <iostream>
#include <stack>
#include <map>

#include <xercesc/dom/DOMDocument.hpp>

using namespace std;

#include "domutils.hpp"
#include "pucspec.hpp"
#include "gadgetcomm.hpp"
#include "message.hpp"

refvalueT::refvalueT(const DOMElement* d) :
  state_(XMLString::transcode(d->getAttribute(XMLString::transcode("state")))) {}

string refvalueT::getState() const {return state_;}

numberT::numberT(const DOMElement* d) :
  data_(XMLString::transcode(d->getFirstChild()->getNodeValue())) {}

string numberT::getData() const {return data_;}

minT::minT(const DOMElement* d) :
  numbert_(getd<numberT>(d, "number")),
  refvaluet_(getd<refvalueT>(d, "refvalue")) {}

minT::minT(const minT& x) :
  numbert_(x.numbert_? new numberT(*x.numbert_): NULL),
  refvaluet_(x.refvaluet_? new refvalueT(*x.refvaluet_): NULL) {}

const numberT* minT::getNumberT() const {return numbert_;}

const refvalueT* minT::getRefvalueT() const {return refvaluet_;}

maxT::maxT(const DOMElement* d) :
  numbert_(getd<numberT>(d, "number")),
  refvaluet_(getd<refvalueT>(d, "refvalue")) {}

maxT::maxT(const maxT& x) :
  numbert_(x.numbert_? new numberT(*x.numbert_): NULL),
  refvaluet_(x.refvaluet_? new refvalueT(*x.refvaluet_): NULL) {}

const numberT* maxT::getNumberT() const {return numbert_;}

const refvalueT* maxT::getRefvalueT() const {return refvaluet_;}

incrT::incrT(const DOMElement* d) :
  numbert_(getd<numberT>(d, "number")),
  refvaluet_(getd<refvalueT>(d, "refvalue")) {}

incrT::incrT(const incrT& x) :
  numbert_(x.numbert_? new numberT(*x.numbert_): NULL),
  refvaluet_(x.refvaluet_? new refvalueT(*x.refvaluet_): NULL) {}

const numberT* incrT::getNumberT() const {return numbert_;}

const refvalueT* incrT::getRefvalueT() const {return refvaluet_;}

items::items(const DOMElement* d) :
  data_(XMLString::transcode(d->getFirstChild()->getNodeValue())) {}

string items::getData() const {return data_;}

mapT::mapT(const DOMElement* d) :
  index_(XMLString::transcode(d->getAttribute(XMLString::transcode("index")))),
  labelv_(getdlv<label>(d, "label")),
  phoneticv_(getdlv<phonetic>(d, "phonetic")),
  refstringv_(getdlv<refstring>(d, "refstring")),
  text_to_speechv_(getdlv<text_to_speech>(d, "text-to-speech")) {
  string ds;
  ds = XMLString::transcode(d->getAttribute(XMLString::transcode("enable")));
  if (ds.length() > 0) enable_ = ds;
}

string mapT::getIndex() const {return index_;}

string mapT::getEnable() const {return enable_;}

const vector<label>& mapT::getLabelV() const {return labelv_;}

const vector<phonetic>& mapT::getPhoneticV() const {return phoneticv_;}

const vector<refstring>& mapT::getRefstringV() const {return refstringv_;}

const vector<text_to_speech>& mapT::getText_To_SpeechV() const {
  return text_to_speechv_;
}

floatingptT::floatingptT(const DOMElement* d) :
  maxt_(getd<maxT>(d, "max")),
  mint_(getd<minT>(d, "min")) {}

floatingptT::floatingptT(const floatingptT& x) :
  maxt_(x.maxt_? new maxT(*x.maxt_): NULL),
  mint_(x.mint_? new minT(*x.mint_): NULL) {}

const maxT* floatingptT::getMax() const {return maxt_;}

const minT* floatingptT::getMin() const {return mint_;}

integerT::integerT(const DOMElement* d) :
  maxt_(getd<maxT>(d, "max")),
  mint_(getd<minT>(d, "min")),
  incrt_(getd<incrT>(d, "incr")) {}

integerT::integerT(const integerT& x) :
  maxt_(x.maxt_? new maxT(*x.maxt_): NULL),
  mint_(x.mint_? new minT(*x.mint_): NULL),
  incrt_(x.incrt_? new incrT(*x.incrt_): NULL) {}

const maxT* integerT::getMax() const {return maxt_;}

const minT* integerT::getMin() const {return mint_;}

const incrT* integerT::getIncr() const {return incrt_;}

fixedptT::fixedptT(const DOMElement* d) :
  maxt_(getd<maxT>(d, "max")),
  mint_(getd<minT>(d, "min")),
  incrt_(getd<incrT>(d, "incr")) {}

fixedptT::fixedptT(const fixedptT& x) :
  maxt_(x.maxt_? new maxT(*x.maxt_): NULL),
  mint_(x.mint_? new minT(*x.mint_): NULL),
  incrt_(x.incrt_? new incrT(*x.incrt_): NULL) {}

const maxT* fixedptT::getMax() const {return maxt_;}

const minT* fixedptT::getMin() const {return mint_;}

const incrT* fixedptT::getIncr() const {return incrt_;}

enumeratedT::enumeratedT(const DOMElement* d) :
  itemsv_(getdlv<items>(d, "items")) {}

const vector<items>& enumeratedT::getItemsV() const {return itemsv_;}

customT::customT(const DOMElement* d) :
  data_(XMLString::transcode(d->getFirstChild()->getNodeValue())) {}

string customT::getData() const {return data_;}

valueSpace::valueSpace(const DOMElement* d) :
  booleant_(getd<booleanT>(d, "boolean")),
  customt_(getd<customT>(d, "custom")),
  enumeratedt_(getd<enumeratedT>(d, "enumerated")),
  fixedptt_(getd<fixedptT>(d, "fixedpt")),
  integert_(getd<integerT>(d, "integer")),
  stringt_(getd<stringT>(d, "string")),
  floatingptt_(getd<floatingptT>(d, "floatingpt")) {}

valueSpace::valueSpace(const valueSpace& x) :
  booleant_(x.booleant_? new booleanT(*x.booleant_): NULL),
  customt_(x.customt_? new customT(*x.customt_): NULL),
  enumeratedt_(x.enumeratedt_? new enumeratedT(*x.enumeratedt_): NULL),
  fixedptt_(x.fixedptt_? new fixedptT(*x.fixedptt_): NULL),
  integert_(x.integert_? new integerT(*x.integert_): NULL),
  stringt_(x.stringt_? new stringT(*x.stringt_): NULL),
  floatingptt_(x.floatingptt_? new floatingptT(*x.floatingptt_): NULL) {}

const booleanT* valueSpace::getBooleanT() const {return booleant_;}

const customT* valueSpace::getCustomT() const {return customt_;}

const enumeratedT* valueSpace::getEnumeratedT() const {return enumeratedt_;}

const fixedptT* valueSpace::getFixedptT() const {return fixedptt_;}

const integerT* valueSpace::getIntegerT() const {return integert_;}

const stringT* valueSpace::getStringT() const {return stringt_;}

const floatingptT* valueSpace::getFloatingptT() const {return floatingptt_;}

valueLabels::valueLabels(const DOMElement* d) :
  maptv_(getdlv<mapT>(d, "map")) {}

const vector<mapT>& valueLabels::getMapTV() const {return maptv_;}

expectedValues::expectedValues(const DOMElement* d) :
  booleant_(getd<booleanT>(d, "boolean")),
  customt_(getd<customT>(d, "custom")),
  enumeratedt_(getd<enumeratedT>(d, "enumerated")),
  fixedptt_(getd<fixedptT>(d, "fixedpt")),
  integert_(getd<integerT>(d, "integer")),
  stringt_(getd<stringT>(d, "string")),
  floatingptt_(getd<floatingptT>(d, "floatingpt")) {}

expectedValues::expectedValues(const expectedValues& x) :
  booleant_(x.booleant_? new booleanT(*x.booleant_): NULL),
  customt_(x.customt_? new customT(*x.customt_): NULL),
  enumeratedt_(x.enumeratedt_? new enumeratedT(*x.enumeratedt_): NULL),
  fixedptt_(x.fixedptt_? new fixedptT(*x.fixedptt_): NULL),
  integert_(x.integert_? new integerT(*x.integert_): NULL),
  stringt_(x.stringt_? new stringT(*x.stringt_): NULL),
  floatingptt_(x.floatingptt_? new floatingptT(*x.floatingptt_): NULL) {}

const booleanT* expectedValues::getBooleanT() const {return booleant_;}

const customT* expectedValues::getCustomT() const {return customt_;}

const enumeratedT* expectedValues::getEnumeratedT() const {
  return enumeratedt_;
}

const fixedptT* expectedValues::getFixedptT() const {return fixedptt_;}

const integerT* expectedValues::getIntegerT() const {return integert_;}

const stringT* expectedValues::getStringT() const {return stringt_;}

const floatingptT* expectedValues::getFloatingptT() const {
  return floatingptt_;
}

refstring::refstring(const DOMElement* d) :
  state_(XMLString::transcode(d->getAttribute(XMLString::transcode("state")))) {}

string refstring::getState() const {return state_;}

apply_type::apply_type(const DOMElement* d) :
  name_(XMLString::transcode(d->getAttribute(XMLString::transcode("name")))) {}

string apply_type::getName() const {return name_;}

type::type(const DOMElement* d) :
  id_(XMLString::transcode(d->getAttribute(XMLString::transcode("name")))),
  valueSpace_(getd<valueSpace>(d, "valueSpace")),
  expectedValues_(getd<expectedValues>(d, "expectedValues")),
  valueLabels_(getd<valueLabels>(d, "valueLabels")) {}

type::type(const type& x) :
  id_(x.id_),
  valueSpace_(x.valueSpace_? new valueSpace(*x.valueSpace_): NULL),
  expectedValues_(x.expectedValues_? new expectedValues(*x.expectedValues_): NULL),
  valueLabels_(x.valueLabels_? new valueLabels(*x.valueLabels_): NULL) {}

string type::getId() const {return id_;}

const valueSpace* type::getValueSpace() const {return valueSpace_;}

const valueLabels* type::getValueLabels() const {return valueLabels_;}


text_to_speech::text_to_speech(const DOMElement* d) :
  text_(XMLString::transcode(d->getAttribute(XMLString::transcode("text")))) {
  cerr << "made a tts thing" << endl;
  string ds = XMLString::transcode(d->getAttribute(XMLString::transcode("recording")));
  if (ds.length() > 0) recording_ = ds;
  else recording_ = string();
}

string text_to_speech::getText() const {return text_;}

string text_to_speech::getRecording() const {return recording_;}

phonetic::phonetic(const DOMElement* d) :
  data_(XMLString::transcode(d->getFirstChild()->getNodeValue())) {}

string phonetic::getData() const {return data_;}

label::label(const DOMElement* d) :
  data_(XMLString::transcode(d->getFirstChild()->getNodeValue())) {}

string label::getData() const {return data_;}

orT::orT(const DOMElement* d) :
  equalsv_(getdlv<equals>(d, "equals")),
  greaterthanv_(getdlv<greaterthan>(d, "greaterthan")),
  lessthanv_(getdlv<lessthan>(d, "lessthan")) {}

const vector<equals>& orT::getEqualsV() const {return equalsv_;}

const vector<greaterthan>& orT::getGreaterthanV() const {return greaterthanv_;}

const vector<lessthan>& orT::getLessthanV() const {return lessthanv_;}

lessthan::lessthan(const DOMElement* d) :
  state_(XMLString::transcode(d->getAttribute(XMLString::transcode("state")))),
  data_(XMLString::transcode(d->getFirstChild()->getNodeValue())) {}

string lessthan::getState() const {return state_;}

string lessthan::getData() const {return data_;}

greaterthan::greaterthan(const DOMElement* d) :
  state_(XMLString::transcode(d->getAttribute(XMLString::transcode("state")))),
  data_(XMLString::transcode(d->getFirstChild()->getNodeValue())) {}

string greaterthan::getState() const {return state_;}

string greaterthan::getData() const {return data_;}

equals::equals(const DOMElement* d) :
  state_(XMLString::transcode(d->getAttribute(XMLString::transcode("state")))) {}

string equals::getState() const {return state_;}

andT::andT(const DOMElement* d) :
  equalsv_(getdlv<equals>(d, "equals")),
  greaterthanv_(getdlv<greaterthan>(d, "greaterthan")),
  lessthanv_(getdlv<lessthan>(d, "lessthan")) {}

const vector<equals>& andT::getEqualsV() const {return equalsv_;}

const vector<greaterthan>& andT::getGreaterthanV() const {return greaterthanv_;}

const vector<lessthan>& andT::getLessthanV() const {return lessthanv_;}

object_ref::object_ref(const DOMElement* d) :
  name_(XMLString::transcode(d->getAttribute(XMLString::transcode("name")))) {}

string object_ref::getName() const {return name_;}

state::state(const DOMElement* d) :
  name_(XMLString::transcode(d->getAttribute((XMLString::transcode("name"))))),
  type_(getd<type>(d, "type")),
  apply_type_(getd<apply_type>(d, "apply-type")),
  labels_(getd<labels>(d, "labels")),
  activ_if_(getd<activ_if>(d, "active-if")) {
  string ds = XMLString::transcode(d->getAttribute(XMLString::transcode("access")));
  if (ds.length() > 0) {
    if (ds == "ReadOnly")
      access_ = gadget::GREADONLY;
    else if (ds == "WriteOnly")
      access_ = gadget::GWRITEONLY;
  } else
    access_ = gadget::GNOACCESS;
  ds = XMLString::transcode(d->getAttribute(XMLString::transcode("priority")));
  priority_ = ds.length()>0? atoi(ds.c_str()): 0;
  if (type_ != NULL && type_->getId().empty())
    type_->setId(name_); //set the type name to the state name if no type name
}

state::state(const state& x) :
  name_(x.name_),
  access_(x.access_),
  priority_(x.priority_),
  type_(x.type_? new type(*x.type_): NULL),
  apply_type_(x.apply_type_? new apply_type(*x.apply_type_): NULL),
  labels_(x.labels_? new labels(*x.labels_): NULL),
  activ_if_(x.activ_if_? new activ_if(*x.activ_if_): NULL) {}

string state::getName() const {return name_;}

gadget::accessT state::getAccess() const {return access_;}

int state::getPriority() const {return priority_;}

const type* state::getType() const {return type_;}

const apply_type* state::getApply_Type() const {return apply_type_;}

const labels* state::getLabels() const {return labels_;}

const activ_if* state::getActiv_If() const {return activ_if_;}

labels::labels(const DOMElement* d) :
  labelv_(getdlv<label>(d, "label")),
  refstringv_(getdlv<refstring>(d, "refstring")),
  phoneticv_(getdlv<phonetic>(d, "phonetic")),
  text_to_speechv_(getdlv<text_to_speech>(d, "text-to-speech")) {
  string ds = XMLString::transcode(d->getAttribute(XMLString::transcode("recording")));
  if (ds.length() > 0)
    recording_ = ds;
  recording_ = string();
}

string labels::getRecording() const {return recording_;}

const vector<label>& labels::getLabelV() const {return labelv_;}

const vector<refstring>& labels::getRefstringV() const {return refstringv_;}

const vector<phonetic>& labels::getPhoneticV() const {return phoneticv_;}

const vector<text_to_speech>& labels::getText_To_SpeechV() const {
  return text_to_speechv_;
}

explanation::explanation(const DOMElement* d) :
  name_(XMLString::transcode(d->getAttribute((XMLString::transcode("name"))))),
  labels_(getd<labels>(d, "labels")),
  activ_if_(getd<activ_if>(d, "active-if")) {
  string ds = XMLString::transcode(d->getAttribute(XMLString::transcode("priority")));
  priority_ = ds.length() > 0? atoi(ds.c_str()): 0;
}

explanation::explanation(const explanation& x) :
  name_(x.name_),
  priority_(x.priority_),
  labels_(x.labels_? new labels(*x.labels_): NULL),
  activ_if_(x.activ_if_? new activ_if(*x.activ_if_): NULL) {}

string explanation::getName() const {return name_;}

int explanation::getPriority() const {return priority_;}

const labels* explanation::getLabels() const {return labels_;}

const activ_if* explanation::getActiv_If() const {return activ_if_;}

command::command(const DOMElement* d) :
  name_(XMLString::transcode(d->getAttribute((XMLString::transcode("name"))))),
  labels_(getd<labels>(d, "labels")),
  activ_if_(getd<activ_if>(d, "active-if")) {
  string ds = XMLString::transcode(d->getAttribute(XMLString::transcode("priority")));
  priority_ = ds.length() > 0? atoi(ds.c_str()): 0;
}

command::command(const command& x) :
  name_(x.name_),
  priority_(x.priority_),
  labels_(x.labels_? new labels(*x.labels_): NULL),
  activ_if_(x.activ_if_? new activ_if(*x.activ_if_): NULL) {}

string command::getName() const {return name_;}

int command::getPriority() const {return priority_;}

const labels* command::getLabels() const {return labels_;}

const activ_if* command::getActiv_If() const {return activ_if_;}

activ_if::activ_if(const DOMElement* d) :
  andt_(getd<andT>(d, "and")),
  ort_(getd<orT>(d, "or")),
  equalsv_(getdlv<equals>(d, "equals")),
  greaterthanv_(getdlv<greaterthan>(d, "greaterthan")),
  lessthanv_(getdlv<lessthan>(d, "lessthan")) {
  string ds = XMLString::transcode(d->getAttribute(XMLString::transcode("ignore")));
  if (ds.length() > 0) {
    if (ds == "parent")
      parent_ = gadget::GPARENT;
    else if (ds == "all")
      parent_ = gadget::GALL;
  } else
    parent_ = gadget::GNOPARENT;
}

activ_if::activ_if(const activ_if& x) :
  parent_(parent_),
  andt_(x.andt_? new andT(*x.andt_): NULL),
  ort_(x.ort_? new orT(*x.ort_): NULL),
  equalsv_(x.equalsv_),
  greaterthanv_(x.greaterthanv_),
  lessthanv_(x.lessthanv_) {}

gadget::parentT activ_if::getParent() const {return parent_;}

const andT* activ_if::getAnd() const {return andt_;}

const orT* activ_if::getOr() const {return ort_;}

const vector<equals>& activ_if::getEqualsV() const {return equalsv_;}

const vector<greaterthan>& activ_if::getGreaterthanV() const {
  return greaterthanv_;
}

const vector<lessthan>& activ_if::getLessthanV() const {return lessthanv_;}

group::group(const DOMElement* d) :
  activ_if_(getd<activ_if>(d, "active-if")),
  labels_(getd<labels>(d, "labels")),
  commandv_(getdlv<command>(d, "command")),
  explanationv_(getdlv<explanation>(d, "explanation")),
  groupv_(getdlv<group>(d, "group")),
  statev_(getdlv<state>(d, "state")),
  object_refv_(getdlv<object_ref>(d, "object-ref")) {
  string ds = XMLString::transcode(d->getAttribute(XMLString::transcode("priority")));
  priority_ = ds.length() > 0? atoi(ds.c_str()): 0;
  nameNumber_ = namecounter++;
}

int group::namecounter = 0;

group::group(const group& x) :
  priority_(x.priority_),
  activ_if_(x.activ_if_? new activ_if(*x.activ_if_): NULL),
  labels_(x.labels_? new labels(*x.labels_): NULL),
  commandv_(x.commandv_),
  explanationv_(x.explanationv_),
  groupv_(x.groupv_),
  statev_(x.statev_),
  object_refv_(x.object_refv_),
  nameNumber_(x.nameNumber_) {}

int group::getPriority() const {return priority_;}

const activ_if* group::getActiv_If() const {return activ_if_;}

const labels* group::getLabels() const {return labels_;}

const vector<command>& group::getCommandV() const {return commandv_;}

const vector<explanation>& group::getExplanationV() const {
  return explanationv_;
}

const vector<group>& group::getGroupV() const {return groupv_;}

const vector<state>& group::getStateV() const {return statev_;}

const vector<object_ref>& group::getObject_RefV() const {return object_refv_;}

ostream& operator<<(ostream& out, const group& x) {
  out << "priority(" << x.getPriority() << ")" << endl;
  if (x.getActiv_If()) out << "activ_if(" << x.getActiv_If() << ")" << endl;
  else out << "activ_if(NULL)" << endl;
  if (x.getLabels()) out << "labels(" << x.getLabels() << ")" << endl;
  else out << "labels(NULL)" << endl;
  return out;
}

int group::getNameNumber() const {return nameNumber_;}

ostream& operator<<(ostream& out, const activ_if& x) {
  out << "something";
  return out;
}

ostream& operator<<(ostream& out, const labels& x) {
  out << "something";
  return out;
}

groupings::groupings(const DOMElement* d) :
  groupv_(getdlv<group>(d, "group")) {}

const vector<group>& groupings::getGroupV() const {return groupv_;}

puc_spec::puc_spec(const DOMDocument* doc) {
  DOMElement* e = doc->getDocumentElement();
  if (e == NULL)
    throw GadgetInitError("doc is null");
  else
    *this = e;
}

puc_spec::puc_spec(const DOMElement* d) :
  name_(XMLString::transcode(d->getAttribute((XMLString::transcode("name"))))),
  groupings_(getd<groupings>(d, "groupings")) {}

puc_spec::puc_spec(const puc_spec& x) :
  name_(x.name_),
  groupings_(x.groupings_? new groupings(*x.groupings_): NULL) {}

string puc_spec::getName() const {return name_;}

const groupings* puc_spec::getGroupings() const {return groupings_;}

type* puc_spec::resolveType(const state& s) const {
  const type *t = s.getType();
  if (t != NULL) return new type(*t);
  const apply_type* at = s.getApply_Type();
  if (at == NULL) {
    cerr << "type and apply_type are null error" << endl;
    return (type*) NULL;
  }
  string reference = at->getName();
  vector<type> tv = getTypeV();
  for(vector<type>::const_iterator i = tv.begin(); i != tv.end(); i++) {
    string cmp = i->getId();
    if (cmp == reference) 
      return new type(*i);
  }
  cerr << "type resolution error for " << reference << endl;
  return (type*) NULL;
}

vector<command> puc_spec::getCommandV() const {
  vector<command> answer;
  vector<group> gv = getGroupV();
  for(vector<group>::const_iterator i = gv.begin(); i != gv.end(); i++) {
    const vector<command>& sv = i->getCommandV();
    answer.insert(answer.end(), sv.begin(), sv.end());
  }
  return answer;
}

vector<state> puc_spec::getStateV() const {
  vector<state> answer;
  vector<group> gv = getGroupV();
  for(vector<group>::const_iterator i = gv.begin(); i != gv.end(); i++) {
    const vector<state>& sv = i->getStateV();
    answer.insert(answer.end(), sv.begin(), sv.end());
  }
  return answer;
}

vector<group> puc_spec::getGroupV() const {
  vector<group> answer;
  stack<vector<group> > sgv;
  const vector<group>& gv = getGroupings()->getGroupV();
  sgv.push(gv);
  while(!sgv.empty()) {
    const vector<group> gv = sgv.top(); sgv.pop();
    for(vector<group>::const_iterator i = gv.begin(); i != gv.end(); i++) {
      answer.push_back(*i);
      const vector<group>& embeddedgv = i->getGroupV();
      if (embeddedgv.size() > 0)
	sgv.push(embeddedgv);
    }
  }
  return answer;
}  

vector<type> puc_spec::getTypeV() const {
  vector<type> answer;
  vector<state> sv = getStateV();
  for(vector<state>::const_iterator i = sv.begin(); i != sv.end(); i++) {
    const type* t = i->getType();
    if (t != NULL)
      answer.push_back(*t);
  }
  return answer;
}




