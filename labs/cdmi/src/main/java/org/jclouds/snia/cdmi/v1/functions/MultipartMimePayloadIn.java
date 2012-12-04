package org.jclouds.snia.cdmi.v1.functions;

import java.io.InputStream;

import org.jclouds.io.MutableContentMetadata;
import org.jclouds.io.payloads.BasePayload;

public class MultipartMimePayloadIn extends BasePayload<MultipartMimeParts> {
	final MultipartMimeParts content;
	public MultipartMimePayloadIn(MultipartMimeParts content) {
	   super(content);
	   this.content = content;
	   MutableContentMetadata contentMetadata = this.getContentMetadata();
	   contentMetadata.setContentLength(content.getContentLength());	   
	   this.setContentMetadata(contentMetadata);
   }

	@Override
	public InputStream getInput() {
		return content.getInput();
	}

}
