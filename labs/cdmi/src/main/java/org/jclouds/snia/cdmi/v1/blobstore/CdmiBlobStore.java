package org.jclouds.snia.cdmi.v1.blobstore;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.jclouds.blobstore.BlobStoreContext;
import org.jclouds.blobstore.domain.Blob;
import org.jclouds.blobstore.domain.BlobMetadata;
import org.jclouds.blobstore.domain.PageSet;
import org.jclouds.blobstore.domain.StorageMetadata;
import org.jclouds.blobstore.domain.StorageType;
import org.jclouds.blobstore.domain.internal.PageSetImpl;
import org.jclouds.blobstore.internal.BaseBlobStore;
import org.jclouds.blobstore.options.CreateContainerOptions;
import org.jclouds.blobstore.options.GetOptions;
import org.jclouds.blobstore.options.ListContainerOptions;
import org.jclouds.blobstore.options.PutOptions;
import org.jclouds.blobstore.util.BlobUtils;
import org.jclouds.collect.Memoized;
import org.jclouds.domain.Location;
import org.jclouds.io.Payload;
import org.jclouds.snia.cdmi.v1.CDMIApi;
import org.jclouds.snia.cdmi.v1.domain.CDMIObject;
import org.jclouds.snia.cdmi.v1.domain.Container;
import org.jclouds.snia.cdmi.v1.domain.DataObject;
import org.jclouds.snia.cdmi.v1.features.ContainerApi;
import org.jclouds.snia.cdmi.v1.features.DataApi;
import org.jclouds.snia.cdmi.v1.features.DataNonCDMIContentTypeApi;
import org.jclouds.snia.cdmi.v1.options.CreateDataObjectOptions;
import org.jclouds.snia.cdmi.v1.queryparams.ContainerQueryParams;
import org.jclouds.snia.cdmi.v1.queryparams.DataObjectQueryParams;

import com.google.common.base.Supplier;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multiset;
import com.google.common.io.CharStreams;
import com.google.common.io.InputSupplier;
import com.google.common.base.Charsets;

import javax.inject.Inject;
import javax.inject.Singleton;
@Singleton
public class CdmiBlobStore extends BaseBlobStore{
	final ContainerApi containerApi;
   final DataApi dataApi;
   final DataNonCDMIContentTypeApi dataNonCDMIContentTypeApi;
   final CdmiObjectToBlobstore cdmiObjectToBlobstore;

	@Inject
	protected CdmiBlobStore(BlobStoreContext context, BlobUtils blobUtils, Supplier<Location> defaultLocation,
				@Memoized Supplier<Set<? extends Location>> locations,
				CDMIApi sync, CdmiObjectToBlobstore cdmiObjectToBlobstore){		
	   super(context, blobUtils, defaultLocation, locations);
	   containerApi = sync.getApi();
	   dataApi = sync.getDataApiForContainer("/");
	   dataNonCDMIContentTypeApi = sync.getDataNonCDMIContentTypeApiForContainer("/");
	   this.cdmiObjectToBlobstore = cdmiObjectToBlobstore;
	   
   }

	@Override
   public PageSet<? extends StorageMetadata> list() {
	   return list("/");
   }
	@Override
   public PageSet<? extends StorageMetadata> list(String containerName) {
		System.out.println("list("+containerName+") entered");
		String parent = (containerName == "/") ? "": containerName; 
		System.out.println("parent: "+parent);
		Container container = containerApi.get(containerName);
		List<CdmiObjectDesc> cdmiobjects = Lists.newArrayList();
		for(String child:container.getChildren()) {
			if(child.endsWith("/")) {
				cdmiobjects.add(new CdmiObjectDesc(containerApi.get(parent+child),StorageType.CONTAINER,parent));
			} else {
				cdmiobjects.add(new CdmiObjectDesc(dataApi.get(parent+child),StorageType.BLOB,parent));
			}				
		}
		PageSet<? extends StorageMetadata> list = cdmiObjectToBlobstore.containerToStorageMetadata(cdmiobjects);

	   return list;
   }


	@Override
   public boolean containerExists(String container) {
	   return containerApi.containerExists(container);
   }

	@Override
   public boolean createContainerInLocation(Location location, String containerName) {
		boolean result = true;
	   try {
	   	//String _containerName =  containerName.endsWith("/") ? containerName : containerName+"/"; 
	   	Container containerOut = containerApi.create(containerName);
	   	if(containerOut==null) {
	   		System.out.println("createContainerInLocation: container is null");
	   		result = false;
	   	}
	   } catch(Exception e) {
	   	System.out.println("createContainerInLocation: "+e);
	   	result = false;	   	
	   }
	   return result;
   }

