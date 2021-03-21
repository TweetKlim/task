package com.client;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
        String REQUEST1 = "{\"getCity\":\"New York\"}";
        Client client1 = new Client(REQUEST1);
        String REQUEST2 = "{\"getCity\":\"London\"}";
        Client client2 = new Client(REQUEST1);
    }
}
