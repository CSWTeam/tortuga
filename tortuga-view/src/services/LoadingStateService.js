(function() {

    angular.module('rms')
        .service('LoadingStateService', LoadingStateService);

    function LoadingStateService() {
        var self = this;

        var loadingCount = 0;

        self.startedLoading = startedLoading;
        self.finishedLoading = finishedLoading;
        self.isLoading = isLoading;

        function startedLoading() {
            loadingCount++;
        }

        function finishedLoading() {
            loadingCount--;
        }

        function isLoading() {
            return loadingCount > 0;
        }
    }

})();