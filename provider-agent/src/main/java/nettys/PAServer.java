package nettys;

import com.alibaba.fastjson.JSON;
import dubbos.RpcClient;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import models.TcpRequest;
import models.TcpResponse;
import registry.EtcdRegistry;
import registry.IRegistry;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import static io.netty.handler.codec.http.HttpHeaderValues.KEEP_ALIVE;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.rtsp.RtspHeaders.Names.CONNECTION;

/**
 * Created by zjw on 2018/06/04 14:52
 * Description:
 */
public class PAServer {


    private int port;

    public PAServer(int port) {
        this.port = port;
    }

    public static boolean writeAll(SocketChannel socketChannel, ByteBuffer buf) throws IOException {
        while (buf.position() != buf.limit())
            if (socketChannel.write(buf) == -1)
                return false;
        return true;
    }

    public static boolean readFull(SocketChannel socketChannel, ByteBuffer buf, int count)  throws IOException {
        int nread = 0;
        while (count > 0 && nread >= 0) {
            nread = socketChannel.read(buf);
            if (nread != -1)
                count -= nread;
        }
        return count == 0;
    }

    public void startServer() throws Exception{
        try {
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.socket().bind(new InetSocketAddress(port));

            while (true) {
                SocketChannel channel = serverSocketChannel.accept();
                channel.socket().setTcpNoDelay(true);
                channel.socket().setKeepAlive(true);
                Executors.newFixedThreadPool(1).submit(new Runnable() {
                    @Override
                    public void run() {

                        byte[] msgContentData = new byte[2048];
                        ByteBuffer lenBuf = ByteBuffer.allocateDirect(4);
                        try {
                            RpcClient client = new RpcClient(channel);
                            while (true) {
                                lenBuf.clear();
                                boolean good = readFull(channel, lenBuf, lenBuf.limit());
                                if (good == false)
                                    throw new RuntimeException("failed to read the entire buffer from socket");
                                lenBuf.flip();
                                int msgLen = lenBuf.getInt();
                                ByteBuffer msgContentBuf = ByteBuffer.wrap(msgContentData, 0, msgLen);
                                good = readFull(channel, msgContentBuf, msgLen);
                                assert good;
                                if (good == false)
                                    throw new RuntimeException("failed to read the entire buffer from socket");
                                TcpRequest req = JSON.parseObject(msgContentData, 0, msgLen, com.alibaba.fastjson.util.IOUtils.UTF8, TcpRequest.class);

                                client.asynInvoke(req);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }

        } finally {

        }
    }

    public static void main(String[] args) throws Exception{
        PAServer server = new PAServer(Integer.valueOf(System.getProperty("server.port")));
        IRegistry registry = new EtcdRegistry(System.getProperty("etcd.url"));
        server.startServer();
    }

}
