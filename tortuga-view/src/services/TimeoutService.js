(function() {

    angular.module('rms')
        .service('TimeoutService', [
            '$timeout',
            'AuthenticationService',
            '$rootScope',
            TimeoutService
        ]);

    function TimeoutService($timeout, AuthenticationService, $rootScope) {
        var self = this;

        self.somethingHappened = somethingHappened;

        var timeout = $timeout(AuthenticationService.logout, 0);


        $rootScope.$on('$stateChangeStart', function onStateChange() {
            somethingHappened();
        });


        //public
        function somethingHappened() {
            if(AuthenticationService.isLoggedIn()) {
                $timeout.cancel(timeout);
                timeout = $timeout(AuthenticationService.logout, AuthenticationService.getTokenValidityTime());
            }
        }

    }

})();