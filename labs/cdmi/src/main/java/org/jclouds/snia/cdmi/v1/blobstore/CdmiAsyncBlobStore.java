package org.jclouds.snia.cdmi.v1.blobstore;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.jclouds.Constants;
import org.jclouds.blobstore.BlobStoreContext;
import org.jclouds.blobstore.domain.Blob;
import org.jclouds.blobstore.domain.BlobMetadata;
import org.jclouds.blobstore.domain.PageSet;
import org.jclouds.blobstore.domain.StorageMetadata;
import org.jclouds.blobstore.domain.StorageType;
import org.jclouds.blobstore.internal.BaseAsyncBlobStore;
import org.jclouds.blobstore.options.CreateContainerOptions;
import org.jclouds.blobstore.options.GetOptions;
import org.jclouds.blobstore.options.ListContainerOptions;
import org.jclouds.blobstore.options.PutOptions;
import org.jclouds.blobstore.util.BlobUtils;
import org.jclouds.collect.Memoized;
import org.jclouds.concurrent.Futures;
import org.jclouds.domain.Location;
import org.jclouds.io.Payload;
import org.jclouds.snia.cdmi.v1.CDMIApi;
import org.jclouds.snia.cdmi.v1.CDMIAsyncApi;
import org.jclouds.snia.cdmi.v1.domain.DataObject;
import org.jclouds.snia.cdmi.v1.domain.Container;
import org.jclouds.snia.cdmi.v1.features.ContainerApi;
import org.jclouds.snia.cdmi.v1.features.ContainerAsyncApi;
import org.jclouds.snia.cdmi.v1.features.DataApi;
import org.jclouds.snia.cdmi.v1.features.DataAsyncApi;
import org.jclouds.snia.cdmi.v1.features.DataNonCDMIContentTypeAsyncApi;
import org.jclouds.snia.cdmi.v1.options.CreateDataObjectOptions;
import org.jclouds.snia.cdmi.v1.queryparams.ContainerQueryParams;
import org.jclouds.snia.cdmi.v1.queryparams.DataObjectQueryParams;

import com.google.common.base.Function;
import com.google.common.base.Supplier;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ListenableFuture;
@Singleton
public class CdmiAsyncBlobStore extends BaseAsyncBlobStore {
	final BlobStoreContext context;
	final BlobUtils blobUtils;
	final ExecutorService service;
	final Supplier<Location> defaultLocation;
	final Supplier<Set<? extends Location>> locations;
	final CDMIApi sync;
	final CDMIAsyncApi async;
	final ContainerAsyncApi containerAsyncApi;
	final ContainerApi containerApi;
	final DataAsyncApi dataAsyncApi;
	final DataApi dataApi;
	DataNonCDMIContentTypeAsyncApi dataNonCDMIContentTypeAsyncApi;
	final CdmiObjectToBlobstore cdmiObjectToBlobstore;
	
	@Inject
	protected CdmiAsyncBlobStore(BlobStoreContext context, BlobUtils blobUtils, @Named(Constants.PROPERTY_USER_THREADS) ExecutorService service,
            Supplier<Location> defaultLocation, @Memoized Supplier<Set<? extends Location>> locations,
            CDMIApi sync, CDMIAsyncApi async, CdmiObjectToBlobstore cdmiObjectToBlobstore){
	   super(context, blobUtils, service, defaultLocation, locations);
	   this.context = context;
	   this.blobUtils = blobUtils;
	   this.service = service;
	   this.defaultLocation = defaultLocation;
	   this.locations = locations;
	   this.sync = sync;
	   this.async = async;
	   this.cdmiObjectToBlobstore = cdmiObjectToBlobstore;
	   this.containerAsyncApi = async.getApi();
	   this.dataApi = sync.getDataApiForContainer("/");
	   this.dataAsyncApi = async.getDataApiForContainer("/");
	   this.dataNonCDMIContentTypeAsyncApi = async.getDataNonCDMIContentTypeApiForContainer("/");	   
	   this.containerApi = sync.getApi();
	   
   }

