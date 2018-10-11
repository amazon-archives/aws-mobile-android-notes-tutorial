# Add Serverless Backend to the Notes App

In the [previous section](./auth.md) of this tutorial , you added a simple sign-up and sign-in flow to the sample note-taking app with email validation. This tutorial assumes you have completed the previous tutorials. If you jumped to this step, [go back to the beginning](./index.md) and start from there. In this tutorial, you add a NoSQL database to the mobile backend, and then configure a data access service to the note-taking app.

You should be able to complete this section of the tutorial in 45-60 minutes.

## Add a Data Access API to the Backend

1. In Android Studio, switch to the Project view.
2. Right-click on the project, and then select **New** > **Directory**.
3. For directory name, enter `server`, and then choose **OK**.
4. Right-click on the `server` directory, and then select **New** > **File**.
5. For file name, enter `schema.graphql`, and then choose **OK**.
6. Copy the following code into the `schema.graphql` file:

    ```graphql
    type Note @model @auth(rules:[{allow: owner}]) {
        id: ID!
        title: String!
        content: String!
    }
    ```

7. In the terminal window, enter the following commands:

    ```
    $ amplify add api
    ```

    When you run this command the CLI prompts you to make API configuration choices, and stores the configuration in local metadata using an Amazon CloudFormation template.
8. When prompted by the CLI, do the following:
   * Select a service type: **GraphQL**.
   * Choose an authorization type: **Amazon Cognito User Pool**.
   * Do you have an annotated GraphQL schema: **Y**.
   * Provide your schema file path: **./server/schema.graphql**.

9. To deploy the new service, enter the following:

    ```
    $ amplify push
    ```

   When you run this command, the CLI uses your configuration choices to create or update the AWS resources that make up your cloud API backend. Once the services are configured, the CLI creates or updates configuration files in your app to connect it to your services.

10. When prompted by the CLI, do the following:
    * Do you want to generate code for your newly created GraphQL API: **Yes**.
    * Enter the file name pattern of graphql queries, mutations and subscriptions: *(enter return to use default)*
    * Do you want to generate/update all possible GraphQL operations - queries, mutations and subscriptions (Y/n): **Yes**

The AWS CloudFormation template that is generated creates an Amazon DynamoDB table that is protected by Amazon Cognito user pool authentication.  Access is provided by AWS AppSync.  AWS AppSync tags each record that is inserted into the database with the user ID of the authenticated user.  The authenticated user can read only the records that they own.

In addition to updating the `awsconfiguration.json` file, the Amplify CLI generates the `schema.json` file in the `app/src/main/graphql` directory.  The `schema.json` file is required by the AWS Mobile SDK for Android to run code generation for GraphQL operations.

## Add Required Libraries to the Project

Edit the project-level `build.gradle` file and add the AWS AppSync plugin path to the dependencies as follows:

```gradle
dependencies {
    classpath "com.android.tools.build:gradle:$gradle_version"
    classpath "com.amazonaws:aws-android-sdk-appsync-gradle-plugin:2.6.+"

    // NOTE: Do not place your application dependencies here; they belong
    // in the individual module build.gradle files
}
```

Edit the `app/build.gradle` file. Add the AWS AppSync plugin below the other plugins:

```gradle
    apply plugin: 'com.android.application'
    apply plugin: 'com.amazonaws.appsync'
```

Add the AWS AppSync dependencies with the other SDKs.

```gradle
dependencies {

    // . . .

    // AWS SDK for Android
    def aws_version = '2.6.27'
    implementation "com.amazonaws:aws-android-sdk-core:$aws_version"
    implementation "com.amazonaws:aws-android-sdk-auth-core:$aws_version@aar"
    implementation "com.amazonaws:aws-android-sdk-auth-ui:$aws_version@aar"
    implementation "com.amazonaws:aws-android-sdk-auth-userpools:$aws_version@aar"
    implementation "com.amazonaws:aws-android-sdk-cognitoidentityprovider:$aws_version"
    implementation "com.amazonaws:aws-android-sdk-pinpoint:$aws_version"

    // AWS AppSync SDK
    implementation "com.amazonaws:aws-android-sdk-appsync:2.6.+"
}
```

On the upper-right side, choose **Sync Now** to incorporate the dependencies you just declared.

Finally, choose **Build** > **Make project** from the top menu.

> Why is it important to build at this point? To enable your mobile app to send GraphQL commands (mutations and queries) to the AWS AppSync service, it needs classes that represent your APIs. Building your project causes gradle to activate the appsync gradle plugin to generate Java classes from the CLI-generated configuration files.

## Add Permissions to the AndroidManifest.xml

