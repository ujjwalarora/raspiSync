package com.myapps.raspisync;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class decrypt {
    private static byte[] S;
    private static byte[] T;
    private static int keylen;

    public static void init(final byte[] key) {
    	S = new byte[256];
    	T = new byte[256];
        keylen = key.length;
		for (int i = 0; i < 256; i++) {
			S[i] = (byte) i;
			T[i] = key[i % keylen];
		}
		int j = 0;
		for (int i = 0; i < 256; i++) {
			j = (j + S[i] + T[i]) & 0xFF;
			S[i] ^= S[j];
			S[j] ^= S[i];
			S[i] ^= S[j];
		}
    }

    public static byte[] crypt(final byte[] inputText) {
        final byte[] ciphertext = new byte[inputText.length];
        int i = 0, j = 0, k, t;
        for (int counter = 0; counter < inputText.length; counter++) {
            i = (i + 1) & 0xFF;
            j = (j + S[i]) & 0xFF;
            S[i] ^= S[j];
            S[j] ^= S[i];
            S[i] ^= S[j];
            t = (S[i] + S[j]) & 0xFF;
            k = S[t];
            ciphertext[counter] = (byte) (inputText[counter] ^ k);
        }
        return ciphertext;
    }
    
    public static void process(String keyPath, String inputPath, String outputPath) throws Exception {
    	byte[] keyBytes, inputBytes, outputBytes;
    	
    	// Key
    	File key = new File(keyPath);
    	keyBytes = new byte[(int)key.length()];
    	DataInputStream dis = new DataInputStream(new FileInputStream(key));
    	dis.readFully(keyBytes);
    	dis.close();
	    
    	// Input
    	File input = new File(inputPath);
    	inputBytes = new byte[(int)input.length()];
    	dis = new DataInputStream(new FileInputStream(input));
    	dis.readFully(inputBytes);
    	dis.close();
	    
	    // Output
	    init(keyBytes);
    	File output = new File(outputPath);
    	DataOutputStream dos = new DataOutputStream(new FileOutputStream(output));
    	outputBytes = crypt(inputBytes);
    	dos.write(outputBytes, 0, outputBytes.length);
    	dos.close();
	}
    

}
