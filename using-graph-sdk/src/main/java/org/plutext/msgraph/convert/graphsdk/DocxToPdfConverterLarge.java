package org.plutext.msgraph.convert.graphsdk;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.plutext.msgraph.convert.AuthConfig;
import org.plutext.msgraph.convert.ConversionException;
import org.plutext.msgraph.convert.DocxToPdfConverter;

public class DocxToPdfConverterLarge extends PdfConverterLarge implements DocxToPdfConverter {

	public DocxToPdfConverterLarge(AuthConfig authConfig) {
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
	
	public byte[] convert(File docx) throws ConversionException, IOException {
		return super.convert(docx);
	}
	

}
