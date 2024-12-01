(function() {

    angular.module('import', [
        'ui.router'
    ]).config(['$stateProvider', config]);

    function config($stateProvider) {
        $stateProvider.state('import', {
            url: '/import',
            templateUrl: 'src/import/import.html',
            data: {
                viewName: 'Importer'
            }
        });
    }

})();