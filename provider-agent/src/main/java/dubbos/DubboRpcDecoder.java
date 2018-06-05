package dubbos;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import models.Bytes;
import models.TcpResponse;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

public class DubboRpcDecoder extends ByteToMessageDecoder {
    // header length.
    protected static final int HEADER_LENGTH = 16;

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) {
        Object o = decode2(byteBuf);
        if (o != null)
            list.add(o);
    }

    private Object decode2(ByteBuf byteBuf){
        if (byteBuf.readableBytes() < HEADER_LENGTH) {
            return null;
        }
        byte[] header = new byte[HEADER_LENGTH];
        byteBuf.getBytes(byteBuf.readerIndex(), header);
        int dataLength = ByteBuffer.wrap(header, 12, 4).getInt();
        if (byteBuf.readableBytes() < HEADER_LENGTH + dataLength) {
            return null;
        }
        byteBuf.skipBytes(HEADER_LENGTH + 2);
        byte[] payload = new byte[dataLength - 2];
        byteBuf.readBytes(payload);
        byte[] requestIdBytes = Arrays.copyOfRange(header,4,12);
        long requestId = Bytes.bytes2long(requestIdBytes,0);

        TcpResponse response = new TcpResponse();
        response.setRequestId(String.valueOf(requestId));
        response.setBytes(payload);
        return response;
    }
}
