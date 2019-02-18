package nettys;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.*;
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
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by zjw on 2018/06/01 14:29
 * Description: 建立consumer agent到provider agent的连接
 */
public class CAClient {

    private IRegistry registry = new EtcdRegistry(System.getProperty("etcd.url"));
    private List<Channel> channels = new ArrayList<>();
    private List<Integer> dice = new ArrayList<>();
    private List<Endpoint> endpoints = null;
    private EventLoopGroup eventLoopGroup = CAServer.getWorkerGroup();
    private Map<EventLoop, Channel> channelMap = new HashMap<>();

    private final Random random = new Random();
    private final Object lock = new Object();

    private static final Logger logger = LoggerFactory.getLogger(CAClient.class);

    private static class CAClientHolder {
        private static final CAClient caClient = new CAClient();
    }

    private CAClient() {
        System.out.println("init caclient");
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

    public void invokeAsync(TcpRequest request, ChannelHandlerContext ctx) {
        Channel channel;
        if(channelMap.get(ctx.channel().eventLoop()) != null) {
//            System.out.println("channel active? " + channelMap.get(ctx.channel().eventLoop()).isActive());
//        if(channelMap.get(ctx.channel().eventLoop()) != null) {
            channel = channelMap.get(ctx.channel().eventLoop());
        } else {
            int index = random.nextInt(dice.size());
            channel = this.channels.get(dice.get(index));
        }

        AsyncRequestHolder.put(request.getId(), ctx);

        channel.writeAndFlush(request);

    }

    private void startClient() throws Exception {
        if(null == endpoints) {
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
                            Channel channel = createNewChannel(endpoint.getHost(), endpoint.getPort());
                            channels.add(channel);
                            channelMap.put(channel.eventLoop(), channel);
                            for (int j = 0 ; j < 110; ++j)
                                dice.add(i);
                            logger.info("fix endpoint:" + endpoints.get(i).getCapacity() + " on " + i);
                        } else if (endpoint.getCapacity().equals("medium")) {
                            Channel channel = createNewChannel(endpoint.getHost(), endpoint.getPort());
                            channels.add(channel);
                            channelMap.put(channel.eventLoop(), channel);
                            for (int j = 0 ; j < 188; ++j)
                                dice.add(i);
                            logger.info("fix endpoint:" + endpoints.get(i).getCapacity() + "on " + i);
                        } else if (endpoints.get(i).getCapacity().equals("large")) {
                            Channel channel = createNewChannel(endpoint.getHost(), endpoint.getPort());
                            channels.add(channel);
                            channelMap.put(channel.eventLoop(), channel);
                            for (int j = 0 ; j < 220; ++j)
                                dice.add(i);
                            logger.info("fix endpoint:" + endpoints.get(i).getCapacity() + "on " + i);
                        }
                    }
                }
            }
        }
    }

    private Channel createNewChannel(String host, int port) throws Exception{
        Bootstrap bootstrap = new Bootstrap()
                .group(eventLoopGroup)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .channel(NioSocketChannel.class)
                .handler(new ClientChannelInitializer());
//        ChannelFuture future = bootstrap.connect(host, port);
//        return future.channel();
        ChannelFuture future =  bootstrap.connect(host, port);
        future.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                System.out.println(future.isSuccess());
            }
        });
        Thread.sleep(100);
        return future.channel();
    }

}
