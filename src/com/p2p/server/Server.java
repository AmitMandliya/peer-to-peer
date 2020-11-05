package com.p2p.server;

import com.p2p.utils.PeerDetails;
import com.p2p.utils.RFCDetails;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Server implements Runnable{
    public static List<RFCDetails> rfcs;
    public static List<PeerDetails> peers;
    public int serverPortNumber;
    public ServerSocket serverSocket;
    private static String version = "";

    public Server() throws IOException {
        this.serverPortNumber = 7734;
        version = "P2P-CI/1.0";
        rfcs = Collections.synchronizedList(new ArrayList<RFCDetails>());
        peers = Collections.synchronizedList(new ArrayList<PeerDetails>());
        System.out.println("Initializing Server...");
        this.serverSocket = new ServerSocket(serverPortNumber);
        System.out.println("Server up at "+ InetAddress.getLocalHost().getHostAddress()+ ", Port = " + serverSocket.getLocalPort());
        System.out.println("Version = P2P-CI/1.0, OS = " + System.getProperty("os.name"));
        System.out.println();
        new Thread(this).start();
    }

    public static void main(String[] args) throws IOException {
            Server server = new Server();
    }

    @Override
    public void run() {
        Socket socket = null;
        ObjectInputStream objectInputStream;
        ObjectOutputStream objectOutputStream;
        String clientHostname;
        int clientPort = 0;
        try {
            socket = serverSocket.accept();
            new Thread(this).start();
            clientPort = socket.getPort();
            objectInputStream = new ObjectInputStream (socket.getInputStream());
            objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            clientHostname = (objectInputStream.readObject()).toString();
            PeerDetails peerDetails = new PeerDetails(clientHostname, clientPort);
            peers.add(peerDetails);
            System.out.println("Peer has joined with system with Hostname = "+clientHostname+", Port = "+ clientPort);
        } catch(Exception e) {
            System.out.println("Exception occurred during connection with Peer.");
            try {
                if (socket.isConnected()) {
                    int finalClientPort = clientPort;
                    peers.removeIf(peer -> peer.getPeerPortNumber() == finalClientPort);
                    rfcs.removeIf(rfc -> rfc.getPeerPortNum() == finalClientPort);
                    try {
                        socket.close();
                    } catch (IOException ioe) {
                        System.out.println("IOException in closing connection" + ioe);
                    }
                }
            } catch (NullPointerException e1) {
                System.out.println("Socket is not available");
            }
            return;
        }
        while(true) {
            int rfcNumber = 0;
            int clientPortNumber;
            String rfcTitle = null;
            String command, peerHostname;
            String[] requestFromClientArray;
            String requestFromClient = null;
            try {
                requestFromClient = (String) objectInputStream.readObject();
            } catch (Exception e) {
                int finalClientPort = clientPort;
                peers.removeIf(peer -> peer.getPeerPortNumber() == finalClientPort);
                rfcs.removeIf(rfc -> rfc.getPeerPortNum() == finalClientPort);
                System.out.println("Connection is reset for client "+clientHostname+" port = "+clientPort+", removing it.");
                break;
            }
            System.out.println("Server is processing below request:");
            System.out.println(requestFromClient);
            requestFromClientArray = requestFromClient.split("\r\n");
            command = requestFromClientArray[0].split(" ")[0];
            peerHostname = requestFromClientArray[1].split(":")[1].trim();
            clientPortNumber = Integer.parseInt(requestFromClientArray[2].split(":")[1].trim());
            if(command.equals("ADD") || command.equals("LOOKUP")) {
                rfcTitle = requestFromClientArray[3].split(":")[1].trim();
                rfcNumber = Integer.parseInt(requestFromClientArray[0].split("RFC")[1].split(" ")[1].trim());
            }
            switch (command) {
                case "ADD":
                    int clientPortNum = 0;
                    try {
                        clientPortNum = (int) objectInputStream.readObject();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                    addRFCDetails(rfcNumber, rfcTitle, peerHostname, clientPortNumber, clientPortNum, objectOutputStream);
                    break;
                case "LIST":
                    listAllRFCDetails(objectOutputStream);
                    break;
                case "LOOKUP":
                    lookupRFCDetails(rfcNumber, rfcTitle, peerHostname, clientPortNumber, objectOutputStream);
                    break;
                default:
                    break;
            }
        }
    }

    private void listAllRFCDetails(ObjectOutputStream objectOutputStream) {
        StringBuilder serverResponse = new StringBuilder();
        serverResponse.append(version+" 200 OK\r\n");
        for(RFCDetails rfcDetails: rfcs) {
            serverResponse.append(rfcDetails.getRfcNumber()+" "+rfcDetails.getRfcTitle()+" "+rfcDetails.getPeerHostname()+" "+rfcDetails.getPeerServerPortNumber()+"\r\n");
        }
        serverResponse.append("\r\n");
        try {
            objectOutputStream.writeObject(serverResponse.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void lookupRFCDetails(int rfcNumber, String rfcTitle, String peerHostname, int clientPortNumber, ObjectOutputStream objectOutputStream) {
        boolean isRFCAvailable = false;
        StringBuilder serverResponse = new StringBuilder();
        for(RFCDetails rfcDetails: rfcs) {
            if(rfcDetails.getRfcNumber() == rfcNumber) {
                if(!isRFCAvailable) {
                    isRFCAvailable = true;
                    serverResponse.append(version+" 200 OK"+"\r\n");
                }
                serverResponse.append("RFC "+rfcDetails.getRfcNumber()+" "+rfcDetails.getRfcTitle()+" "+rfcDetails.getPeerHostname()+" "+rfcDetails.getPeerServerPortNumber()+"\r\n");
            }
        }
        if(!isRFCAvailable) {
            serverResponse.append("404 Not Found");
        }
        try {
            objectOutputStream.writeObject(serverResponse.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addRFCDetails(int rfcNumber, String rfcTitle, String peerHostname, int clientPortNumber, int peerPortNum, ObjectOutputStream objectOutputStream) {
        rfcs.add(new RFCDetails(rfcNumber,rfcTitle,peerHostname,clientPortNumber, peerPortNum));
        try {
            objectOutputStream.writeObject("RFCDetails added successfully");
        } catch (IOException e) {
            int finalClientPort = clientPortNumber;
            peers.removeIf(peer -> peer.getPeerPortNumber() == finalClientPort);
            rfcs.removeIf(rfc -> rfc.getPeerServerPortNumber() == finalClientPort);
            System.out.println("Error in sending update to client.");
            e.printStackTrace();
        }
        System.out.println("RFCDetails added successfully");
    }
}
