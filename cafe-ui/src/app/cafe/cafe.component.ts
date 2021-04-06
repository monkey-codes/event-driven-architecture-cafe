import {Component, OnInit} from '@angular/core';
import {WaiterService} from './waiter.service';
import {KitchenService} from './kitchen.service';
import {StockRoomService} from './stockroom.service';
import {CashierService} from './cashier.service';

@Component({
  selector: 'app-cafe',
  templateUrl: './cafe.component.html',
  styleUrls: ['./cafe.component.scss']
})
export class CafeComponent implements OnInit {

  constructor(public waiterService: WaiterService,
              public kitchenService: KitchenService,
              public stockRoomService: StockRoomService,
              public cashierService: CashierService) {
  }

  ngOnInit(): void {
  }

  placeOrder() {
    return this.waiterService.placeOrder();
  }

}
