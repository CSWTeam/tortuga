(function() {

    angular.module('rms')
        .service('PinService', [
            '$state',
            '$q',

            PinService
        ]);

    function PinService($state, $q) {
        var self = this;

        var promise;

        self.createNewPin = createNewPin;
        self.finished = finished;

        //public
        function createNewPin(user) {
            promise = $q.defer();
            $state.go('newPin',{userId: user.id});
            return promise.promise;
        }

        //public
        function finished() {
            promise.resolve();
        }
    }
})();