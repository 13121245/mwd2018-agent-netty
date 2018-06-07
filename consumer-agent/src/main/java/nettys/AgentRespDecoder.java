package nettys;

import com.alibaba.fastjson.JSON;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import models.AsyncRequestHolder;
import models.TcpResponse;
import sun.misc.IOUtils;
import sun.nio.ch.IOUtil;

import java.util.List;

import static io.netty.handler.codec.http.HttpHeaderValues.KEEP_ALIVE;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.rtsp.RtspHeaders.Names.CONNECTION;

/**
 * Created by zjw on 2018/06/02 16:38
 * Description:
 */
public class AgentRespDecoder extends ByteToMessageDecoder {
    private static ThreadLocal<byte[]> dataHolder = new ThreadLocal<byte[]>() {
        @Override
        public byte[] initialValue() {
            return new byte[512];
        }
    };
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        byte[] data = dataHolder.get();
        while(byteBuf.readableBytes() > 4) {
            int len = byteBuf.getInt(byteBuf.readerIndex());
            if (byteBuf.readableBytes() - 4 < len) {
                return;
            }
            byteBuf.skipBytes(4);

            byteBuf.readBytes(data, 0, len);
            TcpResponse resp = JSON.parseObject(data, 0, len, com.alibaba.fastjson.util.IOUtils.UTF8, TcpResponse.class);
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

        /*
        int len = byteBuf.readableBytes();
        if (len == 0) {
            return;
        }
//        if (len != byteBuf.readableBytes()) {
//            throw new Exception(len + " != " + byteBuf.readableBytes());
//        }
        byte[] data = new byte[len];
        byteBuf.readBytes(data);
        TcpResponse resp = JSON.parseObject(data, TcpResponse.class);
*/
        return;
    }

}

