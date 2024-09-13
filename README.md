### How to Use

**Usage:**
```bash
java -jar apim-swagger-validator-1.0.0.jar [<File uri> | <Directory uri> | <Swagger String>][Validation Level] 
```

##### Validation Levels
**1** - Verify whether the swagger/openAPI definition is valid when relaxed validation in API Manager 4.2.0.
**2** - Validate as in WSO2 API Manager 4.2.0 and verify whether the swagger/openAPI definition is returned by the validator.

##### Examples

- Use with a single swagger File

   ```bash
   java -jar apim-swagger-validator-1.0.0.jar location:/Users/xyz/swagger-definitions/swagger.json 0
   ```
- Use with a folder
    ```bash
    java -jar apim-swagger-validator-1.0.0.jar location:/Users/xyz/swagger-definitions 1
    ```
- Use with a inline definition

    ```bash
    java -jar apim-swagger-validator-1.0.0.jar "Swagger-definition" 2
    ```

##### Responses

When it comes to the outputs, the Following general responses will be returned with each swagger validation.

- If the provided swagger is a Swagger 2.x
    - **Swagger file is valid** - If the swagger file is valid swagger without any errors.
    - **Swagger passed with errors, using may lead to functionality issues.** - The swagger file is passed with errors.
   
- If the provided swagger is a Swagger 3.x
    - **Swagger file is valid OpenAPI 3 definition** - Provided swagger file is parsed without any errors.
    - **OpenAPI passed with errors, using may lead to functionality issues.** - The swagger file is passed with errors.

Apart from the above responses, the following response will be returned when the validation level is set to 2(Validate as in WSO2 API Manager 4.2.0).

**Swagger file will be accepted by the APIM 4.2.0** - This will be returned when the provided swagger file has no parsing errors which is caught by the swagger parser. Apart from that, validator checks for some additional validation like existence of empty resource paths and resource paths with trailing slashes. In this validation level, it is expected that a user will be able to successfully deploy an API in WSO2 API Manager 4.2.0 (But still this will not guarantee that all the functionalities will work)
