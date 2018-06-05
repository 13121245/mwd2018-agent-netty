package nettys;

import com.alibaba.fastjson.JSON;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import models.TcpRequest;
import models.TcpResponse;

import java.util.List;

/**
 * Created by zjw on 2018/06/04 14:58
 * Description:
 */
public class AgentReqDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) {
        int len = byteBuf.readableBytes();
        if (len == 0) {
            return;
        }
        System.out.println(len);
        byte[] data = new byte[len];
        byteBuf.readBytes(data);
        Object obj = JSON.parseObject(data, TcpRequest.class);
        list.add(obj);
    }

}
