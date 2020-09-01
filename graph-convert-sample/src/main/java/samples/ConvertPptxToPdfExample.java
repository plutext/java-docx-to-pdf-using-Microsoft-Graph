package samples;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.plutext.msgraph.convert.ConversionException;
import org.plutext.msgraph.convert.PptxToPdfConverter;
import org.plutext.msgraph.convert.graphsdk.PptxToPdfConverterLarge;

//import com.github.scribejava.httpclient.apache.ApacheHttpClient;

public class ConvertPptxToPdfExample {

	public static void main(String[] args) throws IOException, ConversionException {

		File inFile = new File(System.getProperty("user.dir")
				+ "/sample-docs/test.pptx");
		
		// Choose your converter implementation here, corresponding to the module you've uncommented in your pom
		PptxToPdfConverter converter = new PptxToPdfConverterLarge(new AuthConfigImpl());
		
		// If you choose without-graph-sdk-using-scribe, you can optionally specify your preferred httpclient
		// (uncommented in your pom)
//        httpClient = new ApacheHttpClient();
//		PptxToPdfConverter converter = new PptxToPdfConverterLarge(new AuthConfigImpl(), httpClient);

		byte[] pdfBytes = converter.convert(inFile);
		
		//System.out.println(new String(pdfBytes));
		String sniffed = new String(pdfBytes, 0, 8);  // PDF?
		if (sniffed.startsWith("%PDF")) {
			System.out.println("PDF containing " + pdfBytes.length + " bytes");				
		} else {
			System.out.println("Not a PDF? " + sniffed );								
		}
		
		        
        File file = new File(System.getProperty("user.dir")
				+ "/out.pdf");

        FileUtils.writeByteArrayToFile(file, pdfBytes);
        System.out.println("saved " + file.getName());
		
	}

}
