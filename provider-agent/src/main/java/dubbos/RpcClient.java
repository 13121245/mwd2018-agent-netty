package dubbos;


import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import models.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import registry.IRegistry;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

public class RpcClient {
    private Logger logger = LoggerFactory.getLogger(RpcClient.class);

    private IConnectManager connectManager;

    public RpcClient(){
        this.connectManager = new ConnecManager();
    }

    public void asyncInvoke(long id, String interfaceName, String method, String parameterTypesString, String parameter, ChannelHandlerContext ctx) throws Exception {

        Channel channel = connectManager.getChannel();

        RpcInvocation invocation = new RpcInvocation();
        invocation.setMethodName(method);
        invocation.setAttachment("path", interfaceName);
        invocation.setParameterTypes(parameterTypesString);    // Dubbo内部用"Ljava/lang/String"来表示参数类型是String

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(out));
        JsonUtils.writeObject(parameter, writer);
        invocation.setArguments(out.toByteArray());

        Request request = new Request();
        request.setId(id);
        request.setVersion("2.0.0");
        request.setTwoWay(true);
        request.setData(invocation);

        //logger.info("requestId=" + request.getId());

        RpcRequestHolder.put(String.valueOf(request.getId()), ctx);

        channel.writeAndFlush(request);

    }
}
