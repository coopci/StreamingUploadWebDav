package use.async;

import static org.asynchttpclient.Dsl.asyncHttpClient;
import static org.asynchttpclient.Dsl.post;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.Request;
import org.asynchttpclient.Response;
// 这个测试过，确实是streaming了。 上传1GB左右的数据量，jvm的heap使用 情况一直维持在几十MB。
public class AHCMain {
	public static void main(String[] args) throws InterruptedException, ExecutionException, IOException {
		File file = new File("file.txt");
		AsyncHttpClient asyncHttpClient = asyncHttpClient();
		Request request = post("http://localhost:9999/webdav/file.txt")
				.setMethod("PUT")
				.setHeader("Content-type", "text/plain")
				.setBody(new FileInputStream(file))
				.build();
		
		Future<Response> whenResponse = asyncHttpClient.executeRequest(request);
		Response resp = whenResponse.get();
		resp.getStatusCode();
		System.out.println(resp.getResponseBody());
		asyncHttpClient.close();
		return;
	}
}
