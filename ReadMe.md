# Getting Started

## Build From Source
To build from source, please check the following requirements:

* [JDK](https://www.oracle.com/java/technologies/downloads/)
* [Gradle](https://gradle.org/install/)

Run `./gradlew build` to build.

# Endpoints Documentation

## Overview
This section details the RESTful API endpoints provided by the backend, capable of handling file operations such as uploading, downloading, listing, and searching files and retrieving file metadata.

## Base URL
The base URL for all endpoints is `/f`.

## Endpoints

### 1. Upload File

#### URL
`/f/ul`

#### Method
`POST`

#### Description
Uploads a file to the server.

#### Parameters
- `file` (MultipartFile, required): The file to be uploaded.
- `title` (String, required): The title of the file.
- `description` (String, required): A brief description of the file.

#### Responses
- `200 OK`: File uploaded successfully.
- `400 BAD REQUEST`: Invalid input detected.
- `417 EXPECTATION FAILED`: Failed to upload the file.

#### Example Request
```http
POST /f/ul
Content-Type: multipart/form-data

file: <file>
title: "exampleTitle"
description: "exampleDescription"
```

### 2. Download File

#### URL
`/f/dl/{title}`

#### Method
`GET`

#### Description
Downloads a file from the server.

#### Parameters
- `title` (String, required): The title of the file to be downloaded.
- `Range` (String, optional): The range header for partial content requests.

#### Responses
- `200 OK`: File downloaded successfully.
- `400 BAD REQUEST`: Invalid input detected.
- `404 NOT FOUND`: File not found.

#### Example Request
```http
GET /f/dl/sample-file
```

### 3. List All Files

#### URL
`/f/ls`

#### Method
`GET`

#### Description
Lists title of all uploaded files.

#### Responses
- `200 OK`: List of file titles.

#### Example Request
```http
GET /f/ls
```

### 4. Get File Details

#### URL
`/f/dt`

#### Method
`GET`

#### Description
Retrieves metadata of a file by its title.

#### Parameters
- `title` (String, required): The title of the file.

#### Responses
- `200 OK`: File metadata retrieved successfully.
- `400 BAD REQUEST`: Invalid input detected.
- `404 NOT FOUND`: File not found.

#### Example Request
```http
GET /f/dt?title=exampleTitle
```

### 5. Find Files

#### URL
`/f/fd`

#### Method
`POST`

#### Description
Finds files based on search criteria.

#### Request Body
- `title` (String, optional): The title of the file.
- `date` (LocalDateTime, optional): The upload date of the file.

#### Responses
- `200 OK`: List of file titles matching the search criteria.
- `400 BAD REQUEST`: Invalid input detected.

#### Example Request
```http
POST /f/fd
Content-Type: application/json

{
  "title": "exampleTitle",
  "date": "2023-07-06T00:00:00"
}
```

# Copyright

Copyright (C) nullptr-0 2024.

# Licensing

This software is open-source under [Apache 2.0 License](https://www.apache.org/licenses/LICENSE-2.0.html).
