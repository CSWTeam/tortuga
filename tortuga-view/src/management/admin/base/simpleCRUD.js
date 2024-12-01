(function () {

    angular.module('management')
        .directive('simpleCrud',
            simpeCrudDirective)
        .controller('SimpleCrudController', [
            '$mdDialog',
            SimpleCrud
        ]);

    function SimpleCrud($mdDialog) {
        var self = this;

        //self.service
        //self.name;



        self.items = self.service.query();
        var heads = [];

        self.getHeaders = getHeaders;
        self.getProperty = getProperty;
        self.createItem = createItem;
        self.editItem = editItem;
        self.deleteItem = deleteItem;


        //public
        function getProperty(item, prop) {
            return item[prop];
        }

        //public
        function getHeaders() {
            if(heads.length > 0) {
                return heads;
            }
            if (self.items === undefined || self.items.length == 0) {
                heads = JSON.parse(self.template);
                return head;
            }
            for (var head in self.items[0]) {
                if (head != "id" && head.indexOf("$") == -1 && head.indexOf("toJSON") == -1 && typeof self.items[0][head] !== "function") {
                    heads.push({
                        name: head,
                        type: typeof self.items[0][head]
                    });
                }
            }
            return heads;
        }

        //public
        function createItem(event) {
            editItemIntern(event).then(function(newItem) {
                self.items.push(newItem);
            });
        }

        //public
        function deleteItem(event, item) {
            var itemName = item.name || '';
            var dialog = $mdDialog.confirm()
                .title(self.name + " " + itemName + " löschen?")
                .textContent(self.name + " " + itemName + " wirklich löschen? Dies kann nicht rückgängig gemacht werden!")
                .ok("löschen")
                .targetEvent(event)
                .cancel("abbrechen");
            $mdDialog.show(dialog).then(function() {
                return self.service.delete({id: item.id}).$promise;
            }).then(function(response) {
                self.items.splice(self.items.indexOf(item), 1);
            }).catch(function(fail) {
                console.warn(fail);
            });
        }

        //public
        function editItem(event, item) {
            editItemIntern(event, item).then(function(newItem) {
                self.items[self.items.indexOf(item)] = newItem;
            });
        }

        function editItemIntern(event, item) {
            if (item === undefined) {
                var createNewItem = true;
                item = new self.service();
            } else {
                var createNewItem = false
            }

            return $mdDialog.show({
                templateUrl: 'src/management/admin/base/simpleCRUDcreate.html',
                controller: ['$mdDialog', EditItemModalController],
                controllerAs: 'simpleCrudModal',
                targetEvent: event,
                bindToController: true,
                locals: {
                    item: angular.copy(item),
                    uber: self,
                    newItem: createNewItem
                }
            });



            function EditItemModalController($mdDialog) {
                var self = this;
                var uber = self.uber;

                //self.item argument
                self.newItem = createNewItem;

                self.header = "";

                self.submit = submit;
                self.cancel = cancel;
                self.prop = prop;

                self.getHeaders = uber.getHeaders;


                init();
                function init() {
                    if (self.newItem) {
                        self.header = 'Neues ' + uber.name + ' anlegen';
                    } else {
                        var itemName = item.name || '';
                        self.header = uber.name + " " + itemName + " bearbeiten";
                    }
                }


                //public
                function prop(prop) {
                    return {
                        getSet: function(value) {
                            if(arguments.length == 0) {
                                return self.item[prop];
                            } else {
                                self.item[prop] = value
                            }
                        }
                    };
                }

                //public
                function cancel() {
                    $mdDialog.cancel();
                }

                //public
                function submit() {
                    var promise;
                    if (self.newItem) {
                        promise = uber.service.save(self.item).$promise;
                    } else {
                        promise = uber.service.update({id: self.item.id}, self.item).$promise;
                    }

                    promise.then(function (item) {
                        $mdDialog.hide(item);
                    }).catch(function (reason) {
                        if (reason != undefined)
                            console.warn(reason);
                    });
                }

            }
        }

    }


    function simpeCrudDirective() {
        return {
            restrict: 'E',
            bindToController: true,
            controller: 'SimpleCrudController',
            controllerAs: 'simpleCrud',
            templateUrl: '/src/management/admin/base/simpleCRUD.html',
            scope: {
                service: '=',
                name: '@',
                template: '@'
            }
        }
    }

})();
