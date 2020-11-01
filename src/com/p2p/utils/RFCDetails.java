package com.p2p.utils;

public class RFCDetails {
    int rfcNumber;
    String rfcTitle;
    String peerHostname;
    int clientPortNumber;

    public RFCDetails(int rfcNumber, String rfcTitle, String peerHostname, int clientPortNumber) {
        this.rfcNumber = rfcNumber;
        this.rfcTitle = rfcTitle;
        this.peerHostname = peerHostname;
        this.clientPortNumber = clientPortNumber;
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

    public int getClientPortNumber() {
        return clientPortNumber;
    }

    public void setClientPortNumber(int clientPortNumber) {
        this.clientPortNumber = clientPortNumber;
    }
}
