import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class UseInitiator {
	public static void main(String[] args) throws IOException, InterruptedException {
		
		UploadInitiatorURLConnection uploadInitiator = new UploadInitiatorURLConnection();
		OutputStream os = uploadInitiator.initiate("http://192.168.20.154:9999/webdav/100hellos.txt");
		ByteArrayOutputStream bos;
		for (int i = 0; i < 10000000; ++i) {
			// Thread.sleep(20);
			os.write("hello\n".getBytes());
			
		}
		os.close();
		return;
	}
}
