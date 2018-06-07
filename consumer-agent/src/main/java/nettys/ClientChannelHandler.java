package nettys;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import models.AsyncRequestHolder;
import models.TcpFuture;
import models.TcpRequestHolder;
import models.TcpResponse;

import java.nio.channels.SocketChannel;

import static io.netty.handler.codec.http.HttpHeaderValues.KEEP_ALIVE;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.rtsp.RtspHeaders.Names.CONNECTION;

/**
 * Created by zjw on 2018/06/02 15:27
 * Description:
 */
public class ClientChannelHandler extends SimpleChannelInboundHandler<TcpResponse>{

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, TcpResponse tcpResponse) {
        long requestId = tcpResponse.getRequestId();
        // 使用线程池时采用的方案，没必要其实...
//        TcpFuture tcpFuture = TcpRequestHolder.get(requestId);
//        if(null != tcpFuture) {
//            TcpRequestHolder.remove(requestId);
//            tcpFuture.done(tcpResponse);
//        }
        ChannelHandlerContext ctx = AsyncRequestHolder.get(requestId);
        if(null != ctx) {
            AsyncRequestHolder.remove(requestId);
            byte[] result = new String(tcpResponse.getBytes()).trim().getBytes();
            FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.wrappedBuffer(result));
            response.headers().set(CONTENT_TYPE, "text/html;charset=UTF-8");
            response.headers().set(CONTENT_LENGTH, result.length);
            response.headers().set(CONNECTION, KEEP_ALIVE);
            ctx.writeAndFlush(response);
        }
    }

}
