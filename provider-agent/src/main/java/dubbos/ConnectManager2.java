package dubbos;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by zjw on 2018/05/03 21:54
 * Description: 多个channel的实现
 */
public class ConnectManager2 implements IConnectManager{

    private static final int MAX_CHANNEL_COUNT = 4;

    private Channel[] channels;
    private Object[] locks;
    private Random random = new Random();
    private AtomicInteger atomicInteger = new AtomicInteger();

    public ConnectManager2() {
        this.channels = new Channel[MAX_CHANNEL_COUNT];
        this.locks = new Object[MAX_CHANNEL_COUNT];
        for (int i = 0; i < locks.length; i++) {
            this.locks[i] = new Object();
        }
    }

    public Channel getChannel() throws Exception {
        int index = atomicInteger.incrementAndGet() % MAX_CHANNEL_COUNT;
//        int index = random.nextInt(MAX_CHANNEL_COUNT);
        if(channels[index] == null || !channels[index].isActive()) {
            synchronized (locks[index]) {
                if(channels[index] == null || !channels[index].isActive()) {
                    channels[index] = createNewChannel();
                }
            }
        }
        return channels[index];
    }

    private Channel createNewChannel() throws Exception {
        EventLoopGroup eventLoopGroup = new NioEventLoopGroup(4);
        Bootstrap bootstrap = new Bootstrap()
                .group(eventLoopGroup)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.ALLOCATOR, UnpooledByteBufAllocator.DEFAULT)
                .channel(NioSocketChannel.class)
                .handler(new RpcClientInitializer());
        int port = Integer.valueOf(System.getProperty("dubbo.protocol.port"));
        return bootstrap.connect("127.0.0.1", port).sync().channel();
    }

}
