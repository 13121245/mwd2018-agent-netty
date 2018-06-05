package nettys;

import com.alibaba.fastjson.JSON;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import models.TcpResponse;

import java.util.List;

/**
 * Created by zjw on 2018/06/02 16:38
 * Description:
 */
public class AgentRespDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) {
        int len = byteBuf.readableBytes();
        if (len == 0) {
            return;
        }
        byte[] data = new byte[len];
        byteBuf.readBytes(data);
        Object obj = JSON.parseObject(data, TcpResponse.class);
        list.add(obj);
    }

}

