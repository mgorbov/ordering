angular.module('ordering').controller('OrderCtrl', ['$scope', '$stateParams', function ($scope, $stateParams) {
  $scope.orderItemsGridOptions = {
    enableSorting: true,
    enableFiltering: true,
    enableColumnMenus: false,
    rowHeight: 25,
    columnDefs: [
      {name: 'Код', field: 'code', width: '10%'},
      {name: 'Артикул', field: 'article', width: '10%'},
      {name: 'Наименование', field: 'name', width: '50%'},
      {name: 'Количество', field: 'amount', width: '10%'},
      {name: 'Цена', field: 'price', width: '10%'},
      {name: 'Сумма', field: 'sum', width: '10%'}
    ]
  };

  $scope.hat = $stateParams;
}]);
