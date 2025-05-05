package com.zkm.forum.utils;

import cn.hutool.json.JSONArray;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import okhttp3.*;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Component
public class AiPictureUtils {
    private static final String HOST_URL = " ";
    private static final String appId = " ";
    private static final String apiSecret = " ";
    private static final String apiKey = " ";

    public String analyzeImage(String description, String pictureUrl) throws Exception {
        String authUrl = getAuthUrl(HOST_URL, apiKey, apiSecret);
        OkHttpClient client = new OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS).build();

        PictureWebSocketListener listener = new PictureWebSocketListener(description, pictureUrl);
        Request request = new Request.Builder().url(authUrl.replace("https://", "wss://")).build();

        client.newWebSocket(request, listener);
        return listener.getResult();
    }

    private class PictureWebSocketListener extends WebSocketListener {
        private final StringBuilder fullAnswer = new StringBuilder();
        private final String description;
        private final String pictureUrl;
        private final CompletableFuture<String> resultFuture = new CompletableFuture<>();
        private WebSocket webSocket;

        public PictureWebSocketListener(String description, String pictureUrl) {
            this.description = description;
            this.pictureUrl = pictureUrl;
        }

        public String getResult() throws Exception {
            return resultFuture.get(30, TimeUnit.SECONDS);
        }

        @Override
        public void onOpen(WebSocket webSocket, Response response) {
            this.webSocket = webSocket;
            try {
                JSONObject requestJson = buildRequestJson();
                webSocket.send(requestJson.toString());
            } catch (Exception e) {
                resultFuture.completeExceptionally(e);
            }
        }

        private JSONObject buildRequestJson() throws IOException {
            JSONObject requestJson = new JSONObject();

            // 构建header
            JSONObject header = new JSONObject();
            header.put("app_id", appId);
            header.put("uid", UUID.randomUUID().toString().substring(0, 10));

            // 构建parameter
            JSONObject parameter = new JSONObject();
            JSONObject chat = new JSONObject();
            chat.put("domain", "imagev3");
            chat.put("temperature", 0.2);
            chat.put("max_tokens", 4096);
            chat.put("auditing", "default");
            chat.put("top_k", 1);
            parameter.put("chat", chat);

            // 构建payload
            JSONObject payload = new JSONObject();
            JSONObject message = new JSONObject();
            JSONArray text = new JSONArray();

            // 添加图片
            RoleContent imageContent = new RoleContent();
            imageContent.role = "user";
            imageContent.content = Base64.getEncoder().encodeToString(readFile(pictureUrl));
            imageContent.content_type = "image";
            text.add(JSON.toJSON(imageContent));

            // 添加描述和问题
            RoleContent textContent = new RoleContent();
            textContent.role = "user";
            textContent.content = "【强制指令】作为营养分析AI，通过分析图片中的食物或者配料表按照格式返回结果,下方是是图片描述:\n"
                    + description + "\n"
                    + "按以下格式返回，注意【【【【把返回的结果分成了四组，仅包含数字和单位，不要任何额外文字：\n" +
                    "【【【【\n" +
                    "卡路里:[计算结果]卡路里\n" +
                    "【【【【\n" +
                    "蛋白质:[计算结果]克\n" +
                    "【【【【\n" +
                    "碳水化合物:[计算结果]克\n" +
                    "【【【【\n" +
                    "脂肪:[计算结果]克\n" +
                    "期望输出\n" +
                    "【【【【\n" +
                    "卡路里:658卡路里\n" +
                    "【【【【\n" +
                    "蛋白质:100克\n" +
                    "【【【【\n" +
                    "碳水化合物:200克\n" +
                    "【【【【\n" +
                    "脂肪:50克";
//            textContent.content="分析图片中的食物名字，直接回答，不需要解释";
            textContent.content_type = "text";
            text.add(JSON.toJSON(textContent));

            message.put("text", text);
            payload.put("message", message);

            requestJson.put("header", header);
            requestJson.put("parameter", parameter);
            requestJson.put("payload", payload);

            return requestJson;
        }

        @Override
        public void onMessage(WebSocket webSocket, String text) {
            try {
                JsonParse response = new Gson().fromJson(text, JsonParse.class);
                if (response.header.code != 0) {
                    resultFuture.completeExceptionally(new RuntimeException("AI服务错误: " + response.header.code));
                    webSocket.close(1000, "");
                    return;
                }

                // 拼接所有片段
                for (Text content : response.payload.choices.text) {
                    fullAnswer.append(content.content);
                }

                // 只有status=2时才最终完成
                if (response.header.status == 2) {
                    resultFuture.complete(fullAnswer.toString());
                    webSocket.close(1000, "");
                }
            } catch (Exception e) {
                resultFuture.completeExceptionally(e);
            }
        }

        @Override
        public void onFailure(WebSocket webSocket, Throwable t, Response response) {
            resultFuture.completeExceptionally(t);
        }

        private byte[] readFile(String path) throws IOException {
            if (path.startsWith("http://") || path.startsWith("https://")) {
                // 如果是远程 URL，调用下载方法
                return downloadImageFromUrl(path);
            } else {
                // 否则按本地文件路径处理
                return Files.readAllBytes(Paths.get(path));
            }
        }

        // 新增方法：从远程 URL 下载图片
        private byte[] downloadImageFromUrl(String imageUrl) throws IOException {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(imageUrl).build();
            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("Failed to download image from URL: " + imageUrl);
                }
                return Objects.requireNonNull(response.body()).bytes();
            }
        }
    }

    // 鉴权方法
    public static String getAuthUrl(String hostUrl, String apiKey, String apiSecret) throws Exception {
        URL url = new URL(hostUrl);
        // 时间
        SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
        String date = format.format(new Date());
        // 拼接
        String preStr = "host: " + url.getHost() + "\n" + "date: " + date + "\n" + "GET " + url.getPath() + " HTTP/1.1";
        // System.err.println(preStr);
        // SHA256加密
        Mac mac = Mac.getInstance("hmacsha256");
        SecretKeySpec spec = new SecretKeySpec(apiSecret.getBytes(StandardCharsets.UTF_8), "hmacsha256");
        mac.init(spec);

        byte[] hexDigits = mac.doFinal(preStr.getBytes(StandardCharsets.UTF_8));
        // Base64加密
        String sha = Base64.getEncoder().encodeToString(hexDigits);
        // System.err.println(sha);
        // 拼接
        String authorization = String.format("api_key=\"%s\", algorithm=\"%s\", headers=\"%s\", signature=\"%s\"", apiKey, "hmac-sha256", "host date request-line", sha);
        // 拼接地址
        HttpUrl httpUrl = Objects.requireNonNull(HttpUrl.parse("https://" + url.getHost() + url.getPath())).newBuilder().//
                addQueryParameter("authorization", Base64.getEncoder().encodeToString(authorization.getBytes(StandardCharsets.UTF_8))).//
                addQueryParameter("date", date).//
                addQueryParameter("host", url.getHost()).//
                build();

        // System.err.println(httpUrl.toString());
        return httpUrl.toString();
    }


    private static byte[] read(String filePath) throws IOException {
        InputStream in = new FileInputStream(filePath);
        byte[] data = inputStream2ByteArray(in);
        in.close();
        return data;
    }

    private static byte[] inputStream2ByteArray(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024 * 4];
        int n = 0;
        while ((n = in.read(buffer)) != -1) {
            out.write(buffer, 0, n);
        }
        return out.toByteArray();
    }

    //返回的json结果拆解
    class JsonParse {
        Header header;
        Payload payload;
    }

    class Header {
        int code;
        int status;
        String sid;
    }

    class Payload {
        Choices choices;
    }

    class Choices {
        List<Text> text;
    }

    class Text {
        String role;
        String content;
    }

    class RoleContent {
        String role;
        String content;

        String content_type;

        public String getContent_type() {
            return content_type;
        }

        public void setContent_type(String content_type) {
            this.content_type = content_type;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }
}