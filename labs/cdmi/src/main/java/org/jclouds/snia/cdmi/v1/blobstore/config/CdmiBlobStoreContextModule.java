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
package org.jclouds.snia.cdmi.v1.blobstore.config;


import org.jclouds.blobstore.AsyncBlobStore;
import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.attr.ConsistencyModel;
import org.jclouds.blobstore.config.BlobStoreMapModule;
//import org.jclouds.blobstore.internal.BaseBlobStore;
import org.jclouds.snia.cdmi.v1.blobstore.CdmiBlobStore;
//import org.jclouds.snia.cdmi.v1.blobstore.CdmiBlobStoreImpl;
import org.jclouds.snia.cdmi.v1.blobstore.CdmiAsyncBlobStoreImpl;
import org.jclouds.snia.cdmi.v1.blobstore.CdmiAsyncBlobStore;
//import org.jclouds.snia.cdmi.v1.blobstore.CdmiBlobStoreOld;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

/**
 * Configures the {@link CloudFilesBlobStoreContext}; requires
 * {@link SwiftAsyncBlobStore} bound.
 *
 * @author Kenneth Nagin
 */
public class CdmiBlobStoreContextModule extends AbstractModule {

   @Override
   protected void configure() {
      install(new BlobStoreMapModule());
      bind(ConsistencyModel.class).toInstance(ConsistencyModel.STRICT);
      bind(AsyncBlobStore.class).to(CdmiAsyncBlobStoreImpl.class).in(Scopes.SINGLETON);
      bind(BlobStore.class).to(CdmiBlobStore.class).in(Scopes.SINGLETON);
   }
}
