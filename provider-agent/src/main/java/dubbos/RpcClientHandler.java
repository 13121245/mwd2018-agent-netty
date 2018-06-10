package dubbos;


import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import models.RpcFuture;
import models.RpcRequestHolder;
import models.TcpResponse;

public class RpcClientHandler extends SimpleChannelInboundHandler<TcpResponse> {

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, TcpResponse response) {
        long requestId = response.getRequestId();
        ChannelHandlerContext respCtx = RpcRequestHolder.get(requestId);
        //System.out.println(requestId);
        if(null != respCtx){
            //System.out.println(requestId + " " + respCtx);
            RpcRequestHolder.remove(requestId);
            respCtx.channel().writeAndFlush(response);
        }
    }
}
