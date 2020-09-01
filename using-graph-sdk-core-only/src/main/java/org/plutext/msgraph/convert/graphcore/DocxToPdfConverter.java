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

	private static final String DOCX_MEDIA_TYPE = "application/vnd.openxmlformats-officedocument.wordprocessingml.document; charset=utf-8"; 

	@Override
	public byte[] convert(InputStream docx) throws ConversionException, IOException {
		return convert(docx, ".docx");
	}
	
	public byte[] convert(File inFile) throws ConversionException, IOException {
	
		// TODO: do we care if its a docm, dotx etc?
//	String filename = inFile.getName();
//	String ext = filename.substring(filename.lastIndexOf("."));
			
	MediaType mt = MediaType.parse(DOCX_MEDIA_TYPE);
	// can create RequestBody from byte[] or FIle
	RequestBody body = RequestBody.create(mt, inFile);
	return super.convert(body);
	
}

	@Override
	public byte[] convert(byte[] docx) throws ConversionException {
		return convert(docx, null);  // don't care about extension
	}

	@Override
	public byte[] convert(byte[] docx, String ext) throws ConversionException { 

		MediaType mt = MediaType.parse(DOCX_MEDIA_TYPE);
		// TODO: do we care if its a docm, dotx etc?
		
		RequestBody body = RequestBody.create(mt, docx);
		try {
			return convert(body);
		} catch (ConversionException e) {
			throw e;
		} catch (IOException e) {
			throw new ConversionException(e.getMessage(), e);
		}
		
}
	

}
