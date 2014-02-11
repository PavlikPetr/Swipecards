/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.topface.topface.requests.multipart;

import java.util.Iterator;

/**
 * <p> This class provides support for accessing the headers for a file or form
 * item that was received within a <code>multipart/form-data</code> POST
 * request.</p>
 *
 * @author Michael C. Macaluso
 * @since 1.3
 */
@SuppressWarnings("UnusedDeclaration")
public interface FileItemHeaders {
    /**
     * Returns the value of the specified part header as a <code>String</code>.
     * If the part did not include a header of the specified name, this method
     * return <code>null</code>.  If there are multiple headers with the same
     * name, this method returns the first header in the item.  The header
     * name is case insensitive.
     *
     * @param name a <code>String</code> specifying the header name
     * @return a <code>String</code> containing the value of the requested
     * header, or <code>null</code> if the item does not have a header
     * of that name
     */
    String getHeader(String name);

    /**
     * <p>
     * Returns all the values of the specified item header as an
     * <code>Enumeration</code> of <code>String</code> objects.
     * </p>
     * <p>
     * If the item did not include any headers of the specified name, this
     * method returns an empty <code>Enumeration</code>. The header name is
     * case insensitive.
     * </p>
     *
     * @param name a <code>String</code> specifying the header name
     * @return an <code>Enumeration</code> containing the values of the
     * requested header. If the item does not have any headers of
     * that name, return an empty <code>Enumeration</code>
     */
    Iterator getHeaders(String name);

    /**
     * <p>
     * Returns an <code>Enumeration</code> of all the header names.
     * </p>
     * <p>
     * If the item did not include any headers of the specified name, this
     * method returns an empty <code>Enumeration</code>. The header name is
     * case insensitive.
     * </p>
     *
     * @return an <code>Enumeration</code> containing the values of the
     * requested header. If the item does not have any headers of
     * that name return an empty <code>Enumeration</code>
     */
    Iterator getHeaderNames();
}