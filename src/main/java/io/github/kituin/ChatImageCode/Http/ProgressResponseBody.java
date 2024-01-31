package io.github.kituin.ChatImageCode.Http;

import io.github.kituin.ChatImageCode.Http.IProgressListener;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.*;

import java.io.IOException;

public class ProgressResponseBody extends ResponseBody {

    private final ResponseBody responseBody;
    private final IProgressListener progressListener;
    private BufferedSource bufferedSource;

    public ProgressResponseBody(ResponseBody responseBody, IProgressListener progressListener){
        this.responseBody = responseBody;
        this.progressListener = progressListener;
    }

    @Override
    public MediaType contentType() {
        return responseBody.contentType();
    }

    @Override
    public long contentLength() {
        return responseBody.contentLength();
    }


    @Override
    public BufferedSource source() {
        if (bufferedSource == null){
            bufferedSource = Okio.buffer(createSource(responseBody.source()));
        }
        return bufferedSource;
    }

    private Source createSource(Source source){
        return new ForwardingSource(source) {
            long totalBytesRead = 0L;
            @Override
            public long read(Buffer sink, long byteCount) throws IOException {
                long bytesRead = super.read(sink,byteCount);
                //不断统计当前下载好的数据
                totalBytesRead += bytesRead != -1 ? bytesRead : 0;
                //接口回调
                progressListener.update(totalBytesRead,responseBody.contentLength(),bytesRead == -1);
                return bytesRead;
            }
        };
    }
}