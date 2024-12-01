/**
 * Created by Schir on 13.01.2016.
 */

(function() {
    angular.module('management')
        .controller('ManagementDeviceReservationListController', [
            'DeviceReservation',
            '$mdDialog',
            'AuthenticationService',
            DeviceReservationListController
        ]);


    function DeviceReservationListController(DeviceReservation, $mdDialog, AuthenticationService) {
        var self = this;

        self.oldReservations = DeviceReservation.query({
            'timeSpan.end': '<' + new Date().valueOf(),
            'borrowed': 'true'
        });

        self.currentReservations = DeviceReservation.query({
            'timeSpan.beginning': '<' + new Date().valueOf(),
            'timeSpan.end': '>' + new Date().valueOf()
        });


        self.futureReservations = DeviceReservation.query({
            'timeSpan.beginning': '>' + new Date().valueOf(),
        });


        self.deviceReservationDeleted = deviceReservationDeleted;
        self.reservationFilter = reservationFilter;
        self.foundReservation = foundReservation;

        //public
        function deviceReservationDeleted(reservation) {
            self.reservations.splice(self.reservations.indexOf(reservation), 1);
        }

        //public
        function reservationFilter(reservation) {
            return reservation.borrowed || reservation.timeSpan.end > (new Date()).valueOf();
        }

        //public
        function foundReservation() {
            return self.oldReservations.length != 0 ||
                self.currentReservations.length != 0 ||
                self.futureReservations.length != 0;
        }
    }

})();