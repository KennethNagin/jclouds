/**
 * Licensed to jclouds, Inc. (jclouds) under one or more
 * contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  jclouds licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jclouds.snia.cdmi.v1.features;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import org.jclouds.ContextBuilder;
import org.jclouds.blobstore.AsyncBlobStore;
import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.BlobStoreContext;
import org.jclouds.blobstore.domain.Blob;
import org.jclouds.blobstore.domain.PageSet;
import org.jclouds.blobstore.domain.StorageMetadata;
import org.jclouds.blobstore.options.CreateContainerOptions;
import org.jclouds.domain.JsonBall;
import org.jclouds.domain.Location;
import org.jclouds.io.Payload;
import org.jclouds.io.payloads.BaseMutableContentMetadata;
import org.jclouds.io.payloads.InputStreamPayload;
import org.jclouds.snia.cdmi.v1.ObjectTypes;
import org.jclouds.snia.cdmi.v1.config.CDMIProperties;
import org.jclouds.snia.cdmi.v1.domain.Container;
import org.jclouds.snia.cdmi.v1.filters.AuthTypes;
import org.jclouds.snia.cdmi.v1.internal.BaseCDMIApiLiveTest;
//import org.jclouds.snia.cdmi.v1.options.CreateContainerOptions;
import org.jclouds.snia.cdmi.v1.queryparams.ContainerQueryParams;
import org.jclouds.Constants;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import com.google.common.net.MediaType;
import com.google.common.util.concurrent.ListenableFuture;

/**
 * Example Setup: -Dtest.cdmi.identity=admin:Admin?authType=openstackKeystone -Dtest.cdmi.credential=passw0rd
 * -Dtest.cdmi.endpoint=http://pds-stack2:5000/v2.0/
 * 
 * @author Kenneth Nagin
 */

@Test(groups = "live", testName = "ContainerApiLiveTest")
public class BlobStoreApiLiveTest extends BaseCDMIApiLiveTest {
   static final ImmutableMap<String, String> userMetadata =
      new ImmutableMap.Builder<String, String>()
          .put("one", "1")
          .put("two", "2")
          .put("three", "3")
          .build();
   static final Logger logger = Logger.getAnonymousLogger();

