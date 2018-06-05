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
        ChannelHandlerContext respCtx = RpcRequestHolder.get(requestId);
        if(null != respCtx){
            RpcRequestHolder.remove(requestId);
            String paramString = RpcRequestHolder.getString(requestId);
            String hashVal = new String(response.getBytes()).trim();
            if (!hashVal.equals(String.valueOf(paramString.hashCode()))){
                System.out.println(paramString.hashCode() + " != " + hashVal);
            }
            RpcRequestHolder.removeString(requestId);
            respCtx.writeAndFlush(response);
        }
    }
}
