package dubbos;


import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import models.RpcFuture;
import models.RpcRequestHolder;
import models.TcpResponse;

import static io.netty.handler.codec.http.HttpHeaderValues.KEEP_ALIVE;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.rtsp.RtspHeaders.Names.CONNECTION;

public class RpcClientHandler extends SimpleChannelInboundHandler<TcpResponse> {

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, TcpResponse resp) {
        long requestId = resp.getRequestId();
        ChannelHandlerContext respCtx = RpcRequestHolder.get(requestId);
        if(null != respCtx){
            RpcRequestHolder.remove(requestId);
            FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.wrappedBuffer(resp.getBytes()));
            response.headers().set(CONTENT_TYPE, "text/html;charset=UTF-8");
            response.headers().set(CONTENT_LENGTH, resp.getBytes().length);
            response.headers().set(CONNECTION, KEEP_ALIVE);
            respCtx.writeAndFlush(response);
        }
    }
}
