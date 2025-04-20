package com.chtrembl.petstoreapp.service;

import com.chtrembl.petstoreapp.model.Order;
import com.fasterxml.jackson.core.JsonProcessingException;

public interface ItemReserveService {
    void saveOrderDataInBlob(Order order) throws JsonProcessingException;
}
