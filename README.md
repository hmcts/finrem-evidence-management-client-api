# Financial Remedy Evidence Management Client API

## The Evidence Management Client API is responsible for providing an interface between the Financial Remedy services and the HMCTS Document Management Service

## Introduction
* This API provides below endpoints:
  * File Upload(s)
  * File Download
* It uses the following tech stack:
  * Java8
  * Spring Boot
  * Junit, Mockito, SpringBootTest and Powermockito
  * Gradle
  * Spring Hateos
  * Traverson
* Plugins used by project:
  * Jacoco
  * OWASP dependency check
  * Sonar

## Project setup
* git clone [https://github.com/hmcts/finrem-evidence-management-client-api.git](https://github.com/hmcts/finrem-evidence-management-client-api.git)

* cd finrem-evidence-management-client-api

* Run `./gradlew bootRun` 
    (This command will start the spring boot application in an embedded tomcat on port 4006.
    To change the port change the configuration in `application.properties`. 
    This will output 
    `<==========---> 80% EXECUTING [43s]
     :bootRun
    ` but this is expected behaviour of Gradle and means the project is running.)

### API documentation

API documentation is provided with Swagger. This is available locally at: `http://localhost:4006/swagger-ui.html`

## Developing

### Unit tests

To run all unit tests please execute following command:

```bash
./gradlew test
```

### Coding style tests

To run all checks (including unit tests) please execute following command:

```bash
./gradlew check
```
 
## API Consumption

| File Upload Endpoint | HTTP Protocol | Header Attribute  Condition | Headers | Body |
|:----------------------------------:|---------------|:---------------------------:|:------------------------------------:|:----------------------------------------------------------------:|
| /emclientapi/version/1/uploadFiles | POST | Required | AuthorizationToken : { User Token }  | [key=file,value=MultipartFile1,key=file,value=MultipartFile2,....] |
|  |  | Required | Content-Type :multipart/form-data  |  |
|  |  | Optional | RequestId :{RequestId} |  |

###### File Upload Response:

``` [
    {
        "fileUrl": "http://localhost:8080/documents/214",
        "fileName": "file",
        "mimeType": "application/pdf",
        "createdBy": "13",
        "lastModifiedBy": "13",
        "createdOn": "2017-09-25T22:41:38.569+0000",
        "modifiedOn": "null",
        "status": "OK"
    }
] 

```

| File Download Endpoint | HTTP Protocol | Header Attribute  Condition | Headers |
|:-------------------------------------------------------------------------------:|---------------|:---------------------------:|:------------------------------------:|
| /emclientapi/version/1/downloadFile?fileUrl=http://localhost:8080/documents/195 | GET | Required | AuthorizationToken : { User Token }  |
|  |  | Optional | RequestId :{RequestId} |

###### File Download Response:

``` Actual file for the given URL. ```

## License

This project is licensed under the MIT License - see the [LICENSE.md](LICENSE) file for details.