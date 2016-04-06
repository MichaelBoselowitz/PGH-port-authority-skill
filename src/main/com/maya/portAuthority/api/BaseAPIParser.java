package com.maya.portAuthority.api;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

public abstract class BaseAPIParser implements APIParser {
	protected URL feedURL;
	
	protected BaseAPIParser(String urlString) {
		try {
            this.feedURL = new URL(urlString);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
	}
	
    protected InputStream getInputStream() {
        try {
            return feedURL.openConnection().getInputStream();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
