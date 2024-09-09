### How to Use

**Usage:**
```bash
java -jar apim-swagger-validator-1.0.0.jar [<File uri> | <Directory uri> | <Swagger String>][Validation Level] 
```

##### Validation Levels
**1** - Verify whether the swagger/openAPI definition is valid as per validation level 1 in APIM 4.0.0.
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
    - **Swagger passed with errors, using may lead to functionality issues.** - The swagger file is passed with errors but the swagger definition is retured by the validator.
    - **Malformed Swagger, Please fix the listed issues before proceeding** - The swagger file doesn't parsed and returned by the validator and needed to fix before using the file.

- If the provided swagger is a Swagger 3.x
    - **Swagger file is valid OpenAPI 3 definition** - Provided swagger file is parsed without any errors.
    - **OpenAPI passed with errors, using may lead to functionality issues.** - The swagger file is passed with errors but the swagger definition is retured by the validator.
    - **Malformed OpenAPI, Please fix the listed issues before proceeding** - The swagger file doesn't parsed and returned by the validator and needed to fix before using the file.

Apart from the above responses, the following response will be returned when the validation level is set to 2(Validate as in WSO2 API Manager 4.2.0).

**Swagger file will be accepted by the APIM 4.2.0** - This will be returned when the provided swagger file has errors but will work with APIM 4.2.0 distribution. (But this will not guarantee that all the functionalities will work)