	@Override
   public boolean createContainerInLocation(Location location, String container, CreateContainerOptions options) {
	   return createContainerInLocation(location,container);
   }

	@Override
   public PageSet<? extends StorageMetadata> list(String container, ListContainerOptions options) {
		System.out.println("list("+container+","+options+")"+" entered" );
	   // TODO Auto-generated method stub
	   return null;
   }

	@Override
   public boolean blobExists(String containerName, String objectName) {
		boolean result = true;
	   try {
	   	Container container = containerApi.get(containerName, ContainerQueryParams.Builder.children());
	   	if(container==null) {
	   		System.out.println("blobExists container null: ");
	   		result = false;
	   	}
	   	result = container.getChildren().contains(objectName);
	   } catch(Exception e) {
	   	System.out.println("blobExists exception: "+e);
	   	result = false;	   	
	   }
	   return result;
   }

	@Override
   public String putBlob(String container, Blob blob) {
	   return putBlob(container,blob,null);
   }

	@Override
   public String putBlob(String container, Blob blob, PutOptions options) {
	   // TODO add support for put options
		System.out.println("putBlob("+container+","+blob+","+options+")");
		System.out.println("putBlob metadata: "+blob.getMetadata().getUserMetadata());
		String etag = "";
		// TODO replace below with multipart mime put when openstack provides support
		if(blob.getMetadata().getUserMetadata().isEmpty()) {
			etag = dataNonCDMIContentTypeApi.create(container+blob.getMetadata().getName(), blob.getPayload());
		} else {
			try {
		      //dataApi.create(container+blob.getMetadata().getName(), CreateDataObjectOptions.Builder.metadata(blob.getMetadata().getUserMetadata()).value(blob.getPayload().getInput()));
		      etag = dataApi.putObject(container+blob.getMetadata().getName(), CreateDataObjectOptions.Builder.metadata(blob.getMetadata().getUserMetadata()).value(blob.getPayload().getInput()));
	      } catch (IOException e) {
		      // TODO Auto-generated catch block
		      e.printStackTrace();
	      }
		}
	   return etag;
   }

	@Override
   public BlobMetadata blobMetadata(String containerName,String objectName) {
		Payload payload = dataNonCDMIContentTypeApi.getPayload(containerName+objectName);
		DataObject dataObject = dataApi.get(containerName+objectName, DataObjectQueryParams.Builder.metadata().field("objectName").field("objectId").field("mimetype"));
	   return cdmiObjectToBlobstore.getBlobMetadata(containerName, dataObject, payload);
   }

	@Override
   public Blob getBlob(String containerName, String objectName, GetOptions options) {
		Blob blob = null;
		try{
			Payload payload = dataNonCDMIContentTypeApi.getPayload(containerName+objectName);
			DataObject dataObject = dataApi.get(containerName+objectName);
			blob = cdmiObjectToBlobstore.getBlob(containerName, dataObject, payload);			
		}
		catch (Exception e){
			System.out.println("getBlob Exception "+e);			
		}
	   return blob;
   }

	@Override
   public void removeBlob(String container, String name) {
		dataNonCDMIContentTypeApi.delete(container+name);	   
   }
	
	@Override
	public void clearContainer(String containerName) { 
		String parent = containerName;
		Container container =  containerApi.get(containerName, ContainerQueryParams.Builder.children());
		if(container != null) {
			if (containerName.matches("/")) {
				parent = "";
			}
			for(String child: container.getChildren()) {
				deleteCDMIObject(parent + child);				
			}
		}		
	}

	@Override
	public void deleteContainer(String containerName) {
		clearContainer(containerName);
		containerApi.delete(containerName);		
	}	
	private void deleteCDMIObject(String objectName) {
		if (objectName.endsWith("/")) {
			deleteContainer(objectName);
		} else {
			deleteDataObject(objectName);
		}
		return;
	}
	
	private void deleteDataObject(String objectName) {
		dataApi.delete(objectName);		
	}



	@Override
   protected boolean deleteAndVerifyContainerGone(String container) {
		deleteContainer(container);
	   return !containerExists(container);
   }
	
	@Override
	public long countBlobs(String containerName, ListContainerOptions options) {
		long result = 0;
		Container container = containerApi.get(containerName);
		if(container!=null) {
			result = container.getChildren().size();
		}
		return result;
		
		
	}

}
