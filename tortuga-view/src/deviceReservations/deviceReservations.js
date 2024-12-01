(function() {

  angular.module('deviceReservations', [
    'ui.router'
  ]).config(['$stateProvider', function($stateProvider) {
    $stateProvider.state('deviceReservationCreate', {
      url: '/devicereservations/new',
      templateUrl: 'src/deviceReservations/create.html',
      controller: 'DeviceReservationCreateController',
      controllerAs: 'createController',
      data: {
        viewName: 'Gerät reservieren'
      }
    }).state('deviceReservationAdHoc', {
      url: '/devicereservations/adhoc',
      templateUrl: 'src/deviceReservations/adHoc.html',
      controller: 'DeviceReservationAdHocController',
      controllerAs: 'createController',
      data: {
        viewName: 'Gerät reservieren'
      }
    }).state('deviceReservationList', {
      url: '/devicereservations/list',
      templateUrl: 'src/deviceReservations/list.html',
      controller: 'DeviceReservationListController',
      controllerAs: 'deviceListController',
      data: {
        viewName: 'Gerätereservierungen'
      }
    });
  }]);

})();
