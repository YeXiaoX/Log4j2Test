package test;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.slf4j.MDC;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;

/**
 * Created by Ivan on 2016/6/27.
 */
public class SelectSockets {
    public static int PORT_NUMBER = 9999;
    private static final Logger logger = LogManager.getLogger(SelectSockets.class);

    public static void main(String[] argv) throws Exception {
        new SelectSockets().go(argv);
    }

    public void go(String[] argv) {
        int port = PORT_NUMBER;
        if (argv.length > 0) {
            port = Integer.parseInt(argv[0]);
        }
        System.out.println("Listening on port " + port);
        ServerSocketChannel serverChannel = null;
        try {
            serverChannel = ServerSocketChannel.open();
        } catch (IOException e) {
            logger.error(e);
            e.printStackTrace();
        }
        ServerSocket serverSocket = serverChannel.socket();
        serverSocket = serverChannel.socket();
        Selector selector = null;
        try {
            selector = Selector.open();
        } catch (IOException e) {
            logger.error(e);
            e.printStackTrace();
        }
        try {
            serverSocket.bind(new InetSocketAddress(port));
        } catch (IOException e) {
            logger.error(e);
            e.printStackTrace();
        }
        try {
            serverChannel.configureBlocking(false);
        } catch (IOException e) {
            logger.error(e);
            e.printStackTrace();
        }
        try {
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);
        } catch (ClosedChannelException e) {
            logger.error(e);
            e.printStackTrace();
        }
        while (true) {
            System.out.println(":11");
            //selector.close();
           // selector.wakeup();
            // int n = selector.selectNow();//selectNow()过程是非阻塞的,执行结果类似在select()方法执行前添加selector.wakeup()
            int n = 0;//服务器启动时，执行到这里(没有就绪的channel时）会阻塞，不断去轮询，直到有连接进来时才会继续往下执行，以后每次selector被唤醒时，循环执行一次，否则继续阻塞。
            try {
                n = selector.select();
            } catch (IOException e) {
                logger.error(e);
                e.printStackTrace();
            }
            System.out.println("就绪的channel个数为：" + n);
            if (n == 0) {
                System.out.println(":0");
                continue;
            }
            Iterator it = selector.selectedKeys().iterator();
            while (it.hasNext()) {
                SelectionKey key = (SelectionKey) it.next();
                if (key.isAcceptable()) {//如果有用户接入，就必须消费掉这个请求（处理请求)，否则会陷入死循环，因为此时selector.select()返回值始终为1

                    System.out.println(":1");
                    ServerSocketChannel server = (ServerSocketChannel) key.channel();
                    SocketChannel channel = null;
                    try {
                        channel = server.accept();
                    } catch (IOException e) {
                        logger.error(e);
                        e.printStackTrace();
                    }
                    MDC.put("username", channel.toString());
                    logger.info("新的链接接入");
                    System.out.println(channel);
                    try {
                        registerChannel(selector, channel, SelectionKey.OP_READ);
                    } catch (Exception e) {
                        logger.error(e);
                        e.printStackTrace();
                    }
                    try {
                        sayHello(channel);
                    } catch (Exception e) {
                        logger.error(e);
                        e.printStackTrace();
                    }
                }
                if (key.isReadable()) {
                    try {
                        readDataFromSocket(key);
                    } catch (Exception e) {
                        logger.error(e);
                        e.printStackTrace();
                    }
                }

                it.remove();
            }
        }
    }

    protected void registerChannel(Selector selector, SelectableChannel channel, int ops)  {
        if (channel == null) {
            System.out.println("channel is null");
            return;
        }
        try {
            channel.configureBlocking(false);
        } catch (IOException e) {
            logger.error(e);
            e.printStackTrace();
        }
        try {
            channel.register(selector, ops);
        } catch (ClosedChannelException e) {
            logger.error(e);
            e.printStackTrace();
        }
    }

    private ByteBuffer buffer = ByteBuffer.allocateDirect(1024);

    protected void readDataFromSocket(SelectionKey key) throws Exception {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        int count;
        buffer.clear();
        while ((count = socketChannel.read(buffer)) > 0) {
            buffer.flip();
            while (buffer.hasRemaining()) {
                socketChannel.write(buffer);
            }
            buffer.clear();
        }
        if (count < 0) {
            socketChannel.close();
        }
    }

    private void sayHello(SocketChannel channel) throws Exception {
        buffer.clear();
        buffer.put("Hi there!\r\n".getBytes());
        buffer.flip();
        channel.write(buffer);
    }
}

