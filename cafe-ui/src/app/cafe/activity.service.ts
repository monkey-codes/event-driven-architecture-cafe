import {NgZone} from '@angular/core';
import {Subject} from 'rxjs';
import {Activity} from './activity.model';
import {HttpClient} from '@angular/common/http';
import {bufferCount, delay, filter, publishReplay, refCount} from 'rxjs/operators';

export class ActivityService {

    private activitySubject$: Subject<Activity> = new Subject<Activity>();
    private lastEventId = '-1';
    activity$ = this.activitySubject$
        .asObservable()
        .pipe(
            filter(activity => activity.activity !== 'NOOP'),
            delay(2000)
        );

    constructor(protected zone: NgZone, protected http: HttpClient, protected baseUrl: string) {
        this.initSSEEventFeed();
    }

    private initSSEEventFeed() {
        const eventSource = new EventSource(`${this.baseUrl}/activity`, {withCredentials: true});
        eventSource.onmessage = (e) => {
            if (e.lastEventId === this.lastEventId) {
                return;
            }
            this.lastEventId = e.lastEventId;
            const json: { eventId: string, activity: string } = JSON.parse(e.data);
            this.zone.run(() => {
                this.activitySubject$.next(new Activity(json.eventId, json.activity));
            });
        };
        eventSource.onerror = error => {
            console.log('event source error', error);
        };
    }

}
