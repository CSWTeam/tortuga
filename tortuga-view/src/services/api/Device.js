(function() {

    angular.module('rms')
        .factory('Device', [
            '$resource',
            'apiAddress',
            Device
        ]);

    function Device($resource, apiAddress) {
        return $resource(apiAddress + 'devices/:deviceId', null, {update: { method: 'PATCH'}});
    }

})();
