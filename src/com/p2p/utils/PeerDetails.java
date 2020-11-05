package com.p2p.utils;

public class PeerDetails {
    String peerHostname;
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
