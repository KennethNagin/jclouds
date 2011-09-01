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
package org.jclouds.collect;

import java.util.Map.Entry;

import javax.annotation.Nullable;

import com.google.common.base.Objects;


public abstract class AbstractMapEntry<K, V> implements Entry<K, V> {

   public abstract K getKey();

   public abstract V getValue();

   public V setValue(V value) {
      throw new UnsupportedOperationException();
   }

   @Override
   public boolean equals(@Nullable Object object) {
      if (object instanceof Entry) {
         Entry<?, ?> that = (Entry<?, ?>) object;
         return Objects.equal(this.getKey(), that.getKey()) && Objects.equal(this.getValue(), that.getValue());
      }
      return false;
   }

   @Override
   public int hashCode() {
      K k = getKey();
      V v = getValue();
      return ((k == null) ? 0 : k.hashCode()) ^ ((v == null) ? 0 : v.hashCode());
   }

   /**
    * Returns a string representation of the form <code>{key}={value}</code>.
    */
   @Override
   public String toString() {
      return getKey() + "=" + getValue();
   }
}