   @Test
   public void testSyncBlobStore() throws Exception {
      String containerName = "MyContainer" + System.currentTimeMillis() + "/";
      Iterator<String> keys;
      Properties overrides = super.setupProperties();
      overrides.setProperty(CDMIProperties.AUTHTYPE, AuthTypes.OPENSTACKKEYSTONE_AUTHTYPE);
      overrides.setProperty(Constants.PROPERTY_ENDPOINT,endpoint); 
      System.out.println(overrides);
      BlobStoreContext blobStoreContext = ContextBuilder.newBuilder(provider).credentials(identity, credential).overrides(overrides).buildView(BlobStoreContext.class);
      BlobStore blobStore = blobStoreContext.getBlobStore();
      Blob blob;
      assertEquals(blobStore.containerExists(containerName),false);
      long rootBlobCount = blobStore.countBlobs("/");
      logger.info("create: " + containerName);
      assertEquals(blobStore.list().size(),rootBlobCount);
      assertEquals(blobStore.createContainerInLocation(null,containerName),true);
      try {
         assertEquals(blobStore.containerExists(containerName),true);
         assertEquals(blobStore.countBlobs("/"),++rootBlobCount);
         assertEquals(blobStore.list().size(),rootBlobCount);
 //         File inFile = new File(System.getProperty("user.dir") + "/src/test/resources/yellow-flowers.jpg");
         File inFile = new File(System.getProperty("user.dir") + "/src/test/resources/container.json");
         assertEquals(true, inFile.isFile());
         assertEquals(blobStore.blobExists(containerName, inFile.getName()),false);
         assertEquals(blobStore.countBlobs(containerName),0);
         assertEquals(blobStore.list(containerName).size(),0);
         blob = blobStore.blobBuilder(inFile.getName()).payload(inFile).userMetadata(userMetadata).build();
         assertNotNull(blobStore.putBlob(containerName, blob));
         assertEquals(blobStore.blobExists(containerName, inFile.getName()),true);
         assertEquals(blobStore.countBlobs(containerName),1);
         assertEquals(blobStore.list(containerName).size(),1);
         blob = blobStore.getBlob(containerName, inFile.getName());      
         validate(inFile,blob);
         validate(userMetadata,blob);
         assertEquals(blobStore.blobMetadata(containerName, inFile.getName()).getName(),inFile.getName());
         assertEquals(blobStore.blobMetadata(containerName, inFile.getName()).getContainer(),containerName);         
         blobStore.removeBlob(containerName, inFile.getName());
         assertEquals(blobStore.blobExists(containerName, inFile.getName()),false);
         assertEquals(blobStore.countBlobs(containerName),0);
         assertEquals(blobStore.list(containerName).isEmpty(),true); 
         for(int i=0;i<5;i++) {
         	assertNotNull(blobStore.putBlob(containerName, blobStore.blobBuilder(inFile.getName()+"_"+i).payload(inFile).build()));
         	assertEquals(blobStore.countBlobs(containerName),i+1);
         }
         assertEquals(blobStore.createContainerInLocation(null,containerName+"childContainer/"),true); 
         assertEquals(blobStore.countBlobs(containerName),6);
         assertNotNull(blobStore.putBlob(containerName+"childContainer/", blobStore.blobBuilder(inFile.getName()).payload(inFile).build()));
         assertEquals(blobStore.countBlobs(containerName),6);
         blobStore.clearContainer(containerName);
         assertEquals(blobStore.containerExists(containerName),true);
         assertEquals(blobStore.countBlobs(containerName),0);
      } 
      finally {
      	logger.info("delete: " + containerName);
      	blobStore.deleteContainer(containerName);
      	assertEquals(blobStore.containerExists(containerName),false);
      	assertEquals(blobStore.countBlobs("/"),--rootBlobCount);
      }
   }
      
