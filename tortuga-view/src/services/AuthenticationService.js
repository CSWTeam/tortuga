(function () {

    angular.module('rms')
        .service('AuthenticationService', [
            '$http',
            'apiAddress',
            '$cookies',
            '$state',
            '$rootScope',
            AuthenticationService
        ]);

    function AuthenticationService($http, apiAddress, $cookies, $state, $rootScope) {
        var self = this;

        var loggedIn = $cookies.get('auth_token') != undefined;
        var user = loggedIn ? decodeUser($cookies.get('auth_token')) : null;

        $rootScope.$on('$stateChangeStart', function onStateChange(event, to) {
            if(!loggedIn && to.name != 'login' && to.name != 'calendar') {
                event.preventDefault();
                $state.go('login');
            }
        });

        self.login = login;
        self.logout = logout;
        self.isLoggedIn = isLoggedIn;
        self.getUser = getUser;

        self.getTokenValidityTime = getTokenValidityTime;

        self.isStudent = isStudent;
        self.isLecturer = isLecturer;
        self.isCswTeam = isCswTeam;
        self.isAdmin = isAdmin;

        function decodeUser(encodedUser) {
            var split = encodedUser.split('.');

            var ret = JSON.parse(atob(decodeURIComponent(split[0])));

            return ret.user;
        }

        //public
        function getTokenValidityTime() {
            var split = $cookies.get('auth_token').split('.');

            var ret = JSON.parse(atob(decodeURIComponent(split[0])));

            return ret.validFor;
        }

        //public
        function isLoggedIn() {
            return loggedIn;
        }

        //public
        function getUser() {
            return user;
        }

        //public
        function isStudent() {
            return user != null && user.role == 'STUDENT';
        }

        //public
        function isLecturer() {
            return user != null && (user.role == 'LECTURER' || isCswTeam());
        }

        //public
        function isCswTeam() {
            return user != null && (user.role == 'CSW_TEAM' || isAdmin());
        }

        //public
        function isAdmin() {
            return user != null && user.role == 'ADMIN';
        }

        //public
        function login(username, password, longToken) {
            var httpPromise = $http.post(apiAddress + 'login', {
                loginName: username,
                password: password,
                longToken: longToken
            });

            httpPromise.then(function(response) {
                loggedIn = true;
                user = response.data;
            });

            return httpPromise;
        }

        //public
        function logout() {
            $cookies.remove('auth_token');

            loggedIn = false;
            user = null;

            $state.go('login');
        }
    }

})();
