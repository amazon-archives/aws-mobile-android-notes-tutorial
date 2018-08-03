/*
Copyright 2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.

Permission is hereby granted, free of charge, to any person obtaining a copy of this
software and associated documentation files (the "Software"), to deal in the Software
without restriction, including without limitation the rights to use, copy, modify,
merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
permit persons to whom the Software is furnished to do so.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.amazonaws.mobile.samples.mynotes.models;

import java.util.List;

/**
 * Model class used as the response to an API call.  In this particular instance, it's
 * a paged list response where the next page is denoted by a nextToken - ostensibly a
 * string, but really an opaque blob that you should not mess with - just submit it along
 * with the API call to get the next page.
 * @param <T> the type of elements within the response
 */
public class PagedListConnectionResponse<T> {
    private List<T> items;
    private String nextToken;

    public PagedListConnectionResponse(List<T> items, String nextToken) {
        this.items = items;
        this.nextToken = nextToken;
    }

    public List<T> getItems() {
        return items;
    }

    public String getNextToken() {
        return nextToken;
    }
}