	@Override
	public ListenableFuture<PageSet<? extends StorageMetadata>> list() {
		return list("/");
	}
	
	@Override
	public ListenableFuture<PageSet<? extends StorageMetadata>> list(String containerName, ListContainerOptions options) {
		final String parent = (containerName == "/") ? "": containerName;
      return Futures.compose(containerAsyncApi.get(containerName),
               new Function<Container, PageSet<? extends StorageMetadata>>() {
                  @Override
                  public PageSet<? extends StorageMetadata> apply(Container container) {
               		List<CdmiObjectDesc> cdmiobjects = Lists.newArrayList();
               		for(String child:container.getChildren()) {
               			if(child.endsWith("/")) {
               				ListenableFuture<Container> childContainerFuture = containerAsyncApi.get(parent+child);
               				CdmiObjectDesc cdmiObjectDesc = new CdmiObjectDesc(StorageType.CONTAINER,parent);
               				cdmiObjectDesc.setContainerFuture(childContainerFuture);
               				cdmiobjects.add(cdmiObjectDesc);
               			} else {
               				cdmiobjects.add(new CdmiObjectDesc(dataAsyncApi.get(parent+child),StorageType.BLOB,parent));
               			}				
               		}
               		for(CdmiObjectDesc desc:cdmiobjects) {
               			try {
	                        desc.setCDMIObjectFromFuture();
                        } catch (InterruptedException e) {
	                        e.printStackTrace();
                        } catch (ExecutionException e) {
	                        e.printStackTrace();
                        }
               		}
               		PageSet<? extends StorageMetadata> list = cdmiObjectToBlobstore.containerToStorageMetadata(cdmiobjects);
               		return list;
                  }
               }, service);
	}


	@Override
	public ListenableFuture<Boolean> containerExists(String container) {
		System.out.println("containerExists("+container+")");
		return containerAsyncApi.containerExists(container);
	}

	@Override
	public ListenableFuture<Boolean> createContainerInLocation(Location location, String container) {
		System.out.println("createContainerInLocation("+location+","+container+")");
		return containerAsyncApi.make(container);
	}

	@Override
	public ListenableFuture<Boolean> createContainerInLocation(Location location, String container,
	         CreateContainerOptions options) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public ListenableFuture<Void> clearContainer(final String containerName, final ListContainerOptions options) {
		// TODO deal with options
		return org.jclouds.concurrent.Futures.makeListenable(service.submit(new Callable<Void>() {
         public Void call() throws Exception {
            //blobUtils.clearContainer(containerName, options);
         	_clearContainer(containerName);         	
            return null;
         }

         @Override
         public String toString() {
            return "clearContainer(" + containerName + ")";
         }
      }), service);
	}
	
	private void _clearContainer(String containerName) throws Exception { 
		String parent = containerName;
		Container container =  containerApi.get(containerName, ContainerQueryParams.Builder.children());
		if(container != null) {
			if (containerName.matches("/")) {
				parent = "";
			}
			List<ListenableFuture<Void>> futureVoids = Lists.newArrayList();
			for(String child: container.getChildren()) {
				futureVoids.add(deleteCDMIObject(parent + child));				
			}
			// Wait for completion
			for(ListenableFuture<Void> furtureVoid:futureVoids) {
				furtureVoid.get();				
			}
		}		
	}
	
	@Override
	public ListenableFuture<Void> deleteContainer(final String containerName) {
		return org.jclouds.concurrent.Futures.makeListenable(service.submit(new Callable<Void>() {
         public Void call() throws Exception {
            //blobUtils.clearContainer(containerName, options);
         	_clearContainer(containerName);
         	containerApi.delete(containerName);    	
            return null;
         }
         @Override
         public String toString() {
            return "clearContainer(" + containerName + ")";
         }
      }), service);
	}	


	
	private ListenableFuture<Void> deleteCDMIObject(final String objectName) {
		return org.jclouds.concurrent.Futures.makeListenable(service.submit(new Callable<Void>() {
         public Void call() throws Exception {
      		if (objectName.endsWith("/")) {
      			deleteContainer(objectName);
      		} else {
      			deleteDataObject(objectName);
      		}
      		return null;
         }
         @Override
         public String toString() {
            return "deleteCDMIObject(" + objectName + ")";
         }
      }), service);
 	}
	
