import {Component, Input, OnInit} from '@angular/core';
import {Observable} from 'rxjs';
import {Activity} from '../activity.model';
import {tap} from 'rxjs/operators';

@Component({
  selector: 'app-service-activity-card',
  templateUrl: './service-activity-card.component.html',
  styleUrls: ['./service-activity-card.component.scss']
})
export class ServiceActivityCardComponent implements OnInit {

  @Input() title: string;
  @Input() image: string;
  @Input() speechBubbleTop: string;
  @Input() speechBubbleLeft: string;
  @Input() activity$: Observable<Activity>;
  cardActivity$: Observable<Activity>;
  showSpeechBubble = false;
  constructor() { }

  ngOnInit(): void {
    this.cardActivity$ = this.activity$
        .pipe(
            tap(() => this.showSpeechBubble = true)
        );
  }

}
