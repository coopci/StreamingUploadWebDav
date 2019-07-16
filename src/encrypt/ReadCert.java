package encrypt;

import org.bouncycastle.bcpg.ArmoredOutputStream;
import org.bouncycastle.openpgp.PGPCompressedDataGenerator;
import org.bouncycastle.openpgp.PGPEncryptedData;
import org.bouncycastle.openpgp.PGPEncryptedDataGenerator;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPLiteralDataGenerator;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.PGPPublicKeyRingCollection;
import org.bouncycastle.openpgp.PGPUtil;
import org.bouncycastle.openpgp.bc.BcPGPPublicKeyRingCollection;
import org.bouncycastle.openpgp.operator.PGPDataEncryptorBuilder;
import org.bouncycastle.openpgp.operator.PGPKeyEncryptionMethodGenerator;
import org.bouncycastle.openpgp.operator.bc.BcPGPDataEncryptorBuilder;
import org.bouncycastle.openpgp.operator.bc.BcPublicKeyKeyEncryptionMethodGenerator;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Iterator;

public class ReadCert {
    
    public static PGPPublicKey loadFirst(String path) throws FileNotFoundException, IOException, PGPException {
        PGPPublicKeyRingCollection pgpPub = new BcPGPPublicKeyRingCollection(
                PGPUtil.getDecoderStream(new FileInputStream(new File("keys/pubkey.asc"))));
        
        System.out.println(pgpPub.size());
        
        Iterator keyRingIter = pgpPub.getKeyRings();
        while (keyRingIter.hasNext()) {
            PGPPublicKeyRing keyRing = (PGPPublicKeyRing) keyRingIter.next();
            System.out.println("Found one keyRing.");
            Iterator keyIter = keyRing.getPublicKeys();
           
            while (keyIter.hasNext()) {
                PGPPublicKey key = (PGPPublicKey) keyIter.next();

                if (key.isEncryptionKey()) {
                    return key;
                }
            }
        }
        return null;
    }
    
    public static void iterateAll(String path ) throws FileNotFoundException, IOException, PGPException {
        PGPPublicKeyRingCollection pgpPub = new BcPGPPublicKeyRingCollection(
                PGPUtil.getDecoderStream(new FileInputStream(new File(path))));
        
        System.out.println(pgpPub.size());
        
        Iterator keyRingIter = pgpPub.getKeyRings();
        while (keyRingIter.hasNext()) {
            PGPPublicKeyRing keyRing = (PGPPublicKeyRing) keyRingIter.next();
            System.out.println("Found one keyRing.");
            Iterator keyIter = keyRing.getPublicKeys();
           
            while (keyIter.hasNext()) {
                PGPPublicKey key = (PGPPublicKey) keyIter.next();

                if (key.isEncryptionKey()) {
                    System.out.println("Found one key.");
                }
            }
        }
    }
    // gpg -e -a -r bgu@zuora.com hello.txt
    public static void encrypt(PGPPublicKey pubKey, OutputStream paramOutputStream, InputStream ins, boolean withIntegrityPacket) throws IOException, PGPException {
        PGPDataEncryptorBuilder pgpDataEncryptorBuilder = new BcPGPDataEncryptorBuilder(PGPEncryptedData.CAST5)
                .setWithIntegrityPacket(withIntegrityPacket);

        paramOutputStream = new ArmoredOutputStream(paramOutputStream);
        PGPKeyEncryptionMethodGenerator pgpKeyEncryptionMethodGenerator = new BcPublicKeyKeyEncryptionMethodGenerator(pubKey);

        PGPEncryptedDataGenerator localPGPEncryptedDataGenerator = new PGPEncryptedDataGenerator(pgpDataEncryptorBuilder);
        localPGPEncryptedDataGenerator.addMethod(pgpKeyEncryptionMethodGenerator);
        OutputStream encOutputStream = localPGPEncryptedDataGenerator.open(paramOutputStream, new byte[65536]);
        PGPCompressedDataGenerator localPGPCompressedDataGenerator = new PGPCompressedDataGenerator(1);
        OutputStream encCompressOutputStream = localPGPCompressedDataGenerator.open(encOutputStream);
        
        PGPLiteralDataGenerator lData = new PGPLiteralDataGenerator();
        
        //OutputStream literalOutputStream = lData.open(encOutputStream, 'b', "filename1", new Date(), new byte[65536]);
        
        OutputStream literalOutputStream = lData.open(encCompressOutputStream, 'b', "filename1", new Date(), new byte[65536]);
        
        
        byte[] buf = new byte[65536];

        int len;
        while ((len = ins.read(buf)) > 0)
        {
            literalOutputStream.write(buf, 0, len);
        }
        literalOutputStream.close();
        encCompressOutputStream.close();
        encOutputStream.close();
        paramOutputStream.close();
        
    }
    public static void main(String[] args) throws IOException, InterruptedException, PGPException {
    
        iterateAll("keys/pubkey.asc");
        PGPPublicKey key = loadFirst("keys/pubkey.asc");
        
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ByteArrayInputStream bis = new ByteArrayInputStream("hello".getBytes());
        encrypt(key, bos, bis , true);
        // 验证了 可以用 gpg -d -a -r bgu@zuora.com ciper.fromjava.txt 解密。
        System.out.println(new String(bos.toByteArray()));
        return;
    }
}
