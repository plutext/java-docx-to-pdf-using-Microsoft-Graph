package org.plutext.msgraph.convert;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.plutext.msgraph.convert.ConversionException;
import org.plutext.msgraph.convert.DocxToPdfConverter;
import org.plutext.msgraph.convert.graphsdk.DocxToPdfConverterLarge;

import com.github.scribejava.httpclient.apache.ApacheHttpClient;
import com.github.scribejava.httpclient.okhttp.OkHttpHttpClient;

import junit.framework.Assert;

//import com.github.scribejava.httpclient.apache.ApacheHttpClient;

public class ConvertDocxInputStreamToPdfTest {

	static File inFile = new File(System.getProperty("user.dir")
			+ "/src/test/resources/sample-docx.docx");
	
	@Test
	public void testConversionSdk() throws IOException, ConversionException {

		DocxToPdfConverter converter = new DocxToPdfConverterLarge(new PRIVATE_AuthConfigImpl());
		byte[] pdfBytes = null;
		try (FileInputStream fis = new FileInputStream(inFile) ) {
			pdfBytes = converter.convert(fis);			
		}
		
		String sniffed = new String(pdfBytes, 0, 8);  // PDF?
		org.junit.Assert.assertTrue("Not a PDF!", sniffed.startsWith("%PDF"));
		
	}

	@Test
	public void testConversionSdkCore() throws IOException, ConversionException {

		
		// First, using-graph-sdk
		DocxToPdfConverter converter = new org.plutext.msgraph.convert.graphcore.DocxToPdfConverter(new PRIVATE_AuthConfigImpl());
		byte[] pdfBytes = null;
		try (FileInputStream fis = new FileInputStream(inFile) ) {
			pdfBytes = converter.convert(fis);			
		}
		
		String sniffed = new String(pdfBytes, 0, 8);  // PDF?
		org.junit.Assert.assertTrue("Not a PDF!", sniffed.startsWith("%PDF"));
		
	}

	@Test
	public void testConversionScribeDefaultClient() throws IOException, ConversionException {

		DocxToPdfConverter converter = new org.plutext.msgraph.convert.scribe.DocxToPdfConverter(new PRIVATE_AuthConfigImpl());
		// If you choose without-graph-sdk-using-scribe, you can optionally specify your preferred httpclient
		// (uncommented in your pom)
//        httpClient = new ApacheHttpClient();
//		DocxToPdfConverter converter = new PdfConverterLarge(new AuthConfigImpl(), httpClient);
		byte[] pdfBytes = null;
		try (FileInputStream fis = new FileInputStream(inFile) ) {
			pdfBytes = converter.convert(fis);			
		}
		
		String sniffed = new String(pdfBytes, 0, 8);  // PDF?
		org.junit.Assert.assertTrue("Not a PDF!", sniffed.startsWith("%PDF"));
		
	}

	@Test
	public void testConversionScribeApache() throws IOException, ConversionException {

		DocxToPdfConverter converter = new org.plutext.msgraph.convert.scribe.DocxToPdfConverter(new PRIVATE_AuthConfigImpl(), new ApacheHttpClient());
		byte[] pdfBytes = null;
		try (FileInputStream fis = new FileInputStream(inFile) ) {
			pdfBytes = converter.convert(fis);			
		}
		
		String sniffed = new String(pdfBytes, 0, 8);  // PDF?
		org.junit.Assert.assertTrue("Not a PDF!", sniffed.startsWith("%PDF"));
		
	}

	@Test
	public void testConversionScribeOKHttp() throws IOException, ConversionException {

		DocxToPdfConverter converter = new org.plutext.msgraph.convert.scribe.DocxToPdfConverter(new PRIVATE_AuthConfigImpl(), new OkHttpHttpClient());
		byte[] pdfBytes = null;
		try (FileInputStream fis = new FileInputStream(inFile) ) {
			pdfBytes = converter.convert(fis);			
		}
		
		String sniffed = new String(pdfBytes, 0, 8);  // PDF?
		org.junit.Assert.assertTrue("Not a PDF!", sniffed.startsWith("%PDF"));
		
	}
	
	@Test
	public void testConversionMSAL4J() throws IOException, ConversionException {

		DocxToPdfConverter converter = new org.plutext.msgraph.convert.msal.DocxToPdfConverter(new PRIVATE_AuthConfigImpl());
		byte[] pdfBytes = null;
		try (FileInputStream fis = new FileInputStream(inFile) ) {
			pdfBytes = converter.convert(fis);			
		}
		
		String sniffed = new String(pdfBytes, 0, 8);  // PDF?
		org.junit.Assert.assertTrue("Not a PDF!", sniffed.startsWith("%PDF"));
		
	}
	
}
