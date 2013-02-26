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
package org.jclouds.snia.cdmi.v1.config;

/**
 * Configuration properties and constants used in SNIA CDMI connections.
 * 
 * @author Kenneth Nagin
 */
public interface CDMIProperties {
   /**
    * Property type for choosing which authorization type is used when accessing
    * provider. For valid values:
    * 
    * @see org.jclouds.snia.cdmi.v1.filters.AuthTypes
    */
   public static final String AUTHTYPE = "jclouds.cdmi.authType";
   public static final String ENDPOINT = "cdmi.endpoint";

}
