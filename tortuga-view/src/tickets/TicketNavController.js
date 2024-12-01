(function() {
    angular.module('tickets')
        .controller('TicketNavController', [
            'RoomReservation',
            'SupportMessage',
            '$interval',
            'AuthenticationService',
            TicketNavController
        ]);

    function TicketNavController(RoomReservation, SupportMessage, $interval, AuthenticationService) {
        var self = this;

        var reservations = [];
        var tickets = [];


        self.getReservationCount = getReservationCount;
        self.getTicketCount = getTicketCount;
        self.getAllCount = getAllCount;


        $interval(refresh, 10 * 1000);

        refresh();
        function refresh(){
            if(!AuthenticationService.isLoggedIn() || !AuthenticationService.isCswTeam())
                return;

            RoomReservation.query({approved: false, 'timeSpan.end': ">" + (new Date().valueOf())}).$promise
                .then(function(res) {
                    reservations = res;
                });

            SupportMessage.query({done:false}).$promise
                .then(function(tick) {
                    tickets = tick;
                });
        }

        //public
        function getReservationCount() {
            return reservations.length;
        }

        //public
        function getTicketCount() {
            return tickets.length;
        }
        //public
        function getAllCount() {
            return tickets.length + reservations.length;
        }

    }
})();