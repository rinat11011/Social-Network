# Social-Network

The project implement a simple social network server and client. 
The communication between the server and the client(s) is made by using a binary communication protocol. 

A registered user will be able to follow other users and post messages. 

The implementation of the server is based on the Thread-Per-Client (TPC) and Reactor servers. 
The servers, only support pull notifications. 
Any time the server receives a message from a client it can replay back to the client itself. 

But what if we want to send messages between clients, or broadcast an announcment to a group of clients? 
We would like the server to send those messages directly to the client without reciveing a request to do so, e.g. push notifications. 

Users need to register to the service. Once registered, they will be able to post messages and follow other users. It is a binary protocol that uses pre-defined message length for different commands. 

There are two types of commands, Server-to-Client and Client-to-Server. The commands begin with 2 bytes to describe the opcode. The rest of the message will be defined specifically for each command.

Unlike real social network you do not work with real databases. The data (Users, Passwords, Messages, ect...) is saved in memory, and the information is only saved from the
time the server starts and until the server closes.
