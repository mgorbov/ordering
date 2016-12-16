angular.module('ordering').controller('OrdersCtrl', ['$scope', '$state', '$filter',
  function ($scope, $state, $filter) {
    var itemsGridScopeProvider = {
      gridRowClick: function (rowItem) {
        $state.go("index.order", rowItem.entity)
      }
    };

    $scope.ordersGridOptions = {
      enableColumnMenus: false,
      enableSorting: true,
      enableFiltering: true,
      enableRowSelection: true,
      enableRowHeaderSelection: false,
      multiSelect: false,
      noUnselect: true,
      rowHeight: 25,
      columnDefs: [
        {name: 'Номер', field: 'number', width: '20%'},
        {name: 'Дата', field: 'date_txt', width: '20%'},
        {name: 'Автор', field: 'author', width: '20%'},
        {name: 'Контрагент', field: 'counterparty', width: '20%'},
        {name: 'Сумма', field: 'sum', width: '20%', enableFiltering: false}
      ],
      appScopeProvider: itemsGridScopeProvider,
      rowTemplate: '<div ng-dblclick="grid.appScope.gridRowClick(row)" ' +
      'ng-repeat="(colRenderIndex, col) in colContainer.renderedColumns track by col.uid" ' +
      'class="ui-grid-cell" ng-class="{ \'ui-grid-row-header-cell\': col.isRowHeader }"  ui-grid-cell></div>',
      onRegisterApi: function(gridApi){
        $scope.ordersGridApi = gridApi;
        $scope.ordersGridApi.grid.registerRowsProcessor( $scope.periodFilter, 200 );
      }
    };

    var tmpDate = new Date();
    tmpDate.setDate(30);
    $scope.dateTo = angular.copy(tmpDate);
    tmpDate.setDate(1);
    $scope.dateFrom = angular.copy(tmpDate);

    $scope.periodFilter = function (renderedRows) {
      renderedRows.forEach(function(row) {
        if (row.entity.date < $scope.dateFrom ||  row.entity.date > $scope.dateTo){
          row.visible = false;
        }
      });

      return renderedRows;
    };

    $scope.filterChange = function () {
      $scope.ordersGridApi.grid.refresh();
    };
    
    //TODO for preview only
    var data = {
      "number": "12345",
      "counterparty": "OOO Руби бабло",
      "author": "Стёпка"
    };

    $scope.ordersGridOptions.data = [];
    for (var i = 0; i < 200; i++) {
      data = angular.copy(data);
      tmpDate.setDate(tmpDate.getDate()+1);
      data.date = angular.copy(tmpDate);
      data.date_txt = $filter('date')(data.date, 'd/M/yy H:mm');
      data.sum = Math.floor(Math.random() * (9999)) + 1;
      data.id = i;

      $scope.ordersGridOptions.data.push(data);
    }
  }
]);
