package nettys;

import com.alibaba.fastjson.JSON;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * Created by zjw on 2018/06/04 15:00
 * Description:
 */
public class AgentRespEncoder extends MessageToByteEncoder{

    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf buffer) throws Exception {
            byte[] data = JSON.toJSONBytes(msg);
//        buffer.writeInt(data.length);
        buffer.writeBytes(data);
    }
}
