package com.paymentchain.customer.controllers;


import com.fasterxml.jackson.databind.JsonNode;
import com.paymentchain.customer.entities.Customer;
import com.paymentchain.customer.entities.CustomerProduct;
import com.paymentchain.customer.repositories.CustomerRepository;
import io.netty.channel.ChannelOption;
import io.netty.channel.epoll.EpollChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.Entity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.*;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import javax.swing.text.html.Option;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/customer")
@Tag(name="Customer", description = "Crud de customer") //swagger
public class CustomerController {

    @Autowired
    CustomerRepository customerRepository;


    // cliente para comunicarnos con otro microservicio
    private final WebClient.Builder webClientBuilder;

    public CustomerController(WebClient.Builder webClientBuilder){
        this.webClientBuilder =  webClientBuilder;
    }

    HttpClient client = HttpClient.create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS,5000) // tiempo de espera
            .option(ChannelOption.SO_KEEPALIVE,true)            //
            .option(EpollChannelOption.TCP_KEEPIDLE,300)
            .option(EpollChannelOption.TCP_KEEPINTVL,60)
            .responseTimeout(Duration.ofSeconds(1))
            .doOnConnected(connection ->{
                connection.addHandlerLast(new ReadTimeoutHandler(5000 , TimeUnit.MILLISECONDS));
                connection.addHandlerLast(new WriteTimeoutHandler(5000, TimeUnit.MILLISECONDS));

            });

    @Autowired
    private Environment env;
    @GetMapping("/check")
    public String check(){
        return "Hola, estas en el perfil: " + env.getProperty("custom.activeprofileName");
    }


    // crear un customer
    @Operation(summary = "Crear un cliente")
    @PostMapping
    public ResponseEntity<?> post(@RequestBody Customer input) {
        input.getProducts().forEach(x -> x.setCustomer(input));
        Customer save = customerRepository.save(input);
        return ResponseEntity.ok(save);
    }

    // seleccionar todos customer
    @Operation(summary = "Lista de clientes") //swagger
    @GetMapping()
    public List<Customer> list() {
        return customerRepository.findAll();
    }

    // seleccionar customer por id
    @Operation(summary = "Cliente por ID") //swagger
    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable(name = "id") long id) {
        Optional<Customer> customer = customerRepository.findById(id);
        if(!customer.isPresent()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Cliente no encontrado");
        }
        return ResponseEntity.ok(customer);
    }

    // modificar customer por id
    @Operation(summary = "Modificar cliente por ID") //swagger
    @PutMapping("/{id}")
    public ResponseEntity<?> put(@PathVariable(name = "id") long id, @RequestBody Customer input) {
        Customer find = customerRepository.findById(id).get();
        if (find != null) {
            find.setCode(input.getCode());
            find.setName(input.getName());
            find.setIban(input.getIban());
            find.setPhone(input.getPhone());
            find.setSurname(input.getSurname());
        }
        Customer save = customerRepository.save(find);
        return ResponseEntity.ok(save);
    }
    

    // eliminar customer por id
    @Operation(summary = "Eliminar cliente por ID") //swagger
    @DeleteMapping("{id}")
    public ResponseEntity<?> eliminarPorId(@PathVariable ("id") Long id){
        Optional<Customer> customer = customerRepository.findById(id);
        if (!customer.isPresent()){
            return  ResponseEntity.status(HttpStatus.NOT_FOUND).body("Cliente no encontrado");
        }
        customerRepository.deleteById(id);
    return ResponseEntity.ok().body("Cliente eliminado con exito");
}

    //metodo que devuelve NOMBRE de un producto pasado por id
    private String getProductName(long id) {
        WebClient build = webClientBuilder.clientConnector(new ReactorClientHttpConnector(client))
                .baseUrl("http://localhost:8082/product")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultUriVariables(Collections.singletonMap("url", "http://localhost:8082/product"))
                .build();
        JsonNode block = build.method(HttpMethod.GET).uri("/" + id)
                .retrieve().bodyToMono(JsonNode.class).block();
        String name = block.get("name").asText();
        return name;
    }


    //Busca cliente por codigo  que a la vez trae el listado de productos con su nombres, utilizando
    // la funcion getProductName()
    @GetMapping("/full")
    public Customer getByCode(@RequestParam(name = "code") String code) {
        Customer customer = customerRepository.findByCode(code);
        if (customer != null) {
            List<CustomerProduct> products = customer.getProducts();

            //for each product find it name
            products.forEach(x -> {
                String productName = getProductName(x.getProductId());
                x.setProductName(productName);
            });
            //find all transactions that belong this account number
            List<?> transactions = getTransactions(customer.getIban());
            customer.setTransactions(transactions);

        }
        return customer;
    }


    private List<?> getTransactions(String iban) {
        WebClient build = webClientBuilder.clientConnector(new ReactorClientHttpConnector(client))
                .baseUrl("http://localhost:8083/transaction")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();

        Optional<List<?>> transactionsOptional = Optional.ofNullable(build.method(HttpMethod.GET)
                .uri(uriBuilder -> uriBuilder
                        .path("/customer/transactions")
                        .queryParam("ibanAccount", iban)
                        .build())
                .retrieve()
                .bodyToFlux(Object.class)
                .collectList()
                .block());

        return transactionsOptional.orElse(Collections.emptyList());
    }


}
