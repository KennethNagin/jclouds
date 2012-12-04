package org.jclouds.snia.cdmi.v1.functions;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jclouds.io.Payload;
import org.jclouds.io.Payloads;
import org.jclouds.io.payloads.BasePayload;

import com.google.gson.JsonObject;

public class MultipartMimeParts {
	final static String BOUNDARY = "gc0p4Jq0M2Yt08j34c0p";
	public static final String MULTIPARTMIXED = " multipart/mixed; boundary="+BOUNDARY;
	final static String SEPARATER1 = "--"+BOUNDARY+"\n";
	final static String SEPARATER = "\n\n--"+BOUNDARY+"\n";
	final static String END = "\n\n--"+BOUNDARY+"--";
	final Payload payload;
	final JsonObject jsonObjectBody = new JsonObject();
	final String mimetype;
	final String beginning;
	final String middle;
	final long contentLength; 
	public MultipartMimeParts(Map<String, String> metadata, Payload payload, String mimetype) {
		this.payload = payload;
      JsonObject jsonObjectMetadata = new JsonObject();
      if (metadata != null) {
         for (Entry<String, String> entry : metadata.entrySet()) {
            jsonObjectMetadata.addProperty(entry.getKey(), entry.getValue());
         }
      }
      jsonObjectBody.add("metadata", jsonObjectMetadata);
      jsonObjectBody.addProperty("mimetype", mimetype);
      this.mimetype = mimetype;
      System.out.println("payload.ContentMetadata: "+payload.getContentMetadata());
      System.out.println("jsonObjectBody: "+jsonObjectBody);
      beginning = "Content-Type: application/cdmi-object\n"+"Content-Length: "+jsonObjectBody.toString().length()+"\n\n";
      middle = "Content-Type: "+ mimetype +"\n"+"Content-Transfer-Encoding: binary\n"+"Content-Length: "+payload.getContentMetadata().getContentLength()+"\n\n";
      contentLength = SEPARATER1.length() + beginning.length() + jsonObjectBody.toString().length()  + SEPARATER.length() + middle.length()  + payload.getContentMetadata().getContentLength() + END.length();

	}
	public InputStream getInput() {
      List<InputStream> streams = Arrays.asList(
      			new ByteArrayInputStream(SEPARATER1.getBytes()),
      			new ByteArrayInputStream(beginning.getBytes()),
      			new ByteArrayInputStream(jsonObjectBody.toString().getBytes()),
      			new ByteArrayInputStream(SEPARATER.getBytes()),
      			new ByteArrayInputStream(middle.getBytes()),
      			payload.getInput(),
      			new ByteArrayInputStream(END.getBytes()));
		return new SequenceInputStream(Collections.enumeration(streams));
		
	}
	public long getContentLength() {
		return contentLength;
	}

}
