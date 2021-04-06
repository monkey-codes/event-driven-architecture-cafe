import {Injectable, NgZone} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {ActivityService} from './activity.service';

@Injectable({
    providedIn: 'root'
})
export class KitchenService extends ActivityService {

    constructor(zone: NgZone, http: HttpClient) {
        super(zone, http, 'http://localhost:8081/api');
    }
}
