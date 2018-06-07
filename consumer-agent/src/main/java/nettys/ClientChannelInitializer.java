package nettys;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;


/**
 * Created by zjw on 2018/06/02 15:27
 * Description:
 */
public class ClientChannelInitializer extends ChannelInitializer<SocketChannel>{

    @Override
    protected void initChannel(SocketChannel sc) throws Exception {
        // 返回的结果用前四个字节标识数据长度
        sc.pipeline()//.addLast(new LengthFieldBasedFrameDecoder(2 * 1024, 0, 4, 0, 4))
                .addLast(new LengthFieldPrepender(4))
                .addLast(new AgentReqEncoder())
                .addLast(new AgentRespDecoder());
                //.addLast(new ClientChannelHandler());
    }
}
