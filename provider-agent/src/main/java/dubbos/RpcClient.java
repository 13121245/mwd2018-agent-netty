package dubbos;


import com.alibaba.fastjson.JSON;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import models.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RpcClient {
    private Logger logger = LoggerFactory.getLogger(RpcClient.class);
    private int port = Integer.valueOf(System.getProperty("dubbo.protocol.port"));
    private SocketChannel socketChannel = null;
    private SocketChannel outGoingSocketChannel = null;


    private BlockingQueue<TcpRequest> reqQueue = new ArrayBlockingQueue<TcpRequest>(512);
    private ExecutorService writerService = Executors.newSingleThreadExecutor();
    private ExecutorService readerService = Executors.newSingleThreadExecutor();

    public RpcClient(SocketChannel outGoingSocketChannel) throws Exception {
        this.outGoingSocketChannel = outGoingSocketChannel;
        init();
    }

    private void init() throws Exception {
        socketChannel = SocketChannel.open(new InetSocketAddress("127.0.0.1", port));
        //socketChannel.configureBlocking(false);
        socketChannel.socket().setTcpNoDelay(true);
        socketChannel.socket().setKeepAlive(true);
        startReadResponseService();

        Executors.newSingleThreadExecutor().submit(new Runnable() {
            private ByteBuffer byteBuf = ByteBuffer.allocate(4 * 1024);
            @Override
            public void run() {
                try {
                    TcpRequest req;
                    while ((req = reqQueue.take()) != null) {
                        RpcInvocation invocation = new RpcInvocation();
                        invocation.setMethodName("hash");
                        invocation.setAttachment("path", "com.alibaba.dubbo.performance.demo.provider.IHelloService");
                        invocation.setParameterTypes("Ljava/lang/String;");    // Dubbo内部用"Ljava/lang/String"来表示参数类型是String

                        ByteArrayOutputStream out = new ByteArrayOutputStream();
                        PrintWriter writer = new PrintWriter(new OutputStreamWriter(out));
                        JsonUtils.writeObject(req.getParameter(), writer);
                        invocation.setArguments(out.toByteArray());

                        Request request = new Request();
                        request.setId(req.getId());
                        request.setVersion("2.0.0");
                        request.setTwoWay(true);
                        request.setData(invocation);

                        // write request

                        byteBuf.clear();
                        DubboCodec.encode(byteBuf, request);
                        byteBuf.flip();
                        writeAll(socketChannel, byteBuf);
                    }

                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void asynInvoke(TcpRequest req) throws Exception {
        reqQueue.offer(req);
    }

    public void startReadResponseService() {
        readerService.submit(new Runnable() {
            ByteBuffer byteBuffer = ByteBuffer.allocate(4 * 1024);
            ByteBuffer writerBuffer = ByteBuffer.allocate(4 * 1024);

            @Override
            public void run() {
                while(true) {
                    try {
                        byteBuffer.clear();
                        byteBuffer.limit(DubboCodec.HEADER_LENGTH);
                        readFull(socketChannel, byteBuffer, DubboCodec.HEADER_LENGTH);
                        int dataLength = byteBuffer.getInt(12);
                        long requestId = byteBuffer.getLong(4);
                        byteBuffer.clear();
                        byteBuffer.limit(2);
                        readFull(socketChannel, byteBuffer, 2);
                        byte[] data = new byte[dataLength-2];
                        ByteBuffer payloadBuf = ByteBuffer.wrap(data);
                        readFull(socketChannel, payloadBuf, dataLength-2);
                        TcpResponse response = new TcpResponse();
                        response.setRequestId(requestId);
                        response.setBytes(new String(data).trim().getBytes());

                        byte[] jsonBytes = JSON.toJSONBytes(response);
                        writerBuffer.clear();
                        writerBuffer.putInt(jsonBytes.length);
                        writerBuffer.put(jsonBytes);
                        writerBuffer.flip();
                        writeAll(outGoingSocketChannel, writerBuffer);
                    } catch(Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
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


//    private IConnectManager connectManager;
//
//    public RpcClient(){
//        this.connectManager = new ConnecManager();
//    }
//
//    public void asyncInvoke(long id, String parameter, ChannelHandlerContext ctx) throws Exception {
//
//        Channel channel = connectManager.getChannel();
//
//        RpcInvocation invocation = new RpcInvocation();
//        invocation.setMethodName("hash");
//        invocation.setAttachment("path", "com.alibaba.dubbo.performance.demo.provider.IHelloService");
//        invocation.setParameterTypes("Ljava/lang/String;");    // Dubbo内部用"Ljava/lang/String"来表示参数类型是String
//
//        ByteArrayOutputStream out = new ByteArrayOutputStream();
//        PrintWriter writer = new PrintWriter(new OutputStreamWriter(out));
//        JsonUtils.writeObject(parameter, writer);
//        invocation.setArguments(out.toByteArray());
//
//        Request request = new Request();
//        request.setId(id);
//        request.setVersion("2.0.0");
//        request.setTwoWay(true);
//        request.setData(invocation);
//
//        //logger.info("requestId=" + request.getId());
//        channel.eventLoop().execute(new Runnable() {
//            @Override
//            public void run() {
//                RpcRequestHolder.put(id, ctx);
//                channel.write(request, channel.voidPromise());
//            }
//        });
//
//    }
//
//    public void flush() throws Exception {
//        Channel channel = connectManager.getChannel();
//        channel.eventLoop().execute(new Runnable() {
//            @Override
//            public void run() {
//                channel.flush();
//            }
//        });
//    }
}

class DubboCodec {

    // header length.
    protected static final int HEADER_LENGTH = 16;
    // magic header.
    protected static final short MAGIC = (short) 0xdabb;
    // message flag.
    protected static final byte FLAG_REQUEST = (byte) 0x80;
    protected static final byte FLAG_TWOWAY = (byte) 0x40;
    protected static final byte FLAG_EVENT = (byte) 0x20;

    protected static void encode(ByteBuffer buffer, Request req) throws Exception {
        // header.
        byte[] header = new byte[HEADER_LENGTH];
        // set magic number.
        Bytes.short2bytes(MAGIC, header);

        // set request and serialization flag.
        header[2] = (byte) (FLAG_REQUEST | 6);

        if (req.isTwoWay()) header[2] |= FLAG_TWOWAY;
        if (req.isEvent()) header[2] |= FLAG_EVENT;

        // set request id.
        Bytes.long2bytes(req.getId(), header, 4);

        // encode request data.
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        encodeRequestData(bos, req.getData());

        int len = bos.size();
        Bytes.int2bytes(len, header, 12);

        // write
        buffer.put(header); // write header.
        buffer.put(bos.toByteArray());
    }

    private static void encodeRequestData(OutputStream out, Object data) throws Exception {
        RpcInvocation inv = (RpcInvocation)data;

        PrintWriter writer = new PrintWriter(new OutputStreamWriter(out));

        JsonUtils.writeObject(inv.getAttachment("dubbo", "2.0.1"), writer);
        JsonUtils.writeObject(inv.getAttachment("path"), writer);
        JsonUtils.writeObject(inv.getAttachment("version"), writer);
        JsonUtils.writeObject(inv.getMethodName(), writer);
        JsonUtils.writeObject(inv.getParameterTypes(), writer);

        JsonUtils.writeBytes(inv.getArguments(), writer);
        JsonUtils.writeObject(inv.getAttachments(), writer);
    }


}
