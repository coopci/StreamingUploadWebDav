import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

public class Main {
	public static void main(String[] args) {
		URLConnection urlconnection = null;
		try {
			File file = new File("file.txt");
			URL url = new URL("http://localhost:9999/webdav/file.txt");
			urlconnection = url.openConnection();
			urlconnection.setDoOutput(true);
			urlconnection.setDoInput(true);

			if (urlconnection instanceof HttpURLConnection) {
				((HttpURLConnection) urlconnection).setRequestMethod("PUT");
				((HttpURLConnection) urlconnection).setRequestProperty("Content-type", "text/plain");
				((HttpURLConnection) urlconnection).connect();
			}

			BufferedOutputStream bos = new BufferedOutputStream(urlconnection.getOutputStream());
			BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
			int i;
			// read byte by byte until end of stream
			while ((i = bis.read()) > 0) {
				bos.write(i);
			}
			bis.close();
			bos.close();
			System.out.println(((HttpURLConnection) urlconnection).getResponseMessage());
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {

			InputStream inputStream;
			int responseCode = ((HttpURLConnection) urlconnection).getResponseCode();
			if ((responseCode >= 200) && (responseCode <= 202)) {
				inputStream = ((HttpURLConnection) urlconnection).getInputStream();
				int j;
				while ((j = inputStream.read()) > 0) {
					System.out.println(j);
				}

			} else {
				inputStream = ((HttpURLConnection) urlconnection).getErrorStream();
			}
			((HttpURLConnection) urlconnection).disconnect();

		} catch (IOException e) {
			e.printStackTrace();
		}
}
}
