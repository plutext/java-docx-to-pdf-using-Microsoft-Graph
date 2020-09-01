package org.plutext.msgraph.convert.graphsdk;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.plutext.msgraph.convert.AuthConfig;
import org.plutext.msgraph.convert.ConversionException;
import org.plutext.msgraph.convert.PptxToPdfConverter;

public class PptxToPdfConverterLarge extends PdfConverterLarge implements PptxToPdfConverter {

	public PptxToPdfConverterLarge(AuthConfig authConfig) {
		super(authConfig);
	}

	@Override
	public byte[] convert(byte[] pptx) throws ConversionException {
		return convert(pptx, ".pptx");
	}

	@Override
	public byte[] convert(InputStream pptx) throws ConversionException, IOException {
		return convert(pptx, ".pptx");
	}
	
	public byte[] convert(File pptx) throws ConversionException, IOException {
		return super.convert(pptx);
	}
	

}
