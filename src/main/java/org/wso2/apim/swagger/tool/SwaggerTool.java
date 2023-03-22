/*
 *
 *   Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.apim.swagger.tool;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.swagger.parser.OpenAPIParser;
import io.swagger.parser.SwaggerParser;
import io.swagger.v3.parser.ObjectMapperFactory;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.ParseOptions;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Swagger Validation Tool Main Class: This Class will work as a CLI tool to validate the Swagger 2 and OpenAPI
 * definitions according to the WSO2 API Manager validation criteria.
 */
public class SwaggerTool {
    private static final Logger log = LoggerFactory.getLogger(SwaggerTool.class);
    static int totalFileCount = 0;
    static int validationFailedFileCount = 0;
    static int validationSuccessFileCount = 0;
    static int totalMalformedSwaggerFiles = 0;
    static int totalPartialyPasedSwaggerFiles = 0;

    /**
     * @param args 2 parameters are supported when executing the tool.
     *             Param 1: Direct path to the swagger/openAPI file or the folder location
     *             Ex: location:/Users/xyz/Downloads/swagger-definition/invalid-swagger-definitions
     *             Param 2: validationLevel - Default will be 2
     *             If the validationLevel is 0, swagger validation errors won't be returned only verify whether the
     *             swagger definition is returned after the validation.
     *             If the validationLevel is 1, swagger will be validated as per the same behaviour as API Manager 4.0.0
     *             If the validationLevel is 2, All the validation errors will be returned
     */
    public static void main(String[] args) {
        if (args.length == 1 || args.length == 2) {
            String swaggerContent = args[0];
            int validationLevel = 2;
            if (args.length == 2) {
                validationLevel = Integer.parseInt(args[1]);
            }
            if (swaggerContent.startsWith("location:")) {
                validateSwaggerFromLocation(swaggerContent.replace("location:", ""), validationLevel);
            } else {
                validateSwaggerContent(swaggerContent, validationLevel);
            }
            log.info("Summary --- Total Files Processed: " + totalFileCount + ". Total Successful Files Count "
                    + validationSuccessFileCount + ". Total Failed Files Count: " + validationFailedFileCount + ". " +
                    "Total Malformed Swagger File Count: " + totalMalformedSwaggerFiles);
        } else {
            log.info("\nUsage: \t java -jar apim-swagger-validator.jar " +
                    "[<File uri> | <Directory uri> | <Swagger String>] [0 | 1 | 2] \n 0 \tValidation disabled. " +
                    "Only verify whether the swagger/openAPI definition is returned by the validator. " +
                    "\n 1 \tValidate as in WSO2 API Manager 4.0.0 and verify whether the swagger/openAPI " +
                    "definition is returned by the validator. \n 2 \tFully validate the definitions and verify " +
                    "whether the swagger/openAPI definition is returned by the validator");
        }
    }

