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

    public void asyncInvoke(long id, String parameter, ChannelHandlerContext ctx) throws Exception {

        Channel channel = connectManager.getChannel();

        RpcInvocation invocation = new RpcInvocation();
        invocation.setMethodName("hash");
        invocation.setAttachment("path", "com.alibaba.dubbo.performance.demo.provider.IHelloService");
        invocation.setParameterTypes("Ljava/lang/String;");    // Dubbo内部用"Ljava/lang/String"来表示参数类型是String

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
        channel.eventLoop().execute(new Runnable() {
            @Override
            public void run() {
                RpcRequestHolder.put(id, ctx);
                channel.write(request, channel.voidPromise());
            }
        });

    }

    public void flush() throws Exception {
        Channel channel = connectManager.getChannel();
        channel.eventLoop().execute(new Runnable() {
            @Override
            public void run() {
                channel.flush();
            }
        });
    }
}
