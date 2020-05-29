package cn.wildfirechat.app;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;


/**
 * 生产者代码
 */
public class RabbitMQTest {

    //队列
    private static final String QUEUE = "hello world";

    public static void main(String[] args) {
        //通过连接工厂创建新的连接和MQ建立连接
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("localhost");
        connectionFactory.setPort(5672);
        connectionFactory.setUsername("guest");
        connectionFactory.setPassword("guest");
        //设置虚拟机，一个mq服务可以设置多个虚拟机，每个虚拟机就相当于一个独立的mq
        connectionFactory.setVirtualHost("/");

        Connection connection = null;

        try {
            //建立新连接
            connection = connectionFactory.newConnection();
            //创建会话通道，生产者和mq服务所有通信都在channel通道中完成
            Channel channel = connection.createChannel();
            //声明队列,如果队列在mq中没有则要创建
            //参数  String quene , boolean durable , boolean exclusive , boolean autoDelete , Map<String,Object> arguments
            /**
             * 参数明细
             * quene 队列名称
             * durable 是否持久化，如果持久化，mq重启之后队列还在
             * exclusive  是否独占连接，队列只允许在连接中访问。如果连接关闭后队列自动删除，如果将此参数设置为true可用于临时队列的创建
             * autoDelete   自动删除，队列不再使用时，是否自动删除此队列，如果将此参数和exclusive参数设置为true就可以实现临时队列（队列不用了就自动删除）
             * arguments   参数，可以设置一个队列的扩展参数，比如：可以设置存活时间
             */
            channel.queueDeclare(QUEUE, true, false, false, null);
            //发送消息
            /**
             * String exchange, String routingKey, BasicProperties props, byte[] body
             *
             * exchange  交换机，如果不指定将使用mq的默认交换机（设置为 ""）
             * routingKey  路由key，将还击根据路由key来将消息转发到指定的队列，如果使用默认交换机，routingKey设置为队列的名称
             * props  消息的属性
             * body   消息的内容
             */
            channel.basicPublish("",QUEUE,null,"hello world,我是calvin哦".getBytes());
            System.out.println("send to mq");
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
        }


    }
}
