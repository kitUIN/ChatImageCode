package io.github.kituin.ChatImageCode;

import io.github.kituin.ChatImageCode.Http.IProgressListener;
import io.github.kituin.ChatImageCode.Http.ProgressResponseBody;
import okhttp3.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import static io.github.kituin.ChatImageCode.ChatImageCodeInstance.CLIENT_ADAPTER;
import static io.github.kituin.ChatImageCode.ChatImageCodeInstance.LOGGER;
import static io.github.kituin.ChatImageCode.ClientStorage.AddImageError;
import static io.github.kituin.ChatImageCode.ClientStorage.URL_PROGRESS;

/**
 * @author kitUIN
 */
public class HttpImageHandler {

    public static HashMap<String, Integer> HTTPS_MAP = new HashMap<>();

    public static boolean request(String url) {
        Request getRequest;
        try {
            getRequest = new Request.Builder()
                    .url(url)
                    .addHeader("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0 Safari/537.36 Edg/121.0.0.0")
                    .get()
                    .build();
        } catch (IllegalArgumentException ep) {
            return false;
        }
        if (HTTPS_MAP.containsKey(url) && HTTPS_MAP.get(url) == 1) {
            return true;
        } else {
            HTTPS_MAP.put(url, 1);
        }
        LOGGER.debug("[HttpImageHandler][GET] {}", url);
        final IProgressListener listener = (bytesRead, contentLength, done) -> {
            final int percent = (int) (100 * bytesRead / contentLength); // 百分比
            // LOGGER.debug(String.valueOf(percent));
            if(percent >= 0)
                URL_PROGRESS.put(url, percent);
        };
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(CLIENT_ADAPTER.getTimeOut(), TimeUnit.SECONDS) // java.net.SocketTimeoutException
                .readTimeout(CLIENT_ADAPTER.getTimeOut(), TimeUnit.SECONDS)
                .callTimeout(CLIENT_ADAPTER.getTimeOut(), TimeUnit.SECONDS)
                .addNetworkInterceptor(chain -> {
                    // 进度监听器
                    Response response = chain.proceed(chain.request());
                    return response.newBuilder()
                            .body(new ProgressResponseBody(response.body(), listener))
                            .build();
                })
                .addInterceptor(chain -> {
                    // 大小监听器
                    Response response = chain.proceed(chain.request());
                    if (response.body() != null && response.body().contentLength() > CLIENT_ADAPTER.getMaxFileSize() * 1000L) {
                        throw new IOException("File size is too large");
                    }
                    return response;
                })
                .build();
        client.newCall(getRequest).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                HTTPS_MAP.put(url, 2);
                LOGGER.error("{} Error-> {}",url,e);
                AddImageError(url, ChatImageFrame.FrameError.TIMEOUT);
            }

            @Override
            public void onResponse(Call call, Response response) {
                ResponseBody body = response.body();
                if (body != null) {
                    try {
                        FileImageHandler.loadFile(body.bytes(), url);
                    } catch (IOException ignored) {

                    }
                }
                HTTPS_MAP.put(url, 2);
            }
        });
        return true;

    }



}

