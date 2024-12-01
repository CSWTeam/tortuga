(function() {

    angular.module('rms')
        .factory('unauthorizedInterceptor', [
            '$q',
            '$injector',
           unauthorizedInterceptor
        ]);

    function unauthorizedInterceptor($q, $injector) {
        function onResponseError(rejection) {
            var $state = $injector.get('$state');
            console.dir(rejection);

            if((rejection.status == 401 && $state.name != 'login')) {
                $injector.get('AuthenticationService').logout();
            }

            return $q.reject(rejection);
        }

        return {
            responseError: onResponseError
        };
    }

})();
