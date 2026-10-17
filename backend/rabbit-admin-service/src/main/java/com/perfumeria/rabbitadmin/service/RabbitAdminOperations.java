package com.perfumeria.rabbitadmin.service;

import com.perfumeria.rabbitadmin.api.BindingRequest;
import com.perfumeria.rabbitadmin.api.ExchangeRequest;
import com.perfumeria.rabbitadmin.api.QueueRequest;

public interface RabbitAdminOperations {
    void createQueue(QueueRequest request);
    void deleteQueue(String name);
    void createExchange(ExchangeRequest request);
    void deleteExchange(String name);
    void createBinding(BindingRequest request);
    void deleteBinding(BindingRequest request);
}