    /**
     * @param url             url for the swagger file
     * @param validationLevel swagger validation level[0,1,2]
     */
    static void validateSwaggerFromLocation(String url, int validationLevel) {
        try {
            Path swaggerFilePath = Paths.get(url);
            if (Files.isRegularFile(swaggerFilePath)) {
                totalFileCount++;
                String swaggerFileContent = new String(Files.readAllBytes(swaggerFilePath), StandardCharsets.UTF_8);
                log.info("Start Parsing Swagger file " + swaggerFilePath.getFileName().toString());
                validateSwaggerContent(swaggerFileContent, validationLevel);
            } else if (Files.isDirectory(swaggerFilePath)) {
                try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(swaggerFilePath)) {
                    directoryStream.forEach((path) -> {
                        validateSwaggerFromLocation(path.toString(), validationLevel);
                    });
                }
            } else {
                log.error("Error occurred while reading the provided file/folder, " +
                        "please verify the file/folder availability");
            }
        } catch (IOException e) {
            log.error("Error occurred while reading the swagger file from the give location " + url + ", hence the " +
                    "file will not be validated. ", e);
        }
    }

    /**
     * @param swaggerFileContent swagger file content to be validated
     * @param validationLevel    validation level [0,1,2]
     */
    public static void validateSwaggerContent(String swaggerFileContent, int validationLevel) {
        List<Object> swaggerTypeAndName = getSwaggerVersion(swaggerFileContent);

        if (validationLevel == 1) {
            if (swaggerTypeAndName.get(0).equals(Constants.SwaggerVersion.ERROR) && swaggerTypeAndName.size() == 1) {
                return;
            } else {
                if (swaggerTypeAndName.get(0).equals(Constants.SwaggerVersion.SWAGGER)) {
                    log.info("---------------- Parsing Started SwaggerName \"" + swaggerTypeAndName.get(1).toString() +
                            "\" ----------------");
                    swagger2Validator(swaggerFileContent, validationLevel);
                    log.info("---------------- Parsing Complete SwaggerName \"" + swaggerTypeAndName.get(1).toString() +
                            "\" ---------------- \n");
                } else if (swaggerTypeAndName.get(0).equals(Constants.SwaggerVersion.OPEN_API)) {
                    log.info("---------------- Parsing Started openApiName \"" + swaggerTypeAndName.get(1).toString() +
                            "\" ----------------");
                    boolean isOpenAPIMissing = swagger3Validator(swaggerFileContent, validationLevel);
                    if (isOpenAPIMissing) {
                        swagger2Validator(swaggerFileContent, validationLevel);
                    }
                    log.info("---------------- Parsing Complete openApiName \"" + swaggerTypeAndName.get(1).toString() +
                            "\" ----------------\n");
                }
            }
        } else {
            if (swaggerTypeAndName.get(0).equals(Constants.SwaggerVersion.ERROR)) {
                if (swaggerTypeAndName.size() == 2) {
                    log.info("---------------- Parsing Started SwaggerName \"" + swaggerTypeAndName.get(1).toString() +
                            "\" ----------------");
                    boolean isOpenAPIMissing = swagger3Validator(swaggerFileContent, validationLevel);
                    boolean isSwaggerMissing;
                    if (isOpenAPIMissing) {
                        isSwaggerMissing = swagger2Validator(swaggerFileContent, validationLevel);
                        if (isSwaggerMissing) {
                            log.error("Invalid OpenAPI, Error Code: " + Constants.OPENAPI_PARSE_EXCEPTION_ERROR_CODE +
                                    ", Error: " + Constants.OPENAPI_PARSE_EXCEPTION_ERROR_MESSAGE
                                    + ", Swagger Error: " + Constants.SWAGGER_OR_OPENAPI_IS_MISSING_MSG);
                        }
                    }
                    log.info("---------------- Parsing Complete SwaggerName \"" + swaggerTypeAndName.get(1).toString() +
                            "\" ---------------- \n");
                }
                return;
            }
            if (swaggerTypeAndName.get(0).equals(Constants.SwaggerVersion.SWAGGER)) {
                log.info("---------------- Parsing Started SwaggerName \"" + swaggerTypeAndName.get(1).toString() +
                        "\" ----------------");
                swagger2Validator(swaggerFileContent, validationLevel);
                log.info("---------------- Parsing Complete SwaggerName \"" + swaggerTypeAndName.get(1).toString() +
                        "\" ---------------- \n");
            } else if (swaggerTypeAndName.get(0).equals(Constants.SwaggerVersion.OPEN_API)) {
                log.info("---------------- Parsing Started openApiName \"" + swaggerTypeAndName.get(1).toString() +
                        "\" ----------------");
                boolean isOpenAPIMissing = swagger3Validator(swaggerFileContent, validationLevel);
                if (isOpenAPIMissing) {
                    swagger2Validator(swaggerFileContent, validationLevel);
                }
                log.info("---------------- Parsing Complete openApiName \"" + swaggerTypeAndName.get(1).toString() +
                        "\" ----------------\n");
            }
        }
    }

    public static List<Object> getSwaggerVersion(String apiDefinition) {
        List<Object> swaggerTypeAndName = new ArrayList<>(2);
        ObjectMapper mapper;
        if (apiDefinition.trim().startsWith("{")) {
            mapper = ObjectMapperFactory.createJson();
        } else {
            mapper = ObjectMapperFactory.createYaml();
        }
        JsonNode rootNode;
        ObjectNode node;
        try {
            rootNode = mapper.readTree(apiDefinition.getBytes());
            node = (ObjectNode) rootNode;
        } catch (Exception e) {
            log.error("Error occurred while parsing OAS definition. Verify the provided definition format: " + e.getMessage());
            swaggerTypeAndName.add(Constants.SwaggerVersion.ERROR);
            validationFailedFileCount++;
            return swaggerTypeAndName;
        }
        String name = getSwaggerFileName(node.get("info"));
        JsonNode openapi = node.get("openapi");
        if (openapi != null && openapi.asText().startsWith("3.")) {

            swaggerTypeAndName.add(Constants.SwaggerVersion.OPEN_API);
            swaggerTypeAndName.add(name);
            return swaggerTypeAndName;
        }
        JsonNode swagger = node.get("swagger");
        if (swagger != null) {
            swaggerTypeAndName.add(Constants.SwaggerVersion.SWAGGER);
            swaggerTypeAndName.add(name);
            return swaggerTypeAndName;
        }

        log.error("Invalid OAS definition provided.");
        swaggerTypeAndName.add(Constants.SwaggerVersion.ERROR);
        swaggerTypeAndName.add(name);
        return swaggerTypeAndName;
    }

    public static String getSwaggerFileName(JsonNode node) {
        if (node != null) {
            return node.get("title").asText();
        }
        return "";
    }

    public static boolean swagger2Validator(String swagger, int validationLevel) {
        boolean isSwaggerMissing = false;
        boolean isValidForAPIM = true;
        SwaggerParser swaggerParser = new SwaggerParser();
        OpenAPIParser parser= new OpenAPIParser();
        ParseOptions options = new ParseOptions();
        options.setResolve(true);
        options.setFlatten(true);
        options.setResolveFully(true);
        SwaggerParseResult parseAttemptForV2 = parser.readContents(swagger, new ArrayList<>(), options);
        if (parseAttemptForV2.getMessages().size() > 0) {
             if (validationLevel == 1 || validationLevel == 2) {
                for (String message : parseAttemptForV2.getMessages()) {
                    StringBuilder errorMessageBuilder = new StringBuilder("Invalid Swagger, Error Code: ");
                    if (message.contains(Constants.SWAGGER_IS_MISSING_MSG)) {
                        errorMessageBuilder.append(Constants.INVALID_OAS2_FOUND_ERROR_CODE)
                                .append(", Error: ").append(Constants.INVALID_OAS2_FOUND_ERROR_MESSAGE)
                                .append(", Swagger Error: ").append(Constants.SWAGGER_IS_MISSING_MSG);
                        log.error(errorMessageBuilder.toString());
                        isSwaggerMissing = true;
                    } else if (message.contains(Constants.MALFORMED_SWAGGER_ERROR)) {
                        errorMessageBuilder.append(Constants.OPENAPI_PARSE_EXCEPTION_ERROR_CODE)
                                .append(", Error: ").append(Constants.OPENAPI_PARSE_EXCEPTION_ERROR_MESSAGE)
                                .append(", Swagger Error: ").append(message);
                        try {
                            swaggerParser.parse(swagger);
                            log.error(errorMessageBuilder.toString());
                        } catch (Exception e) {
                            if (e.getMessage().contains(Constants.UNABLE_TO_LOAD_REMOTE_REFERENCE)) {
                                logRemoteReferenceIssues(swagger);
                            } else {
                                errorMessageBuilder.append(", Cause by: ").append(e.getMessage());
                                log.error(errorMessageBuilder.toString());
                            }
                        }
                    } else {
                        if (isSchemaMissing(message)) {
                            isValidForAPIM = false;
                        }
                        // Since OpenAPIParser coverts the $ref to #/components/schemas/ when validating
                        // we need to replace #/components/schemas/ with #/definitions/ before printing the message
                        if (message.contains(Constants.SCHEMA_REF_PATH)) {
                            message = message.replace(Constants.SCHEMA_REF_PATH, "#/definitions/");
                        }
                        errorMessageBuilder.append(Constants.OPENAPI_PARSE_EXCEPTION_ERROR_CODE)
                                .append(", Error: ").append(Constants.OPENAPI_PARSE_EXCEPTION_ERROR_MESSAGE)
                                .append(", Swagger Error: ").append(message);
                        log.error(errorMessageBuilder.toString());
                    }
                }
                validationFailedFileCount++;
            }
            if (parseAttemptForV2.getOpenAPI() != null) {
                log.info("Swagger passed with errors, using may lead to functionality issues.");
                totalPartialyPasedSwaggerFiles++;
            } else {
                isValidForAPIM = false;
                log.error("Malformed Swagger, Please fix the listed issues before proceeding");
                totalMalformedSwaggerFiles++;
            }
        } else {
            if (parseAttemptForV2.getOpenAPI() != null) {
                log.info("Swagger file is valid");
                validationSuccessFileCount++;
            } else {
                isValidForAPIM = false;
                log.error(Constants.UNABLE_TO_RENDER_THE_DEFINITION_ERROR);
                validationFailedFileCount++;
            }
        }
        if (isValidForAPIM) {
            log.info("Swagger file will be accepted by the APIM 4.0.0 ");
        }
        return isSwaggerMissing;
    }

    private static boolean isSchemaMissing(String errorMessage) {
        return errorMessage.contains(Constants.SCHEMA_REF_PATH) && errorMessage.contains("is missing");
    }

    public static boolean swagger3Validator(String swagger, int validationLevel) {
        boolean isOpenAPIMissing = false;

        OpenAPIV3Parser openAPIV3Parser = new OpenAPIV3Parser();
        ParseOptions options = new ParseOptions();
        options.setResolve(true);
        options.setResolveFully(true);
        SwaggerParseResult parseResult = openAPIV3Parser.readContents(swagger, null, options);
        if (parseResult.getMessages().size() > 0) {
            if (validationLevel == 1 || validationLevel == 2) {
                for (String message : parseResult.getMessages()) {
                    StringBuilder errorMessageBuilder = new StringBuilder("Invalid OpenAPI, Error Code: ");
                    if (message.contains(Constants.UNABLE_TO_LOAD_REMOTE_REFERENCE)) {
                        logRemoteReferenceIssues(swagger);
                    } else if (message.contains(Constants.OPENAPI_IS_MISSING_MSG)) {
                        errorMessageBuilder.append(Constants.INVALID_OAS3_FOUND_ERROR_CODE)
                                .append(", Error: ").append(Constants.INVALID_OAS3_FOUND_ERROR_MESSAGE);
                        log.error(errorMessageBuilder.toString());
                        isOpenAPIMissing = true;
                    } else {
                        // If the error message contains "schema is unexpected", we modify the error message notifying
                        // that the schema object is not adhering to the OpenAPI Specification. Also, we add a note to
                        // verify the reference object is of the format $ref: '#/components/schemas/{schemaName}'
                        if (message.contains("schema is unexpected")) {
                            message = message.concat(". Please verify whether the schema object is adhering to " +
                                    "the OpenAPI Specification. Make sure that the reference object is of " +
                                    "format $ref: '#/components/schemas/{schemaName}'");
                        }
                        errorMessageBuilder.append(Constants.OPENAPI_PARSE_EXCEPTION_ERROR_CODE)
                                .append(", Error: ").append(Constants.OPENAPI_PARSE_EXCEPTION_ERROR_MESSAGE)
                                .append(", Swagger Error: ").append(message);
                        log.error(errorMessageBuilder.toString());
                    }
                }
            }
            if (!isOpenAPIMissing) {
                if (parseResult.getOpenAPI() != null) {
                    log.info("OpenAPI passed with errors, using may lead to functionality issues.");
                    totalPartialyPasedSwaggerFiles++;
                } else {
                    log.error("Malformed OpenAPI, Please fix the listed issues before proceeding");
                    ++totalMalformedSwaggerFiles;
                }
                if (validationLevel != 0) {
                    validationFailedFileCount++;
                }
            }
        } else {
            if (parseResult.getOpenAPI() != null) {
                log.info("Swagger file is valid OpenAPI 3 definition");
                validationSuccessFileCount++;
            } else {
                log.error(Constants.UNABLE_TO_RENDER_THE_DEFINITION_ERROR);
                validationFailedFileCount++;
            }
        }
        return isOpenAPIMissing;
    }

    /**
     * This method will log the remote references in the given Swagger or OpenAPI definition.
     * @param apiDefinition Swagger or OpenAPI definition
     */
    public static void logRemoteReferenceIssues(String apiDefinition) {
        log.warn("Validate the following remote references and make sure that they are valid and accessible:");

        // Parse the Swagger or OpenAPI definition and extract the remote references by picking
        // the values of the $ref ke
        ObjectMapper mapper;
        if (apiDefinition.trim().startsWith("{")) {
            mapper = ObjectMapperFactory.createJson();
        } else {
            mapper = ObjectMapperFactory.createYaml();
        }

        JsonNode rootNode;
        try {
            rootNode = mapper.readTree(apiDefinition);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        List<JsonNode> refValues = findRefValues(rootNode);

        for (JsonNode refValue : refValues) {
            String remoteReference = refValue.toString();

            // If schema reference starts with #/components/schemas/ (OAS 3 ref objects) or #/definitions/ (Swagger ref objects), it is a local reference.
            // Hence, if reference does not start with a "#/", it is a remote reference.
            if (!remoteReference.startsWith("\"#/")) {
                log.warn(refValue.toString());
            }
        }
    }

    /**
     * This method will recursively traverse the given JSON node and return a list of all the $ref values.
     * @param node JSON node
     * @return List of $ref values
     */
    public static List<JsonNode> findRefValues(JsonNode node) {
        List<JsonNode> refValues = new ArrayList<>();

        if (node instanceof ObjectNode) {
            ObjectNode objectNode = (ObjectNode) node;
            objectNode.fields().forEachRemaining(entry -> {
                if (entry.getKey().equals("$ref")) {
                    refValues.add(entry.getValue());
                } else {
                    refValues.addAll(findRefValues(entry.getValue()));
                }
            });
        } else if (node instanceof ArrayNode) {
            ArrayNode arrayNode = (ArrayNode) node;
            arrayNode.forEach(element -> refValues.addAll(findRefValues(element)));
        }

        return refValues;
    }
}
