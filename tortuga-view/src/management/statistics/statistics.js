(function() {

    angular.module('management.statistics', [
        'ui.router'
    ]).config(['$stateProvider', function($stateProvider) {
        $stateProvider.state('management.statistics', {
            url: '/statistics',
            templateUrl: 'src/management/statistics/statistics.html',
            controller: 'StatisticsController',
            controllerAs: 'statistics',
            data: {
                viewName: 'Statistiken'
            }
        });
    }]);

})();
