package org.jclouds.snia.cdmi.v1.blobstore;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

import org.jclouds.blobstore.domain.Blob;
import org.jclouds.blobstore.domain.BlobMetadata;
import org.jclouds.blobstore.domain.MutableBlobMetadata;
import org.jclouds.blobstore.domain.PageSet;
import org.jclouds.blobstore.domain.StorageMetadata;
import org.jclouds.blobstore.domain.StorageType;
import org.jclouds.blobstore.domain.internal.BlobImpl;
import org.jclouds.blobstore.domain.internal.BlobMetadataImpl;
import org.jclouds.blobstore.domain.internal.MutableBlobMetadataImpl;
import org.jclouds.blobstore.domain.internal.PageSetImpl;
import org.jclouds.blobstore.domain.internal.StorageMetadataImpl;
import org.jclouds.domain.Location;
import org.jclouds.io.ContentMetadata;
import org.jclouds.io.MutableContentMetadata;
import org.jclouds.io.Payload;
import org.jclouds.io.payloads.BaseMutableContentMetadata;
import org.jclouds.javax.annotation.Nullable;
import org.jclouds.snia.cdmi.v1.domain.CDMIObject;
import org.jclouds.snia.cdmi.v1.domain.Container;
import org.jclouds.snia.cdmi.v1.domain.DataObject;
import org.jclouds.snia.cdmi.v1.blobstore.CdmiObjectDesc;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;

public class CdmiObjectToBlobstore {
	public BlobMetadata getBlobMetadata(String containerName,DataObject dataObject,Payload payload) {
	   //public BlobMetadataImpl(String id, String name, @Nullable Location location, URI uri, String eTag,
	   //         Date lastModified, Map<String, String> userMetadata, @Nullable URI publicUri, @Nullable String container,
	   //         ContentMetadata contentMetadata) {
		URI uri = null;
		try {
			uri = new URI(containerName+dataObject.getObjectName());
		} catch (URISyntaxException e) {}
		BlobMetadata blobMetadata = new BlobMetadataImpl("cdmi",dataObject.getObjectName(),null, uri,null,null,dataObject.getUserMetadata(),null,containerName,new CdmiContentMetadataImpl(dataObject,payload));
		return blobMetadata;
		
	}
	public Blob getBlob(String containerName,DataObject dataObject,Payload payload) {
		MutableBlobMetadata mutableBlobMetadata = new MutableBlobMetadataImpl();
		mutableBlobMetadata.setContainer(containerName);
		mutableBlobMetadata.setId(dataObject.getObjectID());
		mutableBlobMetadata.setName(dataObject.getObjectName());
		mutableBlobMetadata.setType(StorageType.BLOB);
		mutableBlobMetadata.setUserMetadata(dataObject.getUserMetadata());
		mutableBlobMetadata.setContentMetadata(BaseMutableContentMetadata.fromContentMetadata(payload.getContentMetadata()));
		try {
	      mutableBlobMetadata.setUri(new URI(containerName+dataObject.getObjectName()));
      } catch (URISyntaxException e) {
	   }
		Blob blob = new BlobImpl(mutableBlobMetadata);
		blob.setPayload(payload);
		return blob;
	}
	Function<CdmiObjectDesc, StorageMetadata> cdmiObjectToStorageMetadataFunc = new Function<CdmiObjectDesc, StorageMetadata>() {
		  public StorageMetadata apply(CdmiObjectDesc cdmiObjectDesc) {
			  URI uri = null;
			  try {
				  uri = new URI(cdmiObjectDesc.containerName+cdmiObjectDesc.cdmiobject.getObjectName());
			  } catch (URISyntaxException e) {
		     }
			   //public StorageMetadataImpl(StorageType type, @Nullable String id, @Nullable String name,
			   //         @Nullable Location location, @Nullable URI uri, @Nullable String eTag, @Nullable Date lastModified,
			   //         Map<String, String> userMetadata) {
		     //return new StorageMetadataImpl(cdmiObjectDesc.storageType,cdmiObjectDesc.cdmiobject.getObjectID(),cdmiObjectDesc.cdmiobject.getObjectName(),null,uri,null,null,cdmiObjectDesc.cdmiobject.getUserMetadata());
		     return new StorageMetadataImpl(cdmiObjectDesc.storageType,"cdmi",cdmiObjectDesc.cdmiobject.getObjectName(),null,uri,null,null,cdmiObjectDesc.cdmiobject.getUserMetadata());
		  }
	};

   protected PageSet<? extends StorageMetadata> containerToStorageMetadata(Iterable<CdmiObjectDesc> containers) {
   	return new PageSetImpl(Iterables.transform(containers, cdmiObjectToStorageMetadataFunc),null);
   }


}
