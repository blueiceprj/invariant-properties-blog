/*
 * This code was written by Bear Giles <bgiles@coyotesong.com> and he
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Any contributions made by others are licensed to this project under
 * one or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * 
 * Copyright (c) 2013 Bear Giles <bgiles@coyotesong.com>
 */
package com.invariantproperties.sandbox.student.webservice.client;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.mockito.Mockito;

import com.invariantproperties.sandbox.student.domain.Student;
import com.invariantproperties.sandbox.student.webservice.client.impl.StudentFinderRestClientImpl;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

/**
 * Unit tests for StudentFinderRestClientImpl. Remember that we want to test the
 * behavior, not the implementation.
 * 
 * @author Bear Giles <bgiles@coyotesong.com>
 */
public class StudentFinderRestClientImplTest {
    private static final String UUID = "uuid";

    @Test
    public void testGetAllStudentsEmpty() {
        StudentFinderRestClient client = new FinderStudentMock(200, new Student[0]);
        Student[] results = client.getAllStudents();
        assertEquals(0, results.length);
    }

    @Test
    public void testGetAllStudentsNonEmpty() {
        Student student = new Student();
        student.setUuid(UUID);
        StudentFinderRestClient client = new FinderStudentMock(200, new Student[] { student });
        Student[] results = client.getAllStudents();
        assertEquals(1, results.length);
    }

    @Test(expected = RestClientFailureException.class)
    public void testGetAllStudentsError() {
        StudentFinderRestClient client = new FinderStudentMock(500, null);
        client.getAllStudents();
    }

    @Test
    public void testGetStudent() {
        Student expected = new Student();
        expected.setUuid(UUID);
        StudentFinderRestClient client = new FinderStudentMock(200, expected);
        Student actual = client.getStudent(expected.getUuid());
        assertEquals(expected.getUuid(), actual.getUuid());
        // assertEquals(StudentRestClientMock.RESOURCE + student.getUuid(),
        // results.getSelf());
    }

    @Test(expected = ObjectNotFoundException.class)
    public void testGetStudentMissing() {
        StudentFinderRestClient client = new FinderStudentMock(404, null);
        client.getStudent(UUID);
    }

    @Test(expected = RestClientFailureException.class)
    public void testGetStudentError() {
        StudentFinderRestClient client = new FinderStudentMock(500, null);
        client.getStudent(UUID);
    }
}

/**
 * StudentFinderRestClientImpl extended to mock jersey API. This class requires
 * implementation details.
 */
class FinderStudentMock extends StudentFinderRestClientImpl {
    static final String RESOURCE = "test://rest/student/";
    private Client client;
    private WebResource webResource;
    private WebResource.Builder webResourceBuilder;
    private ClientResponse response;
    private final int status;
    private final Object results;

    FinderStudentMock(int status, Object results) {
        super(RESOURCE);
        this.status = status;
        this.results = results;
    }

    /**
     * Override createClient() so it returns mocked object. These expectations
     * will handle basic CRUD operations, more advanced functionality will
     * require inspecting JSON payload of POST call.
     */
    @SuppressWarnings("unchecked")
    @Override
    Client createClient() {
        client = Mockito.mock(Client.class);
        webResource = Mockito.mock(WebResource.class);
        webResourceBuilder = Mockito.mock(WebResource.Builder.class);
        response = Mockito.mock(ClientResponse.class);
        when(client.resource(any(String.class))).thenReturn(webResource);
        when(webResource.accept(any(String.class))).thenReturn(webResourceBuilder);
        when(webResource.type(any(String.class))).thenReturn(webResourceBuilder);
        when(webResourceBuilder.accept(any(String.class))).thenReturn(webResourceBuilder);
        when(webResourceBuilder.type(any(String.class))).thenReturn(webResourceBuilder);
        when(webResourceBuilder.get(eq(ClientResponse.class))).thenReturn(response);
        when(webResourceBuilder.post(eq(ClientResponse.class), any(String.class))).thenReturn(response);
        when(webResourceBuilder.put(eq(ClientResponse.class), any(String.class))).thenReturn(response);
        when(webResourceBuilder.delete(eq(ClientResponse.class))).thenReturn(response);
        when(response.getStatus()).thenReturn(status);
        when(response.getEntity(any(Class.class))).thenReturn(results);
        return client;
    }
}
