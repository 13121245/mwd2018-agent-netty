package nettys;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import models.AsyncRequestHolder;
import models.TcpFuture;
import models.TcpRequest;
import models.TcpRequestHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import registry.Endpoint;
import registry.EtcdRegistry;
import registry.IRegistry;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by zjw on 2018/06/01 14:29
 * Description: 建立consumer agent到provider agent的连接
 */
public class CAClient {

    private IRegistry registry = new EtcdRegistry(System.getProperty("etcd.url"));
    private Bootstrap bootstrap = null;
    private List<Channel> channels = new ArrayList<>();
    private List<Integer> dice = new ArrayList<>();
    private List<Endpoint> endpoints = null;

    private final Random random = new Random();
    private final Object lock = new Object();

    private static final Logger logger = LoggerFactory.getLogger(CAClient.class);

    private static class CAClientHolder {
        private static final CAClient caClient = new CAClient();
    }

    private CAClient() {
        try {
            startClient();
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("Error start CAClient");
        }
    }

    public static CAClient getCAClient() {
        return CAClientHolder.caClient;
    }

    public Object invoke(TcpRequest request) {
        int index = random.nextInt(dice.size());
        Channel channel = this.channels.get(dice.get(index));

        TcpFuture tcpFuture = new TcpFuture();
        TcpRequestHolder.put(String.valueOf(request.getId()), tcpFuture);

        channel.writeAndFlush(request);
        Object result = null;
        try {
            result = tcpFuture.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public void invokeAsync(TcpRequest request, ChannelHandlerContext ctx) {
        int index = random.nextInt(dice.size());
        Channel channel = this.channels.get(dice.get(index));

        AsyncRequestHolder.put(String.valueOf(request.getId()), ctx);

        channel.writeAndFlush(request);

    }

    private void startClient() throws Exception {
        if (bootstrap == null) {
            synchronized (lock) {
                if (bootstrap == null) {
                    this.bootstrap = this.createBootstrap();
                }
            }
        }
        if (null == endpoints) {
            synchronized (lock) {
                if (null == endpoints) {
                    try {
                        endpoints = registry.find("com.alibaba.dubbo.performance.demo.provider.IHelloService");
                    } catch (Exception e) {
                        logger.info("Error find endpoints");
                    }
                    for (int i = 0; i < endpoints.size(); i++) {
                        Endpoint endpoint = endpoints.get(i);
                        if (endpoint.getCapacity().equals("small")) {
                            channels.add(createNewChannel(endpoint.getHost(), endpoint.getPort()));
                            for(int j = 0; j < 142; j++)
                                dice.add(i);
                            logger.info("fix endpoint:" + endpoints.get(i).getCapacity() + " on " + i);
                        } else if (endpoint.getCapacity().equals("medium")) {
                            channels.add(createNewChannel(endpoint.getHost(), endpoint.getPort()));
                            for(int j = 0; j < 175; j++)
                                dice.add(i);
                            logger.info("fix endpoint:" + endpoints.get(i).getCapacity() + "on " + i);
                        } else if (endpoints.get(i).getCapacity().equals("large")) {
                            channels.add(createNewChannel(endpoint.getHost(), endpoint.getPort()));
                            for(int j = 0; j < 195; j++)
                                dice.add(i);
                            logger.info("fix endpoint:" + endpoints.get(i).getCapacity() + "on " + i);
                        }
                    }
                }
            }
        }
    }

    private Bootstrap createBootstrap() throws Exception {
        EventLoopGroup eventLoopGroup = new NioEventLoopGroup(12);
        Bootstrap bootstrap = new Bootstrap()
                .group(eventLoopGroup)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.ALLOCATOR, UnpooledByteBufAllocator.DEFAULT)
                .channel(NioSocketChannel.class)
                .handler(new ClientChannelInitializer());
        return bootstrap;
    }

    private Channel createNewChannel(String host, int port) throws Exception {
        return this.bootstrap.connect(host, port).sync().channel();
    }

}
