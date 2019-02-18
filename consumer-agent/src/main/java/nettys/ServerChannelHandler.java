package nettys;

import com.google.common.base.Charsets;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.MixedAttribute;
import models.TcpRequest;
import models.TcpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import registry.EndpointUtil;
import registry.IRegistry;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static io.netty.handler.codec.http.HttpHeaderValues.KEEP_ALIVE;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;


/**
 * Created by zjw on 2018/05/30 10:26
 * Description:
 */
@ChannelHandler.Sharable
public class ServerChannelHandler extends ChannelInboundHandlerAdapter {

    //    private static final ExecutorService executors = Executors.newFixedThreadPool(256);
    private static final CAClient caClient = CAClient.getCAClient();
    private static final Logger logger = LoggerFactory.getLogger(ServerChannelHandler.class);
    private static final Random random = new Random();

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        FullHttpRequest request = (FullHttpRequest) msg;

        String jsonStr = request.content().toString(Charsets.UTF_8);
        QueryStringDecoder decoder = new QueryStringDecoder(jsonStr, false);
        Map<String, List<String>> map = decoder.parameters();
        request.release();

        TcpRequest tcpRequest = new TcpRequest(map.get("parameter").get(0));
        caClient.invokeAsync(tcpRequest, ctx);

//        executors.submit(new Runnable() {
//            @Override
//            public void run() {
//                try{
////                    Thread.sleep(50, random.nextInt(50));
////                    byte[] result = String.valueOf(map.get("parameter").get(0).hashCode()).getBytes();
////
//                    TcpRequest tcpRequest = new TcpRequest(map.get("interface").get(0), map.get("method").get(0),
//                            map.get("parameterTypesString").get(0), map.get("parameter").get(0));
//                    byte[] result = new String((byte[]) caClient.invoke(tcpRequest)).trim().getBytes();
//                    FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.wrappedBuffer(result));
//                    response.headers().set(CONTENT_TYPE, "text/html;charset=UTF-8");
//                    response.headers().set(CONTENT_LENGTH, result.length);
//                    if(HttpUtil.isKeepAlive(request)) {
//                        response.headers().set(CONNECTION, KEEP_ALIVE);
//                    }
//                    ctx.writeAndFlush(response);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        });

    }
}
