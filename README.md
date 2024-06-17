# asyncqlitekt (Asynchronous SQLite for Kotlin)

[![Maven Central](https://img.shields.io/maven-central/v/asia.hombre/asyncqlitekt-jvm.svg)](https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22asia.hombre%20asyncqlitekt%22)

SQLite suffers with concurrency issues. It corrupts itself when it is accessed by multiple threads. To solve this issue,
asyncqlitekt was born. It uses a single consumer thread to ensure data is written and read serially. That means no more
concurrency issues where multiple accessing threads results in an SQLite crash.

This is done by taking Javascript promises and Java's callbacks as inspirations.

### Simplified Explanation
- Consumer thread is on standby or dead.
- Thread A sends a write job to Consumer thread which revives it if it's dead.
- Thread B sends a write job to Consumer thread before the first job is processed.
- Consumer thread sees new job and works on it. When done, it tells Thread A.
- Consumer thread sees that there is still a job and works on it. When done, it tells Thread B.
- Consumer thread waits for further jobs.

Normally, this would cause concurrency issues with the SQLite database, but it is avoided by the Consumer thread solely
accessing the SQLite database.

### Dependency
- SQLiteBundled (androidx.sqlite:sqlite-bundled:2.5.0-alpha03) <- Kotlin Multiplatform (Google Maven Repo)

### Implementation
```kotlin
implementation("asia.hombre:asyncqlitekt-jvm:0.0.1")

//Add google() to your repositories {} for the SQLiteBundled dependency if you don't already have it.
```

## Usage
```kotlin
val sqlite = BundledSQLiteDriver().open("sqlite.db") //Sync
val asyncQLiteConnection = AsyncQLiteConnection.wrap(sqlite) //Async

//Sync execSQL (Not concurrent or thread safe)
asyncQLiteConnection.synced.execSQL(
    """
        CREATE TABLE IF NOT EXISTS employees (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            name TEXT NOT NULL,
            employeeid INT NOT NULL
        )
    """.trimIndent()
)

//Async execSQL
asyncQLiteConnection.execSQL(
    """
        CREATE TABLE IF NOT EXISTS employees (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            name TEXT NOT NULL,
            employeeid INT NOT NULL
        )
    """.trimIndent()
) { exception -> //onSuccess
    exception?.let {
        println("Failed to create table due to " + exception.message)
    }?: println("Table created successfully!") //If exception is null, then it's a success.
}

//Sync prepare (Not concurrent or thread safe)
asyncQLiteConnection.synced.prepare(
    """
        INSERT INTO employees (
            name,
            employeeid
        ) VALUES (?, ?)
    """.trimIndent()
).use {
    it.bindText(1, "Johnathan Myers")
    it.bindInt(2, 1)
    it.step() //Executes the prepare statement.
}

//Async prepare
asyncQLiteConnection.prepare(
    """
        INSERT INTO employees (
            name,
            employeeid
        ) VALUES (?, ?)
    """.trimIndent()
) { exception -> //onSuccess
    exception?.let {
        println("Failed to insert due to " + exception.message)
    }?: println("Inserted successfully!") //If exception is null, then it's a success.
}.async {
    it.bindText(1, "Johnathan Myers")
    it.bindInt(2, 1)
    it.step() //Adds the statement to the job queue.
}

//Sync prepare with returned values (Not concurrent or thread safe)
asyncQLiteConnection.synced.prepare(
    "SELECT * FROM employees WHERE employeeid = ?"
).use {
    it.bindInt(1, 1)
    while(it.step()) {
        println("Employee: " + it.getText(1) + " has ID: " + it.getInt(2))
    }
}

//Async prepare with returned values
asyncQLiteConnection.prepare(
    "SELECT * FROM employees WHERE employeeid = ?"
) { exception -> //onSuccess
    exception?.let {
        println("Failed to select due to " + exception.message)
    }?: println("Selected successfully!") //If exception is null, then it's a success.
}.async {
    it.bindInt(1, 1)

    //Declare the indexes that you want to get in the result.
    it.getInt(0) //id - Primary Key
    it.getText(1) //name
    it.getInt(2) //employeeid

    it.step()
    it.result = { result -> //Called for each row returned by the SQL statement.
        println("Employee: " + result.getText(1) + " has ID: " + result.getInt(2))
    }
}

//Blocks the thread waiting for the Job Queue to finish and closes the SQLiteConnection
asyncQLiteConnection.close()
```

### License

```
Copyright 2024 Ron Lauren Hombre

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

       https://www.apache.org/licenses/LICENSE-2.0
       
       and included as LICENSE.txt in this Project.

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```