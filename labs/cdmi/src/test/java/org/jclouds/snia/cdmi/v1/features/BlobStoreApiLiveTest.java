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
   public void testCreateContainer() throws Exception {
      String containerName = "MyContainer" + System.currentTimeMillis() + "/";
      Iterator<String> keys;
      Properties overrides = super.setupProperties();
      overrides.setProperty(CDMIProperties.AUTHTYPE, AuthTypes.OPENSTACKKEYSTONE_AUTHTYPE);
      overrides.setProperty(Constants.PROPERTY_ENDPOINT,endpoint); 
      System.out.println(overrides);
      BlobStoreContext blobStoreContext = ContextBuilder.newBuilder(provider).credentials(identity, credential).overrides(overrides).buildView(BlobStoreContext.class);
      BlobStore blobStore = blobStoreContext.getBlobStore();
      assertEquals(blobStore.containerExists(containerName),false);
      long rootBlobCount = blobStore.countBlobs("/");
      logger.info("create: " + containerName);
      assertEquals(blobStore.createContainerInLocation(null,containerName),true);
      assertEquals(blobStore.containerExists(containerName),true);
      assertEquals(blobStore.countBlobs("/"),++rootBlobCount);      
      for(StorageMetadata md: blobStore.list() ) {
      	System.out.println("md: "+md);      	
      }
      File inFile = new File(System.getProperty("user.dir") + "/src/test/resources/yellow-flowers.jpg");
      assertEquals(true, inFile.isFile());
      assertEquals(blobStore.blobExists(containerName, inFile.getName()),false);
      assertEquals(blobStore.countBlobs(containerName),0);
      assertEquals(blobStore.list(containerName).size(),0);
      Blob blob = blobStore.blobBuilder(inFile.getName()).payload(inFile).userMetadata(userMetadata).build();
      
      System.out.println("blob: before put "+blob);
 
      blobStore.putBlob(containerName, blob);
      assertEquals(blobStore.blobExists(containerName, inFile.getName()),true);
      assertEquals(blobStore.countBlobs(containerName),1);
      assertEquals(blobStore.list(containerName).size(),1);
      blob = blobStore.getBlob(containerName, inFile.getName());      
      System.out.println("blob: after put "+blob);
      System.out.println("blob payload: after put "+blob.getPayload());
      System.out.println("blob metadata: after put "+blob.getMetadata());
      System.out.println("blobStore.blobMetadata: after put "+blobStore.blobMetadata(containerName, inFile.getName()));
      System.out.println("blobStore.blobMetadata.getContentMetadata(): after put "+blobStore.blobMetadata(containerName, inFile.getName()).getContentMetadata().toString());
      Payload payloadOut = blob.getPayload();
      validateDataObjectPayload(inFile,payloadOut);
      
      //payloadOut.getInput().
      blobStore.removeBlob(containerName, inFile.getName());
      assertEquals(blobStore.blobExists(containerName, inFile.getName()),false);
      assertEquals(blobStore.countBlobs(containerName),0);
      assertEquals(blobStore.list(containerName).isEmpty(),true);      
      logger.info("delete: " + containerName);
      blobStore.deleteContainer(containerName);
      assertEquals(blobStore.containerExists(containerName),false);
      assertEquals(blobStore.countBlobs("/"),--rootBlobCount);
      
      /*
      containerName = "MyContainerNoSlash";
      logger.info("create: " + containerName);
      assertEquals(blobStore.createContainerInLocation(null,containerName),true);
      assertEquals(blobStore.containerExists(containerName),true);
      for(StorageMetadata md: blobStore.list() ) {
      	System.out.println("md: "+md);      	
      }
      
      logger.info("delete: " + containerName);
      blobStore.deleteContainer(containerName);
      */
  
      /*
      try {
         logger.info(container.toString());
         logger.info("exists: "+api.containerExists(containerName));
         assertEquals(container.getObjectType(), ObjectTypes.CONTAINER);
         assertNotNull(container.getObjectID());
         assertNotNull(container.getObjectName());
         assertNotNull(container.getUserMetadata());
         Map<String, String> pContainerMetaDataOut = container.getUserMetadata();
         keys = userMetaDataIn.keySet().iterator();
         while (keys.hasNext()) {
            String key = keys.next();
            assertEquals(pContainerMetaDataOut.containsKey(key), true);
            assertEquals(pContainerMetaDataOut.get(key), userMetaDataIn.get(key));
         }
         logger.info("UserMetaData: " + container.getUserMetadata());
         assertNotNull(container.getSystemMetadata());
         logger.info("SystemMetaData: " + container.getSystemMetadata());
         assertNotNull(container.getACLMetadata());
         List<Map<String, String>> aclMetadataOut = container.getACLMetadata();
         logger.info("ACLMetaData: ");
         for (Map<String, String> aclMap : aclMetadataOut) {
            logger.info(aclMap.toString());
         }
         container = api.get("/");
         assertNotNull(container);
         logger.info("root container: " + container);
         assertEquals(container.getChildren().contains(containerName), true);
         logger.info("exists: "+api.containerExists(containerName));

      } finally {
         logger.info("deleteContainer: " + containerName);
         for (String containerChild : api.get(containerName).getChildren()) {
            logger.info("Deleting " + containerChild);
            api.delete(containerChild);
         }
         api.delete(containerName);
         container = api.get("/");
         logger.info("root container: " + container.toString());
         assertEquals(container.getChildren().contains(containerName), false);
         //logger.info("exists: "+api.containerExists(containerName));
      }
      */

   }
   private void validateDataObjectPayload(File fileIn, Payload payloadOut) throws IOException {
   	File tempDir = Files.createTempDir();   	
      File fileOut = new File(tempDir, fileIn.getName());
      FileOutputStream fos = new FileOutputStream(fileOut);
      ByteStreams.copy(payloadOut.getInput(), fos);
      fos.flush();
      fos.close();
      assertEquals(Files.equal(fileOut, fileIn), true);
      fileOut.delete();
      tempDir.delete();

   }


}
