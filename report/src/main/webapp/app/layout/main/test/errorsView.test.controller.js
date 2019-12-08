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
define([], function () {
  'use strict';
  return ['$rootScope', '$stateParams', 'metadataAccessService', 'errorsService',
    ErrorsViewTestController];

  function ErrorsViewTestController($rootScope, $stateParams,
      metadataAccessService,errorsService) {
    var vm = this;

    $rootScope.$on('metadata:changed', updateErrorsView);
    $('[data-toggle="popover"]').popover({
      placement: 'bottom'
    });

    updateErrorsView();
    /***************************************
     ***********  Private methods  *********
     ***************************************/

    function updateErrorsView() {
      errorsService.getErrors($stateParams.test).then(function(data) {
        console.log('errors', data);
          vm.errors = data;
          vm.fakeErrors = [
            {
              code: "403",
              text: "Error refactor za gruby znowu",
              url: "URL: url1"
            },
            {
              code: "403",
              text: "Error refactor za gruby znowu",
              url: "URL: url2"
            },
            {
              code: "403",
              text: "Error refactor za gruby znowu",
              url: "URL: url3"
            },
            {
              code: "403",
              text: "Error refactor za gruby znowu",
              url: "URL: url2"
            },
            {
              code: "404",
              text: "Brak obrazka, mocny error",
              url: "URL: url3"
            },
            {
              code: "404",
              text: "Brak obrazka, mocny error",
              url: "URL: url1"
            },
            {
              code: "404",
              text: "Brak obrazka, mocny error",
              url: "URL: url1"
            },
            {
              code: "403",
              text: "Error patryk znowu pił error",
              url: "URL: url2"
            },
            {
              code: "401",
              text: "Error mocny error",
              url: "URL: url3"
            }
          ]
      });
      vm.urls = metadataAccessService.getTestUrls($stateParams.test);
      vm.testName = $stateParams.test;

    }
  }
});
