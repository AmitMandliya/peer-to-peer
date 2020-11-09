package com.p2p.client;

import com.p2p.utils.RFCDetails;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.IntStream;

public class Peer implements Runnable{
    public static List<RFCDetails> rfcs = Collections.synchronizedList(new ArrayList());
    public ServerSocket peerServerSocket;
    private static Socket clientSocket;
    private static ObjectOutputStream objectOutputStream;
    private static ObjectInputStream objectInputStream;
    private static String serverAddress,hostName;
    private static int clientPort, peerServerPortNumber, serverPortNumber ;
    private static String localResourcesPath = "resources/localRFCs";
    private static final String version = "P2P-CI/1.0";

    public Peer(int peerPort) {
        try {
            peerServerSocket = new ServerSocket(peerPort);
            System.out.println("Starting peer on Port Number = " + peerServerSocket.getLocalPort() + ", Host Address =  " + InetAddress.getLocalHost().getHostAddress());
            new Thread(this).start();
        }
        catch (IOException e) {
            System.out.println("IOException while trying to create a Socket at Peer.");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        if(args.length != 2) {
            System.out.println("Wrong number of arguments provided.");
            System.exit(1);
        }
        try {
            serverPortNumber = Integer.parseInt(args[1]);
            serverAddress = args[0];
            startPeerServer(serverAddress);
            connectToServer(serverAddress, serverPortNumber, hostName);
            String command = "";
            String rfc, rfcName, rfcTitle;
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
            while(!command.equals("QUIT")) {
                System.out.println("Select from: ADD, LOOKUP, LIST, GET, QUIT");
                command = bufferedReader.readLine();
                switch (command) {
                    case "ADD":
                        System.out.println("Enter the RFC Name and RFC Title to be added (separated by space)");
                        rfc = bufferedReader.readLine();
                        if(rfc.split(" ").length != 2) {
                            System.out.println("Please enter correct details.");
                            continue;
                        };
                        rfcName = rfc.split(" ")[0];
                        rfcTitle = rfc.split(" ")[1];
                        addRFCDetailsToServer(rfcName, rfcTitle, hostName, objectOutputStream, objectInputStream, peerServerPortNumber);
                        break;
                    case "LOOKUP":
                        System.out.println("Enter the RFC Name and RFC Title to be looked up (separated by space)");
                        rfc = bufferedReader.readLine();
                        if(rfc.split(" ").length != 2) {
                            System.out.println("Please enter correct details.");
                            continue;
                        };
                        rfcName = rfc.split(" ")[0];
                        rfcTitle = rfc.split(" ")[1];
                        lookupRFCDetailsFromServer(rfcName, rfcTitle, hostName, objectOutputStream, objectInputStream, peerServerPortNumber);
                        break;
                    case "LIST":
                        getAllRFCDetailsFromServer(hostName, peerServerPortNumber, objectOutputStream, objectInputStream);
                        break;
                    case "GET":
                        System.out.println("Enter the RFC Name to be downloaded and Hostname and Port number of the host where this RFC is present (separated by space)");
                        rfc = bufferedReader.readLine();
                        if(rfc.split(" ").length != 3) {
                            System.out.println("Please enter correct details.");
                            continue;
                        }
                        rfcName = rfc.split(" ")[0];
                        String peerhostName = rfc.split(" ")[1];
                        try {
                            int peerPortNumberForRFC = Integer.parseInt(rfc.split(" ")[2]);
                            obtainRFCFromPeer(rfcName, peerhostName, peerPortNumberForRFC);
                        } catch (NumberFormatException e) {
                            System.out.println("Please enter correct details.");
                            continue;
                        }
                        break;
                    case "QUIT":
                        System.out.println("Quitting.");
                        System.exit(1);
                        break;
                    default:
                        System.out.println("Error: 400 Bad Request");
                        System.out.println("Enter the right command.");
                        break;
                }
            }
        } catch (IOException e) {
            System.err.print("IOException");
            e.printStackTrace();
        }
    }

    private static void obtainRFCFromPeer(String rfcName, String peerhostName, int peerPortNumberForRFC) {
        Socket socketForRFC = null;
        try {
            socketForRFC = new Socket(peerhostName, peerPortNumberForRFC);
            ObjectInputStream objectInputStreamPeerClient = new ObjectInputStream(socketForRFC.getInputStream());
            ObjectOutputStream objectOutputStreamPeerClient = new ObjectOutputStream(socketForRFC.getOutputStream());
            String requestQuery = "GET RFC "+rfcName+" "+version+"\r\n"+
                    "Host: "+peerhostName+"\r\n"+
                    "OS: "+System.getProperty("os.name");
            objectOutputStreamPeerClient.writeObject(requestQuery);
            String peerResponse = (String) objectInputStreamPeerClient.readObject();
            String[] peerResponseArray = peerResponse.split("\r\n");
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < peerResponseArray.length-1; i++) {
                builder.append(peerResponseArray[i]);
                builder.append("\r\n");
            }
            System.out.println(builder.toString());
            String peerVersion = peerResponseArray[0].split(" ")[0];
            int peerStatus = Integer.parseInt(peerResponseArray[0].split(" ")[1].trim());
            String peerPhrase = peerResponseArray[0].split(" ")[2];
            String Date = peerResponseArray[1].split(":")[1];
            String peerOS = peerResponseArray[2].split(":")[1];
            String lastModified = peerResponseArray[3].split(":")[1];
            String contentLength = peerResponseArray[4].split(":")[1];
            String contentType = peerResponseArray[5].split(":")[1];
            String data = peerResponseArray[6];
            if(!peerVersion.equals(version)) {
                System.out.println("Error: 505 P2P-CI Version Not Supported");
                return;
            }
            if(peerStatus == 200) {
                File folder = new File(localResourcesPath);
                File downloadedRFC = new File(folder.getCanonicalPath()+"\\rfc"+rfcName+".txt");
                downloadedRFC.createNewFile();
                FileWriter fileWriter = new FileWriter(downloadedRFC.getAbsolutePath());
                fileWriter.write(data);
                fileWriter.close();
                System.out.println("RFC has been downloaded. Date = "+Date+", peerOS = "+peerOS+", lastModified = "+lastModified
                        +", contentLength = "+contentLength+", contentType = "+contentType);
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error while connecting with peer.");
            e.printStackTrace();
        }

    }

    private static void getAllRFCDetailsFromServer(String hostName, int peerServerPortNumber, ObjectOutputStream objectOutputStream, ObjectInputStream objectInputStream) {
        String requestToServer =
                "LIST ALL RFC "+version+"\r\n"+
                        "Host: "+hostName+"\r\n"+
                        "Port: "+ peerServerPortNumber +"\r\n";
        try {
            objectOutputStream.writeObject(requestToServer);
            String serverResponse = (String) objectInputStream.readObject();
            System.out.println(serverResponse);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

    private static void lookupRFCDetailsFromServer(String rfcName, String rfcTitle, String hostName, ObjectOutputStream objectOutputStream, ObjectInputStream objectInputStream, int peerServerPortNumber) {
        String requestToServer =
                "LOOKUP RFC "+rfcName+" "+version+"\r\n"+
                        "Host: "+hostName+"\r\n"+
                        "Port: "+ peerServerPortNumber +"\r\n"+
                        "Title: "+rfcTitle+"\r\n";

        try {
            objectOutputStream.writeObject(requestToServer);
            String serverResponse = (String)objectInputStream.readObject();
            System.out.println(serverResponse);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static void addRFCDetailsToServer(String rfcName, String rfcTitle, String hostName, ObjectOutputStream objectOutputStream, ObjectInputStream objectInputStream, int peerServerPortNumber) {
        try {
            File folder = new File(localResourcesPath);
            File localRFC = new File(folder.getCanonicalPath()+"\\"+"rfc"+rfcName+".txt");
            if(!localRFC.exists()) {
                System.out.println(localRFC.getAbsolutePath()+" does not exists. Cannot add RFC details to Server.");
                System.out.println("Error: 404 Not Found");
            } else {
                String requestToServer =
                        "ADD RFC "+rfcName+" "+version+"\r\n"+
                        "Host: "+hostName+"\r\n"+
                        "Port: "+peerServerPortNumber+"\r\n"+
                        "Title: "+rfcTitle+"\r\n";
                objectOutputStream.writeObject(requestToServer);
                objectOutputStream.writeObject(clientPort);
                String serverResponse = (String) objectInputStream.readObject();
                System.out.println(serverResponse);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static void connectToServer(String serverAddress, int serverPortNumber, String hostName) throws IOException {
        clientSocket = new Socket(serverAddress, serverPortNumber);
        objectOutputStream = new ObjectOutputStream(clientSocket.getOutputStream());
        objectInputStream = new ObjectInputStream(clientSocket.getInputStream());
        objectOutputStream.writeObject(hostName);
        clientPort = clientSocket.getLocalPort();
        System.out.println("Client started at hostName = "+hostName+" ,port = "+clientPort);

    }

    private static void startPeerServer(String serverAddress) throws UnknownHostException {
        hostName = InetAddress.getByName(serverAddress).getHostName();
        Random randomPeerPort = new Random();
        peerServerPortNumber = randomPeerPort.nextInt(6000) + 7000;
        new Peer(peerServerPortNumber);
    }

    @Override
    public void run() {
        Socket socket = null;
        ObjectOutputStream objectOutputStreamPeer = null;
        ObjectInputStream objectInputStreamPeer = null;
        try {
            socket = peerServerSocket.accept();
            new Thread(this).start();
        } catch (Exception e) {
            System.out.println("Failed during set-up connection.");
            if(socket.isConnected()) {
                try {
                    socket.close();
                } catch (Exception e1) {
                    System.out.println("Error while closing connection.");
                }
            }
            return;
        }
        try {
            objectOutputStreamPeer = new ObjectOutputStream(socket.getOutputStream());
            objectInputStreamPeer = new ObjectInputStream(socket.getInputStream());
            String peerRequest = (String) objectInputStreamPeer.readObject();
            System.out.println(peerRequest);
            String[] peerRequestArray = peerRequest.split("\r\n");
            String method = peerRequestArray[0].split(" ")[0];
            String peerHostname = peerRequestArray[1].split(":")[1];
            String peerOS = peerRequestArray[2].split(":")[1];
            int rfcNumber = Integer.parseInt(peerRequestArray[0].split("RFC")[1].split(" ")[1].trim());
            if(method.equals("GET")) {
                sendRFC(rfcNumber, objectOutputStreamPeer,objectInputStreamPeer);
            }
        } catch (Exception e) {
            System.out.println("Sending RFC has been failed.");
        } finally {
            try {
                objectOutputStreamPeer.close();
                objectInputStreamPeer.close();
                socket.close();
            } catch (IOException e) {
                System.out.println("Error while closing socket.");
            }
        }
    }

    private void sendRFC(int rfcNumber, ObjectOutputStream objectOutputStreamPeer, ObjectInputStream objectInputStreamPeer) {
        File folder = new File(localResourcesPath);
        StringBuilder serverResponse = new StringBuilder();
        try {
            File rfcFile = new File(folder.getCanonicalPath()+"\\rfc"+rfcNumber+".txt");
            if(!rfcFile.exists()) {
                serverResponse.append(version+" Error: 404 Not Found\r\n");
                System.out.println("File does not exist.");
            } else {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss");
                serverResponse.append(version+" 200 OK\r\n");
                serverResponse.append("Date: "+simpleDateFormat.format(new Date())+" GMT\r\n");
                serverResponse.append("OS: "+System.getProperty("os.name")+"\r\n");
                serverResponse.append("Last Modified: "+simpleDateFormat.format(rfcFile.lastModified())+"\r\n");
                serverResponse.append("Content-Length: "+rfcFile.length()+"\r\n");
                serverResponse.append("Content-Type: text/text"+"\r\n");
                serverResponse.append(new String(Files.readAllBytes(rfcFile.toPath()))+"\r\n");
            }
            objectOutputStreamPeer.writeObject(serverResponse.toString());
        } catch (IOException e) {
            System.out.println("File does not exist.");
            e.printStackTrace();
        }
    }
}
