(function() {
    angular.module('management.device', [
        'ngSweets'
    ]).config(['$stateProvider', function ($stateProvider) {
            $stateProvider.state('management.devices', {
                url: '/devices',
                templateUrl: 'src/management/devices/list.html',
                controller: 'DeviceListController',
                controllerAs: 'deviceList'
            });
        }])
})();