package org.plutext.msgraph.convert.graphsdk;

import java.io.IOException;
import java.io.InputStream;

import org.plutext.msgraph.convert.AuthConfig;
import org.plutext.msgraph.convert.ConversionException;
import org.plutext.msgraph.convert.DocxToPdfConverter;

public class Limited4MBDocxToPDF extends Limited4MB implements DocxToPdfConverter {

	public Limited4MBDocxToPDF(AuthConfig authConfig) {
		super(authConfig);
	}

	@Override
	public byte[] convert(byte[] docx) throws ConversionException {
		return convert(docx, ".docx");
	}

	@Override
	public byte[] convert(InputStream docx) throws ConversionException, IOException {
		return convert(docx, ".docx");
	}

}
