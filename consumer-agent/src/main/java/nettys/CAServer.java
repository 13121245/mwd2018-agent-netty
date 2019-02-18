package nettys;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by zjw on 2018/05/30 10:17
 * Description:
 */
public class CAServer {

    private int port;

    private static final Logger logger = LoggerFactory.getLogger(CAServer.class);

    private static final EventLoopGroup workerGroup = new NioEventLoopGroup(8);

    public CAServer(int port) {
        this.port = port;
    }

    public void startServer() throws Exception{
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        CAClient caClient = CAClient.getCAClient();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ServerChannelInitializer())
                    .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                    .option(ChannelOption.SO_BACKLOG, 256)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
            ChannelFuture f = b.bind(this.port).sync();
            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    public static EventLoopGroup getWorkerGroup() {
        return workerGroup;
    }

    public static void shutdownWorkerGroup() {
        if(!workerGroup.isShutdown()) {
            workerGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception{
        try {
            CAServer server = new CAServer(20000);
            server.startServer();
        } finally {
            CAServer.shutdownWorkerGroup();
        }

    }

}
