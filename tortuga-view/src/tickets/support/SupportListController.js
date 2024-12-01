(function() {

    angular.module('tickets.support')
        .controller('SupportListController', [
            'SupportMessage',
            '$mdDialog',
            SupportListController
        ]);

    function SupportListController(SupportMessage, $mdDialog) {
        var self = this;

        self.messages = SupportMessage.query({done: false});

        self.ignore = ignore;
        self.answer = answer;

        //public
        function ignore(message) {
            message.done = true;
            SupportMessage.update({id: message.id}, message).$promise
                .then(function() {
                    self.messages.splice(self.messages.indexOf(message), 1);
                });
        }


        //public
        function answer(message, event) {
            $mdDialog.show({
                clickOutsideToClose: true,
                templateUrl: 'src/tickets/support/answer.html',
                controller: ['$mdDialog', AnswerController],
                controllerAs: 'answerModal',
                targetEvent: event,
                bindToController: true
            }).then(function() {
                self.messages.splice(self.messages.indexOf(message), 1);
            });

            function AnswerController($mdDialog) {
                var self = this;

                self.submit = submit;
                self.cancel = cancel;


                //public
                function cancel() {
                    $mdDialog.cancel();
                }

                //public
                function submit() {
                    SupportMessage.update({id: message.id}, {
                        done: true,
                        answer: self.answer
                    }).$promise
                        .then(function() {
                            $mdDialog.hide();
                        });
                }
            }

        }


    }

})();