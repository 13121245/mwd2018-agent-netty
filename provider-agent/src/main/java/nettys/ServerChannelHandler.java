package nettys;

import com.google.common.base.Charsets;
import dubbos.RpcClient;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import models.RpcRequestHolder;
import models.TcpRequest;
import models.TcpResponse;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by zjw on 2018/06/04 14:56
 * Description:
 */
public class ServerChannelHandler extends ChannelInboundHandlerAdapter{

    private static final RpcClient rpcClient = new RpcClient();

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception{
        FullHttpRequest request = (FullHttpRequest) msg;

        String jsonStr = request.content().toString(Charsets.UTF_8);
        QueryStringDecoder decoder = new QueryStringDecoder(jsonStr, false);
        Map<String, List<String>> map = decoder.parameters();
        request.release();

        TcpRequest tcpRequest = new TcpRequest(map.get("parameter").get(0));

        rpcClient.asyncInvoke(tcpRequest.getId(), tcpRequest.getParameter(), ctx);
    }

}
