(function() {

    angular.module('rms')
        .factory('RoomReservation', [
            '$resource',
            'apiAddress',
            RoomReservation
        ]);

    function RoomReservation($resource, apiAddress) {
        return $resource(apiAddress + 'roomreservations/:id', null, {update: { method: 'PATCH'}});
    }

})();
