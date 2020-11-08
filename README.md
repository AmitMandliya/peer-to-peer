# peer-to-peer
peer-to-peer




## instructions to run the project:
The code has been tested on Windows 10 with Java 8 environment.
1. Run the Server by following below steps: <br>
  a. cd src <br>
  b. javac com/p2p/server/Server.java <br>
  c. java com/p2p/server/Server <br>
  
2. Once the server is up and running, run the Peer systems (can be more than one) by following below steps: <br>
  a. cd src <br>
  b. javac com/p2p/client/Peer.java <br>
  c. java com/p2p/client/Peer <Server IP Address> 7734 <br>
  
Note: When you run the Server, you will get system details in below format: <br>
```
Initializing Server...
Server up at 192.168.0.14, Port = 7734
Version = P2P-CI/1.0, OS = Windows 10
```
Use the IP address (Here, 192.168.0.14) to start the Peer.
