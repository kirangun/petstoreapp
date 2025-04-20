package com.chtrembl.petstoreapp.service;

import com.chtrembl.petstoreapp.model.ContainerEnvironment;
import com.chtrembl.petstoreapp.model.Order;
import com.chtrembl.petstoreapp.model.User;
import com.chtrembl.petstoreapp.model.WebRequest;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.util.function.Consumer;

@Service
public class ItemReserveServiceImpl implements ItemReserveService {

    private static final Logger logger = LoggerFactory.getLogger(ItemReserveServiceImpl.class);

    private final User sessionUser;
    private final ContainerEnvironment containerEnvironment;
    private final WebRequest webRequest;

    private WebClient itemReserverWebClient = null;

    public ItemReserveServiceImpl(User sessionUser, ContainerEnvironment containerEnvironment, WebRequest webRequest) {
        this.sessionUser = sessionUser;
        this.containerEnvironment = containerEnvironment;
        this.webRequest = webRequest;
    }

    @PostConstruct
    public void initialize() {
        this.itemReserverWebClient = WebClient.builder()
                .baseUrl(this.containerEnvironment.getPetStoreOrderSaveUrl())
                .build();
    }
    @Override
    public void saveOrderDataInBlob(Order order) throws JsonProcessingException {
        String orderJSON = new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                .configure(SerializationFeature.FAIL_ON_SELF_REFERENCES, false).writeValueAsString(order);
        logger.info("order json before sending to function {}", orderJSON);

        Consumer<HttpHeaders> consumer = it -> it.addAll(this.webRequest.getHeaders());
         this.itemReserverWebClient.post().uri("api/processOrder")
                .body(BodyInserters.fromPublisher(Mono.just(orderJSON), String.class))
                .accept(MediaType.APPLICATION_JSON)
                .headers(consumer)
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .header("Cache-Control", "no-cache")
                .retrieve()
                .bodyToMono(Order.class).block();
    }
}
