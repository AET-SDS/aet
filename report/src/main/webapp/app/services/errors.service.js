/*
 * AET
 *
 * Copyright (C) 2013 Cognifide Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
define(['angularAMD', 'endpointConfiguration', 'requestParametersService'],
    function (angularAMD) {
        'use strict';
        angularAMD.factory('errorsService', ErrorsService);

        /**
         * Service responsible for fetching all errors.
         */
        function ErrorsService($q, $http, endpointConfiguration,
                               requestParametersService) {
            var service = {
                getErrorsUrl: getErrorsUrl,
                getErrors: getErrors
            },
                requestParams = requestParametersService.get(),
            endpoint = endpointConfiguration.getEndpoint();

            return service;

            function getErrorsUrl(testName) {
                return endpoint.getUrl + 'errors?' +
                    'company=' + requestParams.company +
                    '&project=' + requestParams.project +
                    '&correlationId=' + requestParams.correlationId +
                    '&testName=' + testName;
            }

            function getErrors(testName) {
                var deferred = $q.defer(),
                    url = getErrorsUrl(testName);

                return $http({
                    method: 'GET',
                    url: url,
                    headers: {
                        'Content-Type': 'application/json'
                    }
                }).then(function (data) {
                    console.log('data', data);
                    deferred.resolve(data.data);
                    return deferred.promise;
                }).catch(function (exception) {
                    handleFailed('Failed to load errors for test ' + testName, exception);
                });
            }

            function handleFailed(text, exception) {
                console.error(text, requestParams, exception);
                alert(text);
            }
        }
    }
);