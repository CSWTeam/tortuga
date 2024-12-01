(function() {

    angular.module('rms')
        .controller('RoomReservationController', [
            '$scope',
            'RoomReservation',
            '$mdDialog',
            'LocalService',
            RoomReservationController
        ]);

    function RoomReservationController($scope, RoomReservation, $mdDialog, LocalService) {
        var self = this;

        //self.reservation
        //self.onDelete

        self.openRoom = openRoom;
        self.closeRoom = closeRoom;
        self.deleteReservation = deleteReservation;
        self.canBeOpenedNow = canBeOpenedNow;
        self.getState = getState;

        self.isLocal = LocalService.isLocal;


        //public
        function getState() {
            if(self.reservation.open) {
                return "Daueröffnung aktiv";
            }
            if(self.reservation.approved) {
                return "Bestätigt";
            }
            return "In Bearbeitung";
        }


        //public
        function openRoom() {
            var updatedReservation = RoomReservation.update({id: self.reservation.id}, {open: true});

            updatedReservation.$promise.then(function() {
                self.reservation = updatedReservation;
            });
        }

        //public
        function closeRoom() {
            var updatedReservation = RoomReservation.update({id: self.reservation.id}, {open: false});

            updatedReservation.$promise.then(function() {
                self.reservation = updatedReservation;
            });
        }

        //public
        function deleteReservation() {
            var dialog = $mdDialog.confirm()
                .title("Raumbuchung löschen?")
                .textContent("Raumbuchung wirklich löschen? Dies kann nicht rückgängig gemacht werden!")
                .ok("löschen")
                .targetEvent(event)
                .cancel("abbrechen");
            $mdDialog.show(dialog, event).then(function () {
                return RoomReservation.delete({id: self.reservation.id}).$promise;
            }).then(function () {
                $scope.$parent.$eval(self.onDelete);
            }).catch(function (fail) {
                console.warn(fail);
            });
        }

        //public
        function canBeOpenedNow() {

            var now = new Date().valueOf();
            return self.reservation.approved && self.reservation.openedTimeSpan.beginning < now && now < self.reservation.openedTimeSpan.end;

        }

    }

})();