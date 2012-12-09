package org.jclouds.snia.cdmi.v1.blobstore;

import java.util.concurrent.ExecutionException;

import org.jclouds.blobstore.domain.StorageType;
import org.jclouds.io.Payload;
import org.jclouds.snia.cdmi.v1.domain.CDMIObject;
import org.jclouds.snia.cdmi.v1.domain.Container;
import org.jclouds.snia.cdmi.v1.domain.DataObject;

import com.google.common.util.concurrent.ListenableFuture;

class CdmiObjectDesc {
	protected CDMIObject cdmiobject;
	protected ListenableFuture<Container> containerFuture;
	protected ListenableFuture<DataObject> dataObjectFuture;
	final protected StorageType storageType;
	final protected String containerName;
	final protected Payload payload;
	protected CdmiObjectDesc(StorageType storageType, String containerName) {
		this.storageType = storageType;
		this.containerName = containerName;
		this.payload = null;
	}

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
	protected CdmiObjectDesc(ListenableFuture<DataObject> dataObjectFuture, StorageType storageType, String containerName) {
		this.dataObjectFuture = dataObjectFuture;
		this.storageType = storageType;
		this.containerName = containerName;	
		this.payload = null;
	}
	protected void setContainerFuture(ListenableFuture<Container> containerFuture) {
		this.containerFuture = containerFuture;		
	}
	protected void setDataObjectFuture(ListenableFuture<DataObject> dataObjectFuture) {
		this.dataObjectFuture = dataObjectFuture;		
	}
	protected void setCDMIObjectFromFuture() throws InterruptedException, ExecutionException {
		if(dataObjectFuture != null) {
			cdmiobject = dataObjectFuture.get();
		} else if(containerFuture != null) {
			cdmiobject = containerFuture.get();			
		}
		
	}



	

}