1. To find the app manifest, change the project browser view menu at the top to **Android**, and then open the `app/manifests` folder.
2. Add the `WAKE_LOCK`, `READ_PHONE_STATE`, `WRITE_EXTERNAL_STORAGE`, and `READ_EXTERNAL_STORAGE`: permissions to your project's `AndroidManifest.xml` file.

    ```xml
    <?xml version="1.0" encoding="utf-8"?>
    <manifest xmlns:android="http://schemas.android.com/apk/res/android"
        package="com.amazonaws.mobile.samples.mynotes">

        <uses-permission android:name="android.permission.INTERNET"/>
        <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>
        <uses-permission android:name="android.permission.ACCESS_WIFI_STATE"/>
        <uses-permission android:name="android.permission.WAKE_LOCK" />
        <uses-permission android:name="android.permission.READ_PHONE_STATE" />
        <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
        <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"/>

        <application
            android:name=".NotesApp"
            android:allowBackup="true"
            android:icon="@mipmap/ic_launcher"
            android:label="@string/app_name"
            android:roundIcon="@mipmap/ic_launcher_round"
            android:supportsRtl="true"
            android:theme="@style/AppTheme">

            <!-- . . . -->
        </application>
    </manifest>
    ```

## Create an AWSDataService Class

Data access is proxied through a class that implements the `DataService` interface.  At this point, the data access is provided by the `MockDataService` class that stores a number of notes in memory.  In this section, you replace this class with an `AWSDataService` class that provides access to the API that you recently deployed.

1. Right-click on the `services/aws` folder, and then select **New** > **Java Class**.
2. For class name, enter `AWSDataService`, and then choose **OK**.
3. Replace the contents of the file with the following:

    ```java
    package com.amazonaws.mobile.samples.mynotes.services.aws;

    import android.content.Context;
    import android.util.Log;

    import com.amazonaws.mobile.config.AWSConfiguration;
    import com.amazonaws.amplify.generated.graphql.CreateNoteMutation;
    import com.amazonaws.amplify.generated.graphql.DeleteNoteMutation;
    import com.amazonaws.amplify.generated.graphql.GetNoteQuery;
    import com.amazonaws.amplify.generated.graphql.ListNotesQuery;
    import com.amazonaws.amplify.generated.graphql.UpdateNoteMutation;
    import com.amazonaws.mobile.samples.mynotes.models.Note;
    import com.amazonaws.mobile.samples.mynotes.models.PagedListConnectionResponse;
    import com.amazonaws.mobile.samples.mynotes.models.ResultCallback;
    import com.amazonaws.mobile.samples.mynotes.services.DataService;
    import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient;
    import com.amazonaws.mobileconnectors.appsync.fetcher.AppSyncResponseFetchers;
    import com.amazonaws.mobileconnectors.appsync.sigv4.BasicCognitoUserPoolsAuthProvider;
    import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserPool;
    import com.apollographql.apollo.GraphQLCall;
    import com.apollographql.apollo.api.Error;
    import com.apollographql.apollo.api.Response;
    import com.apollographql.apollo.exception.ApolloException;

    import java.util.ArrayList;
    import java.util.List;
    import java.util.Locale;
    import javax.annotation.Nonnull;

    import type.CreateNoteInput;
    import type.UpdateNoteInput;
    import type.DeleteNoteInput;

    import static com.amazonaws.mobile.auth.core.internal.util.ThreadUtils.runOnUiThread;

    public class AWSDataService implements DataService {
        private static final String TAG = "AWSDataService";
        private AWSAppSyncClient client;

        public AWSDataService(Context context, AWSService awsService) {
            // Create an AppSync client from the AWSConfiguration
            AWSConfiguration config = awsService.getConfiguration();
            CognitoUserPool userPool = new CognitoUserPool(context, awsService.getConfiguration());
            client = AWSAppSyncClient.builder()
                    .context(context)
                    .awsConfiguration(config)
                    .cognitoUserPoolsAuthProvider(new BasicCognitoUserPoolsAuthProvider(userPool))
                    .build();
        }

        @Override
        public void loadNotes(int limit, String after, ResultCallback<PagedListConnectionResponse<Note>> callback) {
            // Load notes will go here
        }

        @Override
        public void getNote(String noteId, ResultCallback<Note> callback) {
            // Get note will go here
        }

        @Override
        public void deleteNote(String noteId, ResultCallback<Boolean> callback) {
            // Delete note will go here
        }

        @Override
        public void createNote(String title, String content, ResultCallback<Note> callback) {
            // Create note will go here
        }

        @Override
        public void updateNote(Note note, ResultCallback<Note> callback) {
            // Update note will go here
        }

        private void showErrors(List<Error> errors) {
            Log.e(TAG, "Response has errors:");
            for (Error e : errors) {
                Log.e(TAG, String.format(Locale.ENGLISH, "Error: %s", e.message()));
            }
            Log.e(TAG, "End of Response errors");
        }
    }
    ```