	private void deleteDataObject(String objectName) {
		dataApi.delete(objectName);
		return;
	}


	


	@Override
	public ListenableFuture<Boolean> blobExists(String container, final String key) {
      return Futures.compose(containerAsyncApi.get(container),
               new Function<Container, Boolean>() {
                  @Override
                  public Boolean apply(Container from) {
                     return from.getChildren().contains(key);
                  }
               }, service);
	}

	
	@Override
	public ListenableFuture<Long> countBlobs(String containerName, ListContainerOptions options) {
      return Futures.compose(containerAsyncApi.get(containerName),
               new Function<Container, Long>() {
                  @Override
                  public Long apply(Container from) {
                     return Long.valueOf(from.getChildren().size());
                  }
               }, service);		
	}


	@Override
	public ListenableFuture<String> putBlob(String container, Blob blob) {
		return putBlob(container,blob,null);
	}

	@Override
	public ListenableFuture<String> putBlob(String container, Blob blob, PutOptions options) {
		System.out.println("putBlob("+container+","+blob+","+options+")");
		System.out.println("putBlob metadata: "+blob.getMetadata().getUserMetadata());
		ListenableFuture<String> eTag = null;
		// TODO replace below with multipart mime put when openstack provides support
		if(blob.getMetadata().getUserMetadata().isEmpty()) {
			eTag = dataNonCDMIContentTypeAsyncApi.create(container+blob.getMetadata().getName(), blob.getPayload());
		} else {
			try {
				eTag = dataAsyncApi.putObject(container+blob.getMetadata().getName(), CreateDataObjectOptions.Builder.metadata(blob.getMetadata().getUserMetadata()).value(blob.getPayload().getInput()));
	      } catch (IOException e) {
		      // TODO Auto-generated catch block
		      e.printStackTrace();
	      }
		}
	   return eTag;
	}

	@Override
	public ListenableFuture<BlobMetadata> blobMetadata(final String containerName, final String objectName) {
		return org.jclouds.concurrent.Futures.makeListenable(service.submit(new Callable<BlobMetadata>() {
         public BlobMetadata call() throws Exception {
   			ListenableFuture<Payload> payloadFuture = dataNonCDMIContentTypeAsyncApi.getPayload(containerName+objectName);
   			ListenableFuture<DataObject> dataObjectFuture = dataAsyncApi.get(containerName+objectName);
   			Payload payload = payloadFuture.get();
   			DataObject dataObject = dataObjectFuture.get();
   		   return cdmiObjectToBlobstore.getBlobMetadata(containerName, dataObject, payload);
         }
         @Override
         public String toString() {
            return "getBlob(" + containerName + "," + objectName + ")";
         }
      }), service);
	}

	@Override
	public ListenableFuture<Blob> getBlob(final String containerName, final String objectName, GetOptions options) {
		return org.jclouds.concurrent.Futures.makeListenable(service.submit(new Callable<Blob>() {
         public Blob call() throws Exception {
   			ListenableFuture<Payload> payloadFuture = dataNonCDMIContentTypeAsyncApi.getPayload(containerName+objectName);
   			ListenableFuture<DataObject> dataObjectFuture = dataAsyncApi.get(containerName+objectName);
   			Payload payload = payloadFuture.get();
   			DataObject dataObject = dataObjectFuture.get();
      	   return cdmiObjectToBlobstore.getBlob(containerName, dataObject, payload);
         }
         @Override
         public String toString() {
            return "getBlob(" + containerName + "," + objectName + ")";
         }
      }), service);
	}

	@Override
	public ListenableFuture<Void> removeBlob(String container, String key) {
		return dataAsyncApi.delete(container+key);
	}

	@Override
   protected boolean deleteAndVerifyContainerGone(String container) {
		deleteContainer(container);
	   return !containerApi.containerExists(container);
   }
	


}
