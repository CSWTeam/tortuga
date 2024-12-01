(function() {

    angular.module('management')
        .controller('RoomListController', [
            'RoomReservation',
            '$mdDialog',
            'AuthenticationService',
            '$stateParams',
            RoomListController
        ]);

    function RoomListController(RoomReservation, $mdDialog, AuthenticationService, $stateParams) {
        var self = this;

        self.reservations = [];
        reloadReservations();


        self.addReservation = addReservation;
        self.reservationDeleted = reservationDeleted;

        if($stateParams.create == 'true') {
            addReservation();
        }

        function reloadReservations() {
            return RoomReservation.query({
                "timeSpan.end": ">" + (new Date()).valueOf(),
                "user": AuthenticationService.getUser().id
            }).$promise.then(function(data) {
                self.reservations.forEach(function (reservation) {
                    if(data.map(function(res) {
                            return res.id
                        }).indexOf(reservation.id) == -1) {
                        self.reservations.splice(index, 1);
                    }
                });

                data.forEach(function(reservation) {
                    if(self.reservations.map(function(res) {
                            return res.id
                        }).indexOf(reservation.id) == -1) {
                        self.reservations.push(reservation);
                    }
                });

            })
        }

        //public
        function reservationDeleted(reservation) {
            self.reservations.splice(self.reservations.indexOf(reservation), 1);
        }


        //public
        function addReservation(event) {
            return $mdDialog.show({
                templateUrl: 'src/room/create.html',
                controller: ['$mdDialog', AddReservationController],
                controllerAs: 'roomModal',
                targetEvent: event
            }).then(function(reservation) {
                self.reservations.push(reservation);
                reloadReservations();
            });


            function AddReservationController($mdDialog) {
                var self = this;

                self.submit = submit;
                self.cancel = cancel;

                self.validateTimeInput = validateTimeInput;
                self.startTimeIsInFuture = startTimeIsInFuture;
                self.durationIsValid = durationIsValid;
                self.timeSpanIsValid = timeSpanIsValid;

                self.startTime = '';
                self.endTime = '';

                self.isRepeated = false;

                //public
                function cancel() {
                    $mdDialog.cancel();
                }

                function timesAreValid() {
                    return self.bookingForm.startTime.$valid && self.bookingForm.endTime.$valid;
                }

                //public
                function startTimeIsInFuture() {
                    if(!timesAreValid())
                        return true;

                    var date = angular.copy(self.day);
                    var split = self.startTime.split(':');

                    date.setHours(split[0]);
                    date.setMinutes(split[1]);

                    return date.getTime() > (new Date()).getTime();
                }

                //public
                function timeSpanIsValid() {
                    if(!timesAreValid())
                        return true;

                    var startSplit = self.startTime.split(':');
                    var endSplit = self.endTime.split(':');

                    return parseInt(startSplit[0]) * 60 + parseInt(startSplit[1]) <
                        parseInt(endSplit[0]) * 60 + parseInt(endSplit[1]);
                }

                //public
                function durationIsValid(){
                    if(!self.isRepeated){
                        return true;
                    }
                    if(self.day == undefined || self.day == ''){
                        return true;
                    }
                    if(self.repetitionEndDate == undefined || self.repetitionEndDate == ''){
                        return true;
                    }

                    return self.day.getTime() <= self.repetitionEndDate.getTime();

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
                function submit() {
                    var timeStart = angular.copy(self.day);
                    var time = self.startTime.split(":");

                    timeStart.setHours(time[0]);
                    timeStart.setMinutes(time[1]);

                    self.reservation.timeSpan = {};
                    self.reservation.timeSpan.beginning = timeStart.valueOf();


                    timeStart = angular.copy(self.day);
                    time = self.endTime.split(":");

                    timeStart.setHours(time[0]);
                    timeStart.setMinutes(time[1]);

                    self.reservation.timeSpan.end = timeStart.valueOf();

                    if(self.isRepeated) {
                        self.reservation.repeatOption = self.frequency;
                        self.reservation.repeatUntil = self.repetitionEndDate.getTime();
                    }

                    RoomReservation.save(self.reservation).$promise
                        .then(function(reservation) {
                            $mdDialog.hide(reservation);
                        });
                }

            }
        }

    }

})
();
