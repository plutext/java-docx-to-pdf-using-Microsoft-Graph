package org.plutext.msgraph.convert.graphcore;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.RequestBody;


import org.plutext.msgraph.convert.AuthConfig;
import org.plutext.msgraph.convert.ConversionException;

public class DocxToPdfConverter extends PdfConverterCore implements org.plutext.msgraph.convert.DocxToPdfConverter {

	public DocxToPdfConverter(AuthConfig authConfig) {
		super(authConfig);
	}


	@Override
	public byte[] convert(InputStream docx) throws ConversionException, IOException {
		return convert(docx, ".docx");
	}
	
	public byte[] convert(File inFile) throws ConversionException, IOException {
	
		String filename = inFile.getName();
		String ext = filename.substring(filename.lastIndexOf("."));
				
		MediaType mt = MediaType.parse(DOCX_MEDIA_TYPE);
		// can create RequestBody from byte[] or FIle
		RequestBody body = RequestBody.create(mt, inFile);
		return super.convert(body, ext);
		
	}

	@Override
	public byte[] convert(byte[] docx) throws ConversionException {
		return convert(docx, ".docx");  
	}

	@Override
	public byte[] convert(byte[] docx, String ext) throws ConversionException { 

		MediaType mt = MediaType.parse(DOCX_MEDIA_TYPE);
		
		RequestBody body = RequestBody.create(mt, docx);
		try {
			return convert(body, ext);
		} catch (ConversionException e) {
			throw e;
		} catch (IOException e) {
			throw new ConversionException(e.getMessage(), e);
		}
		
}
	

}
