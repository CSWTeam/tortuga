(function() {

    angular.module('tickets', [
        'ui.router',
        'tickets.roomReservation',
        'tickets.support'
    ]).config(['$stateProvider', function($stateProvider) {
        $stateProvider
            .state('tickets', {
                url: '/tickets',
                templateUrl: 'src/tickets/tickets.html',
                redirectTo: 'tickets.roomReservation',
                data: {
                    viewName: 'Tickets'
                }
            });
    }]);

})();

