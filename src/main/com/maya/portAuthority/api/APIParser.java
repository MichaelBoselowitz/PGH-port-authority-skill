package com.maya.portAuthority.api;

import java.util.List;

public interface APIParser{
	List<Message> parse(String urlString) throws Exception;
}
