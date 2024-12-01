(function() {

    angular.module('rms')
        .factory('DoorAuthorisationAttempt', [
            '$resource',
            'apiAddress',
            DeviceCategory
        ]);

    function DeviceCategory($resource, apiAddress) {
        return $resource(apiAddress + 'stats/doorauthorisationattempts:id');
    }

})();
