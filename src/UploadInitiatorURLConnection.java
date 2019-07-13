import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;

import sun.net.www.http.PosterOutputStream;

/**
 * PosterOutputStream 并不会真得向服务器streaming。它会存到内存里。
 * 
 **/
public class UploadInitiatorURLConnection {

	public static class WebDavUploadStream extends OutputStream {
		HttpURLConnection htconn;
		public WebDavUploadStream(HttpURLConnection htconn) {
			try {
				OutputStream os = htconn.getOutputStream();
				PosterOutputStream pos = (PosterOutputStream) os;
				this.htconn = htconn;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		@Override
		public void write(int b) throws IOException {
			this.htconn.getOutputStream().write(b);
		}
		
		@Override
		public void close() throws IOException {
			this.htconn.getOutputStream().close();
			int code = htconn.getResponseCode();
			String msg = htconn.getResponseMessage();
			System.out.println(msg);
		}
		
		@Override
		public void flush() throws IOException {
			this.htconn.getOutputStream().flush();
		}
	}
	OutputStream initiate(String uri) throws IOException {
		
		URL url = new URL(uri);
		URLConnection urlconnection = url.openConnection();
		urlconnection.setDoOutput(true);
		urlconnection.setDoInput(true);

		if (urlconnection instanceof HttpURLConnection) {
			HttpURLConnection htconn = (HttpURLConnection)urlconnection;
			htconn.setRequestMethod("PUT");
			htconn.setRequestProperty("Content-type", "text/plain");
			htconn.connect();
			return new WebDavUploadStream(htconn);
		}
		throw new ProtocolException("Only http(s) is supported.");
	}
}
