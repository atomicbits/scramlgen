/*
 *
 *  (C) Copyright 2015 Atomic BITS (http://atomicbits.io).
 *
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the GNU Affero General Public License
 *  (AGPL) version 3.0 which accompanies this distribution, and is available in
 *  the LICENSE file or at http://www.gnu.org/licenses/agpl-3.0.en.html
 *  Alternatively, you may also use this code under the terms of the
 *  Scraml Commercial License, see http://scraml.io
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  Affero General Public License or the Scraml Commercial License for more
 *  details.
 *
 *  Contributors:
 *      Peter Rigole
 *
 */

package io.atomicbits.scraml.dsl.androidjavajackson;

import io.atomicbits.scraml.dsl.androidjavajackson.json.Json;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by peter on 19/08/15.
 */
// ToDo: rename to MethodSegment to Request (which is what it is)
public abstract class MethodSegment<B, R> extends Segment {

    private B body;
    private RequestBuilder requestBuilder;

    protected MethodSegment(Method method,
                            B theBody,
                            Map<String, HttpParam> queryParams,
                            TypedQueryParams queryString,
                            Map<String, HttpParam> formParams,
                            List<BodyPart> multipartParams,
                            BinaryRequest binaryRequest,
                            String expectedAcceptHeader,
                            String expectedContentTypeHeader,
                            RequestBuilder req) {

        this.body = theBody;

        RequestBuilder requestBuilder = req.fold(); // We're at the end of the resource path, we can fold the resource here.
        requestBuilder.setMethod(method);
        Map<String, HttpParam> actualQueryParams;
        if (queryString != null) {
            actualQueryParams = queryString.getParams();
        } else {
            actualQueryParams = queryParams;
        }
        requestBuilder.setQueryParameters(removeNullParams(actualQueryParams));
        requestBuilder.setFormParameters(removeNullParams(formParams));
        requestBuilder.setMultipartParams(multipartParams);
        requestBuilder.setBinaryRequest(binaryRequest);

        String accept = "Accept";
        String contentType = "Content-Type";

        if (expectedAcceptHeader != null && !requestBuilder.getHeaderMap().hasKey(accept)) {
            requestBuilder.getHeaderMap().addHeader(accept, expectedAcceptHeader);
        }

        if (expectedContentTypeHeader != null && !requestBuilder.getHeaderMap().hasKey(contentType)) {
            requestBuilder.getHeaderMap().addHeader(contentType, expectedContentTypeHeader);
        }

        // set request charset if necessary
        setRequestCharset(requestBuilder, contentType);

        this.requestBuilder = requestBuilder;
    }


    protected B getBody() {
        return body;
    }

    protected String getPlainStringBody() {
        String stringBody = null;
        if (this.getBody() != null) {
            stringBody = this.getBody().toString();
        }
        return stringBody;
    }

    protected String getJsonStringBody(String canonicalContentType) {
        String stringBody = null;
        if (this.getBody() != null) {
            stringBody = Json.writeBodyToString(this.getBody(), canonicalContentType);
        }
        return stringBody;
    }

    protected Boolean isFormUrlEncoded() {
        List<String> contentValues = requestBuilder.getHeaderMap().getValues("Content-Type");
        Boolean isFormUrlEncoded = false;
        for (String contentValue : contentValues) {
            if (contentValue.contains("application/x-www-form-urlencoded")) isFormUrlEncoded = true;
        }
        return isFormUrlEncoded;
    }

    protected String jsonBodyToString(String canonicalContentType) {
        if (getRequestBuilder().getFormParameters().isEmpty() && getBody() != null && isFormUrlEncoded()) {
            Map<String, HttpParam> formPs = Json.toFormUrlEncoded(getBody());
            getRequestBuilder().setFormParameters(formPs);
            return null;
        } else {
            return getJsonStringBody(canonicalContentType);
        }
    }

    protected RequestBuilder getRequestBuilder() {
        return requestBuilder;
    }


    /**
     * see https://www.w3.org/Protocols/rfc1341/4_Content-Type.html
     * charset is case-insensitive:
     * * http://stackoverflow.com/questions/7718476/are-http-headers-content-type-c-case-sensitive
     * * https://www.w3.org/TR/html4/charset.html#h-5.2.1
     */
    private void setRequestCharset(RequestBuilder requestBuilder, String contentType) {
        if (requestBuilder.getHeaderMap().hasKey(contentType)) {
            List<String> contentTypeValues = requestBuilder.getHeaderMap().getValues(contentType);
            Boolean hasCharset = false;
            Boolean isBinary = false;
            for (String value : contentTypeValues) {
                if (value.toLowerCase().contains("charset")) {
                    hasCharset = true;
                }
                if (value.toLowerCase().contains("octet-stream")) {
                    isBinary = true;
                }
            }
            if (!isBinary && !hasCharset && !contentTypeValues.isEmpty()) {
                Charset defaultCharset = requestBuilder.getClient().getConfig().getRequestCharset();
                if (defaultCharset != null) {
                    String value = contentTypeValues.get(0);
                    String updatedValue = value + "; charset=" + defaultCharset.name();
                    contentTypeValues.set(0, updatedValue);
                    requestBuilder.getHeaderMap().setHeader(contentType, contentTypeValues);
                }
            }
        }
    }

    /**
     * Java 1.7 specific
     */
    private Map<String, HttpParam> removeNullParams(Map<String, HttpParam> map) {
        Map<String, HttpParam> nonNullParams = new HashMap<>();
        if (map != null) {
            for (Map.Entry<String, HttpParam> entry : map.entrySet()) {
                String key = entry.getKey();
                HttpParam value = entry.getValue();
                if (value != null && value.nonEmpty()) nonNullParams.put(key, value);
            }
        }
        return nonNullParams;
    }

}
