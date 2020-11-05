package com.p2p.utils;

public class RFCDetails {
    int rfcNumber;
    String rfcTitle;
    String peerHostname;
    int peerServerPortNumber;
    int peerPortNum;

    public RFCDetails(int rfcNumber, String rfcTitle, String peerHostname, int peerServerPortNumber, int peerPortNum) {
        this.rfcNumber = rfcNumber;
        this.rfcTitle = rfcTitle;
        this.peerHostname = peerHostname;
        this.peerServerPortNumber = peerServerPortNumber;
        this.peerPortNum = peerPortNum;
    }

    public int getPeerPortNum() {
        return peerPortNum;
    }

    public void setPeerPortNum(int peerPortNum) {
        this.peerPortNum = peerPortNum;
    }

    public int getRfcNumber() {
        return rfcNumber;
    }

    public void setRfcNumber(int rfcNumber) {
        this.rfcNumber = rfcNumber;
    }

    public String getRfcTitle() {
        return rfcTitle;
    }

    public void setRfcTitle(String rfcTitle) {
        this.rfcTitle = rfcTitle;
    }

    public String getPeerHostname() {
        return peerHostname;
    }

    public void setPeerHostname(String peerHostname) {
        this.peerHostname = peerHostname;
    }

    public int getPeerServerPortNumber() {
        return peerServerPortNumber;
    }

    public void setPeerServerPortNumber(int peerServerPortNumber) {
        this.peerServerPortNumber = peerServerPortNumber;
    }
}
