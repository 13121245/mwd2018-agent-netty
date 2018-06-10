package nettys;

import com.alibaba.fastjson.JSON;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import models.AsyncRequestHolder;
import models.TcpRequest;
import models.TcpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import registry.Endpoint;
import registry.EtcdRegistry;
import registry.IRegistry;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;

import static io.netty.handler.codec.http.HttpHeaderValues.KEEP_ALIVE;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.rtsp.RtspHeaders.Names.CONNECTION;

/**
 * Created by zjw on 2018/06/01 14:29
 * Description: 建立consumer agent到provider agent的连接
 */
public class CAClient {

    private IRegistry registry = new EtcdRegistry(System.getProperty("etcd.url"));
    private List<BlockingQueue<TcpRequest>> reqQueues = new ArrayList<>();
    private List<Integer> dice = new ArrayList<>();
    private List<Endpoint> endpoints = null;

    private final Random random = new Random();
    private final Object lock = new Object();

    private static final Logger logger = LoggerFactory.getLogger(CAClient.class);

    private static class CAClientHolder {
        private static final CAClient caClient = new CAClient();
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

    public void invokeAsync(TcpRequest request, ChannelHandlerContext ctx) {
        int index = random.nextInt(dice.size());
        BlockingQueue queue = reqQueues.get(dice.get(index));

        queue.offer(request);
        AsyncRequestHolder.put(request.getId(), ctx);
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
                            reqQueues.add(createNewChannel(endpoint.getHost(), endpoint.getPort()));
                            for (int j = 0 ; j < 132; ++j)
                                dice.add(i);
                            logger.info("fix endpoint:" + endpoints.get(i).getCapacity() + " on " + i);
                        } else if (endpoint.getCapacity().equals("medium")) {
                            reqQueues.add(createNewChannel(endpoint.getHost(), endpoint.getPort()));
                            for (int j = 0 ; j < 180; ++j)
                                dice.add(i);
                            logger.info("fix endpoint:" + endpoints.get(i).getCapacity() + "on " + i);
                        } else if (endpoints.get(i).getCapacity().equals("large")) {
                            reqQueues.add(createNewChannel(endpoint.getHost(), endpoint.getPort()));
                            for (int j = 0 ; j < 200; ++j)
                                dice.add(i);
                            logger.info("fix endpoint:" + endpoints.get(i).getCapacity() + "on " + i);
                        }
                    }
                }
            }
        }
    }
    private static SocketChannel connect(String host, int port ) throws IOException, InterruptedException {
        InetSocketAddress providerAgentAddr = new InetSocketAddress(host, port);
        SocketChannel socketChannel = null;
        while (true) {
            logger.info("connecting to {}:{}", host, port);
            try {
                socketChannel = SocketChannel.open();
                socketChannel.connect(new InetSocketAddress(host, port));
            } catch (IOException e){
                e.printStackTrace();
                socketChannel.close();
                logger.info("{}, retrying...", e.getStackTrace());
                Thread.sleep(10);
                continue;
            }
            logger.info("connected to {}:{}", host, port);
            break;
        }
        socketChannel.socket().setTcpNoDelay(true);
        socketChannel.socket().setKeepAlive(true);
        return socketChannel;

    }
    private BlockingQueue<TcpRequest> createNewChannel(String host, int port) throws Exception{

        SocketChannel channel = connect(host, port);

        BlockingQueue<TcpRequest> reqQueue = new ArrayBlockingQueue<TcpRequest>(512);
        // writer
        Executors.newFixedThreadPool(1).submit(new Runnable() {
            @Override
            public void run(){
                TcpRequest req;
                try {
                    List<TcpRequest> reqs;
                    ByteBuffer byteBuffer = ByteBuffer.allocateDirect(2048);
                    while ((req = reqQueue.take()) != null) {

                        reqs = new ArrayList<>();
                        reqs.add(req);
                        while ((req = reqQueue.poll()) != null) {
                            reqs.add(req);
                        }
                        for (TcpRequest req2 : reqs) {
                            logger.info(req2.toString());
                            byte[] data = JSON.toJSONBytes(req2);
                            byteBuffer.clear();
                            byteBuffer.putInt(data.length);
                            byteBuffer.put(data);
                            byteBuffer.flip();
                            writeAll(channel, byteBuffer);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        // reader
        Executors.newFixedThreadPool(1).submit(new Runnable() {
            @Override
            public void run(){
                byte[] msgContentData = new byte[2048];
                ByteBuffer lenBuf = ByteBuffer.allocateDirect(4);
                try {
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
                        TcpResponse resp = JSON.parseObject(msgContentData, 0, msgLen, com.alibaba.fastjson.util.IOUtils.UTF8, TcpResponse.class);
                        long requestId = resp.getRequestId();
                        ChannelHandlerContext ctx = AsyncRequestHolder.get(requestId);
                        if(null != ctx) {
                            AsyncRequestHolder.remove(requestId);
                            FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.wrappedBuffer(resp.getBytes()));
                            response.headers().set(CONTENT_TYPE, "text/html;charset=UTF-8");
                            response.headers().set(CONTENT_LENGTH, resp.getBytes().length);
                            response.headers().set(CONNECTION, KEEP_ALIVE);
                            ctx.writeAndFlush(response);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
        return reqQueue;
    }

}
