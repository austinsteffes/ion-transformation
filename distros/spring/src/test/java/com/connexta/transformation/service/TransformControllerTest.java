/**
 * Copyright (c) Connexta
 *
 * <p>This is free software: you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or any later version.
 *
 * <p>This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details. A copy of the GNU Lesser General Public
 * License is distributed along with this program and can be found at
 * <http://www.gnu.org/licenses/lgpl.html>.
 */
package com.connexta.transformation.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.connexta.transformation.commons.api.status.Transformation;
import com.connexta.transformation.commons.inmemory.InMemoryTransformationManager;
import com.connexta.transformation.rest.models.TransformRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

@RunWith(SpringRunner.class)
@WebMvcTest(TransformController.class)
public class TransformControllerTest {

  private static final String TEST_URI = "http://test:9000";

  private static final String ACCEPT_VERSION = "Accept-Version";

  private static final String ACCEPT_VERSION_NUM = "0.1.0";

  private ObjectMapper mapper = new ObjectMapper();

  @Autowired private MockMvc mockMvc;

  @MockBean InMemoryTransformationManager manager;

  @Test
  public void testCreatedResponseEntity() throws Exception {
    Transformation transformation = mock(Transformation.class);
    when(transformation.getTransformId()).thenReturn("id123");
    when(manager.createTransform(any(), any(), any())).thenReturn(transformation);
    this.mockMvc
        .perform(
            post("/transform")
                .header(ACCEPT_VERSION, ACCEPT_VERSION_NUM)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    mapper.writeValueAsString(
                        new TransformRequest()
                            .currentLocation(new URI(TEST_URI))
                            .finalLocation(new URI(TEST_URI))
                            .metacardLocation(new URI(TEST_URI))))
                .characterEncoding(StandardCharsets.UTF_8.name()))
        .andExpect(status().isCreated())
        .andExpect(header().string("Location", "http://localhost/transform/id123"))
        .andExpect(redirectedUrl("http://localhost/transform/id123"));

  }

  @Test
  public void testCurrentLocationCannotBeNull400() throws Exception {
    this.mockMvc
        .perform(
            post("/transform")
                .header(ACCEPT_VERSION, ACCEPT_VERSION_NUM)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    mapper.writeValueAsString(
                        new TransformRequest()
                            .currentLocation(null)
                            .finalLocation(new URI(TEST_URI))
                            .metacardLocation(new URI(TEST_URI))))
                .characterEncoding(StandardCharsets.UTF_8.name()))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void testFinalLocationNullCannotBe400() throws Exception {
    this.mockMvc
        .perform(
            post("/transform")
                .header(ACCEPT_VERSION, ACCEPT_VERSION_NUM)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    mapper.writeValueAsString(
                        new TransformRequest()
                            .currentLocation(new URI(TEST_URI))
                            .finalLocation(null)
                            .metacardLocation(new URI(TEST_URI))))
                .characterEncoding(StandardCharsets.UTF_8.name()))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void testMetacardlLocationCannotBeNull400() throws Exception {
    this.mockMvc
        .perform(
            post("/transform")
                .header(ACCEPT_VERSION, ACCEPT_VERSION_NUM)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    mapper.writeValueAsString(
                        new TransformRequest()
                            .currentLocation(new URI(TEST_URI))
                            .finalLocation(new URI(TEST_URI))
                            .metacardLocation(null)))
                .characterEncoding(StandardCharsets.UTF_8.name()))
        .andExpect(status().isBadRequest());
  }

  @Test(expected = NullPointerException.class)
  public void testManagerCannotBeNull() {
    new TransformController(null);
  }
}
