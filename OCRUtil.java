package com.ck.ocr;

import java.io.UnsupportedEncodingException;

import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.lept;
import org.bytedeco.javacpp.lept.PIX;
import org.bytedeco.javacpp.tesseract.TessBaseAPI;

public class OCRUtil {

	public String process(String file) {
        TessBaseAPI api = new TessBaseAPI();
        if (api.Init("C:\\APPS\\tesseract", "eng") != 0) {
            throw new RuntimeException("Could not initialize tesseract.");
        }       
    
        PIX image = null;
        BytePointer outText = null;
        try {
            image = lept.pixRead(file);
            api.SetImage(image);
            outText = api.GetUTF8Text();
            String string = outText.getString("UTF-8");
            System.out.println(outText);
            System.out.println(string);
            if (string != null) {
                string = string.trim();
            }
            return string;
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("charset", e);
        } finally {
            if (outText != null) {
                outText.deallocate();
            }
            if (image != null) {
                lept.pixDestroy(image);
            }
            if (api != null) {
                api.End();
            }
        }
    }
	
	public static void main(String[] args){
    	OCRUtil util = new OCRUtil();
    	util.process("C:\\APPS\\tesseract\\difficult - Copy.jpg");
    }
}
