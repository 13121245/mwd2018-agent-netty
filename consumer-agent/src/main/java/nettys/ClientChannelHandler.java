package nettys;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import models.TcpFuture;
import models.TcpRequestHolder;
import models.TcpResponse;

import java.nio.channels.SocketChannel;

/**
 * Created by zjw on 2018/06/02 15:27
 * Description:
 */
public class ClientChannelHandler extends SimpleChannelInboundHandler<TcpResponse>{

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, TcpResponse tcpResponse) {
        String requestId = tcpResponse.getRequestId();
        TcpFuture tcpFuture = TcpRequestHolder.get(requestId);
        if(null != tcpFuture) {
            TcpRequestHolder.remove(requestId);
            tcpFuture.done(tcpResponse);
        }
    }

}
