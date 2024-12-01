(function() {

    angular.module('rms')
        .directive('roomReservation', roomReservationDirective);

    function roomReservationDirective() {
        return {
            restrict: 'E',
            templateUrl: 'src/directives/roomReservation/roomReservation.html',
            scope: {
                reservation: '=',
                onDelete: '@'
            },
            bindToController: true,
            controller: 'RoomReservationController',
            controllerAs: 'roomReservation'
        };
    }

})();