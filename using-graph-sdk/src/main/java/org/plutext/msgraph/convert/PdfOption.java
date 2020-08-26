package org.plutext.msgraph.convert;

import com.microsoft.graph.options.Option;

class PdfOption extends Option /* its protected! */ { 
	
    protected PdfOption(String name, Object value) {
		super(name, value);
	}

	/**
     * Gets the name of the option
     * 
     * @return the name of the option
     */
    @Override
    public String getName() {
        return "format";
    }

    /**
     * Gets the value of the option
     * 
     * @return the value of the option
     */
    @Override
    public Object getValue() {
        return "pdf";
    }
	
}
