(function() {

    angular.module('rms')
        .factory('DeviceReservation', [
            '$resource',
            'apiAddress',
            DeviceReservationResource
        ]);

    function DeviceReservationResource($resource, apiAddress) {
        return $resource(apiAddress + 'devicereservations/:id', null, {update: { method: 'PATCH'}});
    }

})();