(function() {

    angular.module('rms')
        .factory('connectionTimeoutInterceptor', [
            '$q',
            '$injector',
            'errorToastDelay',
            connectionTimeoutInterceptor
        ]);

    function connectionTimeoutInterceptor($q, $injector, errorToastDelay) {
        function onResponseError(rejection) {
            if(rejection.status == 502) {
                $injector.get('ErrorToasts').show('Der Server ist nicht erreichbar. Bitter überprüfen Sie Ihre Internetverbindung.', errorToastDelay, 'OK', 'error');
            }

            return $q.reject(rejection);
        }

        return {
            responseError: onResponseError
        };
    }

})();
