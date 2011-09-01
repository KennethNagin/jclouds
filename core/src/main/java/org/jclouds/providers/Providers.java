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
package org.jclouds.providers;

import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.find;

import com.google.common.base.Predicates;

import java.util.NoSuchElementException;
import java.util.ServiceLoader;

/**
 * The Providers class provides static methods for accessing providers.
 * 
 * @author Jeremy Whitlock <jwhitlock@apache.org>
 */
public class Providers {

   /**
    * Returns the providers located on the classpath via {@link java.util.ServiceLoader}.
    * 
    * @return all available providers loaded from classpath via ServiceLoader
    */
   private static Iterable<ProviderMetadata> fromServiceLoader() {
      return ServiceLoader.load(ProviderMetadata.class);
   }

   /**
    * Returns all available providers.
    * 
    * @return all available providers
    */
   public static Iterable<ProviderMetadata> all() {
      return fromServiceLoader();
   }

   /**
    * Returns the first provider with the provided id
    * 
    * @param id
    *           the id of the provider to return
    * 
    * @return the provider with the given id
    * 
    * @throws NoSuchElementException
    *            whenever there are no providers with the provided id
    */
   public static ProviderMetadata withId(String id) throws NoSuchElementException {
      return find(all(), ProviderPredicates.id(id));
   }

   /**
    * Returns the providers that are of type
    * {@link org.jclouds.providers.ProviderMetadata#BLOBSTORE_TYPE}.
    * 
    * @return the blobstore providers
    */
   public static Iterable<ProviderMetadata> allBlobStore() {
      return filter(all(), ProviderPredicates.type(ProviderMetadata.BLOBSTORE_TYPE));
   }

   /**
    * Returns the providers that are of type
    * {@link org.jclouds.providers.ProviderMetadata#COMPUTE_TYPE}.
    * 
    * @return the compute service providers
    */
   public static Iterable<ProviderMetadata> allCompute() {
      return filter(all(), ProviderPredicates.type(ProviderMetadata.COMPUTE_TYPE));
   }

   /**
    * Returns the providers that are of type
    * {@link org.jclouds.providers.ProviderMetadata#QUEUE_TYPE}.
    * 
    * @return the queue service providers
    */
   public static Iterable<ProviderMetadata> allQueue() {
      return filter(all(), ProviderPredicates.type(ProviderMetadata.QUEUE_TYPE));
   }

   /**
    * Returns the providers that are of type
    * {@link org.jclouds.providers.ProviderMetadata#TABLE_TYPE}.
    * 
    * @return the table service providers
    */
   public static Iterable<ProviderMetadata> allTable() {
      return filter(all(), ProviderPredicates.type(ProviderMetadata.TABLE_TYPE));
   }

   /**
    * Returns the providers that are of type
    * {@link org.jclouds.providers.ProviderMetadata#LOADBALANCER_TYPE}.
    * 
    * @return the load balancer service providers
    */
   public static Iterable<ProviderMetadata> allLoadBalancer() {
      return filter(all(), ProviderPredicates.type(ProviderMetadata.LOADBALANCER_TYPE));
   }

   /**
    * Returns the providers that are of the provided type.
    * 
    * @param type
    *           the type to providers to return
    * 
    * @return the providers of the provided type
    */
   public static Iterable<ProviderMetadata> ofType(String type) {
      return filter(all(), ProviderPredicates.type(type));
   }

   /**
    * Returns the providers that are bound to the same location as the given ISO 3166 code regardless of type.
    * 
    * @param isoCode
    *                the ISO 3166 code to filter providers by
    * 
    * @return the providers bound by the given ISO 3166 code
    */
   public static Iterable<ProviderMetadata> boundedByIso3166Code(String iso3166Code) {
      return filter(all(), ProviderPredicates.boundedByIso3166Code(iso3166Code));
   }

   /**
    * Returns the providers that are bound to the same location as the given ISO 3166 code and of the given type.
    * 
    * @param iso3166Code
    *                    the ISO 3166 code to filter providers by
    * @param type
    *             the type to filter providers by
    * 
    * @return the providers bound by the given ISO 3166 code and of the proper type
    */
   public static Iterable<ProviderMetadata> boundedByIso3166Code(String iso3166Code, String type) {
      return filter(all(), Predicates.and(ProviderPredicates.boundedByIso3166Code(iso3166Code),
            ProviderPredicates.type(type)));
   }

   /**
    * Returns the providers that have at least one common ISO 3166 code in common regardless of type.
    * 
    * @param providerMetadata
    *                         the provider metadata to use to filter providers by
    * 
    * @return the providers that share at least one common ISO 3166 code
    */
   public static Iterable<ProviderMetadata> collocatedWith(ProviderMetadata providerMetadata) {
      return filter(all(), ProviderPredicates.intersectingIso3166Code(providerMetadata));
   }

   /**
    * Returns the providers that have at least one common ISO 3166 code and are of the given type.
    * 
    * @param providerMetadata
    *                         the provider metadata to use to filter providers by
    * @param type
    *             the type to filter providers by
    * 
    * @return the providers that share at least one common ISO 3166 code and of the given type
    */
   public static Iterable<ProviderMetadata> collocatedWith(ProviderMetadata providerMetadata, String type) {
      return filter(all(), Predicates.and(ProviderPredicates.intersectingIso3166Code(providerMetadata),
            ProviderPredicates.type(type)));
   }
}
