package use.async;
import encrypt.ReadCert;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPublicKey;

import java.io.IOException;
import java.io.OutputStream;

public class UseAhcInitiator {
	public static void main(String[] args) throws IOException, InterruptedException, PGPException {
		
		UploadInitiatorAHC uploadInitiator = new UploadInitiatorAHC();
		// OutputStream os = uploadInitiator.initiate("http://192.168.20.154:9999/webdav/100hellos.txt");
		int n = 400000000;
		
		PGPPublicKey pubkey = ReadCert.loadFirst("keys/pubkey.asc");
		// OutputStream os = uploadInitiator.initiate("http://localhost/webdav/" + n + "hellos.txt");
		// 用gpg -d -a -r bgu@zuora.com /usr/local/opt/httpd/webdav/12hellos.txt  验证了解密成功。
		// 用jconsole验证了java heap的占用有上限，远没有达到最后上传的文件的大小。上传了压缩后1.2GB的文件，java heap占用最高在170MB左右。
		OutputStream os = uploadInitiator.initiateEncrypted("http://localhost/webdav/" + n + "hellos.txt", pubkey);
		long before = System.currentTimeMillis();
		for (int i = 0; i < n; ++i) {
		    if (n < 200) {
                long now = System.currentTimeMillis();
                System.out.println("Writing for i = " + i + " milliseconds:" + (now - before));
            }
			os.write(("hello" + i + "\n").getBytes());
			
			//Thread.sleep(5010);
		}
		os.close();
		long now = System.currentTimeMillis();
        System.out.println("Total time elapsed = " + (now - before));
    
		uploadInitiator.stop();
		return;
	}
}
