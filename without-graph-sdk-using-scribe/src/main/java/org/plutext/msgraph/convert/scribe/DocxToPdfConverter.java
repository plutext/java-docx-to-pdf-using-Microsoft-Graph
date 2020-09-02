package org.plutext.msgraph.convert.scribe;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.plutext.msgraph.convert.AbstractOpenXmlToPDF;
import org.plutext.msgraph.convert.AuthConfig;
import org.plutext.msgraph.convert.ConversionException;

import com.github.scribejava.core.httpclient.HttpClient;

public class DocxToPdfConverter extends PdfConverter implements org.plutext.msgraph.convert.DocxToPdfConverter {

	public DocxToPdfConverter(AuthConfig authConfig) throws ConversionException {
		super(authConfig);
	}
	
	public DocxToPdfConverter(AuthConfig authConfig, HttpClient httpClient) throws ConversionException {
		super(authConfig, httpClient);
	}
	
	
	@Override
	public byte[] convert(byte[] docx) throws ConversionException {
		return convertMime(docx, DOCX_MIME_TYPE);
	}

	@Override
	public byte[] convert(byte[] bytes, String ext) throws ConversionException {
		return convertMime(bytes, DOCX_MIME_TYPE);
		
	}
	
 
	public byte[] convert(File inFile) throws ConversionException, IOException {

		return convertMime(inFile, DOCX_MIME_TYPE);
	}

	@Override
	public byte[] convert(InputStream docx) throws ConversionException, IOException {
		return convert( IOUtils.toByteArray(docx) );
	}

	@Override
	public byte[] convert(InputStream is, String ext) throws ConversionException, IOException {
		return convert( IOUtils.toByteArray(is)  );
	}
	
}
