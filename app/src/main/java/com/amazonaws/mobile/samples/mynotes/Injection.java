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
package com.amazonaws.mobile.samples.mynotes;

import com.amazonaws.mobile.samples.mynotes.repository.NotesRepository;
import com.amazonaws.mobile.samples.mynotes.services.AnalyticsService;
import com.amazonaws.mobile.samples.mynotes.services.DataService;
import com.amazonaws.mobile.samples.mynotes.services.mock.MockAnalyticsService;
import com.amazonaws.mobile.samples.mynotes.services.mock.MockDataService;

/**
 * This is a "fake" dependency injection system.
 */
public class Injection {
    private static DataService dataService = null;
    private static AnalyticsService analyticsService = null;
    private static NotesRepository notesRepository = null;

    public static synchronized DataService getDataService() {
        initialize();
        return dataService;
    }

    public static synchronized AnalyticsService getAnalyticsService() {
        initialize();
        return analyticsService;
    }

    public static synchronized NotesRepository getNotesRepository() {
        initialize();
        return notesRepository;
    }

    private static synchronized void initialize() {
        if (analyticsService == null) {
            analyticsService = new MockAnalyticsService();
        }

        if (dataService == null) {
            dataService = new MockDataService();
        }

        if (notesRepository == null) {
            notesRepository = new NotesRepository(dataService);
        }
    }
}
