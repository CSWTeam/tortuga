/**
 * Created by Schir on 22.01.2016.
 */

(function() {
    angular.module('deviceReservations')
        .controller('DeviceReservationCreateController', [
            'DeviceCategory',
            'Device',
            'DeviceReservation',
            'AuthenticationService',
            '$state',
            DeviceReservationCreateController
        ]);


    function DeviceReservationCreateController(DeviceCategory, Device, DeviceReservation,
                                               AuthenticationService, $state){
        var self = this;

        self.deviceSelection = false;
        self.nextButtonText = "Weiter";
        self.deviceCategories = DeviceCategory.query({active: true});
        self.devices = undefined; //["Laptop 1", "Laptop 2", "Laptop 3"];
        self.selectedDeviceCategory = undefined;
        self.selectedDevice = undefined;
        self.endTime = undefined;
        self.startTime = undefined;
        self.startDate = undefined;
        self.user = AuthenticationService.getUser();

        self.activateDeviceSelection = activateDeviceSelection;
        self.isFormOneValid = isFormOneValid;
        self.validateTimeInput = validateTimeInput;
        self.timesAreValid = timesAreValid;
        self.timespanIsValid = timespanIsValid;
        self.startTimeIsInFuture = startTimeIsInFuture;
        self.isFormTwoValid = isFormTwoValid;
        self.submit = submit;

        //public
        function isFormTwoValid(){
            return self.createForm.deviceSelector.$valid;
        }
        //public
        function isFormOneValid(){
            return self.createForm.startTime.$valid && self.createForm.endTime.$valid
                && self.createForm.deviceCategorySelector.$valid && self.createForm.datePicker.$valid
                && self.timespanIsValid() && self.startTimeIsInFuture();
        }

        //public
        function activateDeviceSelection(){
            self.deviceSelection = !self.deviceSelection;

            var timeStart = angular.copy(self.startDate);
            var time = self.startTime.split(":");

            timeStart.setHours(time[0]);
            timeStart.setMinutes(time[1]);

            var timeEnd = angular.copy(self.startDate);
            time = self.endTime.split(":");

            timeEnd.setHours(time[0]);
            timeEnd.setMinutes(time[1]);

            self.devices = Device.query({category: self.selectedDeviceCategory.id, beginningTime: timeStart.valueOf(),
                endTime: timeEnd.valueOf()}).$promise.then(function (devices){

                self.devices = devices;
                if(devices.length != 0){
                    self.selectedDevice = devices[0];
                }
            });

        }

        //public
        function timesAreValid() {
            return self.createForm.startTime.$valid && self.createForm.endTime.$valid;
        }

        //public
        function startTimeIsInFuture() {
            if(!timesAreValid())
                return true;

            var date = angular.copy(self.startDate);
            var split = self.startTime.split(':');

            date.setHours(split[0]);
            date.setMinutes(split[1]);

            return date.getTime() > (new Date()).getTime();
        }

        //public
        function timespanIsValid() {
            if(!timesAreValid())
                return true;

            var startSplit = self.startTime.split(':');
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

            var deviceReservation = {device: self.selectedDevice, user: self.user};

            var timeStart = angular.copy(self.startDate);
            var time = self.startTime.split(":");

            timeStart.setHours(time[0]);
            timeStart.setMinutes(time[1]);

            deviceReservation.timeSpan = {};
            deviceReservation.timeSpan.beginning = timeStart.valueOf();

            time = self.endTime.split(":");

            timeStart.setHours(time[0]);
            timeStart.setMinutes(time[1]);

            deviceReservation.timeSpan.end = timeStart.valueOf();

            DeviceReservation.save(deviceReservation).$promise
                .then(function (){
                        $state.go('deviceReservationList');
                });
        }
    }

})();