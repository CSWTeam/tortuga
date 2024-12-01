(function () {

    angular.module('rms')
        .service('ErrorToasts', [
            '$mdToast',
            ErrorToasts
        ]);

    function ErrorToasts($mdToast) {
        var self = this;

        self.show = show;

        function show(error, delay, action, theme) {
            if(action === undefined) {
                action = 'OK';
            }

            if(delay === undefined) {
                delay = false;
            }
            var cfg =
                $mdToast.simple()
                    .content(error)
                    .action(action)
                    .position('bottom right')
                    .theme(theme)
                    .hideDelay(delay);
            return $mdToast.show(cfg);
        }
    }

})();