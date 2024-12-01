(function() {

    angular.module('rms')
        .factory('errorInterceptor', [
            '$q',
            '$injector',
            'errorToastDelay',
            errorInterceptor
        ]);

    function errorInterceptor($q, $injector, errorToastDelay) {
        function onResponseError(rejection) {
            if(rejection.status != 502 && rejection.status != 401) {
                var ErrorToasts = $injector.get('ErrorToasts');

                if(rejection.data.errors) {
                    var errors = rejection.data.errors;

                    ErrorToasts.show(errors[Object.keys(errors)[0]], errorToastDelay);
                } else {
                    ErrorToasts.show(rejection.data.errorMessage, errorToastDelay);
                }
            }

            return $q.reject(rejection);
        }

        return {
            responseError: onResponseError
        };
    }

})();
