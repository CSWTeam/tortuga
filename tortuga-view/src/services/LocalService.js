(function() {

    angular.module('rms')
        .service('LocalService', [
            'apiAddress',
            '$cookies',
            '$http',
            LocalService
        ]);

    function LocalService(apiAddress, $cookies, $http) {
        var self = this;

        self.isLocal = isLocal;

        var localPromise = null;

        function isLocal() {
            if($cookies.get('is_local') == 'true') {
                return true;
            } else if($cookies.get('is_local') == 'false') {
                return false;
            } else {
                if(localPromise != null)
                    return false;

                localPromise = $http.get(apiAddress + 'localnet').then(function(response) {
                    if(!!response.data)
                        $cookies.put('is_local', 'true');
                    else
                        $cookies.put('is_local', 'false');
                });

                return false;
            }
        }
    }

})();