## Register the AWSDataService with the Injection Service

Similar to the `AWSService` class, the `AWSDataService` class should be instantiated as a singleton object.  You use the `Injection` service to do this.  Open the `Injection` class, and replace the `initialize()` method with the following code:

```java
public static synchronized void initialize(Context context) {
    if (awsService == null) {
        awsService = new AWSService(context);
    }

    if (analyticsService == null) {
        analyticsService = new AWSAnalyticsService(context, awsService);
    }

    if (dataService == null) {
        dataService = new AWSDataService(context, awsService);
    }

    if (notesRepository == null) {
        notesRepository = new NotesRepository(dataService);
    }
}
```

You should also add the `AWSDataService` class to the list of imports for the class.  You can easily do this using **Alt+Enter** within the editor.

## Add the Create, Update, and Delete Mutations

We added some placeholder methods in the `AWSDataService`.  These placeholders should contain the API calls to the backend.  Mutations follow a pattern:

1. Create an input object to represent the arguments that are required to perform the mutation.
2. Create a request object with the input object.
3. Enqueue the request with the AppSync client object.
4. When the request returns, handle the response on the UI thread.

Use the following code for the `createNote()` and `updateNote()` methods:

```java
@Override
public void createNote(String title, String content, ResultCallback<Note> callback) {
    CreateNoteInput input = CreateNoteInput.builder()
        .title(title.isEmpty() ? " " : title)
        .content(content.isEmpty() ? " " : content)
        .build();
    CreateNoteMutation mutation = CreateNoteMutation.builder().input(input).build();

    client.mutate(mutation)
        .enqueue(new GraphQLCall.Callback<CreateNoteMutation.Data>() {
            @Override
            public void onResponse(@Nonnull Response<CreateNoteMutation.Data> response) {
                if (response.hasErrors()) {
                    showErrors(response.errors());
                    runOnUiThread(() -> callback.onResult(null));
                } else {
                    CreateNoteMutation.CreateNote item = response.data().createNote();
                    final Note returnedNote = new Note(item.id());
                    returnedNote.setTitle(item.title().equals(" ") ? "" : item.title());
                    returnedNote.setContent(item.content().equals(" ") ? "" : item.content());
                    runOnUiThread(() -> callback.onResult(returnedNote));
                }
            }

            @Override
            public void onFailure(@Nonnull ApolloException e) {
                Log.e(TAG, String.format(Locale.ENGLISH, "Error during GraphQL Operation: %s", e.getMessage()), e);
            }
        });
}

@Override
public void updateNote(Note note, ResultCallback<Note> callback) {
    UpdateNoteInput input = UpdateNoteInput.builder()
        .id(note.getNoteId())
        .title(note.getTitle().isEmpty() ? " " : note.getTitle())
        .content(note.getContent().isEmpty() ? " " : note.getContent())
        .build();
    UpdateNoteMutation mutation = UpdateNoteMutation.builder().input(input).build();

    client.mutate(mutation)
        .enqueue(new GraphQLCall.Callback<UpdateNoteMutation.Data>() {
            @Override
            public void onResponse(@Nonnull Response<UpdateNoteMutation.Data> response) {
                if (response.hasErrors()) {
                    showErrors(response.errors());
                    runOnUiThread(() -> callback.onResult(null));
                } else {
                    UpdateNoteMutation.UpdateNote item = response.data().updateNote();
                    final Note returnedNote = new Note(item.id());
                    returnedNote.setTitle(item.title().equals(" ") ? "" : item.title());
                    returnedNote.setContent(item.content().equals(" ") ? "" : item.content());
                    runOnUiThread(() -> callback.onResult(returnedNote));
                }
            }

            @Override
            public void onFailure(@Nonnull ApolloException e) {
                Log.e(TAG, String.format(Locale.ENGLISH, "Error during GraphQL Operation: %s", e.getMessage()), e);
            }
        });
}
```

The classes for the input, mutation, and response data are all generated from the information within the files that were generated as part of the `amplify codegen` process.  The names of the classes are based on the query or mutation name within the file.

Note that Amazon DynamoDB does not allow blank string values.  The code here ensures that blanks are replaced with something that is not blank for the purposes of storage.

The code for the `deleteNote()` method is similar to the `createNote()` and `deleteNote()` methods.  However, the `DeleteNote` operation does not take an input object as an argument. We can feed the `noteId` directly into the mutation operation object:

```java
@Override
public void deleteNote(String noteId, ResultCallback<Boolean> callback) {
    DeleteNoteInput input = DeleteNoteInput.builder().id(noteId).build();
    DeleteNoteMutation mutation = DeleteNoteMutation.builder().input(input).build();

    client.mutate(mutation)
        .enqueue(new GraphQLCall.Callback<DeleteNoteMutation.Data>() {
            @Override
            public void onResponse(@Nonnull Response<DeleteNoteMutation.Data> response) {
                runOnUiThread(() -> callback.onResult(true));
            }

            @Override
            public void onFailure(@Nonnull ApolloException e) {
                Log.e(TAG, String.format(Locale.ENGLISH, "Error during GraphQL Operation: %s", e.getMessage()), e);
                callback.onResult(false);
            }
        });
}
```

