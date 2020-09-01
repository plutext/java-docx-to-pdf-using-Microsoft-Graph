package org.plutext.msgraph.convert.graphsdk;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.plutext.msgraph.convert.AuthConfig;
import org.plutext.msgraph.convert.ConversionException;
import org.plutext.msgraph.convert.XlsxToPdfConverter;

public class XlsxToPdfConverterLarge extends PdfConverterLarge implements XlsxToPdfConverter {

	public XlsxToPdfConverterLarge(AuthConfig authConfig) {
		super(authConfig);
	}

	@Override
	public byte[] convert(byte[] xlsx) throws ConversionException {
		return convert(xlsx, ".xlsx");
	}

	@Override
	public byte[] convert(InputStream xlsx) throws ConversionException, IOException {
		return convert(xlsx, ".xlsx");
	}
	
	public byte[] convert(File xlsx) throws ConversionException, IOException {
		return super.convert(xlsx);
	}
	

}
