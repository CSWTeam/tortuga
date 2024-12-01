/**
 * Created by Schir on 13.01.2016.
 */

(function() {
    angular.module('deviceReservations')
        .controller('DeviceReservationListController', [
            'DeviceReservation',
            '$mdDialog',
            'AuthenticationService',
            DeviceReservationListController
        ]);


    function DeviceReservationListController(DeviceReservation, $mdDialog, AuthenticationService){
        var self = this;

        self.reservations = DeviceReservation.query({
            "user": AuthenticationService.getUser().id
        });


        self.deviceReservationDeleted = deviceReservationDeleted;
        self.reservationFilter = reservationFilter;

        //public
        function deviceReservationDeleted(reservation){
            self.reservations.splice(self.reservations.indexOf(reservation), 1);
        }

        //public
        function reservationFilter(reservation) {
            return reservation.borrowed || reservation.timeSpan.end > (new Date()).valueOf();
        }
    }

})();