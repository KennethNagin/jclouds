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
package org.jclouds.cloudstack.predicates;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Predicates.alwaysTrue;
import static com.google.common.base.Predicates.and;
import static com.google.common.base.Predicates.not;

import org.jclouds.cloudstack.domain.NetworkType;
import org.jclouds.cloudstack.domain.Zone;

import com.google.common.base.Predicate;

/**
 * 
 * @author Adrian Cole
 */
public class ZonePredicates {
   /**
    * 
    * @return true, if the zone supports {@link NetworkType.ADVANCED}
    */
   public static Predicate<Zone> supportsAdvancedNetworks() {
      return new Predicate<Zone>() {

         @Override
         public boolean apply(Zone input) {
            return NetworkType.ADVANCED.equals(checkNotNull(input, "zone").getNetworkType());
         }

         @Override
         public String toString() {
            return "supportsAvancedNetworks()";
         }
      };
   }

   /**
    * 
    * @return true, if the zone supports security groups
    */
   public static Predicate<Zone> supportsSecurityGroups() {
      return new Predicate<Zone>() {

         @Override
         public boolean apply(Zone input) {
            return input.isSecurityGroupsEnabled();
         }

         @Override
         public String toString() {
            return "supportsSecurityGroups()";
         }
      };
   }

   /**
    * 
    * @return true, if the zone supports creation of GuestVirtual Networks
    */
   public static Predicate<Zone> supportsGuestVirtualNetworks() {
      return and(supportsAdvancedNetworks(), not(supportsSecurityGroups()));
   }

   /**
    * 
    * @return always returns true.
    */
   public static Predicate<Zone> any() {
      return alwaysTrue();
   }
}
