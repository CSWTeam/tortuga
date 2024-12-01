(function() {

    angular.module('rms')
        .controller('DeviceReservationController', [
            '$scope',
            'DeviceReservation',
            '$mdDialog',
            '$filter',
            'AuthenticationService',
            'LocalService',
            DeviceReservationController
        ]);

    function DeviceReservationController($scope, DeviceReservation, $mdDialog, $filter, AuthenticationService, LocalService) {
        var self = this;

        //self.reservation
        //self.onDelete

        self.borrow = borrow;
        self.returnDevice = returnDevice;
        self.deleteReservation = deleteReservation;
        self.isActive = isActive;
        self.isBorrowed = isBorrowed;
        self.canBeBorrowed = canBeBorrowed;
        self.editReservation = editReservation;

        self.isLocal = LocalService.isLocal;

        //public
        function borrow(event) {
            var patch = {
                borrowed: true
            };

            DeviceReservation.update({id: self.reservation.id}, patch).$promise.then(function(reservation) {
                self.reservation = reservation;

                var cabinet = self.reservation.device.cabinet;

                $mdDialog.show(
                    $mdDialog.alert()
                        .title(cabinet + ' öffnet')
                        .content(cabinet + ' öffnet, bitte das Gerät herausnehmen.')
                        .targetEvent(event)
                        .ok('OK')
                );
            });
        }

        //public
        function returnDevice(event) {
            var patch = {
                borrowed: false
            };

            self.reservation = DeviceReservation.update({id: self.reservation.id}, patch).$promise.then(function(reservation) {
                self.reservation = reservation;

                var cabinet = self.reservation.device.cabinet;

                $mdDialog.show(
                    $mdDialog.alert()
                        .title(cabinet + ' öfffnet')
                        .content(cabinet + ' öffnet, bitte das Gerät hineinlegen.')
                        .targetEvent(event)
                        .ok('OK')
                );
            });
        }

        //public
        function editReservation(event, extend) {
            return $mdDialog.show({
                templateUrl: 'src/directives/deviceReservation/edit.html',
                controller: ['$mdDialog', EditDeviceReservationController],
                controllerAs: 'editModal',
                targetEvent: event,
                bindToController: true,
                locals: {
                    reservation: angular.copy(self.reservation),
                    extend: angular.copy(extend)
                }
            }).then(function(reservation){
                self.reservation=reservation
            });

            function EditDeviceReservationController($mdDialog) {
                var self = this;

                self.cancel = cancel;
                self.submit = submit;

                self.validateTimeInput = validateTimeInput;
                self.startTimeIsInFuture = startTimeIsInFuture;
                self.timespanIsValid = timespanIsValid;

                self.beginningDate = new Date(self.reservation.timeSpan.beginning);
                self.endDate = new Date(self.reservation.timeSpan.end);


                self.beginningTime = intToTwoDigitString(self.beginningDate.getHours()) + ":" +
                    intToTwoDigitString(self.beginningDate.getMinutes());
                self.endTime = intToTwoDigitString(self.endDate.getHours()) + ":" +
                    intToTwoDigitString(self.endDate.getMinutes());

                function intToTwoDigitString(int){
                    var twoDigitString = int.toString();
                    if(twoDigitString.length < 2){
                        twoDigitString = "0" + twoDigitString;
                    }
                    return twoDigitString;
                }

                //public
                function cancel(){
                    $mdDialog.cancel();
                }

                function timesAreValid() {
                    return self.editForm.beginningTime.$valid && self.editForm.endTime.$valid;
                }

                //public
                function startTimeIsInFuture() {
                    if(!timesAreValid())
                        return true;

                    var date = angular.copy(self.beginningDate);
                    var split = self.beginningTime.split(':');

                    date.setHours(split[0]);
                    date.setMinutes(split[1]);

                    return date.getTime() > (new Date()).getTime();
                }

                //public
                function timespanIsValid() {
                    if(!timesAreValid())
                        return true;

                    var startSplit = self.beginningTime.split(':');
                    var endSplit = self.endTime.split(':');

                    return parseInt(startSplit[0]) * 60 + parseInt(startSplit[1]) <
                        parseInt(endSplit[0]) * 60 + parseInt(endSplit[1]);
                }

                //public
                function validateTimeInput(input) {
                    if(input == undefined || input == '')
                        return {
                            validTime: true
                        };

                    var ret = {
                        validTime: false
                    };

                    var split = input.split(':');

                    if(split.length != 2)
                        return ret;

                    if(split[0].length > 2)
                        return ret;

                    var hours = parseInt(split[0]);
                    if(isNaN(hours) || hours > 24 || hours < 0)
                        return ret;

                    if(split[1].length != 2)
                        return ret;

                    var minutes = parseInt(split[1]);
                    if(isNaN(minutes) || minutes > 60 || minutes < 0)
                        return ret;

                    return {
                        validTime: true
                    };
                }

                //public
                function submit(){
                    var res = {};
                    res.timeSpan = {};

                    var timeStart = angular.copy(self.beginningDate);
                    var time = self.beginningTime.split(":");

                    timeStart.setHours(time[0]);
                    timeStart.setMinutes(time[1]);

                    self.reservation.timeSpan = {};
                    self.reservation.timeSpan.beginning = timeStart.valueOf();


                    timeStart = angular.copy(self.endDate);
                    time = self.endTime.split(":");

                    timeStart.setHours(time[0]);
                    timeStart.setMinutes(time[1]);

                    self.reservation.timeSpan.end = timeStart.valueOf();

                    self.reservation.user = AuthenticationService.getUser();

                    res.timeSpan = this.reservation.timeSpan

                    DeviceReservation.update({id:self.reservation.id}, self.reservation).$promise
                        .then(function (deviceReservation) {
                            $mdDialog.hide(deviceReservation);
                        });
                }

            }


        }

        //public
        function deleteReservation() {

            var dialog = $mdDialog.confirm()
                .title("Diese Reservierung löschen?")
                .textContent("Reservierung für  " + self.reservation.device.name + " am " +
                    $filter('date')(self.reservation.timeSpan.beginning,  'MMM d, HH:mm') +
                    " wirklich löschen? Dies kann nicht rückgängig gemacht werden!")
                .ok("löschen")
                .targetEvent(event)
                .cancel("abbrechen");
            $mdDialog.show(dialog).then(function() {
                return DeviceReservation.delete({id: self.reservation.id}).$promise;
            }).then(function(response) {
                $scope.$parent.$eval(self.onDelete);

            }).catch(function(fail) {
                console.warn(fail);
            });

        }

        //public
        function edit(event) {
            $mdDialog.show({
                template: [
                    '<md-dialog>',
                        '',
                        '',
                        '',
                    '</md-dialog>'
                ].join(''),
                controller: function EditDeviceReservationController() {

                },
                targetEvent: event
            })
        }

        //public
        function isActive() {
            var now = (new Date()).getTime();

            return now >= self.reservation.timeSpan.beginning && now <= self.reservation.timeSpan.end;
        }

        //public
        function isBorrowed() {
            return self.reservation.borrowed;
        }

        //public
        function canBeBorrowed() {
            return isActive() && !isBorrowed();
        }
    }

})();