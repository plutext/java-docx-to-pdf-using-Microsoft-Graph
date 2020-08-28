package org.plutext.msgraph.convert;

@SuppressWarnings("serial")
public class ConversionException extends Exception {
	
    public ConversionException(String msg) {
        super(msg);
}

	public ConversionException(String msg, Exception e) {
	        super(msg, e);
	}
	
	public ConversionException(String msg, Throwable t) {
	        super(msg, t);
	}


}
