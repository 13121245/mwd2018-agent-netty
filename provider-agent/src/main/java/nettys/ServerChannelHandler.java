package nettys;

import dubbos.RpcClient;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import models.TcpRequest;
import models.TcpResponse;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by zjw on 2018/06/04 14:56
 * Description:
 */
public class ServerChannelHandler extends ChannelInboundHandlerAdapter{

    private static final ExecutorService executors = Executors.newFixedThreadPool(200);
    private static final RpcClient rpcClient = new RpcClient();

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception{
        TcpRequest tcpRequest = (TcpRequest)msg;

        executors.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    TcpResponse response = (TcpResponse) rpcClient.invoke(tcpRequest.getId(), tcpRequest.getInterfaceName(),
                            tcpRequest.getMethodName(), tcpRequest.getParameterTypeString(), tcpRequest.getParameter());
                    ctx.writeAndFlush(response);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

}