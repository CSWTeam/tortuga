(function() {

    angular.module('rms')
        .factory('ComplaintTemplate', [
            '$resource',
            'apiAddress',
            ComplaintTemplate
        ]);

    function ComplaintTemplate($resource, apiAddress) {
        return $resource(apiAddress + 'complainttemplates/:id', null, {update: { method: 'PATCH'}});
    }

})();
