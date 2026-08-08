package com.networkattack.demo.Model;



import jakarta.persistence.*;

@Entity
public class PreprocessedNetworkAttack {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String no;
    private String time;
    private String source;
    private String destination;
    private String protocol;
    private String length;
    private String info;

    // New fields
    private boolean isMalicious; // Indicates if the packet is malicious
    private String packetSizeCategory; // Categorizes packet size (Small, Medium, Large)
    private double packetSentRate; // Packet sent rate (e.g., packets per second)

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNo() {
        return no;
    }

    public void setNo(String no) {
        this.no = no;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getLength() {
        return length;
    }

    public void setLength(String length) {
        this.length = length;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public boolean isMalicious() {
        return isMalicious;
    }

    public void setMalicious(boolean malicious) {
        isMalicious = malicious;
    }

    public String getPacketSizeCategory() {
        return packetSizeCategory;
    }

    public void setPacketSizeCategory(String packetSizeCategory) {
        this.packetSizeCategory = packetSizeCategory;
    }

    public double getPacketSentRate() {
        return packetSentRate;
    }

    public void setPacketSentRate(double packetSentRate) {
        this.packetSentRate = packetSentRate;
    }
}