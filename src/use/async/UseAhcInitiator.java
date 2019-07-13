package use.async;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class UseAhcInitiator {
	public static void main(String[] args) throws IOException, InterruptedException {
		
		UploadInitiatorAHC uploadInitiator = new UploadInitiatorAHC();
		// OutputStream os = uploadInitiator.initiate("http://192.168.20.154:9999/webdav/100hellos.txt");
		OutputStream os = uploadInitiator.initiate("http://localhost/webdav/100hellos.txt");
		ByteArrayOutputStream bos;
		for (int i = 0; i < 100; ++i) {
			Thread.sleep(20);
			os.write(("hello" + i + "\n").getBytes());
			
		}
		os.close();
		uploadInitiator.stop();
		return;
	}
}
