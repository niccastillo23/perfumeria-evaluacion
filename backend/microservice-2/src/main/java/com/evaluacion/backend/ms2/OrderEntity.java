package com.evaluacion.backend.ms2;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;

@Entity
@Table(name = "orders", schema = "orders_schema")
public class OrderEntity {

    @Id
    private String orderId;

    @Column(columnDefinition = "text")
    private String itemsJson;

    private Double total;
    private String status;

    public OrderEntity() {}

    public OrderEntity(String orderId, String itemsJson, Double total, String status) {
        this.orderId = orderId;
        this.itemsJson = itemsJson;
        this.total = total;
        this.status = status;
    }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public String getItemsJson() { return itemsJson; }
    public void setItemsJson(String itemsJson) { this.itemsJson = itemsJson; }
    public Double getTotal() { return total; }
    public void setTotal(Double total) { this.total = total; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
