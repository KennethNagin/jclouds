package org.jclouds.snia.cdmi.v1.blobstore;

import org.jclouds.blobstore.domain.StorageType;
import org.jclouds.io.Payload;
import org.jclouds.snia.cdmi.v1.domain.CDMIObject;

class CdmiObjectDesc {
	final protected CDMIObject cdmiobject;
	final protected StorageType storageType;
	final protected String containerName;
	final protected Payload payload;
	protected CdmiObjectDesc(CDMIObject cdmiobject, StorageType storageType, String containerName) {
		this.cdmiobject = cdmiobject;
		this.storageType = storageType;
		this.containerName = containerName;	
		this.payload = null;
	}
	protected CdmiObjectDesc(CDMIObject cdmiobject, StorageType storageType, String containerName, Payload payload) {
		this.cdmiobject = cdmiobject;
		this.storageType = storageType;
		this.containerName = containerName;
		this.payload = payload;
	}

	

}
