package use.async;

import static org.asynchttpclient.Dsl.asyncHttpClient;
import static org.asynchttpclient.Dsl.post;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.Request;
import org.asynchttpclient.Response;
import org.bouncycastle.bcpg.ArmoredOutputStream;
import org.bouncycastle.openpgp.PGPCompressedDataGenerator;
import org.bouncycastle.openpgp.PGPEncryptedData;
import org.bouncycastle.openpgp.PGPEncryptedDataGenerator;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPLiteralDataGenerator;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.operator.PGPDataEncryptorBuilder;
import org.bouncycastle.openpgp.operator.PGPKeyEncryptionMethodGenerator;
import org.bouncycastle.openpgp.operator.bc.BcPGPDataEncryptorBuilder;
import org.bouncycastle.openpgp.operator.bc.BcPublicKeyKeyEncryptionMethodGenerator;

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
	
	
	public static class PGPAhcUploadStream extends OutputStream {
        PipedOutputStream pipedOutputStream;
        
        OutputStream literalOutputStream;
        OutputStream encCompressOutputStream=null;
        OutputStream encOutputStream ;
        ArmoredOutputStream armoredOutputStream;
        Future<Response> whenResponse;
        public PGPAhcUploadStream(PipedOutputStream delegate, PGPPublicKey pubKey, Future<Response> whenResponse) throws IOException, PGPException {
            this.pipedOutputStream = delegate;
            this.whenResponse = whenResponse;
            boolean withIntegrityPacket = true;
            PGPDataEncryptorBuilder pgpDataEncryptorBuilder = new BcPGPDataEncryptorBuilder(PGPEncryptedData.CAST5)
                    .setWithIntegrityPacket(withIntegrityPacket);

            this.armoredOutputStream = new ArmoredOutputStream(this.pipedOutputStream);
            PGPKeyEncryptionMethodGenerator pgpKeyEncryptionMethodGenerator = new BcPublicKeyKeyEncryptionMethodGenerator(pubKey);

            PGPEncryptedDataGenerator localPGPEncryptedDataGenerator = new PGPEncryptedDataGenerator(pgpDataEncryptorBuilder);
            localPGPEncryptedDataGenerator.addMethod(pgpKeyEncryptionMethodGenerator);
            this.encOutputStream = localPGPEncryptedDataGenerator.open(armoredOutputStream, new byte[65536]);
            PGPCompressedDataGenerator localPGPCompressedDataGenerator = new PGPCompressedDataGenerator(1);
            this.encCompressOutputStream = localPGPCompressedDataGenerator.open(encOutputStream);
            
            PGPLiteralDataGenerator lData = new PGPLiteralDataGenerator();
            
            //OutputStream literalOutputStream = lData.open(encOutputStream, 'b', "filename1", new Date(), new byte[65536]);
            
            this.literalOutputStream = lData.open(
                    encCompressOutputStream!=null?encCompressOutputStream:this.encOutputStream
                            , 'b', "filename1", new Date(), new byte[65536]);
            
        }

        @Override
        public void write(int b) throws IOException {
           this.literalOutputStream.write(b);
           
           
        }

        @Override
        public void close() throws IOException {
            this.literalOutputStream.close();
            if(this.encCompressOutputStream!=null) {
                this.encCompressOutputStream.close();
            }
            
            this.encOutputStream.close();
            this.armoredOutputStream.close();
            this.pipedOutputStream.close();
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
            this.literalOutputStream.flush();
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
	
	OutputStream initiateEncrypted(String uri, PGPPublicKey key) throws IOException, PGPException {
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
        return new PGPAhcUploadStream(out, key, whenResponse);
    }
}
