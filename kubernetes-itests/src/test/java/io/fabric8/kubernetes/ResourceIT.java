/**
 * Copyright (C) 2015 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.fabric8.kubernetes;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.kubernetes.api.model.PodListBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.apache.commons.lang.RandomStringUtils;
import org.arquillian.cube.kubernetes.api.Session;
import org.arquillian.cube.kubernetes.impl.requirement.RequiresKubernetes;
import org.arquillian.cube.requirement.ArquillianConditionalRunner;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(ArquillianConditionalRunner.class)
@RequiresKubernetes
public class ResourceIT {
  @ArquillianResource
  KubernetesClient client;

  @ArquillianResource
  Session session;

  private Pod pod1, pod2;

  private String currentNamespace;

  @Before
  public void init() {
    currentNamespace = session.getNamespace();
    pod1 = new PodBuilder()
      .withNewMetadata().withName("resource-pod-" + RandomStringUtils.randomAlphanumeric(6).toLowerCase()).endMetadata()
      .withNewSpec()
      .addNewContainer().withName("nginx").withImage("nginx").endContainer()
      .endSpec()
      .build();
    pod2 = new PodBuilder()
      .withNewMetadata().withName("resource-pod-" + RandomStringUtils.randomAlphanumeric(6).toLowerCase()).endMetadata()
      .withNewSpec()
      .addNewContainer().withName("httpd").withImage("httpd").endContainer()
      .endSpec()
      .build();

    client.resource(pod1).inNamespace(currentNamespace).createOrReplace();
    client.resource(pod2).inNamespace(currentNamespace).createOrReplace();
  }

  @Test
  public void get() {
    assertNotNull(client.pods().inNamespace(currentNamespace).withName(pod1.getMetadata().getName()).get());
    assertNotNull(client.pods().inNamespace(currentNamespace).withName(pod2.getMetadata().getName()).get());
  }

  @Test
  public void list() {
    Pod listPod1 = new PodBuilder()
      .withNewMetadata().withName("pod3").endMetadata()
      .withNewSpec()
      .addNewContainer().withName("nginx").withImage("nginx").endContainer()
      .endSpec()
      .build();
    Pod listPod2 = new PodBuilder()
      .withNewMetadata().withName("pod4").endMetadata()
      .withNewSpec()
      .addNewContainer().withName("httpd").withImage("httpd").endContainer()
      .endSpec()
      .build();
    client.resourceList(new PodListBuilder().withItems(listPod1, listPod2).build())
      .inNamespace(currentNamespace)
      .apply();

    assertTrue(client.pods().inNamespace(currentNamespace).withName("pod3") != null);
    assertTrue(client.pods().inNamespace(currentNamespace).withName("pod4") != null);

    boolean bDeleted = client.resourceList(new PodListBuilder().withItems(listPod1, listPod2).build())
      .inNamespace(currentNamespace)
      .delete();
    assertTrue(bDeleted);
  }

  @Test
  public void delete() {
    assertTrue(client.resource(pod1).inNamespace(currentNamespace).delete());
    assertTrue(client.resource(pod2).inNamespace(currentNamespace).delete());
  }

  @After
  public void cleanup() {
    client.pods().inNamespace(currentNamespace).delete();
  }
}
