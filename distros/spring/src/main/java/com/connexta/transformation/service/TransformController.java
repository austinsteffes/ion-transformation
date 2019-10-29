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

import com.connexta.transformation.commons.api.exceptions.TransformationException;
import com.connexta.transformation.commons.api.status.Transformation;
import com.connexta.transformation.commons.inmemory.InMemoryTransformationManager;
import com.connexta.transformation.rest.models.TransformRequest;
import com.connexta.transformation.rest.spring.TransformApiTransform;
import com.google.common.base.Preconditions;
import java.net.URI;
import java.net.URISyntaxException;
import javax.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * Implementation of the Transformation RESTful POST/TRANSFORM service
 */
@RestController
@CrossOrigin(origins = "*")
public class TransformController implements TransformApiTransform {

  private static final Logger LOGGER = LoggerFactory.getLogger(TransformController.class);

  private static final String ACCEPT_VERSION = "Accept-Version";

  private final InMemoryTransformationManager transformationManager;

  @Autowired
  public TransformController(InMemoryTransformationManager transformationManager) {
    Preconditions.checkNotNull(transformationManager, "TransformationManager cannot be null");
    this.transformationManager = transformationManager;
  }

  /**
   * Handles application/json POST transform requests to the /transform context. This method
   * handles: - Validating the message - Forwarding the request to the service lookup service.
   * Returns an HTTP Status Code of 201 Created on success and a location URI; otherwise, an error
   * status code.
   */
  @RequestMapping(
      value = "/transform",
      consumes = {MediaType.APPLICATION_JSON_VALUE},
      method = RequestMethod.POST)
  public ResponseEntity<Void> transform(
      @RequestHeader(ACCEPT_VERSION) String acceptVersion,
      @Valid @RequestBody TransformRequest transformRequest)
      throws URISyntaxException, TransformationException {

    LOGGER.debug("{}: {}", ACCEPT_VERSION, acceptVersion);
    return postURI(transformRequest);
  }

  private ResponseEntity<Void> postURI(TransformRequest transformRequest)
      throws TransformationException, URISyntaxException {

    ResponseEntity<Void> response;
    Transformation trans =
        transformationManager.createTransform(
            transformRequest.getCurrentLocation(),
            transformRequest.getFinalLocation(),
            transformRequest.getMetacardLocation());

    URI s = ServletUriComponentsBuilder.fromCurrentRequest().build().toUri();
    URI uri = new URI(String.format("%s/%s", s.toString(), trans.getTransformId()));
    response = ResponseEntity.created(uri).build();

    return response;
  }
}
