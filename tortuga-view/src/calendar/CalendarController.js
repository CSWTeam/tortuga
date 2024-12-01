(function() {

    angular.module('calendar')
        .controller('CalendarController', [
            'RoomReservation',
            CalendarController
        ]);

    function CalendarController(RoomReservation) {
        var self = this;

        self.getTextForDay = getTextForDay;

        var roomReservations = RoomReservation.query({
            approved: true
        });

        function getShortTimeString(date) {
            return date.getHours() + ':' +
                ((date.getMinutes() < 10) ? ('0' + date.getMinutes()) : date.getMinutes());
        }

        //public
        function getTextForDay(date) {
            return roomReservations.$promise.then(function(result) {
                var reservations = result.filter(function(reservation) {
                    return date.toDateString() == (new Date(reservation.timeSpan.beginning)).toDateString();
                }).sort(function(first, second) {
                    return first.timeSpan.beginning - second.timeSpan.beginning;
                });

                if(reservations.length == 0)
                    return '<div></div>';

                var ret = '<div>';

                for(var i = 0; i < reservations.length; i++) {
                    var startDate = new Date(reservations[i].timeSpan.beginning);
                    var endDate = new Date(reservations[i].timeSpan.end);

                    ret += '<p>' + getShortTimeString(startDate) + ' - ' +
                        getShortTimeString(endDate) + '<br>' + reservations[i].title + '</p>';
                }

                return ret + '</div>';
            });
        }
    }

})();