(function() {

    angular.module('management')
        .config(['$stateProvider', function($stateProvider) {
            $stateProvider.state('management.admin', {
                url: '/admin',
                templateUrl: 'src/management/admin/admin.html',
                controller: 'AdminController',
                controllerAs: 'adminController'
            });
        }]).controller('AdminController', [
        'Major',
        'DeviceCategory',
        'ComplaintTemplate',
        AdminController
    ]);

    function AdminController(Major, DeviceCategory, ComplaintTemplate) {
        var self = this;
        self.majorService = Major;
        self.deviceCategoryService = DeviceCategory;
        self.complaintTemplateService = ComplaintTemplate;
    }

})();
