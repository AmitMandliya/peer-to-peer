package com.p2p.server;

public class PeerDetails {
    // the hostname of the peer (of type string)
    String peerHostname;
    // the port number (of type integer) to which the upload server of this peer is listening.
    int peerPortNumber;

    public String getPeerHostname() {
        return peerHostname;
    }

    public void setPeerHostname(String peerHostname) {
        this.peerHostname = peerHostname;
    }

    public int getPeerPortNumber() {
        return peerPortNumber;
    }

    public void setPeerPortNumber(int peerPortNumber) {
        this.peerPortNumber = peerPortNumber;
    }

    public PeerDetails(String peerHostname, int peerPortNumber) {
        this.peerHostname = peerHostname;
        this.peerPortNumber = peerPortNumber;
    }
}
