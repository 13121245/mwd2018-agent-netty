package nettys;

import com.alibaba.fastjson.JSON;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.handler.codec.http.websocketx.WebSocket08FrameEncoder;
import models.TcpRequest;

/**
 * Created by zjw on 2018/06/02 16:38
 * Description:
 */
public class AgentReqEncoder extends MessageToByteEncoder{

    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf buffer) throws Exception {
        byte[] data = JSON.toJSONBytes(msg);
//        System.out.println(data.length);
//        buffer.writeInt(data.length);
        buffer.writeBytes(data);
    }
}
