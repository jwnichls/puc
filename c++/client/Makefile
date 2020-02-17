#This is the Makefile for compiling the c++ client library for the PUC.
#See Thomas Harris <tkharris@cs.cmu.edu> for details

#You'll need to change this to wherever you keep xerces
#XERCESDIR=../../../xerces-c-src1_7_0

#INCLUDEDIRS= -I$(XERCESDIR)/include -Iinclude
INCLUDEDIRS= -I/usr0/local/include -Iinclude
CLIENTOBJS=usiacspec.o pucspec.o DOMTreeErrorReporter.o DOMPrint.o message.o domutils.o DOMPrintErrorHandler.o

all: PUCclient.a

%.o: %.cpp
	g++ -c $(DEBUG) $(INCLUDEDIRS) $< -o $@

PUCclient.a: $(CLIENTOBJS)
	ld -r -o $@ $(CLIENTOBJS)

clean:
	rm -f $(CLIENTOBJS) PUCclient.a
