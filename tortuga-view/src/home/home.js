(function() {

    angular.module('home', [
        'ui.router'
    ]).config(['$stateProvider', function($stateProvider) {
        $stateProvider
            .state('home', {
                url: '/home',
                templateUrl: 'src/home/home.html',
                controller: 'HomeController',
                controllerAs: 'homeController',
                data: {
                    viewName: 'Home'
                }
            });
    }]);

})();
