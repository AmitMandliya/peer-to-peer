# Peer-to-Peer File sharing system
A console based Peer to Peer (P2P) file sharing system with a centralized index (CI). Peers can join the system and share files (RFC text files) directly with each other. The centralized server is responsible for keeping a track what files each Peer and files it has.

## Instructions to run the project:
The code has been tested on Windows 10 with Java 8 environment.
1. Run the Server by following below steps: <br>
```
  cd src 
  javac com/p2p/server/Server.java 
  java com/p2p/server/Server 
```
  
2. Once the server is up and running, run the Peer systems (can be more than one) by following below steps: <br>
```
  cd src 
  javac com/p2p/client/Peer.java 
  java com/p2p/client/Peer <Server IP Address> 7734 
```
Ignore the warnings that may come during the compilation step.

Notes: 
1. When you run the Server, you will get system details in below format: <br>
```
Initializing Server...
Server up at 192.168.0.14, Port = 7734
Version = P2P-CI/1.0, OS = Windows 10
```
Use the IP address (Here, 192.168.0.14) to start the Peer.

2. This project does not allow spaces to be entered in the Title of the RFC.

3. Peer can choose from one of the following option: <br>

  * ADD: This adds the rfc file details to the List maintained by the Server. Simply type "ADD" and Enter. Post that, Type the name of the RFC (i.e. number of RFC, for example 4) it has in the src/resources/localRFCs folder. If the RFC is present in that folder (RFC name should be like rfc4.txt, 4 represents the number you had put earlier), then Server will add the details. If the RFC is not present at Peer end, then 400 Bad Request will be raised and printed on console. <br>

  * LIST: This will list all the RFC details and the peer details which has those files. (This list is maintained by Centralized Server). <br>

  * LOOKUP: If a Peer wants to look up a particular RFC it is interested in, then it can give the name of the RFC (i.e. the number, for example 4). A list will be returned by the server which contains details of all the Peers which has this file and have ADDed its details into the system. <br>

  * GET: If a Peer wants to GET a RFC from other Peers, it can simply raise a GET request and give the hostname and port number of the Peer it want to get the RFC from. Note that, these details can be obtained from the Lookup operation. <br>

  * QUIT: A peer can Quit the system. When, it quits, the Server will remove all the RFC details from the List maintained by it. <br>

