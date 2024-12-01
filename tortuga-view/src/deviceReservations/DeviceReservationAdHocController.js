/**
 * Created by Schir on 22.01.2016.
 */

(function() {
    angular.module('deviceReservations')
        .controller('DeviceReservationAdHocController', [
            'DeviceCategory',
            'Device',
            'DeviceReservation',
            'AuthenticationService',
            '$state',
            'ErrorToasts',
            '$mdDialog',
            DeviceReservationAdHocController
        ]);


    function DeviceReservationAdHocController(DeviceCategory, Device, DeviceReservation,
                                              AuthenticationService, $state, ErrorToast, $mdDialog) {
        var self = this;

        self.deviceCategories = DeviceCategory.query({active: true});

        self.devices = undefined;

        self.selectedDeviceCategory = undefined;
        self.hoursToReservate = undefined;

        self.selectedDevice = undefined;

        self.user = AuthenticationService.getUser();

        self.loadPossibleDevices = loadPossibleDevices;
        self.canBeBorrowedForHours = canBeBorrowedForHours;
        self.submit = submit;


        //public
        function canBeBorrowedForHours(hour) {
            var date = new Date();
            return date.getHours() + hour < 24;

        }

        //public
        function loadPossibleDevices() {
            if(self.hoursToReservate == undefined || self.selectedDeviceCategory == undefined) {
                return;
            }

            var timeStart = new Date().valueOf();

            var timeEnd = timeStart + self.hoursToReservate * 60 * 60 * 1000;

            self.devices = Device.query({
                category: self.selectedDeviceCategory.id,
                beginningTime: timeStart,
                endTime: timeEnd
            }).$promise.then(function(devices) {
                self.devices = devices;
                if(devices.length != 0) {
                    self.selectedDevice = devices[0];
                } else {
                    ErrorToast.show("Kein verfügbares Gerät gefunden.");
                }
            });

        }

        //public
        function submit() {

            var deviceReservation = {device: self.selectedDevice, user: self.user};

            var timeStart = new Date().valueOf();

            deviceReservation.timeSpan = {};
            deviceReservation.timeSpan.beginning = timeStart;

            deviceReservation.timeSpan.end = timeStart + self.hoursToReservate * 60 * 60 * 1000;

            DeviceReservation.save(deviceReservation).$promise
                .then(function(reservation) {

                    var patch = {
                        borrowed: true
                    };

                    DeviceReservation.update({id: reservation.id}, patch).$promise.then(function(reservation) {
                        self.reservation = reservation;

                        var cabinet = self.reservation.device.cabinet;

                        return $mdDialog.show(
                            $mdDialog.alert()
                                .title(cabinet + ' öffnet')
                                .content(cabinet + ' öffnet, bitte das Gerät herausnehmen.')
                                .ok('OK')
                        );
                    }).then(function() {
                        $state.go('deviceReservationList');
                    }).catch(function(reason) {
                        ErrorToast.show(reason);
                        console.error(reason);
                    })
                });

        }
    }

})
();