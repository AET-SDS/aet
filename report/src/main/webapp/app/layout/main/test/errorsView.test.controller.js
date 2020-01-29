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
    vm.handleGroupClick = handleGroupClick;

    $rootScope.$on('metadata:changed', updateErrorsView);
    $('[data-toggle="popover"]').popover({
      placement: 'bottom'
    });

    $rootScope.$watch('groupingChecked',function () {
      setTimeout(function(){
        const groups = document.querySelectorAll(".relativeContainer");
        groups.forEach((group,i) => {
          const groupItems = group.querySelectorAll('.reports-list-new-item');
          const groupItemsLength = groupItems.length;
          groupItemsLength > 4 ? group.style.height = `${83 + 4 * 12}px` : group.style.height = `${83 + groupItemsLength * 12}px`;
          groupItemsLength > 4 ? group.style.width = `calc(${group.offsetWidth}px - 18px)` : group.style.width = `calc(${group.offsetWidth}px - ${groupItemsLength*6}px + 6px)`;
          groupItems.forEach((item,i)=>{
            i > 3 ? item.style.display = 'none': null;
          })

        })
      },100)
    })

    updateErrorsView();
    /***************************************
     ***********  Private methods  *********
     ***************************************/
    function handleGroupClick ($event) {
      if($event.currentTarget.style.position === "static"){
        $event.currentTarget.style.position = "relative";
        const listItems = $event.currentTarget.querySelectorAll('.reports-list-new-item');
        listItems.forEach((item,i) =>{
          item.style.position = 'absolute';
          i > 3 ? item.style.display = 'none': null;
          item.style.marginLeft = '0px'
        })
        listItems.length > 4 ? $event.currentTarget.style.height = `${83 + 4 * 12}px` : $event.currentTarget.style.height = `${83 + listItems.length * 12}px`;
        listItems.length > 4 ? $event.currentTarget.style.width = `calc(${$event.currentTarget.offsetWidth}px - ${3*6}px + 40px)`: $event.currentTarget.style.width = `calc(${$event.currentTarget.offsetWidth}px - ${listItems.length*6}px + 46px)`;
      } else {
        $event.currentTarget.style.position = "static";
        const listItems = $event.currentTarget.querySelectorAll('.reports-list-new-item');
        listItems.forEach(item =>{
          item.style.position = 'static';
          item.style.display = 'flex';
          item.style.marginLeft = '40px';
        })
        $event.currentTarget.style.height = `${95 * listItems.length}px`;
        listItems.length > 4 ? $event.currentTarget.style.width = `calc(${$event.currentTarget.offsetWidth}px + ${3*6}px - 40px)`: $event.currentTarget.style.width = `calc(${$event.currentTarget.offsetWidth}px + ${listItems.length*6}px - 46px)`;
      }
    }

    function updateErrorsView() {
      errorsService.getErrors($stateParams.test).then(function(data) {
        console.log('errors', data);
          vm.errors = data;
      });

      errorsService.getGroups($stateParams.test).then(function(data) {
        console.log('groups', data);
          vm.groups = data;
          
      });
      vm.urls = metadataAccessService.getTestUrls($stateParams.test);
      vm.testName = $stateParams.test;
      vm.formatDifference = formatDifference;

    }
    function formatDifference (difference){
      return Number(difference).toFixed(2)
    }
  }
});
