(function() {

    angular.module('rms')
        .factory('DeviceCategory', [
            '$resource',
            'apiAddress',
            DeviceCategory
        ]);

    function DeviceCategory($resource, apiAddress) {
        return $resource(apiAddress + 'devicecategories/:id', null, {update: { method: 'PATCH'}});
    }

})();
