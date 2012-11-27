package org.jclouds.snia.cdmi.v1.blobstore;

import java.util.Date;

import org.jclouds.io.ContentMetadata;
import org.jclouds.io.ContentMetadataBuilder;
import org.jclouds.io.Payload;
import org.jclouds.snia.cdmi.v1.domain.DataObject;

public class CdmiContentMetadataImpl implements ContentMetadata {
	final DataObject dataObject;
	final Payload payload;
	CdmiContentMetadataImpl(DataObject dataObject, Payload payload) {
		this.dataObject = dataObject;
		this.payload = payload;
	}

	@Override
	public Long getContentLength() {
		// TODO Auto-generated method stub
		return payload.getContentMetadata().getContentLength();
	}

	@Override
	public String getContentDisposition() {
		// TODO Auto-generated method stub
		return payload.getContentMetadata().getContentDisposition();
	}

	@Override
	public String getContentEncoding() {
		// TODO Auto-generated method stub
		return payload.getContentMetadata().getContentEncoding();
	}

	@Override
	public String getContentType() {
		// TODO Auto-generated method stub
		return payload.getContentMetadata().getContentType();
	}

	@Override
	public byte[] getContentMD5() {
		// TODO Auto-generated method stub
		return payload.getContentMetadata().getContentMD5();
	}

	@Override
	public String getContentLanguage() {
		// TODO Auto-generated method stub
		return payload.getContentMetadata().getContentLanguage();
	}

	@Override
	public Date getExpires() {
		// TODO Auto-generated method stub
		return payload.getContentMetadata().getExpires();
	}
	
	@Override
	public String toString() {
		return "[contentLength="+getContentLength() +" contentDisposition="+getContentDisposition()+" contentEncoding="+getContentEncoding()+" contentType="+getContentType()+" contentMD5="+getContentMD5()+" contentLanguage="+getContentLanguage()+" contentExpires="+getExpires();
	}

	@Override
	public ContentMetadataBuilder toBuilder() {
		// TODO Auto-generated method stub
		return null;
	}

}