## Add the LoadNotes and GetNote Queries

Queries operate very similarly to the mutations.  However, you have to take care to convert all the records that are received to the proper form for the application, and you have to deal with caching.  The AWS Mobile SDK performs caching for you, but you have to select the appropriate cache policy.

*  `CACHE_ONLY` consults the cache only and never requests data from the backend.  This is useful in an offline scenario.
*  `NETWORK_ONLY` is the reverse of `CACHE_ONLY`. It consults the backend only and never uses the cache.
*  `CACHE_FIRST` fetches the data from the cache if available, and fetches from the backend if it is not available in the cache.
*  `NETWORK_FIRST` fetches the data from the network.  If the network is unavailable, it uses the cache.
*  `CACHE_AND_NETWORK` consults both the cache and network for data. If both are available, you get two callbacks.

In the sample application, use a `NETWORK_FIRST` cache policy.  This guarantees that the callback is only called once, but it still uses the cache when the application goes offline.

The `getNote()` method looks very similar to the mutations covered earlier:

```java
@Override
public void getNote(String noteId, ResultCallback<Note> callback) {
    GetNoteQuery query = GetNoteQuery.builder().id(noteId).build();
    client.query(query)
        .responseFetcher(AppSyncResponseFetchers.NETWORK_FIRST)
        .enqueue(new GraphQLCall.Callback<GetNoteQuery.Data>() {
            @Override
            public void onResponse(@Nonnull Response<GetNoteQuery.Data> response) {
                GetNoteQuery.GetNote item = response.data().getNote();
                final Note note = new Note(noteId);
                note.setTitle(item != null ? (item.title().equals(" ") ? "" : item.title()) : "");
                note.setContent(item != null ? (item.content().equals(" ") ? "" : item.content()) : "");
                runOnUiThread(() -> callback.onResult(note));
            }

            @Override
            public void onFailure(@Nonnull ApolloException e) {
                Log.e(TAG, String.format(Locale.ENGLISH, "Error during GraphQL Operation: %s", e.getMessage()), e);
            }
        });
}
```

You need to convert the return value to the internal representation prior to returning the data to the main application.  The `loadNotes()` method is a little more involved because the return value is a complex type that needs to be decoded before returning:

```java
@Override
public void loadNotes(int limit, String after, ResultCallback<PagedListConnectionResponse<Note>> callback) {
    ListNotesQuery query = ListNotesQuery.builder().limit(limit).nextToken(after).build();
    client.query(query)
        .responseFetcher(AppSyncResponseFetchers.NETWORK_FIRST)
        .enqueue(new GraphQLCall.Callback<ListNotesQuery.Data>() {
            @Override
            public void onResponse(@Nonnull Response<ListNotesQuery.Data> response) {
                String nextToken = response.data().listNotes().nextToken();
                List<ListNotesQuery.Item> rItems = response.data().listNotes().items();

                List<Note> items = new ArrayList<>();
                for (ListNotesQuery.Item item : rItems) {
                    Note n = new Note(item.id());
                    n.setTitle(item.title().equals(" ") ? "" : item.title());
                    n.setContent(item.content().equals(" ") ? "" : item.content());
                    items.add(n);
                }
                runOnUiThread(() -> callback.onResult(new PagedListConnectionResponse<>(items, nextToken)));
            }

            @Override
            public void onFailure(@Nonnull ApolloException e) {
                Log.e(TAG, String.format(Locale.ENGLISH, "Error during GraphQL Operation: %s", e.getMessage()), e);
            }
        });
}
```

## Run the Application

You must be online in order to run this application. Run the application in the emulator. Note that the initial startup after logging in is slightly longer. This happens because the app is reading the data from the remote database.

Data is available immediately in the mobile backend. Create a few notes, and then view the records in the AWS Console:

1. Open the [AWS AppSync console](https://console.aws.amazon.com/appsync/home).
2. Choose the API that you created for your app, the name should match the one you provided to the CLI.
3. In the left navigation, choose **Data Sources**.
4. Choose the resource of the table with the name you provided to the CLI. This will open table that the CLI created for you in the Amazon DynamoDB console.

When you  insert, edit, or delete notes in the app, you should be able to see that the data on the server reflect your actions immediately.

## Next Steps

* Learn about [AWS AppSync](https://aws.amazon.com/appsync/).
* Learn about [Amazon DynamoDB](https://aws.amazon.com/dynamodb/).
