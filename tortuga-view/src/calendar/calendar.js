(function() {

    angular.module('calendar', [
        'ui.router',
        'materialCalendar'
    ]).config([
        '$stateProvider',
        config
    ]);

    function config($stateProvider) {
        $stateProvider.state('calendar', {
            url: '/calendar',
            templateUrl: 'src/calendar/calendar.html',
            controller: 'CalendarController',
            controllerAs: 'calendar',
            data: {
                viewName: 'Kalender'
            }
        });
    }

})();