      @Test
      public void testASyncBlobStore() throws Exception {
         String containerName = "MyContainer" + System.currentTimeMillis() + "/";
         Iterator<String> keys;
         Properties overrides = super.setupProperties();
         overrides.setProperty(CDMIProperties.AUTHTYPE, AuthTypes.OPENSTACKKEYSTONE_AUTHTYPE);
         overrides.setProperty(Constants.PROPERTY_ENDPOINT,endpoint); 
         System.out.println(overrides);
         BlobStoreContext blobStoreContext = ContextBuilder.newBuilder(provider).credentials(identity, credential).overrides(overrides).buildView(BlobStoreContext.class);
         AsyncBlobStore asyncBlobStore = blobStoreContext.getAsyncBlobStore();
         BlobStore blobStore = blobStoreContext.getBlobStore();
         long rootBlobCount = asyncBlobStore.countBlobs("/").get();
         assertEquals(asyncBlobStore.list().get().size(),rootBlobCount);
         try {
         	assertEquals(asyncBlobStore.containerExists(containerName).get(),Boolean.valueOf(false)); 
         	logger.info("create: " + containerName);
            assertEquals(asyncBlobStore.createContainerInLocation(null,containerName).get(),Boolean.valueOf(true)); 
            assertEquals(asyncBlobStore.containerExists(containerName).get(),Boolean.valueOf(true));            
            assertEquals(asyncBlobStore.countBlobs("/").get(),Long.valueOf(++rootBlobCount));
            assertEquals(asyncBlobStore.list().get().size(),rootBlobCount);
    //         File inFile = new File(System.getProperty("user.dir") + "/src/test/resources/yellow-flowers.jpg");
            File inFile = new File(System.getProperty("user.dir") + "/src/test/resources/container.json");
            assertEquals(true, inFile.isFile());
            assertEquals(asyncBlobStore.blobExists(containerName, inFile.getName()).get(),Boolean.valueOf(false));
            assertEquals(asyncBlobStore.countBlobs(containerName).get(),Long.valueOf(0));
            assertEquals(asyncBlobStore.list(containerName).get().size(),0);
            Blob blob = blobStore.blobBuilder(inFile.getName()).payload(inFile).userMetadata(userMetadata).build();
            assertNotNull(asyncBlobStore.putBlob(containerName, blob).get());
            assertEquals(asyncBlobStore.blobExists(containerName, inFile.getName()).get(),Boolean.valueOf(true));
            assertEquals(asyncBlobStore.countBlobs(containerName).get(),Long.valueOf(1));
            assertEquals(asyncBlobStore.list(containerName).get().size(),1);
            blob = asyncBlobStore.getBlob(containerName, inFile.getName()).get();      
            System.out.println("blobStore.blobMetadata.getContentMetadata(): after put "+asyncBlobStore.blobMetadata(containerName, inFile.getName()).get().getContentMetadata().toString());
            validate(inFile,blob);
            validate(userMetadata,blob);
            assertEquals(asyncBlobStore.blobMetadata(containerName, inFile.getName()).get().getName(),inFile.getName());
            assertEquals(asyncBlobStore.blobMetadata(containerName, inFile.getName()).get().getContainer(),containerName);
            asyncBlobStore.removeBlob(containerName, inFile.getName()).get();
            assertEquals(asyncBlobStore.blobExists(containerName, inFile.getName()).get(),Boolean.valueOf(false));
            assertEquals(asyncBlobStore.countBlobs(containerName).get(),Long.valueOf(0));
            assertEquals(blobStore.list(containerName).isEmpty(),true);
            for(int i=0;i<5;i++) {
            	assertNotNull(blobStore.putBlob(containerName, blobStore.blobBuilder(inFile.getName()+"_"+i).payload(inFile).build()));
            	assertEquals(blobStore.countBlobs(containerName),i+1);
            }
            //assertEquals(blobStore.createContainerInLocation(null,containerName+"childContainer/"),true); 
            //assertEquals(asyncBlobStore.countBlobs(containerName).get(),Long.valueOf(6));
            //assertNotNull(blobStore.putBlob(containerName+"childContainer/", blobStore.blobBuilder(inFile.getName()).payload(inFile).build()));
            //assertEquals(asyncBlobStore.countBlobs(containerName).get(),Long.valueOf(6));
            asyncBlobStore.clearContainer(containerName).get();
            assertEquals(asyncBlobStore.containerExists(containerName).get(),Boolean.valueOf(true));
            assertEquals(asyncBlobStore.countBlobs(containerName).get(),Long.valueOf(0));            
         } 
         finally {         	
         	logger.info("delete: " + containerName);
         	asyncBlobStore.deleteContainer(containerName).get();
         	assertEquals(asyncBlobStore.containerExists(containerName).get(),Boolean.valueOf(false)); 
         	assertEquals(asyncBlobStore.countBlobs("/").get(),Long.valueOf(--rootBlobCount)); 
         }
      }

   private void validate(File fileIn, Blob blob) throws IOException {
   	Payload payload = blob.getPayload();
   	File tempDir = Files.createTempDir();   	
      File fileOut = new File(tempDir, fileIn.getName());
      FileOutputStream fos = new FileOutputStream(fileOut);
      ByteStreams.copy(payload.getInput(), fos);
      fos.flush();
      fos.close();
      assertEquals(Files.equal(fileOut, fileIn), true);
      fileOut.delete();
      tempDir.delete();

   }
   private void validate(Map<String, String> in, Blob blob) {
   	Iterator<String> keys = in.keySet().iterator();
   	Map<String, String> out = blob.getMetadata().getUserMetadata();   	
      while (keys.hasNext()) {
         String key = keys.next();
         assertEquals(out.containsKey(key), true);
         assertEquals(out.get(key), in.get(key));
      }
   	
   }


}
