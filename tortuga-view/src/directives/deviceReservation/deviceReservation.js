(function() {

    angular.module('rms')
        .directive('deviceReservation', deviceReservationDirective);

    function deviceReservationDirective() {
        return {
            restrict: 'E',
            templateUrl: 'src/directives/deviceReservation/deviceReservation.html',
            scope: {
                reservation: '=',
                onDelete: '@',
                showUser: '='
            },
            bindToController: true,
            controller: 'DeviceReservationController',
            controllerAs: 'deviceReservation'
        };
    }

})();