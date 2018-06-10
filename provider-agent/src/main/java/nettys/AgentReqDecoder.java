package nettys;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.util.IOUtils;
import dubbos.RpcClient;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import models.TcpRequest;
import models.TcpResponse;

import java.util.ArrayList;
import java.util.List;

import static io.netty.handler.codec.http.HttpHeaderValues.KEEP_ALIVE;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.rtsp.RtspHeaders.Names.CONNECTION;

/**
 * Created by zjw on 2018/06/04 14:58
 * Description:
 */
public class AgentReqDecoder extends ByteToMessageDecoder {
    //private static final RpcClient rpcClient = new RpcClient();
    private static ThreadLocal<byte[]> dataHolder = new ThreadLocal<byte[]>() {
        @Override
        public byte[] initialValue() {
            return new byte[1280];
        }
    };

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        byte[] data = dataHolder.get();
        int len = byteBuf.getInt(byteBuf.readerIndex());
        if (byteBuf.readableBytes() - 4 < len) {
            return;
        }byteBuf.skipBytes(4);
        byteBuf.readBytes(data, 0, len);
        TcpRequest tcpRequest = JSON.parseObject(data,0, len, IOUtils.UTF8, TcpRequest.class);
        //rpcClient.asyncInvoke(tcpRequest.getId(), tcpRequest.getParameter(), channelHandlerContext);
    }
}
