package nettys;

import dubbos.RpcClient;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;
import models.RpcRequestHolder;
import models.TcpRequest;
import models.TcpResponse;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by zjw on 2018/06/04 14:56
 * Description:
 */
public class ServerChannelHandler extends SimpleChannelInboundHandler<TcpRequest>{

    private static final RpcClient rpcClient = new RpcClient();

    @Override
    public void channelRead0(ChannelHandlerContext ctx, TcpRequest tcpRequest) throws Exception{
        rpcClient.asyncInvoke(tcpRequest.getId(), tcpRequest.getParameter(), ctx);
    }

}
