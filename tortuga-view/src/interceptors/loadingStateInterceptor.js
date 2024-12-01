(function () {

    angular.module('rms')
        .factory('loadingStateInterceptor', [
            '$q',
            'LoadingStateService',
            loadingStateInterceptor
        ]);

    function loadingStateInterceptor($q, LoadingStateService) {
        function onRequest(reqConfig) {
            LoadingStateService.startedLoading();

            return reqConfig;
        }

        function onResponse(response) {
            LoadingStateService.finishedLoading();

            return response;
        }

        function onResponseError(rejection) {
            LoadingStateService.finishedLoading();

            return $q.reject(rejection);
        }

        return {
            request: onRequest,
            response: onResponse,
            responseError: onResponseError
        };
    }

})();
