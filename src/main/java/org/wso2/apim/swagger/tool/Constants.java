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

public class Constants {
    public static final String SWAGGER_IS_MISSING_MSG = "swagger is missing";

    public static final String SWAGGER_OR_OPENAPI_IS_MISSING_MSG = "attribute swagger or openapi should present";
    public static final int INVALID_OAS2_FOUND_ERROR_CODE = 900761;
    public static final String INVALID_OAS2_FOUND_ERROR_MESSAGE = "Invalid OpenAPI V2 definition found";
    public static final String OPENAPI_IS_MISSING_MSG = "openapi is missing";
    public static final int INVALID_OAS3_FOUND_ERROR_CODE = 900762;

    public static final int OPENAPI_PARSE_EXCEPTION_ERROR_CODE = 900754;
    public static final String OPENAPI_PARSE_EXCEPTION_ERROR_MESSAGE = "Error while parsing OpenAPI definition";
    public static final String INVALID_OAS3_FOUND_ERROR_MESSAGE = "Invalid OpenAPI V3 definition found";
    public static final String MALFORMED_SWAGGER_ERROR = "malformed or unreadable swagger supplied";
    public static final String UNABLE_TO_RENDER_THE_DEFINITION_ERROR = "Unable to render this definition, " +
            "The provided definition does not specify a valid version field.";

    public static final String UNABLE_TO_LOAD_REMOTE_REFERENCE = "Unable to load RELATIVE ref:";
    public static final String EMPTY_RESOURCE_PATH_ERROR_MESSAGE = "Empty resource paths";
    public static final String EMPTY_OPERATION_OBJECT_ERROR_MESSAGE = "An operation object is empty";
    public static final String NO_OPERATIONS_FOUND_ERROR_MESSAGE = "No operations found for the resource path";
    public static final String MULTIPLE_RESOURCE_PATHS_WITH_SAME_NAME_ERROR_MESSAGE =
            "Multiple resource paths with the same name:";


    public static final String SCHEMA_REF_PATH = "#/components/schemas/";

    public enum SwaggerVersion {
        SWAGGER,
        OPEN_API,
        ERROR,
    }
    public static final String TITLE_NULL = "NULL";
}
