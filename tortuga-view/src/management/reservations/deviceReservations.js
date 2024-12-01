(function() {

    angular.module('management.reservations', [
        'ui.router'
    ]).config(['$stateProvider', function($stateProvider) {
        $stateProvider.state('management.devicereservations', {
            url: '/devicereservations',
            templateUrl: 'src/management/reservations/reservationList.html',
            controller: 'ManagementDeviceReservationListController',
            controllerAs: 'listController',
            data: {
                viewName: 'Reservierungen'
            }
        });
    }]);

})();
