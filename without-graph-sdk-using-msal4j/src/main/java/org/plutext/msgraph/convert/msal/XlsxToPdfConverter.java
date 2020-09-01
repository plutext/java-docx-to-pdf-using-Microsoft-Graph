package org.plutext.msgraph.convert.msal;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.plutext.msgraph.convert.AuthConfig;
import org.plutext.msgraph.convert.ConversionException;

public class XlsxToPdfConverter extends PdfConverter implements org.plutext.msgraph.convert.XlsxToPdfConverter {

	public XlsxToPdfConverter(AuthConfig authConfig) {
		super(authConfig);
	}

	private static final String XLSX_MIME_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
	
	
	@Override
	public byte[] convert(byte[] xlsx) throws ConversionException {
		return convertMime(xlsx, XLSX_MIME_TYPE);
	}

	@Override
	public byte[] convert(byte[] bytes, String ext) throws ConversionException {
		return convertMime(bytes, XLSX_MIME_TYPE);
		
	}
	
 
	public byte[] convert(File inFile) throws ConversionException, IOException {

		return convertMime(inFile, XLSX_MIME_TYPE);
	}

	@Override
	public byte[] convert(InputStream xlsx) throws ConversionException, IOException {
		return convert( IOUtils.toByteArray(xlsx) );
	}

	@Override
	public byte[] convert(InputStream is, String ext) throws ConversionException, IOException {
		return convert( IOUtils.toByteArray(is)  );
	}
	
}
