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
    private static final String version = "P2P-CI/1.0";

    public Server() throws IOException {
        this.serverPortNumber = 7734;
        this.rfcs = Collections.synchronizedList(new ArrayList<RFCDetails>());
        this.peers = Collections.synchronizedList(new ArrayList<PeerDetails>());
        this.serverSocket = new ServerSocket(serverPortNumber);
        System.out.println("Centralized Server running at "+ InetAddress.getLocalHost().getHostAddress()+ " on port " + serverSocket.getLocalPort());
        System.out.println("OS: " + System.getProperty("os.name"));
        System.out.println("Version: P2P-CI/1.0");
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
        String hostname;
        int clientPort = 0;
        try {
            socket = serverSocket.accept();
            new Thread(this).start();
            clientPort = socket.getPort();
            System.out.println("Connection Established with client @ port "+ clientPort);
            objectInputStream = new ObjectInputStream (socket.getInputStream());
            objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            hostname = (objectInputStream.readObject()).toString();
            PeerDetails peerDetails = new PeerDetails(hostname, clientPort);
            peers.add(peerDetails);
            System.out.println("Peer Added successfully");
        } catch(Exception e) {
            System.out.println("Connection failure at start " + e);
            if (socket.isConnected())
            {
                removePeer(clientPort, false);
                try {
                    socket.close();
                } catch(IOException ioe){
                    System.out.println("IOException in closing connection" + e);
                }
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
                // TODO: remove peer
                System.out.println("Connection is reset for client "+hostname+" port = "+clientPort+", removing it.");
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
                    addRFCDetails(rfcNumber, rfcTitle, peerHostname, clientPortNumber, objectOutputStream);
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
            serverResponse.append(rfcDetails.getRfcNumber()+" "+rfcDetails.getRfcTitle()+" "+rfcDetails.getPeerHostname()+" "+rfcDetails.getClientPortNumber()+"\r\n");
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
                serverResponse.append("RFC "+rfcDetails.getRfcNumber()+" "+rfcDetails.getRfcTitle()+" "+rfcDetails.getPeerHostname()+" "+rfcDetails.getClientPortNumber());
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

    private void addRFCDetails(int rfcNumber, String rfcTitle, String peerHostname, int clientPortNumber, ObjectOutputStream objectOutputStream) {
        rfcs.add(new RFCDetails(rfcNumber,rfcTitle,peerHostname,clientPortNumber));
        try {
            objectOutputStream.writeObject("RFCDetails added successfully");
        } catch (IOException e) {
            System.out.println("Error in sending update to client.");
            e.printStackTrace();
        }
        System.out.println("RFCDetails added successfully");
    }

    private void removePeer(int clientPort, boolean flag) {
        if(flag){
            RFCDetails listOne = null;
            int index = 0;

            while(index < rfcs.size()) {
                listOne = rfcs.get(index);
                if(clientPort == listOne.getClientPortNumber()) {
                    rfcs.remove(index);
                    index = 0;
                    continue;
                }
                index += 1;
            }
        }
        try {
            ListIterator<PeerDetails> piterator = peers.listIterator();
            PeerDetails peerOne = null;
            while((piterator.hasNext()))
            {
                peerOne = piterator.next();
                if(clientPort == peerOne.getPeerPortNumber()){
                    peers.remove(peerOne);
                }
            }
        } catch (ConcurrentModificationException e) {}
    }

}
