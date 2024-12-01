(function() {

    angular.module('profile', [
        'ui.router'
    ]).config(['$stateProvider', function($stateProvider) {
        $stateProvider.state('profile', {
            url: '/profile',
            templateUrl: 'src/profile/profile.html',
            data: {
                viewName: 'Profil'
            }
        });
    }]);

})();