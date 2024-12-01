(function() {

    angular.module('supportTicket')
        .controller('CreateSupportTicketModalController',[
            '$mdDialog',
            'AuthenticationService',
            'SupportMessage',
            CreateSupportTicketModalController
        ]);
    function CreateSupportTicketModalController($mdDialog, AuthenticationService, SupportMessage) {
        var self = this;

        self.ticket = {};

        self.isLoggedIn = false;

        self.cancel = cancel;
        self.submit = submit;

        init();
        function init() {
            if(AuthenticationService.isLoggedIn()) {
                self.isLoggedIn = true;
                var user = AuthenticationService.getUser();
                self.ticket.name = user.firstName + " " + user.lastName;
                self.ticket.email = user.email;
            } else {
                self.isLoggedIn = false;
            }
            self.ticket.openedAt = new Date().valueOf();
        }

        //public
        function cancel() {
            $mdDialog.cancel();
        }

        //public
        function submit() {
            SupportMessage.save(self.ticket).$promise
                .then(function() {
                   $mdDialog.hide();
                });
        }


    }
})();