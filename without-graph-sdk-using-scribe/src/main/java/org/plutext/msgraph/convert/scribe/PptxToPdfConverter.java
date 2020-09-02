package org.plutext.msgraph.convert.scribe;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.plutext.msgraph.convert.AuthConfig;
import org.plutext.msgraph.convert.ConversionException;

import com.github.scribejava.core.httpclient.HttpClient;

public class PptxToPdfConverter extends PdfConverter implements org.plutext.msgraph.convert.PptxToPdfConverter {

	public PptxToPdfConverter(AuthConfig authConfig) throws ConversionException {
		super(authConfig);
	}

	public PptxToPdfConverter(AuthConfig authConfig, HttpClient httpClient) throws ConversionException {
		super(authConfig, httpClient);
	}
	
	
	@Override
	public byte[] convert(byte[] pptx) throws ConversionException {
		return convertMime(pptx, PPTX_MIME_TYPE);
	}

	@Override
	public byte[] convert(byte[] bytes, String ext) throws ConversionException {
		return convertMime(bytes, PPTX_MIME_TYPE);
		
	}
	
 
	public byte[] convert(File inFile) throws ConversionException, IOException {

		return convertMime(inFile, PPTX_MIME_TYPE);
	}

	@Override
	public byte[] convert(InputStream pptx) throws ConversionException, IOException {
		return convert( IOUtils.toByteArray(pptx) );
	}

	@Override
	public byte[] convert(InputStream is, String ext) throws ConversionException, IOException {
		return convert( IOUtils.toByteArray(is)  );
	}
	
}
