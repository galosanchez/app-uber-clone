package com.galosanchez.appuberclone.domain;

public class ClientBooking {
    private String idClient;
    private String idDriver;
    private String origin;
    private String destination;
    private String time;
    private String distance;
    private String status;
    private double originLat;
    private double originLng;
    private double destinationLng;
    private double destinationLat;

    public ClientBooking(String idClient, String idDriver, String origin, String destination, String time, String distance, String status, double originLat, double originLng, double destinationLng, double destinationLat) {
        this.idClient = idClient;
        this.idDriver = idDriver;
        this.origin = origin;
        this.destination = destination;
        this.time = time;
        this.distance = distance;
        this.status = status;
        this.originLat = originLat;
        this.originLng = originLng;
        this.destinationLng = destinationLng;
        this.destinationLat = destinationLat;
    }

    public String getIdClient() {
        return idClient;
    }

    public void setIdClient(String idClient) {
        this.idClient = idClient;
    }

    public String getIdDriver() {
        return idDriver;
    }

    public void setIdDriver(String idDriver) {
        this.idDriver = idDriver;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getOriginLat() {
        return originLat;
    }

    public void setOriginLat(double originLat) {
        this.originLat = originLat;
    }

    public double getOriginLng() {
        return originLng;
    }

    public void setOriginLng(double originLng) {
        this.originLng = originLng;
    }

    public double getDestinationLng() {
        return destinationLng;
    }

    public void setDestinationLng(double destinationLng) {
        this.destinationLng = destinationLng;
    }

    public double getDestinationLat() {
        return destinationLat;
    }

    public void setDestinationLat(double destinationLat) {
        this.destinationLat = destinationLat;
    }
}
