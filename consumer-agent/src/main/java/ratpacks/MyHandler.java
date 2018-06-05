package ratpacks; /**
 * Created by zjw on 2018/05/11 17:07
 * Description:
 */

import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ratpack.exec.Blocking;
import ratpack.exec.Promise;
import ratpack.handling.Handler;
import ratpack.handling.Context;
import ratpack.form.Form;
import registry.Endpoint;
import registry.EndpointUtil;


public class MyHandler implements Handler {

    private final Logger logger = LoggerFactory.getLogger(MyHandler.class);

    private EndpointUtil endpointUtil;
    private OkHttpClient httpClient;

    public MyHandler(EndpointUtil endpointUtil, OkHttpClient okHttpClient) {
        this.endpointUtil = endpointUtil;
        this.httpClient = okHttpClient;
    }

    public void handle(Context context) throws Exception {
        Promise<Form> form = context.parse(Form.class);

//        Endpoint endpoint = endpointUtil.getPoint();
//        String url = "http://" + endpoint.getHost() + ":" + endpoint.getPort();


//        form.then(f -> {
////            logger.info(url);
//            RequestBody requestBody = new FormBody.Builder()
//                    .add("interface", f.get("interface"))
//                    .add("method", f.get("method"))
//                    .add("parameterTypesString", f.get("parameterTypesString"))
//                    .add("parameter", f.get("parameter"))
//                    .build();
//
//            Request request = new Request.Builder()
//                    .url(url)
//                    .post(requestBody)
//                    .build();
//
//            Blocking.get(() -> httpClient.newCall(request).execute()).then(resp -> {
//                try {
//                    String result = resp.body().string().trim();
//                    context.getResponse().status(200).send(result);
//                } finally {
//                    resp.close();
//                }
//            });
//        });


//        Blocking.get(()-> {
//            form.then();
//        }).then(resp -> {
//            try {
//                String result = resp.body().string().trim();
//                context.getResponse().status(200).send(result);
//            } finally {
//                resp.close();
//            }
//        };

        Blocking.get(() -> {
            Thread.sleep(50);
            return "";
        }).then(s -> form.then(f -> context.getResponse().status(200).send(f.get("parameter").hashCode() + "")));

    }
}
