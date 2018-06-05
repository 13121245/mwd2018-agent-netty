package dubbos;


import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import models.RpcFuture;
import models.RpcRequestHolder;
import models.TcpResponse;

public class RpcClientHandler extends SimpleChannelInboundHandler<TcpResponse> {

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, TcpResponse response) {
        String requestId = response.getRequestId();
        RpcFuture future = RpcRequestHolder.get(requestId);
        if(null != future){
            RpcRequestHolder.remove(requestId);
            future.done(response);
        }
    }
}
