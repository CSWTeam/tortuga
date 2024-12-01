(function() {

    angular.module('tickets.roomReservation', [
            'ui.router'
        ])
        .config([
            '$stateProvider',
            function($stateProvider) {
                $stateProvider.state('tickets.roomReservation', {
                    url: '/roomReservation',
                    templateUrl: 'src/tickets/roomReservation/roomReservation.html',
                    controllerAs: 'reservationController',
                    controller: 'RoomReservationApprovalListController'
                });
            }]);

})();