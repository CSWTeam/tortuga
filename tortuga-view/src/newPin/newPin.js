(function() {

    angular.module('newPin', [
        'ui.router'
    ]).config(['$stateProvider', function($stateProvider) {
        $stateProvider.state('newPin', {
            url: '/newPin?userId',
            templateUrl: 'src/newPin/newPin.html',
            controller: 'NewPinController',
            controllerAs: 'pinController',
            data: {
                viewName: 'PIN erstellen'
            }
        });
    }]);

})();