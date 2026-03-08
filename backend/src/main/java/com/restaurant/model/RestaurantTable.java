package com.restaurant.model;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "restaurant_table")
public class RestaurantTable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private int capacity;
    private String zone;
    private double posX;
    private double posY;
    private double width;
    private double height;
    private String shape;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "table_features", joinColumns = @JoinColumn(name = "table_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "feature")
    private Set<TableFeature> features = new HashSet<>();

    public RestaurantTable() {
    }

    public RestaurantTable(Long id, String name, int capacity, String zone,
                           double posX, double posY, double width, double height,
                           String shape, Set<TableFeature> features) {
        this.id = id;
        this.name = name;
        this.capacity = capacity;
        this.zone = zone;
        this.posX = posX;
        this.posY = posY;
        this.width = width;
        this.height = height;
        this.shape = shape;
        this.features = features;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    public double getPosX() {
        return posX;
    }

    public void setPosX(double posX) {
        this.posX = posX;
    }

    public double getPosY() {
        return posY;
    }

    public void setPosY(double posY) {
        this.posY = posY;
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public String getShape() {
        return shape;
    }

    public void setShape(String shape) {
        this.shape = shape;
    }

    public Set<TableFeature> getFeatures() {
        return features;
    }

    public void setFeatures(Set<TableFeature> features) {
        this.features = features;
    }
}
