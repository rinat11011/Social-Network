CFLAGS:=-c -Wall -Weffc++ -g -std=c++11 -Iinclude -Iboost
LDFLAGS:=-lpthread -lboost_thread -lboost_system -lz

all: EchoClient
	g++ -o bin/echoExample bin/connectionHandler.o bin/echoClient.o bin/input.o bin/output.o $(LDFLAGS)

EchoClient: bin/connectionHandler.o bin/echoClient.o bin/input.o bin/output.o
	
bin/connectionHandler.o: src/connectionHandler.cpp
	g++ $(CFLAGS) -o bin/connectionHandler.o src/connectionHandler.cpp

bin/echoClient.o: src/echoClient.cpp
	g++ $(CFLAGS) -o bin/echoClient.o src/echoClient.cpp


bin/input.o: src/input.cpp
	g++ $(CFLAGS) -o bin/input.o src/input.cpp

bin/output.o: src/output.cpp
	g++ $(CFLAGS) -o bin/output.o src/output.cpp

clean:
	rm -f bin/*
