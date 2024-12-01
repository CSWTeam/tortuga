(function() {

    angular.module('tickets.support', [
            'ui.router'
        ])
        .config([
            '$stateProvider',
            function($stateProvider) {
                $stateProvider.state('tickets.support', {
                    url: '/support',
                    templateUrl: 'src/tickets/support/support.html',
                    controllerAs: 'supportController',
                    controller: 'SupportListController'
                });
            }]);

})();