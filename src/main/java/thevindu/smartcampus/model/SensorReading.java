/* Author: Thevindu Sithujaya | 5COSC022W Smart Campus API */
package thevindu.smartcampus.model;

import java.util.UUID;

public class SensorReading {
    private String id;        // UUID
    private long timestamp;   // epoch time in ms
    private double value;     // the measurement recorded

    public SensorReading() {}

    public SensorReading(double value) {
        this.id = UUID.randomUUID().toString();
        this.timestamp = System.currentTimeMillis();
        this.value = value;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public double getValue() { return value; }
    public void setValue(double value) { this.value = value; }
}
