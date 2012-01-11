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
package org.jclouds.aws.ec2.services;

import static org.testng.Assert.assertNotNull;

import java.util.Properties;
import java.util.Set;

import org.jclouds.aws.ec2.AWSEC2AsyncClient;
import org.jclouds.aws.ec2.AWSEC2Client;
import org.jclouds.compute.BaseVersionedServiceLiveTest;
import org.jclouds.compute.ComputeServiceContextFactory;
import org.jclouds.ec2.domain.Reservation;
import org.jclouds.ec2.domain.RunningInstance;
import org.jclouds.logging.log4j.config.Log4JLoggingModule;
import org.jclouds.rest.RestContext;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeGroups;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Module;

/**
 * Tests behavior of {@code EC2Client}
 * 
 * @author Adrian Cole
 */
@Test(groups = "live", singleThreaded = true)
public class AWSInstanceClientLiveTest extends BaseVersionedServiceLiveTest {
   public AWSInstanceClientLiveTest() {
      provider = "aws-ec2";
   }
   
   public static final String PREFIX = System.getProperty("user.name") + "-ec2";

   private AWSInstanceClient client;
   private RestContext<AWSEC2Client, AWSEC2AsyncClient> context;
   
   @BeforeGroups(groups = { "live" })
   public void setupClient() {
      setupCredentials();
      Properties overrides = setupProperties();
      context = new ComputeServiceContextFactory().createContext(provider,
            ImmutableSet.<Module> of(new Log4JLoggingModule()), overrides).getProviderSpecificContext();
      client = context.getApi().getInstanceServices();

   }

   @Test
   void testDescribeInstances() {
      for (String region : context.getApi().getAvailabilityZoneAndRegionServices().describeRegions().keySet()) {
         Set<? extends Reservation<? extends RunningInstance>> allResults = client.describeInstancesInRegion(region);
         assertNotNull(allResults);
         assert allResults.size() >= 0 : allResults.size();
      }
   }

   @AfterTest
   public void shutdown() {
      context.close();
   }
}
