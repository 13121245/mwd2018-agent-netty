package nettys;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;

/**
 * Created by zjw on 2018/06/04 15:05
 * Description:
 */
public class ServerChannelInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        // 客户端请求的结果用前四个字节标识数据长度
        socketChannel.pipeline().addLast(new LengthFieldBasedFrameDecoder(6 * 1024, 0, 4, 0, 4))
                .addLast(new LengthFieldPrepender(4))
                .addLast(new AgentReqDecoder())
                .addLast(new AgentRespEncoder())
                .addLast(new ServerChannelHandler());
    }
}
