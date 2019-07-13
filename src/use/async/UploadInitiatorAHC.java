package use.async;

import static org.asynchttpclient.Dsl.asyncHttpClient;
import static org.asynchttpclient.Dsl.post;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.Request;
import org.asynchttpclient.Response;

public class UploadInitiatorAHC {
	public static class AhcUploadStream extends OutputStream {
		PipedOutputStream delegate;
		Future<Response> whenResponse;
		public AhcUploadStream(PipedOutputStream delegate, Future<Response> whenResponse) {
			this.delegate = delegate;
			this.whenResponse = whenResponse;

		}

		@Override
		public void write(int b) throws IOException {
			this.delegate.write(b);
		}

		@Override
		public void close() throws IOException {
			this.delegate.close();
			Response resp;
			try {
				resp = whenResponse.get();
				resp.getStatusCode();
				System.out.println("resp.getStatusCode():" + resp.getStatusCode());
				System.out.println(resp.getResponseBody());
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}

		@Override
		public void flush() throws IOException {
			this.delegate.flush();
		}
	}
	AsyncHttpClient asyncHttpClient = asyncHttpClient();
	
	public void stop() throws IOException {
		asyncHttpClient.close();
	}
	OutputStream initiate(String uri) throws IOException {
		PipedOutputStream out = new PipedOutputStream();
		PipedInputStream ins = new PipedInputStream(out);
		
		Request request = post(uri)
				.setMethod("PUT")
				.setHeader("Content-type", "text/plain")
				.setRequestTimeout(1000 * 600)
				.setReadTimeout(1000 * 600)
				.setBody(ins).build();
		Future<Response> whenResponse = asyncHttpClient.executeRequest(request);
		// Response resp = whenResponse.get();
		return new AhcUploadStream(out, whenResponse);
	}
}
