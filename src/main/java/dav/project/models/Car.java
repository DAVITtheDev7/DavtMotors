package dav.project.models;

public class Car {
    private String id;
    private String brand;
    private String model;
    private int year;
    private String color;
    private double mileage;
    private double price;
    private String imageUrl;

    public Car() {}

    public Car(String id, String brand, String model, int year, String color, double mileage, double price, String imageUrl, String status) {
        this.id = id;
        this.brand = brand;
        this.model = model;
        this.year = year;
        this.color = color;
        this.mileage = mileage;
        this.price = price;
        this.imageUrl = imageUrl;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getBrand() { return brand; }
    public String getModel() { return model; }
    public int getYear() { return year; }
    public String getColor() { return color; }
    public double getMileage() { return mileage; }
    public double getPrice() { return price; }
    public String getImageUrl() { return imageUrl; }

}