package server;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import java.util.logging.Logger;

//Consumer of MQ
// TODO: figure out the database
// TODO: client get respond?

public class PackagingServer {
    private static final Logger LOG = Logger.getLogger("CheckoutServer.class");
    private static final String TASK_QUEUE_NAME = "task_queue";


    public static void main(String[] args) throws Exception {
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
            channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
            System.out.println(" [x] .......");
            System.out.println(" [x] Package for customer: '" + message + "' is ready.");
            System.out.println(" [*] Waiting for next package. To exit press CTRL+C");
        };
        channel.basicConsume(TASK_QUEUE_NAME, false, deliverCallback, consumerTag -> { });
    }
}
