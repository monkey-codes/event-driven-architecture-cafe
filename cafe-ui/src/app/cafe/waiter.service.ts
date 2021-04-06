import {Injectable, NgZone} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {v4 as uuidv4} from 'uuid';
import {ActivityService} from './activity.service';
import {MatSnackBar} from '@angular/material/snack-bar';

@Injectable({
    providedIn: 'root'
})
export class WaiterService extends ActivityService {

    private waiterId = 'fb5cd662-2250-4a3a-bd52-456926e0f179';

    constructor(zone: NgZone, http: HttpClient, private snackBar: MatSnackBar) {
        super(zone, http, 'http://localhost:8080/api');
    }

    public placeOrder() {
        return this.http.post(`${this.baseUrl}/waiters/${this.waiterId}/orders`, {
            id: uuidv4(),
            items: [{
                name: 'burger',
                quantity: 1
            }]
        }).subscribe({
            next: value => console.log(value),
            // error: err => this.snackBar.open(err)
            error: response => this.snackBar.open(response.error.error, null, {
                duration: 2000
            } )
        });
    }
}
