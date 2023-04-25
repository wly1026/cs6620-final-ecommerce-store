package Server;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import common.ecommerce.Customer;
import common.ecommerce.Product;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

//Consumer of MQ
// TODO: figure out the database
// TODO: client get respond?

public class CheckoutServer {
    private static final Logger LOG = Logger.getLogger("CheckoutServer.class");
    private static final String TASK_QUEUE_NAME = "task_queue";

    private Map<Integer, Customer> customers;
    private Map<String, Product> products;

    public CheckoutServer(Map<Integer, Customer> customers, Map<String, Product> products) throws RemoteException {
        this.products = products;
        this.customers = customers;
    }

    public void consumeMessage() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        final Connection connection = factory.newConnection();
        final Channel channel = connection.createChannel();

        channel.queueDeclare(TASK_QUEUE_NAME, true, false, false, null);
        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        channel.basicQos(1);

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");

            System.out.println(" [x] Received checkout request from customer: '" + message + "'");
            try {
                deductStock(message);
            } finally {
                System.out.println(" [x] Checkout Done");
                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
            }
        };
        channel.basicConsume(TASK_QUEUE_NAME, false, deliverCallback, consumerTag -> { });
    }

    private void deductStock(String task) {
        try {
            System.out.println(" [x] Stock: " + this.products.toString());
            int customerId = Integer.parseInt(task);
            Map<String, Integer> cart = this.customers.get(customerId).getCart();
            for (String productName: cart.keySet()) {
                Product product = products.get(productName);
                product.deductStock(cart.get(productName));
            }
            Thread.sleep(1000);
        } catch (InterruptedException _ignored) {
            Thread.currentThread().interrupt();
        }
    }

}
