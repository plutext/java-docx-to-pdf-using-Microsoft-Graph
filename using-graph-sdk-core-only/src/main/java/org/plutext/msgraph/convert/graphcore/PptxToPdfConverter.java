package org.plutext.msgraph.convert.graphcore;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.RequestBody;


import org.plutext.msgraph.convert.AuthConfig;
import org.plutext.msgraph.convert.ConversionException;

public class PptxToPdfConverter extends PdfConverterCore implements org.plutext.msgraph.convert.PptxToPdfConverter {

	
	public PptxToPdfConverter(AuthConfig authConfig) {
		super(authConfig);
	}


	@Override
	public byte[] convert(InputStream pptx) throws ConversionException, IOException {
		return convert(pptx, ".pptx");
	}
	
	public byte[] convert(File inFile) throws ConversionException, IOException {
	
	String filename = inFile.getName();
	String ext = filename.substring(filename.lastIndexOf("."));
			
	MediaType mt = MediaType.parse(PPTX_MEDIA_TYPE);
	// can create RequestBody from byte[] or FIle
	RequestBody body = RequestBody.create(mt, inFile);
	return super.convert(body, ext);
	
}

	@Override
	public byte[] convert(byte[] pptx) throws ConversionException {
		return convert(pptx, ".pptx");  
	}

	@Override
	public byte[] convert(byte[] pptx, String ext) throws ConversionException { 

		MediaType mt = MediaType.parse(PPTX_MEDIA_TYPE);
		// TODO: do we care if its macro-enabled or a template etc?
		
		RequestBody body = RequestBody.create(mt, pptx);
		try {
			return convert(body, ext);
		} catch (ConversionException e) {
			throw e;
		} catch (IOException e) {
			throw new ConversionException(e.getMessage(), e);
		}
		
}
	

}
