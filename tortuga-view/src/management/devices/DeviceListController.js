(function () {

    angular.module('management')
        .controller('DeviceListController', [
        'Device',
        'DeviceCategory',
        '$mdDialog',
        DeviceListController
    ]);

    function DeviceListController(Device, DeviceCategory, $mdDialog) {
        var self = this;

        self.categories = DeviceCategory.query();

        self.devices = Device.query();

        self.showDetails = showDetails;
        self.editDevice = editDevice;
        self.createDevice = createDevice;
        self.removeDevice = removeDevice;
        self.getCategoryWithDevices = getCategoryWithDevices;

        //public
        function showDetails(device, event) {
            $mdDialog.show({
                clickOutsideToClose: true,
                templateUrl: 'src/management/devices/details.html',
                controller: ['$mdDialog', DeviceDetailsModalController],
                controllerAs: 'deviceDetails',
                targetEvent: event,
                bindToController: true,
                locals: {
                    device: device
                }
            });

            function DeviceDetailsModalController($mdDialog) {
                var self = this;

                //self.device local

                self.close = close;

                function close() {
                    $mdDialog.cancel();
                }
            }
        }

        //public
        function getCategoryWithDevices() {
            return self.categories.filter(function(cat) {
                return self.devices.filter(function (accDev) {
                        return accDev.category.id == cat.id;
                    }).length > 0;
            });
        }

        //public
        function removeDevice(event, device) {
            var dialog = $mdDialog.confirm()
                .title("Gerät " + device.name + " löschen?")
                .textContent("Gerät " + device.name + " wirklich löschen? Dies kann nicht rückgängig gemacht werden!")
                .ok("löschen")
                .targetEvent(event)
                .cancel("abbrechen");
            $mdDialog.show(dialog).then(function () {
                return Device.delete({deviceId: device.id}).$promise;
            }).then(function (response) {
                self.devices.splice(self.devices.indexOf(device), 1);
            }).catch(function (fail) {
                console.warn(fail);
            });
        }


        //public
        function createDevice(event) {
            editDeviceIntern(event).then(function (device) {
                self.devices.push(device);
            });
        }

        //public
        function editDevice(event, device) {
            editDeviceIntern(event, device).then(function (newDevice) {
                self.devices[self.devices.indexOf(device)] = newDevice;
            });
        }

        //public
        function editDeviceIntern(event, device) {
            if (device === undefined) {
                var createNewDevice = true;
                device = new Device();
            } else {
                var createNewDevice = false
            }


            return $mdDialog.show({
                templateUrl: 'src/management/devices/create.html',
                controller: ['$mdDialog', 'DeviceCategory', EditDeviceModalController],
                controllerAs: 'deviceModal',
                targetEvent: event,
                bindToController: true,
                locals: {
                    device: angular.copy(device)
                }
            });


            function EditDeviceModalController($mdDialog, DeviceCategory) {
                var self = this;

                //self.device argument
                self.newDevice = createNewDevice;

                self.submit = submit;
                self.cancel = cancel;

                self.header = "";

                self.categories = DeviceCategory.query();


                init();
                function init() {
                    if (self.newDevice) {
                        self.header = 'Neues Gerät anlegen';
                    } else {
                        self.header = "Gerät '" + self.device.name + "' bearbeiten";
                    }
                    if (self.device.acquisitionDate !== undefined) {
                        self.dateAcquired = new Date(self.device.acquisitionDate);
                    }
                }

                //public
                function cancel() {
                    $mdDialog.cancel();
                }

                //public
                function submit() {

                    if(self.dateAcquired === undefined) {
                        self.device.acquisitionDate = undefined;
                    } else {
                        self.device.acquisitionDate = self.dateAcquired.valueOf();
                    }
                    var promise;
                    if (createNewDevice) {
                        promise = Device.save(self.device).$promise;
                    } else {
                        promise = Device.update({deviceId: self.device.id}, self.device).$promise;
                    }

                    promise.then(function (device) {
                        $mdDialog.hide(device);
                    }).catch(function (reason) {
                        if (reason != undefined)
                            console.warn(reason);
                    });
                }

            }
        }
    }

})
();
