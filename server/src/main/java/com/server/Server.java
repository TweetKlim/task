package com.server;

import com.rabbitmq.client.*;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Server {
    private final static String QUEUE_NAME = "request";
    public static void main(String[] args) throws Exception {




        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");

        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);
            channel.queuePurge(QUEUE_NAME);

            channel.basicQos(1);

            System.out.println(" [x] Awaiting RPC requests");

            Object monitor = new Object();
            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                AMQP.BasicProperties replyProps = new AMQP.BasicProperties
                        .Builder()
                        .correlationId(delivery.getProperties().getCorrelationId())
                        .build();

                String message = new String(delivery.getBody(), "UTF-8");
                System.out.println(" [.] get (" + message + ")");


                String response = null;
                try {
                    response = dbRequest(message);
                } catch (SQLException e) {
                    e.printStackTrace();
                }


                channel.basicPublish("", delivery.getProperties().getReplyTo(), replyProps, response.getBytes("UTF-8"));
                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                synchronized (monitor) {
                    monitor.notify();
                }
            };

            channel.basicConsume(QUEUE_NAME, false, deliverCallback, (consumerTag -> {
            }));
            // Wait and be prepared to consume the message from RPC client.
            while (true) {
                synchronized (monitor) {
                    try {
                        monitor.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
    static String dbRequest(String message) throws SQLException {

        message = message.replace("{","");
        message = message.replace("}","");
        message = message.replace("\"","");
        String[] splitmessage = message.split(":");

        SessionFactory factory = new Configuration()
                .configure("hibernate.cfg.xml")
                .addAnnotatedClass(City.class)
                .buildSessionFactory();
        Session session = factory.getCurrentSession();
        session.beginTransaction();
        City city = (City) session.createQuery("FROM Cities p WHERE p.name= :name")
                .setParameter("name", splitmessage[1]);
        return city.country;
    }
}
